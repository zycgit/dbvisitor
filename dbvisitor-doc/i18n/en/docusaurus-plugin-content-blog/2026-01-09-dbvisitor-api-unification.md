---
slug: dbvisitor-api-unification
title: "One API: Unified Data Access"
authors: [ZhaoYongChun]
tags: [dbVisitor, ORM, JDBC, NoSQL, Architecture]
language: en
---

After [Next-Gen Data Access: dbVisitor](/blog/new-generation-dbvisitor) went live, the core debate was direct: **can a single API truly unify RDBMS and NoSQL?** This post tackles the controversy and explains the design principles behind dbVisitor's "One API, Access Multiple Databases" vision.

<!-- truncate -->

:::note[Capability Scope]
Invocation style is unified, not database semantics. Builders require dialect support, and native commands must be supported by the adapter. Before integrating an ORM or connection pool, check the JDBC methods it requires. See the [feature matrix](../docs/features/support) and [JDBC limitations](../docs/drivers/limited).
:::


## I. Dismantling the Controversy: Our Misunderstanding of APIs

To understand why "Grand Unification" is feasible, we first need to clarify two misconceptions that have long confused the public.

### 1. Abuse of Builder Pattern and "Business-Oriented APIs"

In the current Java database access field, there is an obvious trend: APIs are becoming more and more **"Business-Oriented"**.

