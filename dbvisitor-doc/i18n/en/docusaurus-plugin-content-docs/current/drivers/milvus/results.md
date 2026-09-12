---
id: results
sidebar_position: 5
title: Read Results
---

Use `executeQuery()` for a known result-set command and `executeUpdate()` for a known update-count command. Use `execute()` if the result kind is not known in advance or when executing multiple statements. Returned columns are documented with each statement.

## Multiple Statements and Results

This fragment uses an open `conn` and `books_demo` from the [getting-started program](execution.mdx). Each ResultSet belongs to a SQL statement, not to a group of query vectors inside one SELECT.

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

`getMoreResults()` closes the current result by default. KEEP_CURRENT_RESULT retains it for explicit closing; CLOSE_ALL_RESULTS closes retained results. Reexecuting the same Statement closes its old results. An update count of 0 is not the end: stop only when there is no current ResultSet and the update count is -1.


Supported JDBC interfaces: [Usage Limits](limitations.md).
