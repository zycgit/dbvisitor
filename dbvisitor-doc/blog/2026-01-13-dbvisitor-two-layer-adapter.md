---
slug: dbvisitor-two-layer-adapter
title: dbVisitor 的双层适配器
authors: [ZhaoYongChun]
tags: [dbVisitor, Architecture, JDBC, NoSQL]
---

本文将深入解析 dbVisitor 独特的"双层适配"架构，揭示其如何通过应用层与协议层的双重抽象，打破 RDBMS 与 NoSQL 的壁垒。


摘要
---

在使用 Java 进行现代应用开发时，混合使用关系型数据库（如 MySQL、PostgreSQL）和非关系型数据库（如 MongoDB、Elasticsearch）已成为常态。
然而，这种混合架构往往导致技术栈割裂：开发者需要在 JDBC/MyBatis 和各种 NoSQL 专有客户端之间频繁切换。

dbVisitor 提出的"双层适配"架构旨在解决这一痛点。

* **第一层（应用适配）**：在 API 层面，通过统一的 LambdaTemplate 和 Mapper 接口屏蔽底层语法差异（SQL vs DSL）。
* **第二层（协议适配）**：在驱动层面，实现了标准的 JDBC 接口，将 NoSQL 数据源封装为标准 JDBC 驱动。

这种设计不仅实现了"One API Access Any DataBase"的愿景，还带来了极高的灵活性：开发者既可以享受 dbVisitor 全栈的便捷，也可以仅使用其 JDBC 驱动，让现有的 MyBatis/Hibernate 项目瞬间具备操作 NoSQL 的能力。

*** ** * ** ***

双层适配
----

### 1. 第一层适配：应用层的统一抽象 (API Adapter)

**"应用层适配"** 解决的是 **"怎么写"** 的问题。

在这一层，dbVisitor 通过高度抽象的 API 屏蔽了底层语法的差异。无论后端对接的是 MySQL 的 SQL，还是 MongoDB 的 BSON 过滤器，甚至是 Elasticsearch 的 Query DSL，开发者面对的都是同一套 Java API。

dbVisitor 提供了 **5 种** 不同风格的 API，满足从简单 CRUD 到复杂报表分析的各类场景需求：

### 1. 编程式 API (JdbcTemplate)

这是最基础的形态，紧贴 JDBC 标准。它适合需要精细控制 SQL 执行，或者进行简单且直接的数据库操作的场景。
对于 NoSQL 数据库，你甚至可以在这里直接使用其原生查询脚本（如 Mongo Shell）。

```java
// 传统 SQL 方式
jdbcTemplate.executeUpdate("insert into user_info (id, name) values (?, ?)", 1, "mali");

// Mongo Shell 方式 (直接透传)
jdbcTemplate.executeUpdate("db.user_info.insert({_id: 1, name: 'mali'})");
```

### 2. 声明式 API (Interface)

通过定义 Java 接口并配合 `@Query` 等注解，将数据访问逻辑与业务代码分离。这种方式让代码结构更加清晰，易于维护。

```java
@SimpleMapper
public interface UserMapper {
    @Query("select * from user_info where age > :age")
    List<User> findByAge(@Param("age") int age);
}
```

### 3. 通用 Mapper (BaseMapper)

这是声明式 API 的增强版。通过继承 `BaseMapper<T>`，你无需编写任何代码即可获得标准的 CRUD 能力。
框架会自动根据泛型实体 T 生成对应的 Select/Insert/Update/Delete 语句或指令。

```java
// 仅需一句继承，即拥有全套 CRUD 方法
public interface UserMapper extends BaseMapper<UserInfo> {
}

// 使用
userMapper.insert(new UserInfo("1001", "Tom"));
UserInfo user = userMapper.selectById("1001");
```

### 4. 构造器 API (LambdaTemplate)

这是当前最推荐的用法。它利用 Java 的 Lambda 表达式实现了**类型安全**的查询构建。
最大的优势在于：当你重构 Java 实体类的属性名时，查询条件会自动更新，无需担心字符串硬编码带来的"炸雷"。

