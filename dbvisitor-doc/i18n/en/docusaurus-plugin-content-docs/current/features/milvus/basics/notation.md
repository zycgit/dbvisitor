---
id: notation
slug: /features/milvus/sql/language
sidebar_position: 1
title: Notation and Comments
---

## Notation

Syntax templates appear in `text` blocks. Uppercase words are keywords; lowercase names represent objects or values to substitute. `[ ... ]` denotes an optional part, `{ A | B }` means choose one alternative, and `...` repeats the preceding item using the separator shown. These template markers are not SQL.

`sql` blocks contain syntax examples. Prepare the referenced collections, fields, indexes and data, and bind any `?` parameters before execution. Brackets in SQL vector values such as `[0.1, 0.2]` are actual syntax, not optional-part markers.


## Comments and Multiple Statements

`--` starts a line comment. `/*+ ... */` is a command Hint, not an ordinary comment; place it before the statement it affects.

Separate multiple statements in one execution with semicolons and bind their parameters consecutively. Read results using `execute()` and `getMoreResults()`; see [JDBC Results and Columns](../../../drivers/milvus/results.md). Multiple statements are neither JDBC batch nor a transaction. Failure does not roll back earlier effects.

<span id="hint" />
