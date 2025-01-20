---
id: for_extractor
sidebar_position: 4
hide_table_of_contents: true
title: 8.4 ResultSetExtractor
description: dbVisitor ORM 单表模式是围绕 LambdaTemplate 工具类展开，它继承自 JdbcTemplate 具备后者的所有能力。
---

# ResultSetExtractor

使用 ResultSetExtractor 接口，自定义 `java.sql.ResultSet` 结果集的处理。

```java title='1. 查询数据库返回 ID 和 Name 字段'
Map<Integer, String> result = jdbc.query("select id, name from users", extractor);
```

```java title='2. 接收查询结果并将 ID 和 Name 转换为 Map'
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
```

```text title='3. 执行结果 result 为'
{1=mali, 2=dative, 3=jon wes, 4=mary, 5=matt}
```



[BeanMappingResultSetExtractor.java](..%2F..%2F..%2F..%2Fdbvisitor%2Fsrc%2Fmain%2Fjava%2Fnet%2Fhasor%2Fdbvisitor%2Fjdbc%2Fextractor%2FBeanMappingResultSetExtractor.java)
[CallableMultipleResultSetExtractor.java](..%2F..%2F..%2F..%2Fdbvisitor%2Fsrc%2Fmain%2Fjava%2Fnet%2Fhasor%2Fdbvisitor%2Fjdbc%2Fextractor%2FCallableMultipleResultSetExtractor.java)
[ColumnMapResultSetExtractor.java](..%2F..%2F..%2F..%2Fdbvisitor%2Fsrc%2Fmain%2Fjava%2Fnet%2Fhasor%2Fdbvisitor%2Fjdbc%2Fextractor%2FColumnMapResultSetExtractor.java)
[FilterResultSetExtractor.java](..%2F..%2F..%2F..%2Fdbvisitor%2Fsrc%2Fmain%2Fjava%2Fnet%2Fhasor%2Fdbvisitor%2Fjdbc%2Fextractor%2FFilterResultSetExtractor.java)
[MapMappingResultSetExtractor.java](..%2F..%2F..%2F..%2Fdbvisitor%2Fsrc%2Fmain%2Fjava%2Fnet%2Fhasor%2Fdbvisitor%2Fjdbc%2Fextractor%2FMapMappingResultSetExtractor.java)
[PreparedMultipleResultSetExtractor.java](..%2F..%2F..%2F..%2Fdbvisitor%2Fsrc%2Fmain%2Fjava%2Fnet%2Fhasor%2Fdbvisitor%2Fjdbc%2Fextractor%2FPreparedMultipleResultSetExtractor.java)
[RowCallbackHandlerResultSetExtractor.java](..%2F..%2F..%2F..%2Fdbvisitor%2Fsrc%2Fmain%2Fjava%2Fnet%2Fhasor%2Fdbvisitor%2Fjdbc%2Fextractor%2FRowCallbackHandlerResultSetExtractor.java)
[RowMapperResultSetExtractor.java](..%2F..%2F..%2F..%2Fdbvisitor%2Fsrc%2Fmain%2Fjava%2Fnet%2Fhasor%2Fdbvisitor%2Fjdbc%2Fextractor%2FRowMapperResultSetExtractor.java)