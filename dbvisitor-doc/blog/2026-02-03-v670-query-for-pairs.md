---
slug: v670-query-for-pairs
title: v6.7.0 解读：queryForPairs 让键值查询一步到位
authors: [ZhaoYongChun]
tags: [dbVisitor, JDBC]
---

在日常开发中，"查两列，组成 Map"是一个高频操作：ID 到名称的映射、编码到描述的映射、配置键到值的映射…… 以往你需要查出列表再手动遍历构建 Map。

dbVisitor 6.7.0 新增的 `queryForPairs` 方法，一行代码直接拿到 `Map<K, V>`。

<!--truncate-->

## 痛点：重复的样板代码

之前的写法：

```java
// 查出 ID → Name 映射
List<UserInfo> users = lambda.query(UserInfo.class)
      .between(UserInfo::getId, 1001, 1003)
      .queryForList();

Map<Integer, String> idToName = new LinkedHashMap<>();
for (UserInfo u : users) {
    idToName.put(u.getId(), u.getName());
}
```

先查列表，再遍历填充。如果只需要两列数据，却要查出整个实体，浪费资源。

## 新写法：一行搞定

### Fluent API — Lambda 方式

```java
Map<Integer, String> idToName = lambda.query(UserInfo.class)
      .between(UserInfo::getId, 1001, 1003)
      .queryForPairs(UserInfo::getId, UserInfo::getName, Integer.class, String.class);

// {1001="Alice", 1002="Bob", 1003="Charlie"}
```

框架自动将 SELECT 收窄为指定的两列，然后将第一列作为 Key、第二列作为 Value 构建 `Map`。

### Fluent API — 字符串方式

```java
Map<Integer, String> idToName = lambda.query(UserInfo.class)
      .between("id", 1001, 1003)
      .queryForPairs("id", "name", Integer.class, String.class);
```

### JdbcTemplate — 原生 SQL

`queryForPairs` 同时下沉到了 `JdbcTemplate` 层，支持多种参数传递方式：

```java
// 1. 无参数
Map<Integer, String> result = jdbcTemplate.queryForPairs(
    "SELECT id, name FROM user_info WHERE id BETWEEN 1001 AND 1003",
    Integer.class, String.class
);

// 2. 位置参数
Map<String, Integer> nameToAge = jdbcTemplate.queryForPairs(
    "SELECT name, age FROM user_info WHERE id >= ? AND id <= ?",
    String.class, Integer.class,
    new Object[] { 1001, 1003 }
);

// 3. 命名参数
Map<String, Object> params = new HashMap<>();
params.put("minId", 1001);
params.put("maxId", 1003);

Map<Integer, String> idToEmail = jdbcTemplate.queryForPairs(
    "SELECT id, email FROM user_info WHERE id >= :minId AND id <= :maxId",
    Integer.class, String.class,
    params
);
```

## 设计细节

### 自动类型转换

`queryForPairs` 内部使用 `PairsResultSetExtractor`，根据指定的 Key/Value 类型自动选择对应的 `TypeHandler` 进行类型转换：

```java
// Long Key + Date Value — 自动处理类型转换
Map<Long, Date> idToDate = jdbcTemplate.queryForPairs(
    "SELECT CAST(id AS BIGINT), create_time FROM user_info",
    Long.class, Date.class
);
```

### 单列场景

如果 SELECT 只有一列，Value 自动为 `null`：

```java
Map<Integer, Object> ids = jdbcTemplate.queryForPairs(
    "SELECT id FROM user_info",
    Integer.class, Object.class
);
// {1001=null, 1002=null, 1003=null}
```

### Key 冲突

当多行的 Key 值相同时，后出现的值会覆盖先出现的，这与 Java 的 `Map.put()` 行为一致。

## 适用场景

- **下拉选项加载**：`SELECT code, label FROM dict_item WHERE type = ?`
- **批量 ID 转名称**：`SELECT id, name FROM user WHERE id IN (...)`
- **配置读取**：`SELECT key, value FROM app_config WHERE group = ?`
- **关联预查询**：替代 N+1 查询中的关联表预加载
