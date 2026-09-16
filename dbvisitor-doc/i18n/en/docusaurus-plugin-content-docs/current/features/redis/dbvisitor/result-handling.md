---
id: result-handling
slug: /features/redis/result-handling
sidebar_position: 91
title: Result Handling
---

Once a Redis command returns a result set, dbVisitor can process it with all three result-handling interfaces. Availability depends on the API entry point.

## API Support {#api-support}

| API entry point | RowMapper | RowCallbackHandler | ResultSetExtractor |
| --- | --- | --- | --- |
| Programmatic API | <span className="support-status support-status--yes">Supported</span> | <span className="support-status support-status--yes">Supported</span> | <span className="support-status support-status--yes">Supported</span> |
| Mapper API (method annotations) | <span className="support-status support-status--yes">Supported</span> | <span className="support-status support-status--yes">Supported</span> | <span className="support-status support-status--yes">Supported</span> |
| Builder API | <span className="support-status support-status--no">Unsupported</span> | <span className="support-status support-status--no">Unsupported</span> | <span className="support-status support-status--no">Unsupported</span> |
| Mapper files | <span className="support-status support-status--yes">Supported</span> | <span className="support-status support-status--yes">Supported</span> | <span className="support-status support-status--yes">Supported</span> |

The builder API does not translate query conditions into Redis commands, so it cannot initiate these queries. This is not a limitation of the three handlers; use native commands instead.

## Programmatic API

For example, `ZRANGE scores 0 -1 WITHSCORES` returns the `ELEMENT` and `SCORE` columns.

```java
// RowMapper: map each row to a string.
List<String> members = jdbcTemplate.queryForList(
        "ZRANGE ? 0 -1 WITHSCORES", new Object[] { "scores" },
        (rs, rowNum) -> rs.getString("ELEMENT"));
```

```java
// RowCallbackHandler: process each row without collecting a return list.
jdbcTemplate.query("ZRANGE ? 0 -1 WITHSCORES", new Object[] { "scores" },
        (rs, rowNum) -> System.out.println(rs.getString("ELEMENT")));

// ResultSetExtractor: collect members and scores from the entire result set.
Map<String, Double> scores = jdbcTemplate.query(
        "ZRANGE ? 0 -1 WITHSCORES", new Object[] { "scores" }, rs -> {
            Map<String, Double> result = new LinkedHashMap<>();
            while (rs.next()) {
                result.put(rs.getString("ELEMENT"), rs.getDouble("SCORE"));
            }
            return result;
        });
```

See [Commands and Result Sets](./results.mdx) for column names.

## Mapper API and Mapper Files

Use the `resultRowMapper`, `resultRowCallback`, or `resultSetExtractor` attribute of `@Query` to specify a handler class. A Mapper file's `<select>` supports the same attributes.

```xml
<select id="members"
        resultRowMapper="net.hasor.dbvisitor.jdbc.mapper.ColumnMapRowMapper">
    ZRANGE #{key} 0 -1 WITHSCORES
</select>
```

Both entry points use native Redis commands, not SQL translations. See [Result Handling](../../../guides/result/about.md) for handler configuration and implementation.
