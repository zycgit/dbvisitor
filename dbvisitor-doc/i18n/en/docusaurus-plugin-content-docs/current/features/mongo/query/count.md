---
id: count
sidebar_position: 2
title: Count and Distinct
---

Counting calls SDK `countDocuments`; distinct values use `distinct`.

```text
test.user_info.count({age: {$gte: 18}})
test.user_info.distinct('name', {age: {$gte: 18}})
```

The command is `count(...)`, not the SDK method name `countDocuments(...)`. Read counts through query methods, not `executeUpdate()`. Distinct values form a multi-row result. See [Pagination](../dbvisitor/pagination.mdx) for total counts.
