---
id: differences
sidebar_position: 6
hide_table_of_contents: true
title: 4.6 数据源差异
description: 介绍关系型和非关系型数据库在使用过程中的差异性。
---

# 数据源差异

dbVisitor 力争使用统一的 API 来操作所有 **关系型数据库** 和 **非关系型数据库**。但实际中数据源由于其本身的特性仍存在一些个体差异。
dbVisitor 对待差异的处理方式主要体现在两个方面：
- **API 支持性**，是指被调用的 API 在某数据源上是否支持。
- **数据库方言**，是指在使用 [构造器 API](./lambda_api) 时相同 API 操不同数据库时采用了不同的命令或语法。

:::tip[特点]
您若想改版这些差异，可以参与项目并贡献您的改进代码。
:::

## API 支持性 {#api}

dbVisitor 在统一内核架构下面向使用而设计的 API 主要分为 4 种，[编程式 API](./program_api)、[声明式 API](./declarative_api)、[构造器 API](./lambda_api)、[Mapper File](./file_mapper)

对于以 SQL 语言为依托的 **关系型数据库**，dbVisitor 提供的所有 API 均可使用。在数据库差异细节点上需参考本文 [数据库方言](./differences#dialect) 中的描述。

对于 **非关系型数据库** 通过下列指引可详细了解其中差异：
- [Redis 数据源特异性](../core/redis/about)
- [MongoDB 数据源特异性](../core/mongo/about)
- [ElasticSearch 数据源特异性](../core/elastic/about)

:::info[JDBC 特性支持]
对于非关系型数据库驱动（Mongo、Elastic），dbVisitor 实现了 `Statement.RETURN_GENERATED_KEYS` 特性。
这意味着在使用 `JdbcTemplate` 或 `Statement` 执行插入操作时，可以自动获取生成的 `_id`。
:::

## 数据库方言 {#dialect}

得益于 SQL 规范标准的统一性，通常绝大部分数据源只要支持 SQL 标准并在提供 JDBC 驱动的情况下都可以正常执行。
下面表表格中罗列了 dbVisitor 内置数据库方言对于 [构造器 API](./lambda_api) 的功能支持性差异。

| 数据库           |  分页查询  | 冲突策略                      | 空值排序策略 | 序列  |
|---------------|:------:|:--------------------------|--------|-----|
| DB2           |   支持   | Into                      |        |     |
| Apache Derby  |   支持   | Into                      |        |     |
| 达梦            |   支持   | Into、Ignore（表要有主键）        |        |     |
| H2            |   支持   | Into                      |        | 支持  |
| Hive          |   支持   | Into                      |        |     |
| HSQL          |   支持   | Into                      |        |     |
| Apache Impala |   支持   | Into                      |        |     |
| IBM Informix  |   支持   | Into                      |        |     |
| 人大金仓          |   支持   | Into                      |        |     |
| MariaDB       |   支持   | Into                      |        |     |
| MySQL         |   支持   | Into、Update、Ignore        | 支持     |     |
| Oracle        |   支持   | Into、Update、Ignore（表要有主键） |        |     |
| Phoenix       |   支持   | Into                      |        |     |
| PostgreSQL    |   支持   | Into、Update、Ignore        |        |     |
| SQLite        |   支持   | Into                      |        |     |
| SQL SERVER    |   支持   | Into                      |        |     |
| 虚谷数据库         |   支持   | Into                      |        |     |

