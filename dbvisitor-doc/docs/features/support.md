---
id: support
sidebar_position: 1
hide_table_of_contents: true
title: 数据源支持矩阵
description: 介绍关系型和非关系型数据库在 API、方言和专有能力上的支持差异。
---

# 数据源支持矩阵

dbVisitor 力争使用统一的 API 来操作所有 **关系型数据库** 和 **非关系型数据库**。但实际中数据源由于其本身的特性仍存在一些个体差异。
dbVisitor 对待差异的处理方式主要体现在两个方面：
- **API 支持性**，是指被调用的 API 在某数据源上是否支持。
- **数据库方言**，是指在使用 [构造器 API](../guides/api/lambda) 时相同 API 操不同数据库时采用了不同的命令或语法。

:::tip[特点]
您若想改变这些差异，可以参与项目并贡献您的改进代码。
:::

## 数据源支持一览 {#dialect}

dbVisitor 在统一内核架构下设计了 4 种 API：[编程式 API](../guides/api/jdbc)、[声明式 API](../guides/api/mapper)、[构造器 API](../guides/api/lambda)、[Mapper File](../guides/api/file_mapper)。
其中 **JdbcTemplate**、**注解方式**、**Mapper File** 可以执行各数据源支持的命令；它们不会把任意关系型 SQL 转换成非关系型数据库命令。

dbVisitor 具备智能的方言推断能力，会自动根据 JDBC URL 识别目标数据库类型并配置最佳方言，通常 **无需手动配置**。
如需显式指定，支持 **方言别名**（如 `mysql`）或 **方言全限定类名**。

下表汇总了各数据源的 API 支持与方言特性差异，各列含义：

