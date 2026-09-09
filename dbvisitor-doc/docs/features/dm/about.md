---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: 达梦
description: 达梦数据库在 dbVisitor 中的方言能力、主键回填、IDENTITY、序列和冲突策略。
---

# 达梦

达梦数据库可以使用 dbVisitor 的 JDBC、Mapper、Lambda、BaseMapper、事务和常规对象映射能力。

## 快速了解差异

| 关注点 | 达梦行为 |
|--------|---------|
| 主键生成 | 自增列，通过 JDBC generated keys 回填 |
| 分页 | `LIMIT ?` / `LIMIT offset, count` |
| 写入冲突 | `IGNORE_ROW_ON_DUPKEY_INDEX`（忽略）/ `MERGE`（更新） |
| 批量写入 | 支持 JDBC batch（无回填时） |
| 存储过程 | 支持 |
| 序列 | 支持 `SELECT seq.NEXTVAL` |

## 主键回填

自增列通过 JDBC generated keys 回填。无回填需求时优先使用 JDBC batch；有回填需求时保守逐条执行。

## 写入冲突策略

达梦的冲突策略实现比较特殊：

| 策略 | 达梦实现 |
|------|---------|
| Ignore | `INSERT /*+ IGNORE_ROW_ON_DUPKEY_INDEX */ INTO ...`（需主键） |
| Update | `MERGE INTO ... WHEN MATCHED THEN UPDATE ... WHEN NOT MATCHED THEN INSERT ...`（需主键且至少一个非主键列可更新） |

## 序列

达梦方言实现了序列支持，可使用 `SELECT seq.NEXTVAL`。对象映射中通过 `KeyType.Sequence` + `@KeySeq` 使用。

## 专题

- [自增主键回填](./generated-keys)：自增列、sequence、`keyColumn`、`selectKey` 和冲突策略边界。
- [方言细节](./dialect-details)：IGNORE_ROW_ON_DUPKEY_INDEX HINT、MERGE 语法、序列的底层实现。

## 与通用文档的关系

通用 API 用法见 [核心API](../../guides/overview)。以下内容补充达梦下的具体差异和推荐写法。
