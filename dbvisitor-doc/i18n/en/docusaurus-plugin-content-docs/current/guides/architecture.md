---
sidebar_position: 3
title: 架构设计
description: dbVisitor 架构在整体上体系化，局部层面各个模块遵循独立原则。因此每一个模块几乎都可以独立使用而且互不影响。
---

# 架构设计

dbVisitor 架构在整体上体系化，局部层面各个模块遵循独立原则。因此每一个模块几乎都可以独立使用而且互不影响。

在整体系统上自低向上可以分为两层，低层是公共组件模块。顶层是三个不同的使用模式。

```text
+-------------------------------------------------------+-------+
|                   SQL & CRUD & Mapper                 |       |
+-----------+-------------+---------------+-------------+ Utils |
|   Types   |   Mapping   | Resource & TX |   Dialect   |       |
+-----------+-------------+---------------+-------------+-------+
```

## 公共模块

### Types 模块

主要是指 `net.hasor.dbvisitor.types` 软件包。该软件包以 `net.hasor.dbvisitor.types.TypeHandler` 接口为中心，提供不同类型数据的写入和读取支持。

由于 `TypeHandler` 接口来自于 MyBatis，因此 Types 模块中的代码有相当一部分 `TypeHandler` 的实现是直接从 MyBatis 移植过来。

类 `TypeHandlerRegistry` 的作用是负责注册和管理 dbVisitor 中的 `TypeHandler`，并且实现了 **[类型](./types/type-handlers.md)** 中描述的查找优先级。

此外 Types 还提供了下面四个注解的支持，具体用法请查阅 **[自定义类型处理器](./types/custom-handler.md)**
- `@MappedCross`、`@MappedCrossGroup`、`@MappedJavaTypes`、`@MappedJdbcTypes`

### Mapping 模块

主要是指 `net.hasor.dbvisitor.mapping` 软件包。该软件包通过 `@Table`、`@Column`、`@Ignore` 三个注解，提供简单的 ORM 注解化映射。
并为 dbVisitor 的 **[对象映射](./objects/class-as-table.md)** 机制提供实现。

`TableReader` 接口是该模块的一个能力接口，它提供了 `extractData` 和 `extractRow` 两个方法，可以用于根据映射信息读取 `ResultSet` 中的内容。

而 `TableMappingResolve` 接口则是一个 `TableMapping` 解析器，包内有一个 `ClassTableMappingResolve` 实现类。
而它就是负责解析 `@Table`、`@Column`、`@Ignore` 三个注解的解析器。

### Resource & TX 模块

主要是指 `net.hasor.dbvisitor.transaction` 软件包，它负责提供本地资源管理以及事务控制。具体可参考 **[资源与事务](./transaction/datasource.md)**

该模块主要分为两个部分：

