---
id: hints
sidebar_position: 6
title: Hint 支持
---

Hint 必须位于命令开头，格式为 `/*+ name=value */`，支持多个 Hint 块。

支持的 Hint：

| Hint | 说明 | 示例 |
| --- | --- | --- |
| `overwrite_find_limit` | 覆盖 `find` / `findOne` 的 `limit`。 | `/*+ overwrite_find_limit=10 */ db.mycol.find({})` |
| `overwrite_find_skip` | 覆盖 `find` / `findOne` 的 `skip`。 | `/*+ overwrite_find_skip=20 */ db.mycol.find({})` |
| `overwrite_find_as_count` | 将 `find` 转换为 `countDocuments` 结果。 | `/*+ overwrite_find_as_count */ db.mycol.find({})` |
