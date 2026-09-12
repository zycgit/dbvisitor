---
id: hints
sidebar_position: 6
title: Hint Support
---

Hints must appear at the beginning of the command text. Format: `/*+ name=value */`. Multiple hint blocks are allowed.

Supported hints:

| Hint | Description | Example |
| --- | --- | --- |
| `overwrite_find_limit` | Overrides the `limit` applied to `find` / `findOne`. | `/*+ overwrite_find_limit=10 */ db.mycol.find({})` |
| `overwrite_find_skip` | Overrides the `skip` applied to `find` / `findOne`. | `/*+ overwrite_find_skip=20 */ db.mycol.find({})` |
| `overwrite_find_as_count` | Converts `find` into a `countDocuments` result. | `/*+ overwrite_find_as_count */ db.mycol.find({})` |
