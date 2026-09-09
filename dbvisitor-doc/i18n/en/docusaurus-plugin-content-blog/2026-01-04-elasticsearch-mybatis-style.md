---
slug: elasticsearch-mybatis-style
title: ElasticSearch the MyBatis Way
authors: [ZhaoYongChun]
tags: [ElasticSearch, MyBatis, ORM, JDBC, dbVisitor]
language: en
---
In hybrid projects with both MySQL and ElasticSearch, the data access layer often splits between MyBatis XML and complex DSL builders. dbVisitor lets you operate ElasticSearch using the same Mapper interfaces and XML, unifying your data access code.

<!--truncate-->

## 1. Pain Points of Traditional Approaches

In traditional hybrid architectures, we might encounter the following problems:

*   **Inconsistent API Styles**: RDBMS uses SQL, while ElasticSearch uses REST API and JSON DSL.
*   **Differences in Pagination Implementation**: MyBatis is usually paired with PageHelper, while ElasticSearch requires manually setting `from` and `size`.
*   **High Maintenance Costs**: Maintaining two completely different underlying logics increases code complexity and the probability of errors.

## 2. dbVisitor's Solution

dbVisitor encapsulates ElasticSearch operations into standard JDBC interfaces by providing a JDBC driver layer (`dbvisitor-driver`) and an adapter (`jdbc-elastic`). This means you can operate ElasticSearch just like you operate MySQL.

Furthermore, dbVisitor provides ORM features similar to MyBatis, supporting Mapper interfaces, XML mapping files, annotations, and Lambda expressions.

### 2.1 Object Relational Mapping (ORM)

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

### 2.2 Using Mapper Interface (Annotation Style)

Define commands through Mapper annotations. The path placeholder `{#{id}}` becomes the driver's `{?}` after dbVisitor binding; JSON values use `#{...}` directly.

```java
@SimpleMapper
public interface UserInfoMapper {
    // Insert data
    @Insert(value = "POST /user_info/_doc { \"name\": #{info.name}, \"age\": #{info.age} }",
            useGeneratedKeys = true, keyProperty = "info.id")
    int saveUser(@Param("info") UserInfo info);

    // Query by ID
    @Query("GET /user_info/_doc/{#{id}}")
    UserInfo loadById(@Param("id") String id);

    // Delete data
    @Delete("DELETE /user_info/_doc/{#{id}}")
    int deleteUser(@Param("id") String id);
}
```

### 2.3 Using Common Mapper

If you don't want to write any commands, you can directly inherit `BaseMapper`, and dbVisitor will automatically generate basic CRUD operations.

```java
@SimpleMapper
public interface UserInfoBaseMapper extends BaseMapper<UserInfo> {
    // Automatically possesses methods like insert, update, delete, selectById, listBySample, etc.
}
```

### 2.4 Using Lambda Style

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

### 2.5 Using XML to Manage Mapper (MyBatis Style)

For complex queries or scenarios where unified management of DSL is needed, dbVisitor supports using XML files to define Mappers, which is almost identical to the MyBatis experience.

**Mapper Interface:**

```java
@RefMapper("mapper/user-mapper.xml")
public interface UserInfoXmlMapper {
    int saveUser(@Param("info") UserInfo info);
    List<UserInfo> listByUserName(@Param("userName") String userName, Page page);
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
        POST /user_info/_doc {
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

## 3. Unified Pagination Implementation

In dbVisitor, whether operating on MySQL or ElasticSearch, the implementation of pagination queries is completely unified. You only need to pass a `Page` object.

```java
// Create Page object
Page page = new PageObject();
page.setPageSize(10);
page.setCurrentPage(0); // First page

// Execute query, dbVisitor will automatically intercept and rewrite it as a pagination query
// For ElasticSearch, it will automatically convert to "from": 0, "size": 10
List<UserInfo> list = mapper.listByUserName("mali", page);

// Get total record count (if needed)
long total = page.getTotalCount();

// Next page
page.nextPage();
list = mapper.listByUserName("mali", page);
```

## 4. Scope

jdbc-elastic suits projects reusing JDBC, Mapper, or XML; it is not a complete JDBC implementation or a replacement for the official client. JDBC Batch, transactions, and stored procedures are unsupported. Use the official client for commands outside the adapter's coverage.

For exact string matching with term queries, define the field as keyword first. Searching immediately after a write also requires considering refresh; use ?refresh=wait_for on writes or the relevant connection option. Pagination remains subject to Elasticsearch result-window limits.

## 5. Conclusion

With dbVisitor, we can operate both relational databases and ElasticSearch in the same project using the same set of APIs and the same mindset (Mapper/XML/Lambda). This greatly reduces the development and maintenance costs of hybrid architecture projects, making the data access layer cleaner and more unified.

If you are looking for a tool that can unify the RDBMS and NoSQL development experience, dbVisitor is definitely worth a try.
