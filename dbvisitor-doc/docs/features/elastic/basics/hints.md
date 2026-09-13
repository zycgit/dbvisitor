---
id: hints
sidebar_position: 6
title: Hint 支持
---

Hint 必须位于命令开头，格式为 `/*+ name=value */`。

| Hint | 说明 | 示例 |
| --- | --- | --- |
| `overwrite_find_limit` | 覆盖搜索请求的 `size`。 | `/*+ overwrite_find_limit=10 */ POST /idx/_search` |
| `overwrite_find_skip` | 覆盖搜索请求的 `from`。 | `/*+ overwrite_find_skip=20 */ POST /idx/_search` |
| `overwrite_find_as_count` | 将 `/_search` 转换为 `/_count`。 | `/*+ overwrite_find_as_count */ POST /idx/_search` |
