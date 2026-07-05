---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: SQL Server
description: SQL Server 在 dbVisitor 中的方言能力、主键回填、OUTPUT INSERTED 和 MERGE 冲突策略。
---

# SQL Server

SQL Server 可以使用 dbVisitor 的 JDBC、Mapper、Lambda、BaseMapper、事务、函数和过程等全部通用能力。

## 快速了解差异

| 关注点 | SQL Server 行为 |
|--------|---------------|
| 主键生成 | `IDENTITY` 或 sequence default；推荐 `OUTPUT INSERTED` 回填 |
| 分页 | `OFFSET ? ROWS FETCH NEXT ? ROWS ONLY`（2012+） |
| 写入冲突 | `MERGE INTO ... WHEN MATCHED ... WHEN NOT MATCHED ...` |
| 批量写入 | 支持 JDBC batch |
| 存储过程 | 支持 |
| 序列 | 支持 `NEXT VALUE FOR seq` |

## 主键回填

SQL Server 推荐使用 `OUTPUT INSERTED.<column>` 返回生成的主键：

```sql
INSERT INTO user_info (name, age)
OUTPUT INSERTED.id
VALUES (?, ?)
```

Lambda / BaseMapper 生成的 INSERT 会由 SQL Server 方言自动加上 `OUTPUT INSERTED`，从当前 ResultSet 回填主键。手写 SQL 需要显式写出 `OUTPUT INSERTED` 并配置 `generatedKeySource="resultSet"`。

如果不使用 `OUTPUT INSERTED`，也可以走 JDBC generated keys（和 MySQL 类似）。

## 注意事项

- `ORDER BY` 中不能出现重复列名
- `SELECT` 语句中如使用 `LIMIT` 语法需替换为 `TOP` 或 `OFFSET FETCH`
- 存储过程、函数、TVF 应使用 SQL Server 专有语法

## 写入冲突策略

| 策略 | SQL Server 实现 |
|------|---------------|
| Ignore | `MERGE INTO ... WHEN NOT MATCHED THEN INSERT`（需主键） |
| Update | `MERGE INTO ... WHEN MATCHED THEN UPDATE ... WHEN NOT MATCHED THEN INSERT`（需主键） |

## 专题

- [自增主键回填](./generated-keys)：`IDENTITY`、`OUTPUT INSERTED`、`selectKey`、XML/Annotation 配置的详细说明。
- [方言细节](./dialect-details)：ROW_NUMBER 分页、dbo schema、方括号标识符的底层实现。

## 与通用文档的关系

通用 API 用法见 [核心API](../../guides/overview)。以下内容补充 SQL Server 下的具体差异和推荐写法。
