---
sidebar_position: 6
title: RowMapper
description: 使用 dbVisitor ORM 工具的 RowMapper 处理行数据。
---

# RowMapper

将 `ResultSet` 一行数据读取出来并且转换成对象的工作是 `RowMapper` 来负责完成。

在 dbVisitor 中一共内置了三种 RowMapper，除此之外用户可以自己所以扩展。

- `ColumnMapRowMapper` 将行转换为 Map
- `MappingRowMapper` 基于 `对象映射` 处理行数据。
- `SingleColumnRowMapper` 只有当查询结果中包含一列数据的时候才可以使用。它会利用 `TypeHandler` 机制读取出这一列数据。


## ColumnMapRowMapper

使用 `ColumnMapRowMapper`

```java
String querySql = "select * from test_user where age > 40";
RowMapper rowMapper = new ColumnMapRowMapper();
List<Map<String, Object>> result = jdbcTemplate.queryForList(querySql, rowMapper);
```

下列是简化形式

```java
List<Map<String, Object>> result = jdbcTemplate.queryForList(querySql);
```

## MappingRowMapper

使用 `MappingRowMapper`

```java
String querySql = "select * from test_user where age > 40";
RowMapper rowMapper = new MappingRowMapper<>(TestUser.class);
List<TestUser> result = jdbcTemplate.queryForList(querySql, rowMapper);
```

下列是简化形式

```java
List<TestUser> result = jdbcTemplate.queryForList(querySql, TestUser.class);
```

## SingleColumnRowMapper

使用 `SingleColumnRowMapper`

```java
String querySql = "select name from test_user where age > 40";
RowMapper rowMapper = new SingleColumnRowMapper<>(String.class);
List<String> result = jdbcTemplate.queryForList(querySql, rowMapper);
```

下列是简化形式

```java
List<String> result = jdbcTemplate.queryForList(querySql, String.class);
```

## 自定义 RowMapper

读取一行数据，并且只设置 `age` 和 `name`

```java
String queryString = "select * from test_user where age > 40";
RowMapper rowMapper = new RowMapper<TestUser>() {
    public TestUser mapRow(ResultSet rs,int rowNum)throws SQLException{
        TestUser testUser=new TestUser();
        testUser.setAge(rs.getInt("age"));
        testUser.setName(rs.getString("name"));
        return testUser;
    }
};

List<TestUser> mapList = jdbcTemplate.queryForList(queryString, rowMapper);
```
