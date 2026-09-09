---
slug: elasticsearch-mybatis-style
title: dbVisitor 使用 MyBatis 方式操作 ElasticSearch
authors: [ZhaoYongChun]
tags: [ElasticSearch, MyBatis, ORM, JDBC, dbVisitor]
language: zh-cn
---

在 Java 生态中，操作 ElasticSearch 最常见的方式莫过于使用官方的 `elasticsearch-java` (或旧版的 `RestHighLevelClient`) 或者 Spring 家族的 `spring-data-elasticsearch`。这些工具非常强大，但对于习惯了关系型数据库（RDBMS）和 MyBatis 开发模式的开发者来说，切换到 ElasticSearch 往往意味着需要适应一套全新的 API 和思维模式（DSL 构建、Builder 模式等）。

特别是在一个混合架构的项目中，如果同时存在 MySQL 和 ElasticSearch，数据访问层的代码风格割裂感会非常强：一边是 MyBatis 的 Mapper 接口和 XML，另一边是复杂的 DSL 构建代码或 Repository 接口。这种差异不仅增加了学习成本，也让诸如“分页查询”这样的通用功能难以统一实现。

本文将介绍如何使用 **dbVisitor**，以一种“类 MyBatis”的方式来操作 ElasticSearch，实现架构上的统一。

<!--truncate-->

## 1. 传统方式的痛点

在传统的混合架构中，我们可能会遇到以下问题：

*   **API 风格不统一**：使用 SQL，ElasticSearch 使用 REST API 和 JSON DSL。
*   **分页实现差异**：MyBatis 通常配合 PageHelper，而 ElasticSearch 需要手动设置 `from` 和 `size`。
*   **维护成本高**：需要维护两套完全不同的底层逻辑，增加了代码的复杂度和出错的概率。

## 2. dbVisitor 的解决方案

dbVisitor 通过提供一个 JDBC 驱动层（`dbvisitor-driver`）和适配器（`jdbc-elastic`），将 ElasticSearch 的操作封装成了标准的 JDBC 接口。这意味着你可以像操作 MySQL 一样操作 ElasticSearch。

更进一步，dbVisitor 提供了类似 MyBatis 的 ORM 功能，支持 Mapper 接口、XML 映射文件、注解以及 Lambda 表达式。

### 2.1 对象关系映射 (ORM)

首先，我们定义一个 Java 对象，并使用注解进行映射。这与 MyBatis Plus 或 JPA 非常相似。

```java
@Table("user_info")
public class UserInfo {
    // 映射 _id 字段
    @Column(value = "_id", primary = true)
    private String id;

    @Column("name")
    private String name;

    @Column("age")
    private Integer age;

    // 省略 getter/setter
}
```

### 2.2 使用 Mapper 接口 (注解方式)

你可以定义一个 Mapper 接口，使用注解编写 ElasticSearch 命令。路径中的 `{#{id}}` 经 dbVisitor 绑定后成为驱动的 `{?}` 路径占位符；JSON 数据值直接使用 `#{...}`。

```java
@SimpleMapper
public interface UserInfoMapper {
    // 插入数据
    @Insert(value = "POST /user_info/_doc { \"name\": #{info.name}, \"age\": #{info.age} }",
            useGeneratedKeys = true, keyProperty = "info.id")
    int saveUser(@Param("info") UserInfo info);

    // 根据 ID 查询
    @Query("GET /user_info/_doc/{#{id}}")
    UserInfo loadById(@Param("id") String id);

    // 删除数据
    @Delete("DELETE /user_info/_doc/{#{id}}")
    int deleteUser(@Param("id") String id);
}
```

### 2.3 使用通用 Mapper

如果你不想写任何命令，可以直接继承 `BaseMapper`，dbVisitor 会自动生成基础的 CRUD 操作。

```java
@SimpleMapper
public interface UserInfoBaseMapper extends BaseMapper<UserInfo> {
    // 自动拥有 insert, update, delete, selectById, listBySample 等方法
}
```

### 2.4 使用 Lambda 方式

dbVisitor 也提供了类似 MyBatis Plus 的 Lambda 调用方式，完全类型安全。

```java
LambdaTemplate lambda = new LambdaTemplate(connection);

// 查询 name = "mali" 的用户
UserInfo user = lambda.query(UserInfo.class)
    .eq(UserInfo::getName, "mali")
    .queryForObject();

// 更新操作
lambda.update(UserInfo.class)
    .eq(UserInfo::getId, user.getId())
    .updateTo(UserInfo::getAge, 27)
    .doUpdate();
```

### 2.5 使用 XML 管理 Mapper (MyBatis 风格)

对于复杂的查询或需要统一管理 DSL 的场景，dbVisitor 支持使用 XML 文件来定义 Mapper，这与 MyBatis 的体验几乎一致。

**Mapper 接口：**

```java
@RefMapper("mapper/user-mapper.xml")
public interface UserInfoXmlMapper {
    int saveUser(@Param("info") UserInfo info);
    List<UserInfo> listByUserName(@Param("userName") String userName, Page page);
}
```

**XML 文件 (user-mapper.xml)：**

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

    <!-- 支持自动分页 -->
    <select id="listByUserName" resultMap="userResultMap">
        POST /user_info/_search {
            "query": {
                "term": { "name": #{userName} }
            }
        }
    </select>
</mapper>
```

## 3. 统一的分页实现

在 dbVisitor 中，无论是操作 MySQL 还是 ElasticSearch，分页查询的实现方式是完全统一的。你只需要传递一个 `Page` 对象。

```java
// 创建分页对象
Page page = new PageObject();
page.setPageSize(10);
page.setCurrentPage(0); // 第一页

// 执行查询，dbVisitor 会自动拦截并重写为分页查询
// 对于 ElasticSearch，会自动转换为 "from": 0, "size": 10
List<UserInfo> list = mapper.listByUserName("mali", page);

// 获取总记录数（如果需要）
long total = page.getTotalCount();

// 翻页
page.nextPage();
list = mapper.listByUserName("mali", page);
```

## 4. 适用范围

jdbc-elastic 适合需要复用 JDBC、Mapper 或 XML 的项目，不是完整 JDBC 或官方客户端的替代品。它不支持 JDBC Batch、事务和存储过程；未覆盖的命令应使用官方客户端。

使用 `term` 对字符串做精确匹配时，应先将相应字段定义为 keyword。写入后立即搜索还需考虑 refresh；可在写入请求中指定 `?refresh=wait_for`，或使用驱动相应连接参数。分页仍受 Elasticsearch 结果窗口等限制。

## 5. 总结

通过 dbVisitor，我们可以在同一个项目中，用同一套 API、同一种思维方式（Mapper/XML/Lambda）同时操作关系型数据库和 ElasticSearch。这极大地降低了混合架构项目的开发和维护成本，让数据访问层变得更加整洁和统一。

如果你正在寻找一种能够统一 RDBMS 和 NoSQL 开发体验的工具，dbVisitor 绝对值得一试。