**数据源管理**
- 这部分核心接口有 `DataSourceManager`、`ConnectionHolder`
- 前者提供 **[本地同步](./transaction/datasource.md#本地同步)** 的实现，后者负责连接复用。

**事务管理器**
- 这部分核心接口有 `TransactionTemplate`、`TransactionManager`、`TransactionStatus` 三个。
- 其中前两个分别是操作数据库事务的两个方式，后一个接口用来表示一个具体的事务。

### Dialect 模块

主要是指 `net.hasor.dbvisitor.dialect` 软件包，它负责提供不同数据库的方言实现。具体可参考 **[分页与方言](./page.mdx)**

方言体系中一共有两个主要的概念：`BoundSql`、`SqlDialect`，前者是对要执行的 SQL与其参数的封装后者是方言。

方言接口 `SqlDialect` 有三个子接口分别提供了三个场景的方言方法。

- `ConditionSqlDialect` 条件方言，目前主要是对 like 语句进行生成，例如：`concat('%', ? )`
- `InsertSqlDialect` Insert语句方言，这个方言接口为 CRUD 模式中的 **[冲突策略](./crud/conflict.md)** 提供方言实现
- `PageSqlDialect` 这个是分页方言，它提供了 `countSql`、`pageSql` 两个方法用于分页语句生成

## 使用模式

dbVisitor 一共提供三种数据库操作模式，你可以在同一个项目中按照不同需要同时或者部分使用这些不同模式的 API。

### SQL 模式

主要是指 `net.hasor.dbvisitor.jdbc` 软件包，**[SQL 模式](./jdbc/about.md)** 是 dbVisitor 三大使用模式中 API 最低级别的 API。
在这个模式中开发者需要完全自己编写 SQL 语句，并且 SQL 语句的处理需要代码介入，这种模式对于高度定制化 SQL 将会十分友好。
比如根据某种特定场景下的通用规则来生成 SQL 语句。

这一模式下主要入口 API 为 `JdbcTemplate` 类，该类和 Spring 的 JdbcTemplate 类同名实现思路也完全相同。其 API 接口中的方法大多也是从 Spring 中移植过来的。
熟知的 `RowMapper`、`ConnectionCallback`、`ResultSetExtractor` 接口在 dbVisitor 中也同样存在，因此可以简单的把它和 Spring JDBC 化作等号。

`JdbcTemplate` 架构比 Spring 更加优越的地方在于下面三点：
- 在读取 `ResultSet` 数据时会使用 **Types 模块**
- 例如在使用 `queryForList(String,Class<?>)` 这种方法返回一个 Bean 列表的时候会使用 **Mapping 模块**
- 可以处理多语句查询并且获取多语句的执行结果。

而 Spring JDBC 只有在最新的 Spring Data 中才对上面第二点有所支持。

### 单表 模式

主要是指 `net.hasor.dbvisitor.lambda` 软件包，**[单表 模式](./crud/basic.md)** 中实践了 **ActiveRecord** 思想，主打单表操作。

这一模式下 `LambdaTemplate` 成为入口类，通过它可以创建下面四个特殊化的接口来生成不同类别的 SQL 语句。
- **LambdaDelete** 负责生成和执行 `delete` 语句
- **LambdaInsert** 负责生成和执行 `insert` 语句
- **LambdaQuery** 负责生成和执行单表的 `select` 语句
- **LambdaUpdate** 负责生成和执行 `update` 语句

从 API 设计上 dbVisitor 参考了下列两个框架的 API 风格，因此会有不少熟悉的味道。
- `BeetlSql` 的 Query
- `MyBatis Plus` 的条件构造器

在 CRUD 层面开始支持分页操作，这一逻辑在下面这个方法中实现。

- 类 `net.hasor.dbvisitor.lambda.core.AbstractQueryExecute` 的 `getBoundSql` 方法

### Mapper 模式

主要是指 `net.hasor.dbvisitor.dal` 软件包，**[Mapper 模式](./dal/dal-mapper.md)** 对 MyBatis 的 Mapper 映射文件有着高度的兼容。
这主要体现在 **[动态 SQL](./dal/dynamic.md)** 因此，如果单独从这个维度上来讲，可以说 dbVisitor 是一个翻版的 MyBatis。

从架构上来讲这一部分可以分为 四个小的组成部分
- **dynamic** 提供动态生成 SQL 的框架支持，包括了 dbVisitor 独特的规则机制。
- **execute** 动态 SQL 的执行引擎，支持 `CallableStatement`、`PreparedStatement`、`Statement`
- **repository** 负责解析 mapper 文件使其变成 **dynamic** 模型，同时它也为 **[注解化 Mapper](./dal/anno-mapper.mdx)** 提供实现
- **session** 是个入口，熟知的 `DalSession` 类就是由它提供。另外 `BaseMapper` 也是由它提供。

**dynamic** 模型

模型的抽象接口是 `DynamicSql`，它具有多个子类和实现类用以表达动态 SQL 的逻辑关系。这些模型位于 `net.hasor.dbvisitor.dal.dynamic.nodes` 包中。

**[参数表达式](./dal/parameters.md)** 

`${...}`、`@{...}`、`#{...}` 三种表达式的提取是通过 `net.hasor.dbvisitor.dal.dynamic.tokens` 实现。
原始代码来自于 MyBatis 同名类，dbVisitor 扩展了它的 `openToken` 可以支持更多的前缀。

### 规则

`@{...}`、`#{...}` 两个表达式被归入 规则 体系，一个规则需要实现 `SqlBuildRule` 接口。
dbVisitor 中所有内置规则都位于 `net.hasor.dbvisitor.dal.dynamic.rule` 软件包，规则类似于一个函数。允许自定义动态 SQL 生成的能力。

`#{...}` 写法实际上是 `arg` 规则的简写，`arg` 规则的源码是 `net.hasor.dbvisitor.dal.dynamic.rule.ArgRule`

规则虽然有固定的三段式格式 `@{<规则名> [, <启用条件OGNL> [, 规则内容 ]])` 但除了规则名之外，其余部分都可以拿来自由发挥。
比如 **[OR & AND 规则](./dal/rules.md)** 就是将启用条件那一段作为规则实际的内容。

规则，举一个例子就是有意的将下列 动态 SQL 写法改为表达式写法。将 XML 在有限范围内化作表达式。

```xml title='一段if判断'
<if test="age != null">
    and age = #{age}
</if>
```

```xml title='规则写法'
@{and, age = :age}
```
