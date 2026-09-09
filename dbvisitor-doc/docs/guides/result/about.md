---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: 接收结果
description: dbVisitor 提供了多种方式接收和处理查询结果，从简单的 List/Map 到完全自定义的 ResultSet 控制。
---

# 接收结果

dbVisitor 提供了多种方式处理查询结果。大部分场景用默认映射即可；特殊需求时可以自定义结果处理。

## 场景速查

| 你的需求 | 推荐方式 | 说明 |
|---------|---------|------|
| 返回实体对象列表 | `queryForList(User.class)` | 基于对象映射自动转换 |
| 返回单个实体对象 | `queryForObject(User.class)` | 只有一条记录时使用 |
| 不需要实体类，用 Map | JdbcTemplate 的 `queryForList(sql)` / `queryForMap(sql)` | 列名 → Map key |
| 只取一列的值 | `queryForList(String.class)` | 单列结果集 |
| 只取一个值 | `queryForObject(Integer.class)` | COUNT 结果等 |
| 逐行处理，不收集 | `RowCallbackHandler` | 流式/大数据量 |
| 完全控制 ResultSet | `ResultSetExtractor` | 自定义聚合逻辑 |
| 分页 | `PageObject` + `pageBySample` | 含总记录数和总页数 |

## 基础：直接映射到实体或 Map

```java title='查询实体对象列表'
List<User> users = jdbc.queryForList("select * from users where age > ?", 18, User.class);
```

```java title='查询单个实体'
User user = jdbc.queryForObject("select * from users where id = ?", 1, User.class);
```

```java title='不需要实体类时用 Map'
Map<String, Object> row = jdbc.queryForMap("select * from users where id = ?", 1);
List<Map<String, Object>> rows = jdbc.queryForList("select * from users");
```

## 取单列或单值

```java title='取某一列的所有值'
List<String> names = jdbc.queryForList("select name from users", String.class);
```

```java title='取单个值（如 COUNT）'
int count = jdbc.queryForObject("select count(*) from users", Integer.class);
```

## 逐行处理（适合大数据量）

通过 `RowCallbackHandler` 逐行消费，框架不额外收集结果列表。驱动仍可能缓存结果；大查询还需配置驱动的游标或 fetchSize，并避免在回调中积攒数据：

```java
jdbc.query("select * from users", (RowCallbackHandler) (rs, rowNum) -> {
    String name = rs.getString("name");
    processRow(name); // 处理完即丢弃
});
```

## 完全自定义 ResultSet 处理

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

## 构造器 API 中的结果处理

LambdaTemplate 支持同样的结果处理方式：

```java
// 实体列表
List<User> users = lambda.query(User.class).eq(User::getAge, 18).queryForList();

// Map 列表
List<Map<String, Object>> rows = lambda.query(User.class).eq(User::getAge, 18).queryForMapList();

// 自定义 RowMapper
RowMapper<UserVO> rowMapper = new BeanMappingRowMapper<>(UserVO.class);
List<UserVO> vos = lambda.query(User.class).eq(User::getAge, 18).queryForList(rowMapper);

// ResultSetExtractor
ResultSetExtractor<Map<Integer, String>> extractor = rs -> { ... };
Map<Integer, String> result = lambda.query(User.class).query(extractor);

// RowCallbackHandler
lambda.query(User.class).query((rs, rowNum) -> { ... });
```

## 分页结果

```java
PageObject page = new PageObject(0, 20); // 第 0 页，每页 20 条

// LambdaTemplate 分页
List<User> users = lambda.query(User.class)
        .gt(User::getAge, 18)
        .usePage(page)
        .queryForList();

// BaseMapper 分页（返回 PageResult 含统计信息）
PageResult<User> result = mapper.pageBySample(sample, page);
long totalRecords = result.getTotalCount();
long totalPages = result.getTotalPage();
```

## 深入阅读

- [RowMapper](./for_mapper)：逐行映射，包括内置的 `BeanMappingRowMapper`、`ColumnMapRowMapper`、`SingleColumnRowMapper`
- [Map 结构](./for_map)：不定义实体类，直接用 `Map<String, Object>` 接收数据
- [ResultSetExtractor](./for_extractor)：完全控制 ResultSet 遍历与转换
- [RowCallbackHandler](./row_callback)：逐行回调，不收集结果，适合流式场景
- [分页对象](./page_object)：`PageObject` 的创建、翻页和统计信息
