---
slug: elasticsearch-mybatis-style
title: "Elasticsearch with Mappers"
authors: [ZhaoYongChun]
tags: [ElasticSearch, MyBatis, ORM, JDBC, dbVisitor]
topics: [datasources]
language: en
updated: 2026-09-17
---
In hybrid projects with both MySQL and ElasticSearch, the data access layer often splits between MyBatis XML and complex DSL builders. dbVisitor lets you operate ElasticSearch using the same Mapper interfaces and XML, unifying your data access code.

<!--truncate-->

Regression examples pinned to 6.8.0: [GitHub](https://github.com/zycgit/dbvisitor/tree/main/dbvisitor-example/blog-680) / [Gitee](https://gitee.com/zycgit/dbvisitor/tree/main/dbvisitor-example/blog-680).


Use Java 17+, `net.hasor:dbvisitor:6.8.0`, and the adapter below:

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>jdbc-elastic</artifactId>
    <version>6.8.0</version>
    <classifier>all</classifier>
</dependency>
```


## 1. Existing Challenges {#1-pain-points-of-traditional-approaches}

In traditional hybrid architectures, we might encounter the following problems:

*   **Inconsistent API Styles**: RDBMS uses SQL, while ElasticSearch uses REST API and JSON DSL.
*   **Differences in Pagination Implementation**: MyBatis is usually paired with PageHelper, while ElasticSearch requires manually setting `from` and `size`.
*   **High Maintenance Costs**: Maintaining two completely different underlying logics increases code complexity and the probability of errors.

## 2. dbVisitor Approach {#2-dbvisitors-solution}

dbVisitor encapsulates ElasticSearch operations into standard JDBC interfaces by providing a JDBC driver layer (`dbvisitor-driver`) and an adapter (`jdbc-elastic`). This means you can operate ElasticSearch just like you operate MySQL.

Furthermore, dbVisitor provides ORM features similar to MyBatis, supporting Mapper interfaces, XML mapping files, annotations, and Lambda expressions.

### 2.1 Entity Mapping {#21-object-relational-mapping-orm}

First, we define a Java object and map it using annotations. This is very similar to MyBatis Plus or JPA.

```java
@Table("user_info")
public class UserInfo {
    // Map _id field
    @Column(value = "_id", primary = true)
    private String id;

    @Column("name")
    private String name;

    @Column("age")
    private Integer age;

    // getters/setters omitted
}
```

### 2.2 Mapper Annotations {#22-using-mapper-interface-annotation-style}

Define commands through Mapper annotations. The path placeholder `{#{id}}` becomes the driver's `{?}` after dbVisitor binding; JSON values use `#{...}` directly.

```java
@SimpleMapper
public interface UserInfoMapper {
    // Insert data
    @Insert(value = "POST /user_info/_doc\\?refresh=wait_for { \"name\": #{info.name}, \"age\": #{info.age} }",
            useGeneratedKeys = true, keyProperty = "id", keyColumn = "_id")
    int saveUser(@Param("info") UserInfo info);

    // Query by ID
    @Query("POST /user_info/_search {\"query\": {\"term\": {\"_id\": #{id}}}}")
    UserInfo loadById(@Param("id") String id);

    // Delete data
    @Delete("DELETE /user_info/_doc/{#{id}}")
    int deleteUser(@Param("id") String id);
}
```

This method takes a single bean parameter. `@Param("info")` names the parameter used by `#{info.name}`, while `keyProperty="id"` identifies the bean property receiving the generated key. These serve different purposes: `keyProperty` is not a parameter expression, so do not use `info.id`. A single bean without `@Param` also supports backfill.

The ID query uses `_search` because its hits expose document fields; a direct `GET /index/_doc/id` returns a response containing `_source`, not the same flat row.

### 2.3 BaseMapper {#23-using-common-mapper}

If you don't want to write any commands, you can directly inherit `BaseMapper`, and dbVisitor will automatically generate basic CRUD operations.

```java
@SimpleMapper
public interface UserInfoBaseMapper extends BaseMapper<UserInfo> {
    // Automatically possesses methods like insert, update, delete, selectById, listBySample, etc.
}
```

### 2.4 Lambda Builders {#24-using-lambda-style}

dbVisitor also provides a Lambda call style similar to MyBatis Plus, which is completely type-safe.

```java
LambdaTemplate lambda = new LambdaTemplate(connection);

// Query users with name = "mali"
UserInfo user = lambda.query(UserInfo.class)
    .eq(UserInfo::getName, "mali")
    .queryForObject();

// Update operation
lambda.update(UserInfo.class)
    .eq(UserInfo::getId, user.getId())
    .updateTo(UserInfo::getAge, 27)
    .doUpdate();
```

### 2.5 XML Mappers {#25-using-xml-to-manage-mapper-mybatis-style}

For complex queries or scenarios where unified management of DSL is needed, dbVisitor supports using XML files to define Mappers, which is almost identical to the MyBatis experience.

**Mapper Interface:**

```java
@RefMapper("mapper/user-mapper.xml")
public interface UserInfoXmlMapper {
    int saveUser(@Param("info") UserInfo info);
    PageResult<UserInfo> listByUserName(@Param("userName") String userName, Page page);
}
```

**XML File (user-mapper.xml):**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//dbvisitor.net//DTD Mapper 1.0//EN"
        "https://www.dbvisitor.net/schema/dbvisitor-mapper.dtd">
<mapper namespace="com.example.mapper.UserInfoXmlMapper">
    <resultMap id="userResultMap" type="com.example.entity.UserInfo">
        <result column="_id" property="id"/>
        <result column="name" property="name"/>
        <result column="age" property="age"/>
    </resultMap>

    <insert id="saveUser">
        POST /user_info/_doc\?refresh=wait_for {
            "name": #{info.name},
            "age": #{info.age}
        }
    </insert>

    <!-- Supports automatic pagination -->
    <select id="listByUserName" resultMap="userResultMap">
        POST /user_info/_search {
            "query": {
                "term": { "name": #{userName} }
            }
        }
    </select>
</mapper>
```

## 3. Pagination {#3-unified-pagination-implementation}

The XML Mapper above returns `PageResult<UserInfo>`: it applies pagination and obtains the total count. Returning only `List<UserInfo>` does not automatically count rows. Import `Page`, `PageObject`, and `PageResult` from `net.hasor.dbvisitor.page`.
```java
// Create Page object
Page page = new PageObject();
page.setPageSize(10);
page.setCurrentPage(0); // First page

// Execute query, dbVisitor will automatically intercept and rewrite it as a pagination query
// For ElasticSearch, it will automatically convert to "from": 0, "size": 10
PageResult<UserInfo> result = mapper.listByUserName("mali", page);
List<UserInfo> list = result.getData();

// Get total record count (if needed)
long total = result.getTotalCount();

// Next page
page.nextPage();
result = mapper.listByUserName("mali", page);
list = result.getData();
```

:::caution[6.8.0 pagination boundary]
This example uses an XML statement with `resultMap`. Do not replace it with an `@Query` method returning `PageResult`: that annotation path throws `ClassCastException` in 6.8.0. For BaseMapper, initialize the count using `pageInitBySample(...)` before `pageBySample(...)` when a total is needed.

Annotation pagination is fixed in the unreleased 6.8.1 development branch. This article and its example project still use XML pagination with release 6.8.0.
:::

## 4. Scope

jdbc-elastic suits projects reusing JDBC, Mapper, or XML; it is not a complete JDBC implementation or a replacement for the official client. JDBC Batch, transactions, and stored procedures are unsupported. Use the official client for commands outside the adapter's coverage.

For exact string matching with term queries, define the field as keyword first. Searching immediately after a write also requires considering refresh; use ?refresh=wait_for on writes or the relevant connection option. Pagination remains subject to Elasticsearch result-window limits.

When a command passes through dbVisitor Mapper/JdbcTemplate, escape the literal URL `?`: write `\\?` in a Java string and `\?` in XML. The framework removes that escape before calling JDBC. Direct driver calls with `Statement` / `PreparedStatement` keep the normal `?refresh=wait_for` URL; do not add this framework-layer escape there.

## 5. Summary {#5-conclusion}

With dbVisitor, we can operate both relational databases and ElasticSearch in the same project using the same set of APIs and the same mindset (Mapper/XML/Lambda). This greatly reduces the development and maintenance costs of hybrid architecture projects, making the data access layer cleaner and more unified.

If you are looking for a tool that can unify the RDBMS and NoSQL development experience, dbVisitor is definitely worth a try.