#### What is Business-Oriented?
To solve complex query problems in specific domains, database access frameworks have begun to pursue extreme development efficiency.
For example, excellent projects like [Easy-Query](https://www.easy-query.com/) and [SqlToy-ORM](https://gitee.com/sagacity/sagacity-sqltoy). **SqlToy-ORM** excels in dealing with extreme pagination optimization, cache translation, and hierarchical data queries (such as recursive queries), often solving headache-inducing SQL problems with minimal configuration; while **Easy-Query** achieves the ultimate in type-safe dynamic query construction, allowing you to write extremely complex business logic in Java code in a structured way.

*   **Value**: For complex queries in specific domains (such as multi-table joins, dynamic aggregation, row-to-col conversion), they can even replace dozens of lines of native SQL with very little code. This efficiency improvement is huge and deserves full recognition.
*   **Limitation**: These "artifact" level APIs are often strongly bound to the characteristics of the database. The complex query logic of MySQL is completely unworkable when copied directly to MongoDB or Elasticsearch.

#### dbVisitor's Choice: Returning to the Foundation
Looking back at Hibernate, MyBatis, Commons DBUtils, and even JDBC itself, these enduring projects all have one thing in common: **Single Responsibility, Clear Goal**. They do not do business logic, but focus on being the **Foundation**.

The huge success of MyBatis Plus in China is built on the solid "non-business-oriented" foundation of MyBatis. MyBatis is responsible for mapping, and MP is responsible for providing more advanced features and encapsulation.

**The value of universality lies in making the "skeleton of the house"**. dbVisitor's goal is not to replace tools like Easy-Query to solve specific business complex queries, but aims to become the **Data Access Foundation** of the new era.
Only when the foundation is solid and unified can the upper-layer business ecosystem (like the MyBatis ecosystem) blossom on different data sources.

### 2. The Fallacy of "Simple Mode is Useless"

Opponents often hold two views:
1.  **"CRUD is too simple, unifying it has no value"**
2.  **"Unified API cannot cross the gap of database features"**

#### Rebuttal 1: The Universal Value of Simple CRUD
If your world only has MySQL and Oracle, then indeed, JDBC has already unified it, and reinventing the wheel is meaningless.
But what if your technology stack adds **MongoDB, Elasticsearch, Redis**?
*   MongoDB inserts a piece of data using `db.collection.insertOne()`
*   Elasticsearch inserts a piece of data using `IndexRequest`
*   Redis inserts a piece of data using `set` command

These "simple" operations have vastly different API styles. With the vision of **"One API, Access Multiple Databases"**, being able to complete all the above operations through a unified JDBC/Mapper invocation style (Redis uses commands, not entity insert builders) has extremely high universal value in itself, eliminating the cost of cognitive switching.

#### Rebuttal 2: API is not just a Query Builder
This is a huge cognitive misunderstanding: **"Unified API" does not equal "Unified into a specific interface"**.

- Is a query builder an API? Of course!
- Is a Mapper interface (Dao) an API? Of course!
- Is MyBatis's Mapper XML binding an API? Of course!
- Is the underlying `JDBC Connection` an API? Even more so!

People despise Mapper + XML often because it is cumbersome to write (repetitive labor), but at the architectural level, **Mapper Interface Binding DSL** is the API design that best fits **"Behavior-Centric"**.
It separates "Business Intent" (Method Name) from "Specific Implementation" (SQL/DSL).

As long as we no longer obsess over describing all queries with Java code, but accept the concept of **"API Defines Behavior"**, the gap crossing database features can be easily solved.

## II. The Core Idea of dbVisitor's Grand Unification

**There is no silver bullet**, and any attempt to invent a "Universal Java Syntax" to generate all database queries is doomed to fail.

The reason why dbVisitor dares to say "Yes" is that its core idea is not to **Eliminate Differences** and seek to invent a universal syntax,
but to **Manage Differences** through **JDBC Standardization** and **Layered Abstraction**.

### 1. Unique Double-Layer Adapter Architecture

dbVisitor adopts a unique double-layer architecture to bridge the gap:

#### JDBC Standardization
This layer is the foundation for dbVisitor to achieve the vision of "One API, Access Multiple Databases".

*   **Reuse JDBC Standard**: No new protocol was invented, but drivers following the JDBC specification were written for NoSQL (MongoDB, Elasticsearch, Redis).
    And use the official original DSL language of these databases for database operations. These drivers internally merely map JDBC operations to their respective native SDK calls and map the return values into JDBC standard ways.
*   **Request/Response Model**: To simplify the access of heterogeneous data sources, complex JDBC state management is simplified into a lightweight Request/Response Model. This makes it possible to access a completely new non-standard data source with very little code.
    New data sources can even be directly managed by **HikariCP**. Use the adapter's supported commands and JDBC methods, not the entire relational JDBC feature set.

The Elasticsearch adapter reuses common JDBC state management and concentrates data-source-specific code in command parsing and execution.

#### One API

The **"One API"** here does not refer to covering everything with a rigid interface, but rather to building **a Unified Data Interaction Standard**.
dbVisitor's design philosophy believes that true unification is not to force all database operations into the same narrow entrance, but to provide a unified experience in different dimensions through **Layered Abstraction**.

![API Layered Abstraction Diagram](/img/blog/api-levels.jpg)

The diagram's layers and proportions illustrate abstraction, not feature coverage or performance statistics. JdbcTemplate executes driver-supported statements; it does not translate arbitrary SQL into arbitrary database commands.

dbVisitor designs abstract interfaces of different levels for different scenarios to meet different behavioral needs:

*   **LambdaQuery (Shield Differences)**
    *   **Scenario**: 80% daily CRUD.
    *   **Advantage**: This is the **Most "Grand Unified"** layer. It completely shields the differences in underlying query languages. You only need object-oriented programming without caring whether the underlying is MySQL or NoSQL.

*   **Mapper / XML (Manage Differences)**
    *   **Scenario**: Complex statistics, aggregation, and associated queries.
    *   **Advantage**: This is the **Most "Compatible"** layer. It follows the classic pattern of MyBatis (Interface Defines Behavior + XML Defines Logic), allowing you to use the full power of native dialects (SQL or JSON DSL) of the database instead of trying to simulate them with Java.

*   **JDBC Template (Pass-through Execution)**
    *   **Scenario**: Database-specific management commands or native Shell scripts.
    *   **Advantage**: This is the **Most "Flexible"** layer. It allows you to penetrate the framework directly, talk to the underlying driver, and execute commands listed in the adapter's syntax manual.

:::warning[Unified API ≠ Unified Capability]
Although dbVisitor unifies call forms like insert/update/commit, it cannot change the physical characteristics of the underlying database.
dbVisitor's MongoDB and Elasticsearch adapters do not expose JDBC transactions; commit()/rollback() cannot manage transactions through them. This is an adapter boundary, not a claim that MongoDB servers lack transactions.
:::

## III. Practical: Multidimensional Unified Experience

Let's look at how this concept lands through code.

### 1. Simple Dimension: LambdaQuery (Type Safe)

Regardless of whether the underlying is MySQL or Elasticsearch, standard CRUD code is completely identical.

```java
// Unified Insertion
template.insert(UserInfo.class)
        .applyEntity(new UserInfo("1001", "dbVisitor"))
        .executeSumResult();

// Unified Query
List<UserInfo> list = template.query(UserInfo.class)
        .eq(UserInfo::getAge, 18)  // Automatically translated to SQL / QueryDSL / Bson
        .queryForList();
```

The MySQL and Elasticsearch examples require their own data sources and Mappers; one Session does not switch databases automatically.

### 2. Business Dimension: Mapper (Behavior-Centric)

When we need to leverage ES aggregation capabilities or MySQL complex joins, the Mapper interface is the best choice. dbVisitor provides three postures for using Mapper, which can be mixed flexibly according to business complexity.

#### Method 1: Pure Java Construction

This is one way in dbVisitor. By **inheriting BaseMapper** and using Java 8's **default methods**, you can directly use the query builder inside the Mapper interface to complete DAL logic.
This method avoids the tediousness of XML and does not hardcode SQL in Java files like annotations, perfectly realizing "Zero SQL" development.

```java
@SimpleMapper
public interface UserMapper extends BaseMapper<UserInfo> {

    // Pure Java code constructs query logic, without XML and SQL
    default List<UserInfo> findActiveUsers(int minAge) {
        return this.query()
                   .eq(UserInfo::getStatus, "ENABLE")
                   .gt(UserInfo::getAge, minAge)
                   .queryForList();
    }
}
```

#### Method 2: Annotation-Based

For moderately complex queries, using annotations directly on interface methods is the most concise way. You don't need to write extra XML files to complete SQL or DSL binding.

```java
@SimpleMapper
public interface UserMapper extends BaseMapper<UserInfo> {

    // Mixed Use: MySQL uses SQL
    @Query("select * from user_info where age > #{age}")
    List<UserInfo> findByAge(@Param("age") int age);

    // Mixed Use: Elasticsearch uses JSON DSL
    @Query("POST /user_info/_search {\"query\": {\"term\": {\"age\": #{age}}}}")
    List<UserInfo> searchByAge(@Param("age") int age);
    
    // Also supports standard annotations like @Insert, @Update, @Delete
    @Insert("insert into user_info (name, age) values (#{name}, #{age})")
    int insertUser(@Param("name") String name, @Param("age") int age);
}
```

#### Method 3: Based on Mapper File

When SQL becomes extremely complex (such as hundreds of lines of report SQL), or when the company has a strict DBA review process (need to separate SQL files), XML is still an irreplaceable solution.

**Java Interface (Define Behavior):**
```java
@RefMapper("mapper/user-mapper.xml")
public interface UserMapper {
    // This is a business intent: Count age distribution
    List<Map<String, Object>> groupByAge(@Param("minAge") int minAge);
}
```

**XML Implementation (Define Logic):**
Choose one of the XML fragments below and place it in a mapper whose namespace references the interface. Do not define the same statement ID twice in one file. Elasticsearch aggregation responses differ from SQL grouped rows; read the aggregation response according to the driver manual.

```xml
<!-- If MySQL -->
<select id="groupByAge">
    SELECT age, count(*) FROM user_info WHERE age > #{minAge} GROUP BY age
</select>

<!-- If Elasticsearch (Write JSON DSL directly) -->
<select id="groupByAge">
    POST /user_info/_search
    {
        "query": { "range": { "age": { "gt": #{minAge} } } },
        "aggs": { "age_group": { "terms": { "field": "age" } } }
    }
</select>
```

### 3. Flexible Dimension: JDBC Template (Escape Hatch Mechanism)

This is dbVisitor's **"Escape Hatch"**. When all upper-layer abstractions cannot meet your special needs, such as needing extreme performance optimization, using database-specific non-standard commands, or integrating third-party frameworks like **QueryDSL**, you can retreat to this layer.

#### Scenario 1: Native SQL/Shell Pass-through
Use the target data source's command style. NoSQL adapters still parse supported commands and call the SDK; they do not pass through arbitrary Shell or JavaScript programs.

```java
JdbcTemplate jdbc = new JdbcTemplate(connection);

// MySQL
jdbc.queryForList("select * from user where id = ?", 1);

// MongoDB (Directly write Mongo Shell)
jdbc.queryForList("db.user.find({_id: ?})", 1);
```

#### Scenario 2: Underlying API Reachable
You can break the encapsulation at any time and operate the underlying `Connection` directly. For NoSQL data sources, dbVisitor's driver layer also follows the standard JDBC `Wrapper` specification, allowing you to unwrap the official native driver object.

```java
// Get Standard JDBC Interface
Connection conn = jdbcTemplate.getDataSource().getConnection();

// If needed, you can unwrap the underlying native driver object (like MongoClient) directly
if (conn.isWrapperFor(MongoClient.class)) {
    MongoClient client = conn.unwrap(MongoClient.class);
    // Call Official Driver's API directly
}
```

## IV. Since usage is familiar, why choose dbVisitor?

Many people will ask: "Isn't this just stitching MyBatis and Spring together?" Actually not. dbVisitor is not simple "glue", but a **Redesign** based on a unified architecture.

![Core Component Architecture Diagram](/img/blog/one-api3.jpg)

### 1. Independent Double-Layer Adapter Capability
dbVisitor is **One API + Driver**.
Even if you don't intend to replace your current MyBatis, you can still use dbVisitor's **JDBC Driver** independently. Use MyBatis to map driver-supported commands. Plugins requiring transactions, Batch, or complete metadata are not automatically compatible.

### 2. Highly Unified Underlying Architecture
If you have tried mixing MyBatis and Spring JDBC in a project, you will find a strong sense of fragmentation:
*   MyBatis's `TypeHandler` cannot be used in Spring JDBC.
*   Spring's `RowMapper` cannot be reused in MyBatis.
*   Transaction manager coordination often has pitfalls.

JDBC Template, LambdaQuery, and Mapper XML all share the same **TypeHandler Mechanism**, same **Session Management**, and same **Metadata Mapping**.
In dbVisitor, you can reuse the ResultMap defined by Mapper in Lambda queries. This underlying consistency is unmatched by simple piecing together.

### 3. Ecosystem Framework Agnostic

This is another important feature that distinguishes dbVisitor from Spring Data or MyBatis-Plus.
dbVisitor's core does not depend on Spring, nor does it depend on any Web container. It is built based on pure Java (JDK 17+ from 6.7.1) and JDBC standards.
This means:

* You can use it in **Spring Boot**.
* You can use it in **Solon**, **Vert.x**, **Quarkus**.
* You can even `new` it out and use it directly in a **Main Method** console program without any dependencies.

This zero-coupling feature allows it not only to adapt to various existing technology stacks but also to maintain vitality in future architectural evolution, not being bound to the chariot of a specific framework.

## Conclusion

Physics tells us to respect differences, but software engineering tells us to manage complexity through abstraction.

dbVisitor does not try to use "Grand Unification" to cover up database features, but through providing a **Unified Foundation (JDBC Driver)** and **Layered API Design**, it lets developers enjoy the convenience of "Grand Unification" in simple scenarios and possess "Native-Level" control in complex scenarios.

This is dbVisitor's confidence to challenge data access "Grand Unification".
