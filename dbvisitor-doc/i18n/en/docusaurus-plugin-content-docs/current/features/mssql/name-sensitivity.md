---
id: name-sensitivity
sidebar_position: 61
title: Name Sensitivity
---

## Table and Column Names {#database-names}

SQL Server name casing depends on the collation. In a case-insensitive database, `user_info` and `User_Info` cannot identify two different tables. Use distinct names when separate tables are needed.

`@Table(useDelimited = true)` makes the Builder API quote table and column names, such as `[user_info]`. It does not change the case rules.

## Result Column Case {#result-column-case}

`@Table(caseInsensitive = false)` controls how returned column names match entity properties. It does not change how the database resolves table or field names. See [Name Sensitivity](../../guides/core/mapping/name_sensitivity.md#result-column-case) for configuration examples.
