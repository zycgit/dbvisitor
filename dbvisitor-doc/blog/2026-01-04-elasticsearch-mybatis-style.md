---
last_update:
  date: 2026-09-17
slug: elasticsearch-mybatis-style
topics: [datasources]
title: "用 Mapper 操作 Elasticsearch"
authors: [ZhaoYongChun]
tags: [ElasticSearch, MyBatis, ORM, JDBC, dbVisitor]
language: zh-cn
---

在 Java 生态中，操作 ElasticSearch 最常见的方式莫过于使用官方的 `elasticsearch-java` (或旧版的 `RestHighLevelClient`) 或者 Spring 家族的 `spring-data-elasticsearch`。这些工具非常强大，但对于习惯了关系型数据库（RDBMS）和 MyBatis 开发模式的开发者来说，切换到 ElasticSearch 往往意味着需要适应一套全新的 API 和思维模式（DSL 构建、Builder 模式等）。

特别是在一个混合架构的项目中，如果同时存在 MySQL 和 ElasticSearch，数据访问层的代码风格割裂感会非常强：一边是 MyBatis 的 Mapper 接口和 XML，另一边是复杂的 DSL 构建代码或 Repository 接口。这种差异不仅增加了学习成本，也让诸如“分页查询”这样的通用功能难以统一实现。

本文将介绍如何使用 **dbVisitor**，以一种“类 MyBatis”的方式来操作 ElasticSearch，实现架构上的统一。

<!--truncate-->

