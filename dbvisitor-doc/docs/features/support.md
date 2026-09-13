---
id: support
sidebar_position: 1
hide_table_of_contents: true
title: 数据源支持矩阵
description: 对比各数据源的内置方言与 JDBC 适配器能力。
---

# 数据源支持矩阵

下表对比 dbVisitor 的集成能力，不代表数据库原生功能清单。点击数据源名称可查看具体用法、执行行为与使用限制。

## 内置方言能力 {#dialect}

✅ 提供对应方言能力 · ❌ 未提供对应方言能力 · — 无额外写入冲突策略 · ¹ 需要映射主键列。

| 列名 | 含义 |
| --- | --- |
| 配置 Key | 内置方言别名 |
| 构造器 API | 提供 LambdaTemplate / BaseMapper 内置方言，具体方法仍有使用限制 |
| 分页 | 生成分页 SQL 或原生命令选项，不表示游标遍历或快照隔离 |
| 写入冲突 | 额外提供的 Ignore / Update 策略，匹配条件和写入语义依数据源而定 |
| 序列 | 通过 SeqSqlDialect 获取序列值 |
| 向量 | 通过 VectorSqlDialect 生成向量排序与范围条件 |
| 空值排序 | 转换 OrderNullsStrategy |

| 数据源 | 配置 Key | 构造器 API | 分页 | 写入冲突 | 序列 | 向量 | 空值排序 |
|-------|--------|:--------:|:--:|------|:--:|:--:|:----:|
| [MySQL](mysql/about.md) | mysql  | ✅ | ✅ | Ignore Update | ❌ | ❌ | ✅ |
| MariaDB | mariadb | ✅ | ✅ | Ignore Update | ❌ | ❌ | ✅ |
| [PostgreSQL](postgresql/about.md) | postgresql | ✅ | ✅ | Ignore Update¹ | ✅ | ✅ | ❌ |
| 人大金仓 | kingbase | ✅ | ✅ | Ignore Update¹ | ✅ | ✅ | ❌ |
| [Oracle](oracle/about.md) | oracle | ✅ | ✅ | Ignore¹ Update¹ | ✅ | ❌ | ❌ |
| [达梦](dm/about.md) | dm     | ✅ | ✅ | Ignore¹ Update¹ | ✅ | ❌ | ❌ |
| [SQL Server](mssql/about.md) | sqlserver | ✅ | ✅ | Ignore¹ Update¹ | ✅ | ❌ | ❌ |
| SQL Server (jTDS) | jtds   | ✅ | ✅ | Ignore¹ Update¹ | ✅ | ❌ | ❌ |
| [DB2](db2/about.md) | db2    | ✅ | ✅ | Ignore¹ Update¹ | ✅ | ❌ | ❌ |
| [H2](h2/about.md) | h2     | ✅ | ✅ | Ignore¹ Update¹ | ✅ | ❌ | ❌ |
| Apache Derby | derby  | ✅ | ✅ | — | ❌ | ❌ | ❌ |
| HSQL | hsql   | ✅ | ✅ | — | ❌ | ❌ | ❌ |
| Hive | hive   | ✅ | ❌ | — | ❌ | ❌ | ❌ |
| Apache Impala | impala | ✅ | ✅ | — | ❌ | ❌ | ❌ |
| IBM Informix | informix | ✅ | ✅ | — | ❌ | ❌ | ❌ |
| SQLite | sqlite | ✅ | ✅ | — | ❌ | ❌ | ❌ |
| 虚谷数据库 | xugu   | ✅ | ✅ | — | ❌ | ❌ | ❌ |
| [ClickHouse](clickhouse/about.md) | clickhouse | ✅ | ✅ | — | ❌ | ❌ | ❌ |
| [Redis](redis/about.md) | —      | ❌ | ❌ | — | ❌ | ❌ | ❌ |
| [MongoDB](mongo/about.md) | mongo  | ✅ | ✅ | — | ❌ | ❌ | ❌ |
| [ElasticSearch 6](elastic/about.md) | elastic6 | ✅ | ✅ | — | ❌ | ❌ | ❌ |
| [ElasticSearch 7](elastic/about.md) | elastic7 | ✅ | ✅ | — | ❌ | ✅ | ❌ |
| [ElasticSearch 8](elastic/about.md) | elastic8 | ✅ | ✅ | — | ❌ | ✅ | ❌ |
| [Milvus](milvus/about.md) | milvus | ✅ | ✅ | Update¹ | ❌ | ✅ | ❌ |

## JDBC 适配器能力

下表仅列出 dbVisitor 提供的适配器。关系型数据库的 JDBC 能力由所选厂商驱动、服务端版本及语句决定。JDBC Batch 指 addBatch / executeBatch，不等同于多语句执行或上层 API 逐条执行。

| 适配器 | JDBC Batch | 存储过程 | getGeneratedKeys | 指定列名 / 列序号的生成键重载 |
| --- | :---: | :---: | :---: | :---: |
| [jdbc-redis](redis/dbvisitor/results.mdx) | ❌ | ❌ | ❌ | ❌ |
| [jdbc-mongo](mongo/dbvisitor/results.mdx) | ❌ | ❌ | ✅¹ | ❌ |
| [jdbc-elastic](elastic/dbvisitor/results.mdx) | ❌ | ❌ | ✅¹ | ❌ |
| [jdbc-milvus](../drivers/milvus/limitations.md) | ❌ | ❌ | ✅¹ | ❌ |

¹ 需使用 Statement.RETURN_GENERATED_KEYS，且执行支持返回主键的写入命令；返回列和 Mapper 回填配置见各数据源文档。
