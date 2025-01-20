---
id: for_mapper
sidebar_position: 2
hide_table_of_contents: true
title: 8.2 RowMapper
description: dbVisitor ORM 单表模式是围绕 LambdaTemplate 工具类展开，它继承自 JdbcTemplate 具备后者的所有能力。
---

# RowMapper

`RowMapper` 是在 `ResultSet` 读取每一行时进行映射操作。使用 RowMapper 的好处是无需关心 ResultSet 的处理过程，只需要将编程工作专注在每一行数据的处理上。

```java
String queryString = "select id,name from users;";
RowMapper rowMapper = new RowMapper<User>() {
    public User mapRow(ResultSet rs,int rowNum)throws SQLException{
        User user =new User();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        return user;
    }
};

List<User> mapList = jdbcTemplate.queryForList(queryString, rowMapper);
```

在 dbVisitor 中一共内置了三种 RowMapper，除此之外用户可以自己所以扩展。

- [ColumnMapRowMapper 将行转换为 Map](#map)
- [MappingRowMapper 基于 `对象映射` 处理行数据](#mapping)
- [SingleColumnRowMapper 查询只有一列的行](#value)

## ColumnMapRowMapper {#map}

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

## MappingRowMapper {#mapping}

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

## SingleColumnRowMapper {#value}

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




[BeanMappingRowMapper.java](..%2F..%2F..%2F..%2Fdbvisitor%2Fsrc%2Fmain%2Fjava%2Fnet%2Fhasor%2Fdbvisitor%2Fjdbc%2Fmapper%2FBeanMappingRowMapper.java)
[ColumnMapRowMapper.java](..%2F..%2F..%2F..%2Fdbvisitor%2Fsrc%2Fmain%2Fjava%2Fnet%2Fhasor%2Fdbvisitor%2Fjdbc%2Fmapper%2FColumnMapRowMapper.java)
[MapMappingRowMapper.java](..%2F..%2F..%2F..%2Fdbvisitor%2Fsrc%2Fmain%2Fjava%2Fnet%2Fhasor%2Fdbvisitor%2Fjdbc%2Fmapper%2FMapMappingRowMapper.java)
[SingleColumnRowMapper.java](..%2F..%2F..%2F..%2Fdbvisitor%2Fsrc%2Fmain%2Fjava%2Fnet%2Fhasor%2Fdbvisitor%2Fjdbc%2Fmapper%2FSingleColumnRowMapper.java)