---
id: parameters
slug: /features/redis/parameters
sidebar_position: 70
title: Parameters and Rules
---

## NULL Parameters {#null-values}

Positional parameters can bind non-null values, but Redis String and Hash values cannot store SQL NULL. Passing Java null does not store a null value. An empty string can be stored and is distinct from a missing key.

To clear a value, delete the key or Hash field and handle a missing GET/HGET result:

```java
jdbc.executeUpdate("DEL ?", "user:1001:status");
String status = jdbc.queryForObject("GET ?", "user:1001:status", String.class); // null
```

Do not substitute the text `"null"` unless the application explicitly uses that convention.

## Parameter Reuse {#parameter-reuse}

PreparedStatement supports rebinding and executing again. The restriction is Redis's lack of SQL NULL storage, not parameter reuse. To clear a value, use the deletion approach above.

## General Rules {#general-rules}

Parameter binding and conditional expansion run before the command reaches Redis. They do not require SQL syntax. In JdbcTemplate, method annotations, or Mapper files, write a native command and bind its values; see [Parameter Passing](../../../guides/args/about.md).

For example, this native command binds values instead of concatenating user input:

```text
ZCARD #{key}
```

The `@{if, condition, command fragment}` rule can select a command or fragment. Only the selected branch binds parameters; the resulting command must be valid Redis syntax.

## SQL Fragment Rules {#sql-fragments}

`AND`, `OR`, `SET`, and `IN` rules generate SQL fragments; they do not translate them into Redis commands. In particular, `@{in}` produces a parenthesized, comma-separated list of placeholders, not a native collection parameter.

Use native command syntax with bound values, and use general conditional rules to select fragments. This is a restriction on the generated syntax, not a lack of parameter binding or rule expansion.
