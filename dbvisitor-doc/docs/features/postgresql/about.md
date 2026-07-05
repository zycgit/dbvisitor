---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: PostgreSQL
description: PostgreSQL 在 dbVisitor 中的方言能力、主键回填、ON CONFLICT、RETURNING、序列和类型映射差异。
---

# PostgreSQL

PostgreSQL 可以使用 dbVisitor 的 JDBC、Mapper、Lambda、BaseMapper、事务、函数和序列等全部通用能力。

## 快速了解差异

| 关注点 | PostgreSQL 行为 |
|--------|----------------|
| 主键生成 | `SERIAL`/`BIGSERIAL`/`IDENTITY`，支持 JDBC generated keys 和 `RETURNING` |
| 分页 | `LIMIT ? OFFSET ?` |
| 写入冲突 | `ON CONFLICT DO NOTHING`（忽略）/ `ON CONFLICT ... DO UPDATE`（更新） |
| 批量写入 | 支持 JDBC batch |
| 存储过程 | 支持函数和过程 |
| 序列 | 支持 `nextval()` |
| 向量搜索 | 支持 pgvector（`<->`、`<=>`、`<#>` 运算符） |

## 主键回填

PostgreSQL 支持两种主键回填方式：

1. **JDBC generated keys**：适用于 `SERIAL`/`IDENTITY` 列，和 MySQL 类似
2. **RETURNING 子句**：`INSERT ... RETURNING id`，从当前 ResultSet 读取

Lambda / BaseMapper 多行插入时，PostgreSQL 方言可生成 `VALUES (...), (...) RETURNING id`，一次返回多行主键。手写 SQL（Mapper XML/Annotation）中需要显式写出 `RETURNING` 并配置 `generatedKeySource="resultSet"`。

## 写入冲突策略

| 策略 | PostgreSQL 实现 |
|------|----------------|
| Ignore | `INSERT INTO ... ON CONFLICT DO NOTHING` |
| Update | `INSERT INTO ... ON CONFLICT(pk) DO UPDATE SET ...` |

`ON CONFLICT DO UPDATE` 只更新非冲突键列，避免把主键列也写入 `SET`。

## 序列

PostgreSQL 序列可通过 `KeyType.Sequence` 在对象映射中使用：

```java
@KeySeq("user_info_id_seq")
@Column(value = "id", primary = true, keyType = KeyType.Sequence)
private Integer id;
```

也可在 Mapper XML 中用 `selectKey` 读取 `nextval('seq')`。

## 向量搜索

如果安装了 pgvector 扩展，dbVisitor 的构造器 API 支持向量相似性查询。详见 [向量查询](../../guides/core/vector_query/about)。

## 专题

- [自增主键回填](./generated-keys)：`SERIAL`、`RETURNING`、`ON CONFLICT`、`selectKey` 的详细配置。
- [方言细节](./dialect-details)：分页 SQL、RETURNING 多行、冲突策略、序列、向量搜索的底层实现。

## 与通用文档的关系

通用 API 用法见 [核心API](../../guides/overview)。以下内容补充 PostgreSQL 下的具体差异和推荐写法。
