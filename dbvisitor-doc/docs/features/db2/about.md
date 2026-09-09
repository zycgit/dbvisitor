---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: DB2
description: DB2 在 dbVisitor 中的方言能力、主键回填、IDENTITY、序列和 MERGE 冲突策略。
---

# DB2

DB2 可以使用 dbVisitor 的 JDBC、Mapper、Lambda、BaseMapper、事务、分页、序列和对象映射等全部通用能力。

## 快速了解差异

| 关注点 | DB2 行为 |
|--------|---------|
| 主键生成 | `IDENTITY` 列，通过 JDBC generated keys 回填 |
| 分页 | `ROWNUMBER() OVER()` + 嵌套查询 |
| 写入冲突 | `MERGE INTO ... WHEN MATCHED ... WHEN NOT MATCHED ...` |
| 批量写入 | 支持，但 batch generated keys 存在驱动限制 |
| 存储过程 | 支持 |
| 序列 | 支持 `VALUES NEXT VALUE FOR seq` |

## 主键回填

DB2 `IDENTITY` 列通过 JDBC generated keys 回填。**注意**：DB2 的 batch generated keys 存在驱动和配置限制，部分配置下不能用于 batch updates。因此 dbVisitor 在需要主键回填时会保守逐条执行。

无回填需求时可使用普通 JDBC batch。

## 写入冲突策略

| 策略 | DB2 实现 |
|------|---------|
| Ignore | `MERGE INTO ... WHEN NOT MATCHED THEN INSERT`（需主键） |
| Update | `MERGE INTO ... WHEN MATCHED THEN UPDATE ... WHEN NOT MATCHED THEN INSERT`（需主键） |

## 序列

DB2 方言实现了序列支持，可用 `VALUES NEXT VALUE FOR seq` 读取序列值。对象映射中通过 `KeyType.Sequence` + `@KeySeq` 使用。

## 专题

- [自增主键回填](./generated-keys)：`IDENTITY`、sequence、DB2 generated keys 和 batch 限制。
- [方言细节](./dialect-details)：ROWNUMBER 分页、MERGE 语法、序列、LIKE 的底层实现。

## 与通用文档的关系

通用 API 用法见 [核心API](../../guides/overview)。以下内容补充 DB2 下的具体差异和推荐写法。
