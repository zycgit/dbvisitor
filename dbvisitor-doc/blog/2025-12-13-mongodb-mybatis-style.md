---
slug: mongodb-mybatis-style
title: dbVisitor 使用 MyBatis 方式操作 MongoDB
authors: [ZhaoYongChun]
tags: [mongodb, mybatis, orm, jdbc, dbvisitor]
language: zh-cn
---

在 Java 生态中，操作 MongoDB 最常见的方式莫过于使用官方的 `mongo-java-driver` 或者 Spring 家族的 `spring-data-mongodb`。这些工具非常强大，但对于习惯了关系型数据库（RDBMS）和 MyBatis 开发模式的开发者来说，切换到 MongoDB 往往意味着需要适应一套全新的 API 和思维模式。

特别是在一个混合架构的项目中，如果同时存在 MySQL 和 MongoDB，数据访问层的代码风格割裂感会非常强：一边是 MyBatis 的 Mapper 接口和 XML，另一边是 `MongoTemplate` 的链式调用或 Repository 接口。这种差异不仅增加了学习成本，也让诸如“分页查询”这样的通用功能难以统一实现。

本文将介绍如何使用 **dbVisitor**，以一种“类 MyBatis”的方式来操作 MongoDB，实现架构上的统一。

<!--truncate-->

## 1. 传统方式的痛点

在传统的混合架构中，我们可能会遇到以下问题：

*   **API 风格不统一**：RDBMS 使用 SQL 和 JDBC，MongoDB 使用 BSON 和专有协议。
*   **分页实现差异**：MyBatis 通常配合 PageHelper 或 RowBounds，而 MongoDB 需要手动计算 `skip` 和 `limit`，或者使用 Spring Data 的 `Pageable`。
*   **维护成本高**：需要维护两套完全不同的底层逻辑，增加了代码的复杂度和出错的概率。

## 2. dbVisitor 的解决方案

dbVisitor 通过提供一个 JDBC 驱动层（`dbvisitor-driver`）和适配器（`jdbc-mongo`），将 MongoDB 的操作封装成了标准的 JDBC 接口。这意味着你可以像操作 MySQL 一样操作 MongoDB。

更进一步，dbVisitor 提供了类似 MyBatis 的 ORM 功能，支持 Mapper 接口、XML 映射文件、注解以及 Lambda 表达式。

### 2.1 对象关系映射 (ORM)

首先，我们定义一个 Java 对象，并使用注解进行映射。这与 MyBatis Plus 或 JPA 非常相似。

```java
@Table("user_info")
public class UserInfo {
    // 映射 _id 字段，并自动处理 ObjectId
    @Column(value = "_id", primary = true, keyType = KeyType.Auto, whereValueTemplate = "ObjectId(?)")
    private String id;

    @Column("name")
    private String name;

    @Column("age")
    private Integer age;

    // 省略 getter/setter
}
```

### 2.2 使用 Mapper 接口 (注解方式)

你可以定义一个 Mapper 接口，使用注解来编写 MongoDB 的命令。

```java
@SimpleMapper
public interface UserInfoMapper {
    // 插入数据
    @Insert("test.user_info.insert(#{info})")
    int saveUser(@Param("info") UserInfo info);

    // 根据 ID 查询
    @Query("test.user_info.find({_id: ObjectId(#{id})})")
    UserInfo loadById(@Param("id") String id);

    // 删除数据
    @Delete("test.user_info.remove({_id: ObjectId(#{id})})")
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

对于复杂的查询或需要统一管理 SQL 的场景，dbVisitor 支持使用 XML 文件来定义 Mapper，这与 MyBatis 的体验几乎一致。

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
        test.user_info.insert({
            name: #{info.name},
            age: #{info.age}
        })
    </insert>

    <!-- 支持自动分页 -->
    <select id="listByUserName" resultMap="userResultMap">
        test.user_info.find({name: #{userName}})
    </select>
</mapper>
```

## 3. 统一的分页实现

在 dbVisitor 中，无论是操作 MySQL 还是 MongoDB，分页查询的实现方式是完全统一的。你只需要传递一个 `Page` 对象。

```java
// 创建分页对象
Page page = new PageObject();
page.setPageSize(10);
page.setPageNumber(0); // 第一页

// 执行查询，dbVisitor 会自动拦截并重写为分页查询
// 对于 MongoDB，会自动转换为 .skip(0).limit(10)
List<UserInfo> list = mapper.listByUserName("mali", page);

// 获取总记录数（如果需要）
long total = page.getTotalCount();

// 翻页
page.nextPage();
list = mapper.listByUserName("mali", page);
```

## 4. 总结

通过 dbVisitor，我们可以在同一个项目中，用同一套 API、同一种思维方式（Mapper/XML/Lambda）同时操作关系型数据库和 MongoDB。这极大地降低了混合架构项目的开发和维护成本，让数据访问层变得更加整洁和统一。

如果你正在寻找一种能够统一 RDBMS 和 NoSQL 开发体验的工具，dbVisitor 绝对值得一试。