固定使用 6.8.0 的回归示例：[GitHub](https://github.com/zycgit/dbvisitor/tree/main/dbvisitor-example/blog-680) / [Gitee](https://gitee.com/zycgit/dbvisitor/tree/main/dbvisitor-example/blog-680)。


使用 Java 17+，引入 `net.hasor:dbvisitor:6.8.0` 及以下适配器：

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>jdbc-elastic</artifactId>
    <version>6.8.0</version>
    <classifier>all</classifier>
</dependency>
```


## 1. 传统方式的痛点

在传统的混合架构中，我们可能会遇到以下问题：

*   **API 风格不统一**：使用 SQL，ElasticSearch 使用 REST API 和 JSON DSL。
*   **分页实现差异**：MyBatis 通常配合 PageHelper，而 ElasticSearch 需要手动设置 `from` 和 `size`。
*   **维护成本高**：需要维护两套完全不同的底层逻辑，增加了代码的复杂度和出错的概率。

## 2. dbVisitor 方案 {#2-dbvisitor-的解决方案}

dbVisitor 通过提供一个 JDBC 驱动层（`dbvisitor-driver`）和适配器（`jdbc-elastic`），将 ElasticSearch 的操作封装成了标准的 JDBC 接口。这意味着你可以像操作 MySQL 一样操作 ElasticSearch。

更进一步，dbVisitor 提供了类似 MyBatis 的 ORM 功能，支持 Mapper 接口、XML 映射文件、注解以及 Lambda 表达式。

### 2.1 实体映射 {#21-对象关系映射-orm}

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

### 2.2 注解 Mapper {#22-使用-mapper-接口-注解方式}

你可以定义一个 Mapper 接口，使用注解编写 ElasticSearch 命令。路径中的 `{#{id}}` 经 dbVisitor 绑定后成为驱动的 `{?}` 路径占位符；JSON 数据值直接使用 `#{...}`。

```java
@SimpleMapper
public interface UserInfoMapper {
    // 插入数据
    @Insert(value = "POST /user_info/_doc\\?refresh=wait_for { \"name\": #{info.name}, \"age\": #{info.age} }",
            useGeneratedKeys = true, keyProperty = "id", keyColumn = "_id")
    int saveUser(@Param("info") UserInfo info);

    // 根据 ID 查询
    @Query("POST /user_info/_search {\"query\": {\"term\": {\"_id\": #{id}}}}")
    UserInfo loadById(@Param("id") String id);

    // 删除数据
    @Delete("DELETE /user_info/_doc/{#{id}}")
    int deleteUser(@Param("id") String id);
}
```

这里使用单个实体参数：`@Param("info")` 给命令中的 `#{info.name}` 提供参数名，`keyProperty="id"` 则指定实体的主键属性。两者用途不同，`keyProperty` 不是参数表达式，不要写成 `info.id`；单个实体不加 `@Param` 也可以回填。

按 ID 查询使用 `_search`，其命中结果可映射为实体；直接 `GET /index/_doc/id` 返回含 `_source` 的响应，不能当作同样的扁平行映射。

### 2.3 通用 Mapper {#23-使用通用-mapper}

如果你不想写任何命令，可以直接继承 `BaseMapper`，dbVisitor 会自动生成基础的 CRUD 操作。

```java
@SimpleMapper
public interface UserInfoBaseMapper extends BaseMapper<UserInfo> {
    // 自动拥有 insert, update, delete, selectById, listBySample 等方法
}
```

### 2.4 Lambda 构造器 {#24-使用-lambda-方式}

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

### 2.5 XML Mapper {#25-使用-xml-管理-mapper-mybatis-风格}

对于复杂的查询或需要统一管理 DSL 的场景，dbVisitor 支持使用 XML 文件来定义 Mapper，这与 MyBatis 的体验几乎一致。

**Mapper 接口：**

```java
@RefMapper("mapper/user-mapper.xml")
public interface UserInfoXmlMapper {
    int saveUser(@Param("info") UserInfo info);
    PageResult<UserInfo> listByUserName(@Param("userName") String userName, Page page);
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
        POST /user_info/_doc\?refresh=wait_for {
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

## 3. 分页查询 {#3-统一的分页实现}

上面的 XML Mapper 返回 `PageResult<UserInfo>`，同时分页并查询总记录数。仅返回 `List<UserInfo>` 不会自动统计总数。`Page`、`PageObject`、`PageResult` 均来自 `net.hasor.dbvisitor.page`。
```java
// 创建分页对象
Page page = new PageObject();
page.setPageSize(10);
page.setCurrentPage(0); // 第一页

// 执行查询，dbVisitor 会自动拦截并重写为分页查询
// 对于 ElasticSearch，会自动转换为 "from": 0, "size": 10
PageResult<UserInfo> result = mapper.listByUserName("mali", page);
List<UserInfo> list = result.getData();

// 获取总记录数（如果需要）
long total = result.getTotalCount();

// 翻页
page.nextPage();
result = mapper.listByUserName("mali", page);
list = result.getData();
```

:::caution[6.8.0 分页边界]
此例使用 XML 语句及 `resultMap`，不要直接替换成返回 `PageResult` 的 `@Query` 方法：6.8.0 该注解路径会触发 `ClassCastException`。BaseMapper 需要总数时，先通过 `pageInitBySample(...)` 初始化，再调用 `pageBySample(...)`。

注解分页问题已在 6.8.1 开发分支修复，尚未发布；本文及示例工程仍使用 6.8.0 的 XML 分页。
:::

## 4. 适用范围

jdbc-elastic 适合需要复用 JDBC、Mapper 或 XML 的项目，不是完整 JDBC 或官方客户端的替代品。它不支持 JDBC Batch、事务和存储过程；未覆盖的命令应使用官方客户端。

使用 `term` 对字符串做精确匹配时，应先将相应字段定义为 keyword。写入后立即搜索还需考虑 refresh；可在写入请求中指定 `?refresh=wait_for`，或使用驱动相应连接参数。分页仍受 Elasticsearch 结果窗口等限制。

命令经过 dbVisitor Mapper/JdbcTemplate 时，URL 中作为分隔符的 `?` 需要转义：Java 字符串写 `\\?`，XML 写 `\?`，框架解析后会移除转义。直接使用驱动的 `Statement` / `PreparedStatement` 时仍写正常的 `?refresh=wait_for`，不要加入这一层框架转义。

## 5. 总结

通过 dbVisitor，我们可以在同一个项目中，用同一套 API、同一种思维方式（Mapper/XML/Lambda）同时操作关系型数据库和 ElasticSearch。这极大地降低了混合架构项目的开发和维护成本，让数据访问层变得更加整洁和统一。

如果你正在寻找一种能够统一 RDBMS 和 NoSQL 开发体验的工具，dbVisitor 绝对值得一试。