```java
// 会自动翻译为 SQL 或 NoSQL 对应的查询语句
List<UserInfo> users = lambdaTemplate.lambdaQuery(UserInfo.class)
    .eq(UserInfo::getAge, 18)
    .likeRight(UserInfo::getName, "Tom")
    .list();
```

### 5. 文件 Mapper (XML/DSL)

当遇到极度复杂的查询（如几百行的报表 SQL，或者极其复杂的 ES 聚合查询）时，将 SQL/DSL 放在 XML 文件中管理是最佳选择。
这不仅保持了 Java 代码的整洁，还支持强大的动态规则引擎。

```xml
<!-- UserMapper.xml -->
<mapper namespace="com.example.UserMapper">
    <select id="findComplexUsers">
        select * from user_info
        @{and, age > :minAge}
        @{and, name like :namePattern}
    </select>
</mapper>
```

*** ** * ** ***

第二层适配：协议层的数据标准化 (Driver Adapter)
--------------------------------

**"协议层适配"** 解决的是 **"怎么连"** 和 **"怎么传"** 的问题。

这是 dbVisitor 最具创新性的地方。不同于大多数框架仅在 API 层做封装，dbVisitor 向下深入到了驱动层，完整实现了 Java 的 `java.sql.Driver` 接口。

### NoSQL 的"关系化"伪装

在这一层，dbVisitor 将各类非关系型数据库 "伪装" 成了标准的 JDBC 接口：

* **表映射** : MongoDB 的 `Collection` 和 Elasticsearch 的 `Index` 被映射为 JDBC 的 `Table`。
* **行映射**: 文档（Document）被映射为行（Row），字段被映射为列（Column）。
* **SQL 解析** : 驱动内部内置了命令解析器。当你向驱动发送一条 原生 SQL/DSL 时，驱动会自动将其翻译为符合数据源的 SDK API 调用。

这种底层适配意味着：**任何支持 JDBC 的工具或框架，理论上都可以通过 dbVisitor 连接到 NoSQL 数据库。**

*** ** * ** ***

双层架构的灵活组合 (Synergy)
-------------------

这种 **应用层 (API)** + **协议层 (Driver)** 的双层设计，为项目架构带来了极大的灵活性。你可以根据团队的习惯和存量代码的情况，选择"全栈模式"或"驱动模式"。

### 模式一：全栈模式 (Best Practice)

同时使用 dbVisitor 的 API 和 Driver。这是最顺滑的使用方式，你将获得统一的开发体验、最佳的性能以及完整的类型安全支持。
> **适用场景**: 新项目开发，或者希望彻底统一数据访问层的项目。

### 模式二：驱动模式 (Integration)

**"老瓶装新酒"**。仅使用 dbVisitor 的 JDBC 驱动，而继续使用你熟悉的 ORM 框架（如 MyBatis、Hibernate、Spring Data JDBC）。

想象一下，你有一个运行了 5 年的 MyBatis 项目，现在需要接入 MongoDB 存储日志。
你不需要学习新的 MongoTemplate，也不需要引入繁重的 Spring Data Mongo。只需要：

1. 将 JDBC URL 修改为 dbVisitor 的 JDBC 格式。
2. 像写 MySQL 一样编写 MyBatis Mapper XML。

dbVisitor 驱动会默默地在后台将 MyBatis 发出的 Command 命令 转换为 MongoDB 指令。

```xml
<!-- MyBatis Mapper XML -->
<!-- 这是一个操作 MongoDB 的查询，但在 MyBatis 看来它就是标准 SQL -->
<select id="selectLogs" resultType="LogDoc">
    test.user_info.find({_id: ObjectId(#{id})})
</select>
```

*** ** * ** ***

总结
---

dbVisitor 的双层适配器架构，本质上是对 **JDBC 标准** 的一次致敬与延伸。

通过 **API 层** 的封装，它让开发者从繁杂的异构语法中解放出来，专注于业务逻辑；
通过 **Driver 层** 的实现，它打破了 RDBMS 与 NoSQL 的物理边界，让数据流转不再受限于协议。

无论你是追求极致开发效率的"全栈派"，还是坚守现有技术栈的"整合派"，dbVisitor 都能为你提供一个稳健、统一的数据访问底座。
