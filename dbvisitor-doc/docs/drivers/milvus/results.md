---
id: results
sidebar_position: 5
title: 读取结果
---

已知命令返回结果集时使用 `executeQuery()`，返回更新计数时使用 `executeUpdate()`。不确定返回类型或执行多条语句时使用 `execute()`。具体返回列见对应语句文档。

## 多语句与多结果集

以下片段使用已经打开的 `conn` 和[入门程序](execution.mdx)建立的 `books_demo`，每个结果集都是一条 SQL 的返回，不是一个 SELECT 中多查询向量的结果组：

```java
String sql = "SELECT book_id FROM books_demo LIMIT 2; " +
        "UPDATE books_demo SET word_count = ? WHERE book_id = ? LIMIT 1; " +
        "COUNT FROM books_demo";
try (PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setInt(1, 2000);
    ps.setLong(2, 1L);
    boolean hasResult = ps.execute();
    while (true) {
        if (hasResult) {
            try (ResultSet rs = ps.getResultSet()) {
                ResultSetMetaData md = rs.getMetaData();
                while (rs.next()) {
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        System.out.println(md.getColumnLabel(i) + "=" + rs.getObject(i));
                    }
                }
            }
        } else {
            long count = ps.getLargeUpdateCount();
            if (count == -1) {
                break;
            }
            System.out.println("updated=" + count);
        }
        hasResult = ps.getMoreResults(Statement.CLOSE_CURRENT_RESULT);
    }
}
```

`getMoreResults()` 默认关闭当前结果；需要保留时用 KEEP_CURRENT_RESULT 并自行关闭，CLOSE_ALL_RESULTS 关闭已保留结果。重新执行同一 Statement 会关闭旧结果。不要将“更新计数为 0”误判为结果结束；只有当前无结果集且更新计数为 -1 才结束。


JDBC 接口支持范围见[使用限制](limitations.md)。