- **构造器 API** — LambdaTemplate / BaseMapper 是否有内置方言；具体操作还受该方言支持范围限制。对象映射和结果集映射是独立能力，并不要求构造器方言。
- **写入冲突** — 所有数据源均支持标准写入（Into），此列仅标注额外支持的冲突策略（[详解](#insert-strategy)）
- **分页** — 内置方言是否提供分页 SQL；Hive 的接口仅为占位，不可用
- **序列** — 方言是否实现 `SeqSqlDialect` 接口
- **向量** — 方言是否实现 `VectorSqlDialect` 接口（[详解](#vector)）
- **空值排序** — 构造器是否提供 `OrderNullsStrategy` 的方言转换，不等同于数据库本身是否支持空值排序

| 数据源 | 配置 Key | 构造器 API | 分页 | 写入冲突 | 序列 | 向量 | 空值排序 |
|-------|--------|:--------:|:--:|------|:--:|:--:|:----:|
| MySQL | mysql  | ✅ | ✅ | Ignore Update | | | ✅ |
| MariaDB | mariadb | ✅ | ✅ | Ignore Update | | | ✅ |
| PostgreSQL | postgresql | ✅ | ✅ | Ignore Update¹ | ✅ | ✅ | |
| 人大金仓 | kingbase | ✅ | ✅ | Ignore Update¹ | ✅ | ✅ | |
| Oracle | oracle | ✅ | ✅ | Ignore¹ Update¹ | | | |
| 达梦 | dm     | ✅ | ✅ | Ignore¹ Update¹ | ✅ | | |
| SQL Server | sqlserver | ✅ | ✅ | Ignore¹ Update¹ | | | |
| SQL Server (jTDS) | jtds   | ✅ | ✅ | Ignore¹ Update¹ | | | |
| DB2 | db2    | ✅ | ✅ | Ignore¹ Update¹ | ✅ | | |
| H2 | h2     | ✅ | ✅ | Ignore¹ Update¹ | ✅ | | |
| Apache Derby | derby  | ✅ | ✅ | | | | |
| HSQL | hsql   | ✅ | ✅ | | | | |
| Hive | hive   | ✅ | ⚠️ | | | | |
| Apache Impala | impala | ✅ | ✅ | | | | |
| IBM Informix | informix | ✅ | ✅ | | | | |
| SQLite | sqlite | ✅ | ✅ | | | | |
| 虚谷数据库 | xugu   | ✅ | ✅ | | | | |
| ClickHouse | clickhouse | ✅ | ✅ | | | | |
| Redis | —      | ❌ | | | | | |
| MongoDB | mongo  | ✅ | ✅ | | | | |
| ElasticSearch 6 | elastic6 | ✅ | ✅ | | | | |
| ElasticSearch 7 | elastic7 | ✅ | ✅ | | | ✅ | |
| ElasticSearch 8 | elastic8 | ✅ | ✅ | | | ✅ | |
| Milvus | milvus | ✅ | ✅ | Update¹ | | ✅ | |

> ✅ 支持 &nbsp; ❌ 不支持
>
> ¹ 需有主键
> Milvus 的 Update 要求写入列中包含唯一主键，使用 partial UPSERT，不代表具备事务保证。
>
> **⚠️ Hive**：虽然实现了 `PageSqlDialect`，但 `countSql` 和 `pageSql` 均会抛出 `UnsupportedOperationException`，实际不可用。

JDBC Batch 与多语句执行是不同能力：多语句通过一次 `Statement.execute("SQL1; SQL2")` 提交，通过 `getMoreResults()` 切换结果，并用 `getResultSet()` / `getUpdateCount()` 读取结果集或更新计数，不代表支持 JDBC Batch。单条 INSERT 的多个 VALUES、分页写入和 Import 也不属于 JDBC Batch。

### JDBC 能力

关系型数据库的 JDBC Batch、存储过程和 generated keys 能力，由所选 JDBC 驱动、服务端版本及语句决定，不由方言接口的存在决定。例如 SQLite 不支持存储过程；Oracle 方言逐条插入，不代表 Oracle JDBC 不支持 Batch。

| dbVisitor 适配器 | JDBC Batch | 存储过程 | getGeneratedKeys |
| --- | --- | --- | --- |
| jdbc-redis | 不支持 | 不支持 | 空结果 |
| jdbc-mongo | 不支持 | 不支持 | 插入返回 `_id` |
| jdbc-elastic | 不支持 | 不支持 | 文档写入返回 `_id` |
| jdbc-milvus | 不支持 | 不支持 | INSERT/UPSERT 返回集合主键 |

即使上层批量 API 通过逐条调用完成操作，也不等于驱动支持 `addBatch()` / `executeBatch()`。主键回传应使用 `Statement.RETURN_GENERATED_KEYS`；这些适配器不支持指定列名或列序号数组的 JDBC 重载。

:::info[JDBC 特性支持]
对于非关系型数据库驱动（Mongo、Elastic、Milvus），dbVisitor 实现了 `Statement.RETURN_GENERATED_KEYS` 特性。
使用支持的 JDBC 插入及主键回传接口，可以读取服务端返回的主键；Milvus 的列名取集合主键字段名，不固定为 `_id`。
:::

Mapper 注解/XML 请求主键时，不要指定 `keyColumn`，以免选择适配器不支持的列名数组重载；单列键可通过 `keyProperty` 按位置回填。Lambda/BaseMapper 的 `KeyType.Auto` 实体回填会使用列名数组重载，因此不能直接套用于这些适配器；需要自动主键时，使用 JDBC 标志重载或上述 Mapper 配置。Milvus 版本要求见[版本与支持范围](./milvus/compatibility.md)。

### 非关系型数据源指南

- **[Redis](./redis/about.md)** — 支持 [140+ 命令](./redis/commands)，常用数据类型操作；不支持构造器 API，可使用 RowMapper 或 JSON TypeHandler 映射结果
- **[MongoDB](./mongo/about.md)** — 完整 CRUD 支持，ObjectId 自动映射，分页查询；不支持 JDBC Batch 和存储过程
- **[ElasticSearch](./elastic/about.md)** — 完整 CRUD 支持，基于 REST DSL；不支持 JDBC Batch 和存储过程
- **[Milvus](./milvus/about.md)** — SQL 子集、多语句执行、向量/Hybrid 搜索、分页 Partial UPDATE、主键回传、多行写入和 Import；不支持 JDBC Batch、事务及存储过程。

---

### 写入策略详解 {#insert-strategy}

对于支持写入冲突策略的数据库，各方言采用了不同的底层实现方式：

| 方言         | Ignore 实现                                    | Update 实现                                              | 主键要求 |
|------------|----------------------------------------------|---------------------------------------------------------|:------:|
| MySQL      | `INSERT IGNORE INTO ...`                     | `INSERT INTO ... ON DUPLICATE KEY UPDATE`               | 不要求 |
| PostgreSQL | `INSERT INTO ... ON CONFLICT DO NOTHING`     | `INSERT INTO ... ON CONFLICT(pk) DO UPDATE SET ...`     | Update 需要 |
| Oracle     | `MERGE INTO ... WHEN NOT MATCHED THEN INSERT` | `MERGE INTO ... WHEN MATCHED THEN UPDATE WHEN NOT MATCHED THEN INSERT` | 需要 |
| SQL Server  | `MERGE INTO ... WHEN NOT MATCHED THEN INSERT` | `MERGE INTO ... WHEN MATCHED THEN UPDATE WHEN NOT MATCHED THEN INSERT` | 需要 |
| DB2         | `MERGE INTO ... WHEN NOT MATCHED THEN INSERT` | `MERGE INTO ... WHEN MATCHED THEN UPDATE WHEN NOT MATCHED THEN INSERT` | 需要 |
| H2          | `MERGE INTO ...`                              | `MERGE INTO ...`                                      | 需要 |
| 达梦         | `INSERT /*+ IGNORE_ROW_ON_DUPKEY_INDEX */ INTO ...` | `MERGE INTO ... WHEN MATCHED THEN UPDATE WHEN NOT MATCHED THEN INSERT` | 需要 |
| ClickHouse  | 不支持 | 不支持 | — |

### 生成键回填策略 {#generated-key-strategy}

Lambda / BaseMapper 由 dbVisitor 生成 INSERT SQL，因此方言可以根据是否需要返回列、是否使用重复键策略选择执行方式。

| 方言 | 无回填列 | Into + 回填列 | Ignore + 回填列 | Update + 回填列 |
| --- | --- | --- | --- | --- |
| MySQL | `JdbcBatch` | `JdbcBatchGeneratedKeys` | `OneByOne` | `OneByOne` |
| PostgreSQL | `JdbcBatch` | `MultiValuesResultSet` | `OneByOne` | `MultiValuesResultSet` |
| SQL Server | `JdbcBatch` | `MultiValuesResultSet` | `OneByOne` | `OneByOne` |
| Oracle | `OneByOne` | `OneByOne` | `OneByOne` | `OneByOne` |
| 达梦 | `JdbcBatch` | `OneByOne` | `OneByOne` | `OneByOne` |
| DB2 | `JdbcBatch` | `OneByOne` | `OneByOne` | `OneByOne` |
| H2 | `JdbcBatch` | `OneByOne` | `OneByOne` | `OneByOne` |
| ClickHouse | `JdbcBatch` | `OneByOne` | `OneByOne` | `OneByOne` |

其中 `MultiValuesResultSet` 表示 SQL 本身返回当前 `ResultSet`，例如 PostgreSQL `RETURNING` 或 SQL Server `OUTPUT INSERTED`；`JdbcBatchGeneratedKeys` 表示使用 JDBC batch 的 generated keys；`OneByOne` 表示逐条执行以保证回填语义。

### 向量查询支持详解 {#vector}

不同数据库的向量方言支持的距离度量函数和查询方式有所不同：

| 方言         | 查询方式                | 支持的距离度量                                         |
|------------|---------------------|------------------------------------------------|
| PostgreSQL | pgvector 运算符        | L2（`<->`）、余弦（`<=>`）、内积（`<#>`）等                  |
| Elastic 7  | 脚本排序 / script filter     | l2norm、cosineSimilarity、dotProduct、l1norm       |
| Elastic 8  | 原生 kNN + script 查询  | L2、COSINE、IP（原生）；l2norm 等（script 回退）             |
| Milvus     | 原生向量运算符             | L2（`<->`）、余弦（`<=>`）、内积（`<#>`）等                  |

## 自定义方言 {#custom-dialect}

如果内置方言不满足需求，可以通过继承 `AbstractDialect` 并实现所需接口来自定义方言。下表列出主要方言接口，`SqlDialect` 是公共基础接口：

| 接口 | 职责 |
|------|------|
| `SqlDialect` | 基础接口，管理关键词清单、生成表名/列名/排序列名 |
| `ConditionSqlDialect` | 条件相关的 SQL 生成（如 LIKE 语句） |
| `InsertSqlDialect` | 高级 INSERT 语句生成（如 [写入冲突策略](../guides/core/lambda/insert#conflict)） |
| `PageSqlDialect` | 分页语句生成（`countSql` + `pageSql`） |
| `SeqSqlDialect` | 序列查询语句生成 |
| `VectorSqlDialect` | 向量排序与范围条件生成 |

:::info[提示]
继承 `AbstractDialect` 抽象类并实现 `PageSqlDialect` 接口即可自定义分页方言。
- `countSql` — 生成计算 count 的 SQL 语句
- `pageSql` — 生成分页 SQL 语句
:::

```java title='注册自定义方言'
SqlDialectRegister.registerDialectAlias(JdbcHelper.MYSQL, MyDialect.class);
```
