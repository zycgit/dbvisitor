---
slug: dbvisitor-dialect-refactoring
title: 方言系统架构演进：从分离到统一
authors: [ZhaoYongChun]
tags: [Architecture, dbVisitor]
date: 2026-01-15
---

dbVisitor 是一个旨在提供统一数据库访问体验的 Java 工具库。随着对 MySQL、PostgreSQL 等关系型数据库以及 MongoDB、ElasticSearch 等 NoSQL 数据源支持的不断深入，底层的方言系统（Dialect System）面临着越来越复杂的挑战。

近期，我们对 dbVisitor 的方言系统进行了一次深度的架构重构。本次重构不涉及功能变更，旨在解决旧架构中存在的抽象割裂问题，将“方言元数据”与“命令构建能力”高度内聚。

本文将深入探讨这次架构演进背后的思考、实施方案以及带来的显著优势。

<!--truncate-->

## 背景：旧架构的痛点

在重构之前，dbVisitor 的方言层设计采用了职责分离的原则，主要由两个平行的接口体系构成：

1.  **`SqlDialect`**：负责定义数据库的静态特征和元数据。例如：左右转义符、关键字集合、分页语句的拼接模式、表名/列名的格式化规则等。它通常是**无状态的单例**。
2.  **`SqlCommandBuilder`**（及其子类 `MongoCommandBuilder` 等）：负责动态构建查询命令。它持有查询的上下文（SELECT 哪些列、WHERE 条件是什么），最终生成 `BoundSql`。它是**有状态的**对象。

### 存在的问题

这种分离虽然遵循了单一职责原则，但在实际扩展和维护中暴露出了明显的问题：

*   **抽象割裂**：当我们要适配一种新数据库（例如 TiDB）时，实现一个 `TiDBDialect` 很容易，但如果它的 SQL 语法比较特殊，我们可能需要修改通用的 `SqlCommandBuilder` 甚至继承一个新的 Builder。对于 MongoDB 这种非 SQL 数据源，情况更糟：我们需要创建特定的 `MongoCommandBuilder`，并且必须在上层代码（如 `LambdaTemplate`）中硬编码判断逻辑来决定实例化哪个 Builder。
*   **API 使用繁琐**：用户或上层框架在构建查询时，必须显式地进行“配对”。
    *   MySQL 场景：`new SqlCommandBuilder(new MySqlDialect())`
    *   Mongo 场景：`new MongoCommandBuilder(new MongoDialect())`
*   **中间类冗余**：为了适配 NoSQL，我们引入了 `MongoBuilderDialect` 这样的胶水代码，仅仅是为了把 Dialect 和 Builder 粘合在一起，这增加了代码库的复杂度。

## 演进：方言即工厂

本次重构的核心理念是：**方言对象本身应该是构建器的工厂**。

如果说 `SqlDialect` 定义了数据库“是什么”（元数据），那么由它生产的 `CommandBuilder` 实例就负责解决“怎么做”（构建查询）。

### 核心变更

1.  **引入工厂方法**：
    我们在 `SqlDialect`（及其子接口/抽象类）中引入了 `newBuilder()` 方法。任何一个方言实现，都必须有能力创建一个能理解该方言的构建器。

2.  **原型模式**：
    我们将 Dialect 实现类赋予了“双重身份”：
    *   **作为元数据对象**（单例）：如 `MySqlDialect.DEFAULT`，无状态，提供关键字定义等通用信息。
    *   **作为构建器对象**（原型）：当调用 `MySqlDialect.DEFAULT.newBuilder()` 时，它会返回一个新的 `MySqlDialect` 实例（或者专门的内部类实例），这个新实例持有查询状态（table, where, columns...）。

    ```java
    // 重构后的 MySqlDialect 简略示意
    public class MySqlDialect extends AbstractSqlDialect {
        // 元数据定义...
        
        @Override
        public SqlCommandBuilder newBuilder() {
            // 返回一个新的实例，用于构建 SQL
            return new MySqlDialect(); 
        }
    }
    ```

3.  **继承体系重组与简化**：
    我们彻底移除了独立的 `SqlCommandBuilder` 类文件，将其逻辑下沉到了抽象基类中。新的层级结构如下：

    *   `AbstractBuilderDialect`: 顶层基类，定义通用的 Builder 行为。
    *   `AbstractSqlDialect`: **(核心)** 继承自前者，实现了标准 JDBC SQL 的生成逻辑（SELECT/UPDATE/INSERT...）。所有标准 SQL 数据库（MySQL, PG, Oracle 等）均继承此基类。
    *   `MongoDialect`: 直接继承自 `AbstractBuilderDialect`，内部实现了针对 MongoDB BSON 的构建逻辑。**彻底移除了旧版本中的 `MongoCommandBuilder` 和 `MongoBuilderDialect`**。
    *   `AbstractElasticDialect`: 为 ES 提供 DSL 构建支持。


## 改造后的优势

### 1. 极简且安全的 API
对于上层调用者（如 `LambdaTemplate`），获取构建器变得异常统一和简单。再也不需要 `instanceof` 判断，也不需要在构建时传入 Dialect 参数：

```java
// 旧方式（伪代码）：逻辑分散且冗余
CommandBuilder builder;
if (dialect instanceof MongoDialect) {
    builder = new MongoCommandBuilder();
} else {
    builder = new SqlCommandBuilder();
}
// 需要显式关联，甚至在 build 时还要再次传入，存在不匹配风险
BoundSql sql = builder.buildSelect(dialect, true); 

// 新方式：统一多态，自包含
CommandBuilder builder = dialect.newBuilder();
// 构建器本身就是 Dialect 的一种形态，无需再传入参数，杜绝了“张冠李戴”
BoundSql sql = builder.buildSelect(true); 
```

### 2. 内聚性提升
所有的数据库特定逻辑——无论是“转义符是什么”还是“如何生成 INSERT 语句”——现在都收敛在同一个类（或其父类）中。
例如，`MongoDialect` 现在是一个自包含的单元，它既知道 Mongo 的关键字，也知道如何生成 Mongo 查询。

### 3. 消除了方言不匹配的风险
在旧版本中，`SqlCommandBuilder` 在生成 SQL 时要求传入 `SqlDialect` 对象。这在 API 设计上留下了隐患：理论上，你可以创建一个 `MongoCommandBuilder` 却传给它一个 `MySqlDialect`，这会导致运行时错误或荒谬的查询构建。
重构后，构建器由方言直接生产，并且 `buildSelect` 等方法不再接收 `Dialect` 参数。构建器“自带”元数据知识，从编译层面杜绝了方言不匹配的可能。

### 4. 代码量减少与维护性提高
通过这次重构，我们删除了多个冗余的 Builder 类和适配器类。
测试用例也变得更加通用：我们可以编写一套针对 `dialect.newBuilder()` 的测试，然后用不同的 Dialect 实现去运行它，只需验证生成的 `BoundSql` 字符串即可。

## 升级指南
对于 dbVisitor 的普通使用者，本次重构是完全透明的，API 保持向下兼容。
对于开发自定义 Dialect 的高阶用户，如果您之前依赖了 `SqlCommandBuilder` 类，请将其改为继承 `AbstractSqlDialect` 并重写 `newBuilder()` 方法。
