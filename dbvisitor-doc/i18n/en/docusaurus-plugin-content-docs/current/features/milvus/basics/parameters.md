---
id: parameters
sidebar_position: 3
title: Literals and Parameters
---

## Literals and Parameters

| Value | Syntax and boundaries |
| --- | --- |
| String | Single or double quotes, such as `'book'` or `"book"`. Double an embedded matching quote, as in `'reader''s book'`. Prefer bound parameters for complex text. |
| Number | Integers, decimals and scientific notation, such as `10`, `0.25` and `1e3`. Bind negative numbers in general value positions; numeric DEFAULT separately accepts either sign. |
| Boolean | `TRUE` or `FALSE`; the string `'true'` is not a boolean. |
| Null | `NULL`; acceptance depends on field constraints, the value position and the server API. |
| List | Bracketed values such as `[1, 2]`. Element types, nesting and empty-list acceptance depend on the context. |
| Parameter | `?`; PreparedStatement indexes start at 1 and follow occurrence order across the entire SQL input. |

WHERE parameters are passed separately from filter expressions through SDK `filterTemplateValues`. Vectors, write rows and command options populate their corresponding request fields. Do not quote or pre-escape bound values. `'?'` is a string, not a parameter; a string parameter cannot substitute an entire SQL condition.

```sql
SELECT id, title FROM books WHERE id IN (?, ?) LIMIT ?;
UPDATE books SET title = ? WHERE id = ?;
```

The example has five parameters: two query keys, the query limit, the new title and the update key. Finishing the first statement does not reset parameter indexes. Each command reference identifies its bindable options and accepted types.
