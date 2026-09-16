---
id: parameters
slug: /features/mongo/parameters
sidebar_position: 70
title: Parameters and Rules
---

## General Rules {#general-rules}

Parameter binding and conditional expansion run before the command reaches MongoDB. They do not require SQL syntax. In JdbcTemplate, method annotations, or Mapper files, write a native command and bind its values; see [Parameter Passing](../../../guides/args/about.md).

For example, this native command binds values instead of concatenating user input:

```text
test.user_info.count({id: #{id}, name: #{name}})
```

The `@{if, condition, command fragment}` rule can select a command or fragment. Only the selected branch binds parameters; the resulting command must be valid MongoDB syntax.

## SQL Fragment Rules {#sql-fragments}

`AND`, `OR`, `SET`, and `IN` rules generate SQL fragments; they do not translate them into MongoDB commands. In particular, `@{in}` produces a parenthesized, comma-separated list of placeholders, not a native collection parameter.

Use native command syntax with bound values, and use general conditional rules to select fragments. This is a restriction on the generated syntax, not a lack of parameter binding or rule expansion.
