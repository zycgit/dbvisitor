---
sidebar_position: 7
title: ResultSetExtractor
description: 使用 dbVisitor ORM 工具自定义处理整个结果集的读取。
---

# ResultSetExtractor

`ResultSetExtractor` 负责处理整个结果集通常和 `RowMapper` 配合使用，或者实现对结果集的更复杂处理。

举一个例子查询所有用户，并且构建一个用户 `ID` 和 `名字` 的 Map 映射

```java
String queryString = "select * from test_user";

ResultSetExtractor extractor = new ResultSetExtractor<Map<Integer, String>>() {
    public Map<Integer, String> extractData(ResultSet rs) throws SQLException {
        Map<Integer, String> hashMap = new HashMap<>();

        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            hashMap.put(id, name);
        }

        return hashMap;
    }
};

Map<Integer, String> result = jdbcTemplate.query(queryString, extractor);
```

```text title='执行结果为'
{1=mali, 2=dative, 3=jon wes, 4=mary, 5=matt}
```
