---
id: results
sidebar_position: 2
title: 命令返回结果
---

各命令页中的“更新计数”和“结果集”表示 JDBC 返回形式，不表示命令只读或只写。

| 返回形式 | dbVisitor 用法 | 示例 |
| --- | --- | --- |
| 更新计数 | `executeUpdate`，Mapper 使用写入注解或标签。 | `SET`、`DEL`、`HSET` |
| 结果集 | 查询方法，Mapper 使用 `@Query` 或 `<select>`。 | `GET`、`INCR`、`LPOP` |
| 取决于选项 | 按实际选项选择。 | `SET ... GET` 返回旧值结果集。 |

INCR 会修改数据，但新值在结果集中；更新计数也不统一等于“影响行数”，例如 LPUSH 返回写入后的列表长度。

多列和多行结果的读取见[结果读取](../dbvisitor/results.mdx)。
