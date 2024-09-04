package org.babyfish.jimmer.sql.runtime;

import org.babyfish.jimmer.impl.util.Classes;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.sql.collection.TypedList;
import org.babyfish.jimmer.sql.meta.IdGenerator;
import org.babyfish.jimmer.sql.meta.impl.SequenceIdGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DefaultExecutor implements Executor {

    public static final DefaultExecutor INSTANCE = new DefaultExecutor();

    DefaultExecutor() {}

    @Override
    public <R> R execute(@NotNull Args<R> args) {
        String sql = args.sql;
        List<Object> variables = args.variables;
        JSqlClientImplementor sqlClient = args.sqlClient;
        try (PreparedStatement stmt = args.statementFactory != null ?
                args.statementFactory.preparedStatement(args.con, sql) :
                args.con.prepareStatement(sql)
        ) {
            setParameters(stmt, variables, sqlClient);
            return args.block.apply(stmt);
        } catch (Exception ex) {
            ExceptionTranslator<Exception> exceptionTranslator =
                    args.sqlClient.getExceptionTranslator();
            if (exceptionTranslator != null) {
                ex = exceptionTranslator.translate(ex, args);
            }
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new ExecutionException(
                    "Cannot execute SQL statement: " +
                            sql +
                            ", variables: " +
                            variables,
                    ex
            );
        }
    }

    @Override
    public BatchContext executeBatch(
            @NotNull Connection con,
            @NotNull String sql,
            @Nullable ImmutableProp generatedIdProp,
            @NotNull ExecutionPurpose purpose,
            @NotNull JSqlClientImplementor sqlClient
    ) {
        return new BatchContextImpl(
                con,
                sql,
                generatedIdProp,
                purpose,
                ExecutorContext.create(sqlClient),
                sqlClient
        );
    }

    private static void setParameters(
            PreparedStatement stmt,
            List<Object> variables,
            JSqlClientImplementor sqlClient
    ) throws Exception {
        ParameterIndex parameterIndex = new ParameterIndex();
        for (Object variable : variables) {
            if (variable instanceof DbLiteral) {
                DbLiteral literal = (DbLiteral) variable;
                literal.setParameter(
                        stmt,
                        parameterIndex,
                        sqlClient
                );
            } else if (variable instanceof TypedList<?>) {
                TypedList<?> typedList = (TypedList<?>) variable;
                stmt.setArray(
                        parameterIndex.get(),
                        stmt.getConnection().createArrayOf(typedList.getSqlElementType(), typedList.toArray())
                );
            } else {
                stmt.setObject(parameterIndex.get(), variable);
            }
        }
    }

    private static class BatchContextImpl implements BatchContext {

        private static final Object[] EMPTY_GENERATED_IDS = new Object[0];

        private final Savepoint savepoint;

        private final String sql;

        private final PreparedStatement statement;

        @Nullable
        private final ImmutableProp generatedIdProp;

        private final ExecutionPurpose purpose;

        private final ExecutorContext executorContext;

        private final JSqlClientImplementor sqlClient;

        private int batchCount;

        BatchContextImpl(
                Connection con,
                String sql,
                @Nullable ImmutableProp generatedIdProp,
                ExecutionPurpose purpose,
                ExecutorContext executorContext,
                JSqlClientImplementor sqlClient
        ) {
            if (sqlClient.getDialect().isTransactionAbortedByError()) {
                try {
                    savepoint = con.setSavepoint();
                } catch (SQLException ex) {
                    throwException(ex);
                    throw new AssertionError("Internal bug: impossible logic");
                }
            } else {
                savepoint = null;
            }
            PreparedStatement statement;
            try {
                if (generatedIdProp != null) {
                    IdGenerator idGenerator = sqlClient.getIdGenerator(generatedIdProp.getDeclaringType().getJavaClass());
                    if (idGenerator instanceof SequenceIdGenerator) {
                        statement = con.prepareStatement(sql, new int[] {1});
                    } else {
                        statement = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    }
                } else {
                    statement = con.prepareStatement(sql);
                }
            } catch (SQLException ex) {
                throw new ExecutionException(
                        "Cannot create the batch SQL statement: " + sql,
                        ex
                );
            }
            this.purpose = purpose;
            this.executorContext = executorContext;
            this.sql = sql;
            this.statement = statement;
            this.generatedIdProp = generatedIdProp;
            this.sqlClient = sqlClient;
        }

        @Override
        public JSqlClientImplementor sqlClient() {
            return sqlClient;
        }

        @Override
        public String sql() {
            return sql;
        }

        @Override
        public ExecutionPurpose purpose() {
            return purpose;
        }

        @Override
        public ExecutorContext ctx() {
            return executorContext;
        }

        @Override
        public void add(List<Object> variables) {
            try {
                setParameters(statement, variables, sqlClient);
                statement.addBatch();
                batchCount++;
            } catch (Exception ex) {
                throw new ExecutionException(
                        "Cannot add batch into the batch SQL statement: " +
                                sql +
                                ", variables: " +
                                variables,
                        ex
                );
            }
        }

        @Override
        public int[] execute(BiFunction<SQLException, BatchContext, Exception> exceptionTranslator) {
            try {
                return statement.executeBatch();
            } catch (SQLException ex) {
                if (savepoint != null) {
                    try {
                        statement.getConnection().rollback(savepoint);
                    } catch (SQLException innerEx) {
                        throwException(innerEx);
                    }
                }
                if (exceptionTranslator != null) {
                    Exception translatedException = exceptionTranslator.apply(ex, this);
                    if (translatedException != null) {
                        throwException(translatedException);
                    }
                } else {
                    ExceptionTranslator<Exception> defaultExceptionTranslator =
                            sqlClient.getExceptionTranslator();
                    if (defaultExceptionTranslator != null) {
                        Exception translatedException = defaultExceptionTranslator.translate(ex, this);
                        if (translatedException != null) {
                            throwException(translatedException);
                        }
                    }
                }
                throwException(ex);
                throw new AssertionError("Internal bug");
            }
        }

        private void throwException(Exception ex) {
            if (ex instanceof RuntimeException) {
                throw (RuntimeException)ex;
            }
            throw new ExecutionException(
                    "Cannot execute the batch SQL statement: " + sql,
                    ex
            );
        }

        @Override
        public Object[] generatedIds() {
            if (generatedIdProp == null) {
                return EMPTY_GENERATED_IDS;
            }
            Object[] ids = new Object[batchCount];
            int index = 0;
            ScalarProvider<Object, Object> provider = sqlClient.getScalarProvider(generatedIdProp);
            Class<?> sqlType = provider != null ? provider.getSqlType() : Classes.boxTypeOf(generatedIdProp.getReturnClass());
            try (ResultSet rs = statement.getGeneratedKeys()) {
                while (rs.next()) {
                    Object id = rs.getObject(1, sqlType);
                    if (id != null && provider != null) {
                        id = provider.toSql(id);
                    }
                    ids[index++] = id;
                }
            } catch (Exception ex) {
                throw new ExecutionException(
                        "Cannot get generated ids for batch SQL statement: " + sql,
                        ex
                );
            }
            return ids;
        }

        @Override
        public void close() {
            try {
                statement.close();
            } catch (SQLException ex) {
                throw new ExecutionException(
                        "Cannot execute the batch SQL statement: " + sql,
                        ex
                );
            }
        }
    }
}
