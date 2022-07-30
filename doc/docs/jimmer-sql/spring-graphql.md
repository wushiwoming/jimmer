---
sidebar_position: 9
title: Support for Spring GraphQL
---

Spring Boot 2.7.0 brings Spring GraphQL, and jimmer-sql provides a specialized API to speed up the development of Spring GraphQL.

## Query 

For fields of type `Query`, i.e. root queries, there is no difference between a GraphQL implementation and a REST implementation, and no special support is required.

The key point is the query for associations between objects. It is a simple concept in itself, but for performance, `DataLoader` is usually used in actual projects, and `DataLoader` has caused great damage to the development experience.

:::note
Association queries and `DataLoader` increase the difficulty of developing GraphQL services, but because of this, GraphQL services are very powerful from the client's point of view.
:::

In order to alleviate the disruption of `DataLoader` to the development experience, Spring GraphQL introduces a new annotation [@BatchMapping](https://docs.spring.io/spring-graphql/docs/current/reference/html/#controllers-batch-mapping).

jimmer-sql provides special support for this, providing dedicated API that allows developers to implement Spring GraphQL's [@BatchMapping](https://docs.spring.io/spring-graphql/docs/current/reference/html/#controllers-batch-mapping) method in one sentence.

API related to this

- SqlClient.getReferenceLoader
- SqlClient.getListLoader

### SqlClient.getReferenceLoader

This API is used to quickly implement one-to-one or many-to-one associations

```java title="BookController.java"
@Controller
public class BookController {

    private final SqlClient sqlClient;

    public BookController(SqlClient sqlClient) {
        this.sqlClient = sqlClient;
    }

    // Many-to-one associaton: Book.store
    // highlight-next-line
    @BatchMapping
    public Map<Book, BookStore> store(
        Collection<Book> books
    ) {
        return sqlClient
            // highlight-next-line
            .getReferenceLoader(
                    BookTable.class,
                    BookTable::store
            )
            .batchLoad(books);
    }
}
```

### SqlClient.getListLoader

This API is used to quickly implement one-to-many or many-to-many associations

```java title="BookStoreController.java"
@Controller
public class BookStoreController {

    private final SqlClient sqlClient;

    public BookStoreController(SqlClient sqlClient) {
        this.sqlClient = sqlClient;
    }

    // One-to-many associaton: BookStore.books
    // highlight-next-line
    @BatchMapping
    public Map<BookStore, List<Book>> books(
            List<BookStore> bookStores
    ) {
        return sqlClient
            // highlight-next-line
            .getListLoader(
                BookStoreTableEx.class,
                BookStoreTableEx::books
            )
            .batchLoad(bookStores);
    }
}
```

```java title="BookController.java"
@Controller
public class BookController {

    private final SqlClient sqlClient;

    public BookController(SqlClient sqlClient) {
        this.sqlClient = sqlClient;
    }

    // Many-to-many associaton: Book.authors
    // highlight-next-line
    @BatchMapping
    public Map<Book, List<Author>> authors(List<Book> books) {
        return sqlClient
            // highlight-next-line
            .getListLoader(
                BookTableEx.class,
                BookTableEx::authors
            )
            .batchLoad(books);
    }
}
```

```java title="AuthorController.java"
@Controller
public class AuthorController {

    private final SqlClient sqlClient;

    public AuthorController(SqlClient sqlClient) {
        this.sqlClient = sqlClient;
    }

    // Many-to-many associaton: Author.books
    // highlight-next-line
    @BatchMapping
    public Map<Author, List<Book>> books(
            List<Author> authors
    ) {
        return sqlClient
                // highlight-next-line
                .getListLoader(
                        AuthorTableEx.class,
                        AuthorTableEx::books
                )
                .batchLoad(authors);
    }
}
```

## Mutation

### Existence value of GraphQL Input type

Before introducing the mutation, we need to discuss why the GraphQL protocol introduced the Input type.

From an output perspective, a GraphQL field returns an `Object` type. However, from an input perspective, the parameters of GraphQL fields do not accept `Object` types, only scalar types, `Input` types, and their collection types are acceptable.

The different between `Object` and `Input`:

- The `Object` type is <b>dynamic</b>, and the client can freely define the shape of the object.

     The dynamic nature of the `Object` type realizes the core value of GraphQL, clients can specify which fields are required and which are not, giving them flexibility in controlling the format of the object tree to query.

- The `Input` type is <b>static</b>, and the client must provide parameters that strictly meet the server's requirements.

     Different from query, the mutation business often has strict restrictions on the input data format. If the client arbitrarily passes the data format that does not meet the expectations of the server, it may lead to abnormal business.

     Therefore, the GraphQL protocol introduces the `Input` type, which is static, and the client must pass a data format that strictly conforms to the `Input` type definition before calling the mutation business.

This difference is the fundamental reason for the existence of the `Input` type.

### Define input type

First, we need to define the input type in the Spring GraphQL convention file `src/main/resources/graphql/schema.graphqls`

```graphql
input BookInput {
    id: Long
    name: String!
    edition: Int
    price: BigDecimal!
    storeId: Long
    authorIds: [Long!]!
}
```

Then, in the Java code, define the corresponding class `BookInput` 

```java title="BookInput.java"
public class BookInput {

    @Nullable
    private final Long id;

    private final String name;

    private final int edition;

    private final BigDecimal price;

    @Nullable
    private final Long storeId;

    private final List<Long> authorIds;

    public BookInput(
        @Nullable Long id,
        String name,
        int edition,
        BigDecimal price,
        @Nullable Long storeId,
        List<Long> authorIds
    ) {
        this.id = id;
        this.name = name;
        this.edition = edition;
        this.price = price;
        this.storeId = storeId;
        this.authorIds = authorIds;
    }

    // Convert static input object
    // to dynamic entity object
    // highlight-next-line
    public Book toBook() {
        return BookDraft.$.produce(book -> {
            if (id != null) {
                book.setId(id);
            }
            if (storeId != null) {
                book.setStore(
                    store -> store.setId(storeId)
                );
            }
            book
                .setName(name)
                .setEdition(edition)
                .setPrice(price);
            for (Long authorId : authorIds) {
                book.addIntoAuthors(
                    author -> author.setId(authorId)
                );
            }
        });
    }
}
```

:::info
1. The [Save command](./mutation/save-command) of jimmer-sql provides the function of saving object tree with arbitrary complexity into database. Therefore, jimmer-sql focuses on the entity object tree, not the input object. So, we need to provide the method `BookInput.toBook` to convert the static `BookInput` object to a dynamic `Book` object.

2. The `Book` object is a jimmer-core immutable object, which is dynamic, that is, the format of `Book` is ever-changing and all-encompassing. So, no matter how `BookInput` type is defined, and whether `BookInput` has deep data nesting, it can be converted to `Book` type. It can never happen that `BookInput` cannot be converted to `Book`.

3. The core value of the `BookInput` is to conform to the GraphQL protocol and impose format constraints on the input data passed by the client. Howerver, for jimmer-sql, `BookInput` only has the responsibility of creating `Book` object. So, apart from the `toBook` method, the class `BookInput` does not define any other methods, not even a getter methods, because this is unnessary (of course, if you want to cooperate with the debugger display function, you can define a `toString` for it).
:::

### Implement mutation business

Now, we know

1. The [Save command](./mutation/save-command) of jimmer-sql allows developers to use one sentence to save any complex entity object tree into the database.

2. The `BookInput` defined above can be converted to an entity object tree of `Book` through its  method `toBook`.

Then, the realization of the data mutation business is very simple.

```java
@MutationMapping
@Transactional
public Book saveBook(@Argument BookInput input) {
    return sqlClient
        .getEntities()
        .save(
            // highlight-next-line
            input.toBook()
        )
        .getModifiedEntity();
}
```