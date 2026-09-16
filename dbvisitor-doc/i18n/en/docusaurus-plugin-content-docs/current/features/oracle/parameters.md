---
id: parameters
sidebar_position: 70
title: Parameters and Rules
---

## Empty String Parameters {#empty-strings}

Positional parameters, named parameters, and JDBC PreparedStatement can bind strings, but Oracle treats an empty `VARCHAR2` string as NULL. The binding method does not change this behavior: writing `""` does not return the same empty string when read.

```sql
CREATE TABLE text_example (id NUMBER(10) PRIMARY KEY, note VARCHAR2(100));
```

```java
jdbcTemplate.executeUpdate("INSERT INTO text_example (id, note) VALUES (?, ?)",
        new Object[] { 1, "" });
String note = jdbcTemplate.queryForObject(
        "SELECT note FROM text_example WHERE id = ?", new Object[] { 1 }, String.class);
// note is null
```

The same applies when a method annotation or Mapper file binds `""` through `#{note}`. Use `IS NULL` to find these rows. If the application must distinguish an absent value from an empty string, store the distinction in a separate status field.

## Parameter Reuse {#parameter-reuse}

PreparedStatement supports rebinding and executing again, but reuse does not change empty-string storage semantics. Use the status field described above if the application must preserve the distinction between `""` and null.


See [Where Builder](builder.md#predicates) for querying empty strings and batching large ID collections.
