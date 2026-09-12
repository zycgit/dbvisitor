---
id: hints
sidebar_position: 6
title: Hint Support
---

Hints must appear at the beginning of the command text. Format: `/*+ name=value */`.

| Hint | Description | Example |
| --- | --- | --- |
| `overwrite_find_limit` | Overrides the `size` in search requests. | `/*+ overwrite_find_limit=10 */ POST /idx/_search` |
| `overwrite_find_skip` | Overrides the `from` in search requests. | `/*+ overwrite_find_skip=20 */ POST /idx/_search` |
| `overwrite_find_as_count` | Converts `/_search` to `/_count`. | `/*+ overwrite_find_as_count */ POST /idx/_search` |
