package org.babyfish.jimmer.sql.di;

import org.babyfish.jimmer.Input;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.meta.TypedProp;
import org.babyfish.jimmer.sql.*;
import org.babyfish.jimmer.sql.association.meta.AssociationType;
import org.babyfish.jimmer.sql.ast.mutation.*;
import org.babyfish.jimmer.sql.ast.query.MutableRootQuery;
import org.babyfish.jimmer.sql.ast.query.MutableSubQuery;
import org.babyfish.jimmer.sql.ast.table.AssociationTable;
import org.babyfish.jimmer.sql.ast.table.Table;
import org.babyfish.jimmer.sql.ast.table.TableEx;
import org.babyfish.jimmer.sql.ast.table.spi.TableProxy;
import org.babyfish.jimmer.sql.cache.CacheDisableConfig;
import org.babyfish.jimmer.sql.cache.CacheOperator;
import org.babyfish.jimmer.sql.cache.Caches;
import org.babyfish.jimmer.sql.dialect.Dialect;
import org.babyfish.jimmer.sql.event.TriggerType;
import org.babyfish.jimmer.sql.event.Triggers;
import org.babyfish.jimmer.sql.event.binlog.BinLog;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.babyfish.jimmer.sql.filter.FilterConfig;
import org.babyfish.jimmer.sql.filter.Filters;
import org.babyfish.jimmer.sql.loader.graphql.Loaders;
import org.babyfish.jimmer.sql.meta.*;
import org.babyfish.jimmer.sql.runtime.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class AbstractJSqlClientDelegate implements JSqlClientImplementor {

    protected abstract JSqlClientImplementor sqlClient();

    @Override
    public <T extends SqlContext> T unwrap() {
        return sqlClient().unwrap();
    }

    @Override
    public UserIdGenerator<?> getUserIdGenerator(String ref) throws Exception {
        return sqlClient().getUserIdGenerator(ref);
    }

    @Override
    public UserIdGenerator<?> getUserIdGenerator(Class<?> userIdGeneratorType) throws Exception {
        return sqlClient().getUserIdGenerator(userIdGeneratorType);
    }

    @Override
    public LogicalDeletedValueGenerator<?> getLogicalDeletedValueGenerator(String ref) throws Exception {
        return sqlClient().getLogicalDeletedValueGenerator(ref);
    }

    @Override
    public LogicalDeletedValueGenerator<?> getLogicalDeletedValueGenerator(Class<?> logicalDeletedValueGeneratorType) throws Exception {
        return sqlClient().getLogicalDeletedValueGenerator(logicalDeletedValueGeneratorType);
    }

    @Override
    public <T extends TableProxy<?>> MutableRootQuery<T> createQuery(T table) {
        return sqlClient().createQuery(table);
    }

    @Override
    public MutableUpdate createUpdate(TableProxy<?> table) {
        return sqlClient().createUpdate(table);
    }

    @Override
    public MutableDelete createDelete(TableProxy<?> table) {
        return sqlClient().createDelete(table);
    }

    @Override
    public <SE, ST extends Table<SE>, TE, TT extends Table<TE>> MutableRootQuery<AssociationTable<SE, ST, TE, TT>> createAssociationQuery(AssociationTable<SE, ST, TE, TT> table) {
        return sqlClient().createAssociationQuery(table);
    }

    @Override
    public Entities getEntities() {
        return sqlClient().getEntities();
    }

    @Override
    public Triggers getTriggers() {
        return sqlClient().getTriggers();
    }

    @Override
    public Triggers getTriggers(boolean transaction) {
        return sqlClient().getTriggers(transaction);
    }

    @Override
    public Associations getAssociations(TypedProp.Association<?, ?> prop) {
        return sqlClient().getAssociations(prop);
    }

    @Override
    public Associations getAssociations(ImmutableProp immutableProp) {
        return sqlClient().getAssociations(immutableProp);
    }

    @Override
    public Associations getAssociations(AssociationType associationType) {
        return sqlClient().getAssociations(associationType);
    }

    @Override
    public Caches getCaches() {
        return sqlClient().getCaches();
    }

    @Override
    public Filters getFilters() {
        return sqlClient().getFilters();
    }

    @Override
    public BinLog getBinLog() {
        return sqlClient().getBinLog();
    }

    @Override
    public <E> @Nullable E findById(Class<E> type, Object id) {
        return sqlClient().findById(type, id);
    }

    @Override
    public <E> @Nullable E findById(Fetcher<E> fetcher, Object id) {
        return sqlClient().findById(fetcher, id);
    }

    @Override
    public <E> @NotNull List<E> findByIds(Class<E> type, Iterable<?> ids) {
        return sqlClient().findByIds(type, ids);
    }

    @Override
    public <E> @NotNull List<E> findByIds(Fetcher<E> fetcher, Iterable<?> ids) {
        return sqlClient().findByIds(fetcher, ids);
    }

    @Override
    public <K, V> @NotNull Map<K, V> findMapByIds(Class<V> type, Iterable<K> ids) {
        return sqlClient().findMapByIds(type, ids);
    }

    @Override
    public <K, V> @NotNull Map<K, V> findMapByIds(Fetcher<V> fetcher, Iterable<K> ids) {
        return sqlClient().findMapByIds(fetcher, ids);
    }

    @Override
    public <E> SimpleSaveResult<E> save(E entity, SaveMode mode, @NotNull AssociatedSaveMode associatedMode) {
        return sqlClient().save(entity, mode, associatedMode);
    }

    @Override
    public <E> SimpleSaveResult<E> save(E entity, AssociatedSaveMode associatedMode) {
        return sqlClient().save(entity, associatedMode);
    }

    @Override
    public <E> SimpleSaveResult<E> save(E entity, SaveMode mode) {
        return sqlClient().save(entity, mode);
    }

    @Override
    public <E> SimpleSaveResult<E> save(E entity) {
        return sqlClient().save(entity);
    }

    @Override
    public <E> SimpleSaveResult<E> insert(@NotNull E entity) {
        return sqlClient().insert(entity);
    }

    @Override
    public <E> SimpleSaveResult<E> insert(@NotNull E entity, @NotNull AssociatedSaveMode associatedSaveMode) {
        return sqlClient().insert(entity, associatedSaveMode);
    }

    @Override
    public <E> SimpleSaveResult<E> insertIfAbsent(@NotNull E entity) {
        return sqlClient().insertIfAbsent(entity);
    }

    @Override
    public <E> SimpleSaveResult<E> insertIfAbsent(@NotNull E entity, @NotNull AssociatedSaveMode associatedSaveMode) {
        return sqlClient().insertIfAbsent(entity, associatedSaveMode);
    }

    @Override
    public <E> SimpleSaveResult<E> update(@NotNull E entity) {
        return sqlClient().update(entity);
    }

    @Override
    public <E> SimpleSaveResult<E> update(@NotNull E entity, @NotNull AssociatedSaveMode associatedSaveMode) {
        return sqlClient().update(entity, associatedSaveMode);
    }

    @Override
    public <E> SimpleSaveResult<E> merge(@NotNull E entity) {
        return sqlClient().merge(entity);
    }

    @Override
    public <E> SimpleSaveResult<E> merge(@NotNull E entity, @NotNull AssociatedSaveMode associatedSaveMode) {
        return sqlClient().merge(entity, associatedSaveMode);
    }

    @Override
    public <E> SimpleSaveResult<E> save(@NotNull Input<E> input) {
        return sqlClient().save(input);
    }

    @Override
    public <E> SimpleSaveResult<E> save(@NotNull Input<E> input, @NotNull SaveMode mode) {
        return sqlClient().save(input, mode);
    }

    @Override
    public <E> SimpleSaveResult<E> save(@NotNull Input<E> input, @NotNull AssociatedSaveMode associatedMode) {
        return sqlClient().save(input, associatedMode);
    }

    @Override
    public <E> SimpleSaveResult<E> save(@NotNull Input<E> input, @NotNull SaveMode mode, @NotNull AssociatedSaveMode associatedMode) {
        return sqlClient().save(input, mode, associatedMode);
    }

    @Override
    public <E> SimpleSaveResult<E> insert(@NotNull Input<E> input) {
        return sqlClient().insert(input);
    }

    @Override
    public <E> SimpleSaveResult<E> insert(@NotNull Input<E> input, @NotNull AssociatedSaveMode associatedSaveMode) {
        return sqlClient().insert(input, associatedSaveMode);
    }

    @Override
    public <E> SimpleSaveResult<E> insertIfAbsent(@NotNull Input<E> input) {
        return sqlClient().insertIfAbsent(input);
    }

    @Override
    public <E> SimpleSaveResult<E> insertIfAbsent(@NotNull Input<E> input, @NotNull AssociatedSaveMode associatedSaveMode) {
        return sqlClient().insertIfAbsent(input, associatedSaveMode);
    }

    @Override
    public <E> SimpleSaveResult<E> update(@NotNull Input<E> input) {
        return sqlClient().update(input);
    }

    @Override
    public <E> SimpleSaveResult<E> update(@NotNull Input<E> input, @NotNull AssociatedSaveMode associatedSaveMode) {
        return sqlClient().update(input, associatedSaveMode);
    }

    @Override
    public <E> SimpleSaveResult<E> merge(@NotNull Input<E> input) {
        return sqlClient().merge(input);
    }

    @Override
    public <E> SimpleSaveResult<E> merge(@NotNull Input<E> input, @NotNull AssociatedSaveMode associatedSaveMode) {
        return sqlClient().merge(input, associatedSaveMode);
    }

    @Override
    public DeleteResult deleteById(@NotNull Class<?> type, @NotNull Object id, @NotNull DeleteMode mode) {
        return sqlClient().deleteById(type, id, mode);
    }

    @Override
    public DeleteResult deleteById(Class<?> type, Object id) {
        return sqlClient().deleteById(type, id);
    }

    @Override
    public DeleteResult deleteByIds(Class<?> type, Iterable<?> ids, DeleteMode mode) {
        return sqlClient().deleteByIds(type, ids, mode);
    }

    @Override
    public DeleteResult deleteByIds(Class<?> type, Iterable<?> ids) {
        return sqlClient().deleteByIds(type, ids);
    }

    @Override
    public MutableSubQuery createSubQuery(TableProxy<?> table) {
        return sqlClient().createSubQuery(table);
    }

    @Override
    public <SE, ST extends TableEx<SE>, TE, TT extends TableEx<TE>> MutableSubQuery createAssociationSubQuery(AssociationTable<SE, ST, TE, TT> table) {
        return sqlClient().createAssociationSubQuery(table);
    }

    @Override
    public ConnectionManager getConnectionManager() {
        return sqlClient().getConnectionManager();
    }

    @Override
    public ConnectionManager getSlaveConnectionManager(boolean forUpdate) {
        return sqlClient().getSlaveConnectionManager(forUpdate);
    }

    @Override
    public Dialect getDialect() {
        return sqlClient().getDialect();
    }

    @Override
    public Executor getExecutor() {
        return sqlClient().getExecutor();
    }

    @Override
    public EntityManager getEntityManager() {
        return sqlClient().getEntityManager();
    }

    @Override
    public MetadataStrategy getMetadataStrategy() {
        return sqlClient().getMetadataStrategy();
    }

    @Override
    public List<String> getExecutorContextPrefixes() {
        return sqlClient().getExecutorContextPrefixes();
    }

    @Override
    public SqlFormatter getSqlFormatter() {
        return sqlClient().getSqlFormatter();
    }

    @Override
    public CacheOperator getCacheOperator() {
        return sqlClient().getCacheOperator();
    }

    @Override
    public TriggerType getTriggerType() {
        return sqlClient().getTriggerType();
    }

    @Override
    public <T, S> ScalarProvider<T, S> getScalarProvider(Class<T> scalarType) {
        return sqlClient().getScalarProvider(scalarType);
    }

    @Override
    public <T, S> ScalarProvider<T, S> getScalarProvider(TypedProp<T, ?> prop) {
        return sqlClient().getScalarProvider(prop);
    }

    @Override
    public <T, S> ScalarProvider<T, S> getScalarProvider(ImmutableProp prop) {
        return sqlClient().getScalarProvider(prop);
    }

    @Override
    public ZoneId getZoneId() {
        return sqlClient().getZoneId();
    }

    public IdGenerator getIdGenerator(Class<?> entityType) {
        return sqlClient().getIdGenerator(entityType);
    }

    @Override
    public int getDefaultBatchSize() {
        return sqlClient().getDefaultBatchSize();
    }

    @Override
    public int getDefaultListBatchSize() {
        return sqlClient().getDefaultListBatchSize();
    }

    @Override
    public boolean isInListPaddingEnabled() {
        return sqlClient().isInListPaddingEnabled();
    }

    @Override
    public boolean isExpandedInListPaddingEnabled() {
        return sqlClient().isExpandedInListPaddingEnabled();
    }

    public int getOffsetOptimizingThreshold() {
        return sqlClient().getOffsetOptimizingThreshold();
    }

    @Override
    public LockMode getDefaultLockMode() {
        return sqlClient().getDefaultLockMode();
    }

    @Override
    public int getMaxCommandJoinCount() {
        return sqlClient().getMaxCommandJoinCount();
    }

    @Override
    public boolean isTargetTransferable() {
        return sqlClient().isTargetTransferable();
    }

    @Override
    public @Nullable ExceptionTranslator<Exception> getExceptionTranslator() {
        return sqlClient().getExceptionTranslator();
    }

    @Override
    public TransientResolver<?, ?> getResolver(ImmutableProp prop) {
        return sqlClient().getResolver(prop);
    }

    @Override
    public StrategyProvider<UserIdGenerator<?>> getUserIdGeneratorProvider() {
        return sqlClient().getUserIdGeneratorProvider();
    }

    @Override
    public StrategyProvider<TransientResolver<?, ?>> getTransientResolverProvider() {
        return sqlClient().getTransientResolverProvider();
    }

    @Override
    public boolean isDefaultDissociationActionCheckable() {
        return sqlClient().isDefaultDissociationActionCheckable();
    }

    @Override
    public IdOnlyTargetCheckingLevel getIdOnlyTargetCheckingLevel() {
        return sqlClient().getIdOnlyTargetCheckingLevel();
    }

    @Override
    public DraftPreProcessor<?> getDraftPreProcessor(ImmutableType type) {
        return sqlClient().getDraftPreProcessor(type);
    }

    @Override
    @Nullable
    public DraftInterceptor<?, ?> getDraftInterceptor(ImmutableType type) {
        return sqlClient().getDraftInterceptor(type);
    }

    @Override
    public Reader<?> getReader(Class<?> type) {
        return sqlClient().getReader(type);
    }

    @Override
    public Reader<?> getReader(ImmutableType type) {
        return sqlClient().getReader(type);
    }

    @Override
    public Reader<?> getReader(ImmutableProp prop) {
        return sqlClient().getReader(prop);
    }

    @Override
    public String getMicroServiceName() {
        return sqlClient().getMicroServiceName();
    }

    @Override
    public MicroServiceExchange getMicroServiceExchange() {
        return sqlClient().getMicroServiceExchange();
    }

    @Override
    public JSqlClientImplementor caches(Consumer<CacheDisableConfig> block) {
        return sqlClient().caches(block);
    }

    @Override
    public JSqlClientImplementor filters(Consumer<FilterConfig> block) {
        return sqlClient().filters(block);
    }

    @Override
    public JSqlClientImplementor disableSlaveConnectionManager() {
        return sqlClient().disableSlaveConnectionManager();
    }

    @Override
    public JSqlClientImplementor executor(Executor executor) {
        return sqlClient().executor(executor);
    }

    @Override
    public Loaders getLoaders() {
        return sqlClient().getLoaders();
    }

    @Override
    public void initialize() {
        sqlClient().initialize();
    }
}
