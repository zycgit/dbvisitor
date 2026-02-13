---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: 数据源差异
description: 介绍关系型和非关系型数据库在使用过程中的差异性。
---

# 数据源差异

dbVisitor 力争使用统一的 API 来操作所有 **关系型数据库** 和 **非关系型数据库**。但实际中数据源由于其本身的特性仍存在一些个体差异。
dbVisitor 对待差异的处理方式主要体现在两个方面：
- **API 支持性**，是指被调用的 API 在某数据源上是否支持。
- **数据库方言**，是指在使用 [构造器 API](../lambda_api) 时相同 API 操不同数据库时采用了不同的命令或语法。

:::tip[特点]
您若想改变这些差异，可以参与项目并贡献您的改进代码。
:::

## 数据源支持一览 {#api}

<span id="dialect"></span>

dbVisitor 在统一内核架构下设计了 4 种 API：[编程式 API](../program_api)、[声明式 API](../declarative_api)、[构造器 API](../lambda_api)、[Mapper File](../file_mapper)。
其中 **JdbcTemplate**、**注解方式**、**Mapper File** 在所有数据源上均可使用。

dbVisitor 具备智能的方言推断能力，会自动根据 JDBC URL 识别目标数据库类型并配置最佳方言，通常 **无需手动配置**。
如需显式指定，支持 **方言别名**（如 `mysql`）或 **方言全限定类名**。

