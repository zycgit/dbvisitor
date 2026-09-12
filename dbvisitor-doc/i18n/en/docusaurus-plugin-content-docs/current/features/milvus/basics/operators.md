---
id: operators
sidebar_position: 4
title: Operators
---

## Scalar Expressions

| Expression | Execution semantics |
| --- | --- |
| `=`, `==`, `!=`, `<>`, `>`, `>=`, `<`, `<=` | Server comparison; `=` becomes `==` and `<>` becomes `!=`. |
| `AND`, `OR`, `NOT`, parentheses | Combine filters while preserving the scope of NOT. Use explicit parentheses for complex logic. |
| `field BETWEEN lower AND upper` | Inclusive `>=` and `<=` comparisons; reversed bounds are not swapped. NOT BETWEEN becomes `< lower OR > upper`. |
| `field IN [...]`, `field IN (...)`, `field IN ?` | List membership; NOT IN excludes listed values. A list containing placeholders is bound as one SDK template array. |
| `field IS NULL`, `field IS NOT NULL` | Native null checks, not equivalent to `= NULL`; the field type must support null filtering. |
| `field LIKE 'prefix%'` | Pattern matching; Milvus determines the supported pattern rules. |
| Arithmetic expressions, `function(arguments)` | Forwarded in WHERE for server evaluation, not calculated in Java. Parsing does not guarantee support for a function or type combination. |

Keep spaces around arithmetic operators so a hyphen is not absorbed into an identifier. SET assigns values; `SET age = age + 1` is unsupported. SELECT projects `*` or a field list; `COUNT(*)` uses a separate count form. JOIN, AS aliases, arbitrary projection expressions, relational GROUP BY and scalar ORDER BY are unsupported.

Vector ranges are not ordinary scalar expressions. See [SELECT and Vector Search](../query/select.md) for distance operators and AND/OR/NOT restrictions. Arbitrary JSON paths and every native expression syntax are not automatically passed through.

### Milvus 2.6.2 Parameter Restrictions

Milvus 2.6.2 rejects template parameters on the right of LIKE. `LIKE ?` can be parsed and bound through the SDK but is rejected by the server. A fixed SQL pattern literal can be used; do not work around the restriction by concatenating untrusted input.

Integer template comparisons such as `NOT (age = ?)` and double NOT can trigger a QueryNode assertion in this version. Ordinary `age != ?`, literal NOT and `NOT (field IS NULL)` are not affected by that issue. The driver does not interpolate parameters or remove NOT to simulate support.
