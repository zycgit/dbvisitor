---
id: escape
sidebar_position: 8
title: 6.7 Escaping Parameter Markers
description: Output literal question marks, colons, and ampersands in SQL or native commands while retaining positional and named parameter binding.
---

When `?`, `:`, or `&` belongs to the command itself rather than a parameter, prefix it with a backslash. dbVisitor removes that backslash during parsing and outputs the literal symbol without consuming a bound argument.

| Literal symbol | Command template | Java string |
| --- | --- | --- |
| `?` | `\?` | `"\\?"` |
| `:` | `\:` | `"\\:"` |
| `&` | `\&` | `"\\&"` |

## Question Marks in Native Commands

For example, `?refresh=true` in an Elasticsearch request is a URL query string, not a JDBC parameter. With positional arguments:

```java
jdbcTemplate.executeUpdate(
    "PUT /users/_doc/1\\?refresh=true {\"id\":?,\"name\":?}",
    new Object[] { 1, "Alice" });
```

The driver receives the following command. Only the two question marks inside the JSON body bind values:

```text
PUT /users/_doc/1?refresh=true {"id":?,"name":?}
```

Named arguments work the same way:

```java
jdbcTemplate.executeUpdate(
    "PUT /users/_doc/1\\?refresh=true {\"id\":#{id},\"name\":#{name}}",
    Map.of("id", 1, "name", "Alice"));
```

Escape the `&` between URL parameters as well, for example with the Java string `"\\?op_type=create\\&refresh=true"`.

## A Colon Next to a Bound Parameter

A colon immediately before `?` or `#{...}` requires neither escaping nor a space:

```text
{"id":?,"name":?}
{"id":#{id},"name":#{name}}
```

When using `:name`, separate the field colon from the parameter colon with a space:

```text
{"id": :id,"name": :name}
```

If `:name` is literal command text, write `\:name`. Likewise, write `\&name` for literal `&name`.

## Notes

- Escape symbols in the **command template**, not in argument values. Continue binding values through `?`, `:name`, or `#{...}`.
- Write `\\?` in Java strings, but `\?` in ordinary Mapper XML text. XML also requires `&amp;` for an ampersand, for example `\&amp;pretty`.
- Quoted text and SQL comments remain unchanged; these escaping rules do not apply inside them. PostgreSQL's `::` type cast needs no escaping either.
- Immediately before a parameter marker, the last backslash in an odd-length sequence escapes the marker. An even-length sequence stays unchanged and the following marker is still parsed as a parameter. Other backslashes remain unchanged.