下表汇总了各数据源的 API 支持与方言特性差异，各列含义：
- **构造器 API** — 包含 LambdaTemplate、BaseMapper、对象映射和结果集映射，四者支持性一致
- **写入冲突** — 所有数据源均支持标准写入（Into），此列仅标注额外支持的冲突策略（[详解](#insert-strategy)）
- **分页** — 方言是否实现 `PageSqlDialect` 接口
- **序列** — 方言是否实现 `SeqSqlDialect` 接口
- **向量** — 方言是否实现 `VectorSqlDialect` 接口（[详解](#vector)）
- **空值排序** — 方言是否覆写了 `orderByNulls` 方法

| 数据源 | 配置 Key | 构造器 API | Batch | 存储过程 | 自增回填 | 分页 | 写入冲突 | 序列 | 向量 | 空值排序 |
|-------|--------|:--------:|:-----:|:-----:|:----:|:--:|------|:--:|:--:|:----:|
| MySQL | mysql  | ✅ | ✅ | ✅ |  ✅   | ✅ | Ignore Update | | | ✅ |
| MariaDB | mariadb | ✅ | ✅ | ✅ |  ✅   | ✅ | Ignore Update | | | ✅ |
| PostgreSQL | postgresql | ✅ | ✅ | ✅ |  ✅   | ✅ | Ignore Update¹ | ✅ | ✅ | |
| 人大金仓 | kingbase | ✅ | ✅ | ✅ |  ✅   | ✅ | Ignore Update¹ | ✅ | ✅ | |
| Oracle | oracle | ✅ | ✅ | ✅ |  ✅   | ✅ | Ignore¹ Update¹ | | | |
| 达梦 | dm     | ✅ | ✅ | ✅ |  ✅   | ✅ | Ignore¹ | | | |
| SQL Server | sqlserver/ jtds | ✅ | ✅ | ✅ |  ✅   | ✅ | | | | |
| SQL Server (jTDS) | jtds   | ✅ | ✅ | ✅ |  ✅   | ✅ | | | | |
| DB2 | db2    | ✅ | ✅ | ✅ |  ✅   | ✅ | | | | |
| H2 | h2     | ✅ | ✅ | ✅ |  ✅   | ✅ | | ✅ | | |
| Apache Derby | derby  | ✅ | ✅ | ✅ |  ✅   | ✅ | | | | |
| HSQL | hsql   | ✅ | ✅ | ✅ |  ✅   | ✅ | | | | |
| Hive | hive   | ✅ | ✅ | ✅ |  ✅   | ⚠️ | | | | |
| Apache Impala | impala | ✅ | ✅ | ✅ |  ✅   | ✅ | | | | |
| IBM Informix | informix | ✅ | ✅ | ✅ |  ✅   | ✅ | | | | |
| SQLite | sqlite | ✅ | ✅ | ✅ |  ✅   | ✅ | | | | |
| 虚谷数据库 | xugu   | ✅ | ✅ | ✅ |  ✅   | ✅ | | | | |
| Redis | —      | ❌ | ❌ | ❌ |  ❌   | | | | | |
| MongoDB | mongo  | ✅ | ❌ | ❌ |  ✅   | ✅ | | | | |
| ElasticSearch 6 | elastic6 | ✅ | ❌ | ❌ |  ✅   | ✅ | | | | |
| ElasticSearch 7 | elastic7 | ✅ | ❌ | ❌ |  ✅   | ✅ | | | ✅ | |
| ElasticSearch 8 | elastic8 | ✅ | ❌ | ❌ |  ✅   | ✅ | | | ✅ | |
| Milvus | milvus | ✅ | ❌ | ❌ |  ❌   | ✅ | | | ✅ | |

> ✅ 支持 &nbsp; ❌ 不支持
>
> ¹ 需有主键
>
> **⚠️ Hive**：虽然实现了 `PageSqlDialect`，但 `countSql` 和 `pageSql` 均会抛出 `UnsupportedOperationException`，实际不可用。

:::info[JDBC 特性支持]
对于非关系型数据库驱动（Mongo、Elastic），dbVisitor 实现了 `Statement.RETURN_GENERATED_KEYS` 特性。
这意味着在使用 `JdbcTemplate` 或 `Statement` 执行插入操作时，可以自动获取生成的 `_id`。
:::

### 非关系型数据源指南

- **[Redis](./redis)** — 支持 [140+ 命令](../../drivers/redis/commands)，5 种数据类型操作；不支持构造器 API 和对象映射
- **[MongoDB](./mongo)** — 完整 CRUD 支持，ObjectId 自动映射，分页查询；不支持批量和存储过程
- **[ElasticSearch](./elastic)** — 完整 CRUD 支持，基于 REST DSL；不支持批量和存储过程
- **[Milvus](./milvus)** — SQL 风格语法操作向量数据库，完整 CRUD 支持，KNN 近邻搜索与范围搜索；不支持批量和存储过程

---

### 写入策略详解 {#insert-strategy}

对于支持写入冲突策略的数据库，各方言采用了不同的底层实现方式：

| 方言         | Ignore 实现                                    | Update 实现                                              |
|------------|----------------------------------------------|---------------------------------------------------------|
| MySQL      | `INSERT IGNORE INTO ...`                     | `INSERT INTO ... ON DUPLICATE KEY UPDATE`               |
| PostgreSQL | `INSERT INTO ... ON CONFLICT DO NOTHING`     | `INSERT INTO ... ON CONFLICT(pk) DO UPDATE SET ...`     |
| Oracle     | `MERGE INTO ... WHEN NOT MATCHED THEN INSERT` | `MERGE INTO ... WHEN MATCHED THEN UPDATE WHEN NOT MATCHED THEN INSERT` |
| 达梦         | `INSERT /*+ IGNORE_ROW_ON_DUPKEY_INDEX */ INTO ...` | 不支持                                                |

### 向量查询支持详解 {#vector}

不同数据库的向量方言支持的距离度量函数和查询方式有所不同：

| 方言         | 查询方式                | 支持的距离度量                                         |
|------------|---------------------|------------------------------------------------|
| PostgreSQL | pgvector 运算符        | L2（`<->`）、余弦（`<=>`）、内积（`<#>`）等                  |
| Elastic 7  | script_score 脚本     | l2norm、cosineSimilarity、dotProduct、l1norm       |
| Elastic 8  | 原生 kNN + script 查询  | L2、COSINE、IP（原生）；l2norm 等（script 回退）             |
| Milvus     | 原生向量运算符             | L2（`<->`）、余弦（`<=>`）、内积（`<#>`）等                  |

## 自定义方言 {#custom-dialect}

如果内置方言不满足需求，可以通过继承 `AbstractDialect` 并实现所需接口来自定义方言。和方言相关的接口共 5 个，`SqlDialect` 是公共基础接口：

| 接口 | 职责 |
|------|------|
| `SqlDialect` | 基础接口，管理关键词清单、生成表名/列名/排序列名 |
| `ConditionSqlDialect` | 条件相关的 SQL 生成（如 LIKE 语句） |
| `InsertSqlDialect` | 高级 INSERT 语句生成（如 [写入冲突策略](../../core/lambda/insert#conflict)） |
| `PageSqlDialect` | 分页语句生成（`countSql` + `pageSql`） |
| `SeqSqlDialect` | 序列查询语句生成 |

:::info[提示]
继承 `AbstractDialect` 抽象类并实现 `PageSqlDialect` 接口即可自定义分页方言。
- `countSql` — 生成计算 count 的 SQL 语句
- `pageSql` — 生成分页 SQL 语句
:::

```java title='注册自定义方言'
SqlDialectRegister.registerDialectAlias(JdbcUtils.MYSQL, MyDialect.class);
```
