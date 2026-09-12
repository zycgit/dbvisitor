---
id: hints
sidebar_position: 5
title: Hint 支持
---

## 查询提示 (Hints) {#hint}

Hint 使用 `/*+ ... */` 书写，放在其作用的语句之前。

支持的 Hint：

- `overwrite_find_limit`: 强制覆盖查询的 LIMIT (TopK)。
- `overwrite_find_skip`: 强制覆盖查询的 OFFSET。
- `overwrite_find_as_count`: 按是否出现切换为 COUNT，忽略投影/排序，不按向量选择计算数量，向量范围条件会被拒绝。即使值为 false 也触发，需要关闭时删除 Hint。WHERE、向量、分页、WITH 的占位符仍需按原 SQL 位置绑定并通过校验。

普通 Query/Search 请求的一致性使用连接参数 `consistencyLevel`，`consistency_level` 不是已实现的查询 Hint。Java SDK 2.6.22 的 QueryIterator 会使用集合默认一致性；依赖分页查询“写后立即可读”时，建表应显式使用 `WITH (consistency_level='Strong')`，不能仅依赖连接参数。上述三个 overwrite Hint 仅影响 SELECT，不影响 UPDATE/DELETE；sync/timeout 见 [IMPORT](../write/import.md)与[加载和维护](../admin/load.md)。

示例：

```sql
-- 强制限制返回 5 条记录，跳过前 10 条
/*+ overwrite_find_limit=5, overwrite_find_skip=10 */
SELECT * FROM table_name WHERE status = 1;

-- 使用 Hint 获取匹配条件的记录总数 (等同于 count from ... where ...)
/*+ overwrite_find_as_count=true */
SELECT * FROM table_name WHERE age > 20;
```
