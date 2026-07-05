---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: Receiving Results
description: dbVisitor provides multiple ways to receive and process query results, from simple List/Map to full custom ResultSet control.
---

# Receiving Results

dbVisitor provides multiple ways to process query results. Most scenarios are handled by default mapping; for special requirements, you can customize result processing.

## Quick Reference

| Your Need | Recommended Approach | Description |
|---------|---------|------|
| Return a list of entity objects | `queryForList(User.class)` | Automatic conversion based on object mapping |
| Return a single entity object | `queryForObject(User.class)` | Use when there is only one record |
| No entity class needed, use Map | `queryForMapList()` / `queryForMap()` | Column name → Map key |
| Fetch only one column's values | `queryForList(String.class)` | Single-column result set |
| Fetch only one value | `queryForObject(Integer.class)` | COUNT results, etc. |
| Process row by row without collecting | `RowCallbackHandler` | Streaming / large data volumes |
| Full control over ResultSet | `ResultSetExtractor` | Custom aggregation logic |
| Pagination | `PageObject` + `pageBySample` | Includes total record count and total pages |

## Basics: Direct Mapping to Entity or Map

```java title='Query entity object list'
List<User> users = jdbc.queryForList("select * from users where age > ?", 18, User.class);
```

```java title='Query a single entity'
User user = jdbc.queryForObject("select * from users where id = ?", 1, User.class);
```

```java title='Use Map when no entity class is needed'
Map<String, Object> row = jdbc.queryForMap("select * from users where id = ?", 1);
List<Map<String, Object>> rows = jdbc.queryForList("select * from users");
```

## Fetch a Single Column or Single Value

```java title='Fetch all values of a single column'
List<String> names = jdbc.queryForList("select name from users", String.class);
```

```java title='Fetch a single value (e.g., COUNT)'
int count = jdbc.queryForObject("select count(*) from users", Integer.class);
```

## Row-by-Row Processing (for Large Data Volumes)

Use `RowCallbackHandler` to process rows one by one without keeping results in memory:

```java
jdbc.query("select * from users", (RowCallbackHandler) (rs, rowNum) -> {
    String name = rs.getString("name");
    processRow(name); // Processed and discarded immediately
});
```

## Fully Custom ResultSet Processing

```java
ResultSetExtractor<Map<Integer, String>> extractor = rs -> {
    Map<Integer, String> result = new HashMap<>();
    while (rs.next()) {
        result.put(rs.getInt("id"), rs.getString("name"));
    }
    return result;
};

Map<Integer, String> idNames = jdbc.query("select * from users", extractor);
```

## Result Processing in the Fluent API

LambdaTemplate supports the same result processing approaches:

```java
// Entity list
List<User> users = lambda.query(User.class).eq(User::getAge, 18).queryForList();

// Map list
List<Map<String, Object>> rows = lambda.query(User.class).eq(User::getAge, 18).queryForMapList();

// Custom RowMapper
RowMapper<UserVO> rowMapper = new BeanMappingRowMapper<>(UserVO.class);
List<UserVO> vos = lambda.query(User.class).eq(User::getAge, 18).queryForList(rowMapper);

// ResultSetExtractor
ResultSetExtractor<Map<Integer, String>> extractor = rs -> { ... };
Map<Integer, String> result = lambda.query(User.class).query(extractor);

// RowCallbackHandler
lambda.query(User.class).query((rs, rowNum) -> { ... });
```

## Pagination Results

```java
PageObject page = new PageObject(0, 20); // Page 0, 20 records per page

// LambdaTemplate pagination
List<User> users = lambda.query(User.class)
        .gt(User::getAge, 18)
        .usePage(page)
        .queryForList();

// BaseMapper pagination (returns PageResult with statistics)
PageResult<User> result = mapper.pageBySample(sample, page);
int totalRecords = result.getTotalCount();
int totalPages = result.getTotalPage();
```

## Further Reading

- [RowMapper](./for_mapper): Row-by-row mapping, including built-in `BeanMappingRowMapper`, `ColumnMapRowMapper`, `SingleColumnRowMapper`
- [Map Structures](./for_map): Receive data directly as `Map<String, Object>` without defining entity classes
- [ResultSetExtractor](./for_extractor): Full control over ResultSet traversal and transformation
- [RowCallbackHandler](./row_callback): Row-by-row callbacks without collecting results, suitable for streaming scenarios
- [Pagination Object](./page_object): Creating, paging, and statistics of `PageObject`
