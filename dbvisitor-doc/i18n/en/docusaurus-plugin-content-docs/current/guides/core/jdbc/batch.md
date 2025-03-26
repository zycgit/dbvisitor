---
id: batch
sidebar_position: 6
hide_table_of_contents: true
title: 批量化
description: 了解使用 JdbcTemplate 的批量操作。
---

**JdbcTemplate** 的 **executeBatch** 方法，将一批命令提交到数据库执行，如果所有命令都成功执行，则返回更新计数的数组。
数组元素按照命令的顺序进行排序，命令的顺序根据它们执行时 SQL 顺序或者参数顺序排序。

## 用法

批量执行 SQL 命令适用于不需要参数的语句，它们将会通过 **Statement** 接口来执行。比如：下面这个批量 INSERT。

```java title='不使用参数'
int[] result = jdbc.executeBatch(new String[] {
    "insert into users values (1, 'David')",
    "insert into users values (2, 'Kevin')"
});
```

```java title='使用 位置参数'
String sql = "insert into users values (?,?)";
Object[][] arg = new Object[][] {
    new Object[] { 1, "David"},
    new Object[] { 2, "Kevin"}
};

int[] result = jdbc.executeBatch(sql, arg);
```

```java title='使用 名称参数'
String sql = "insert into users (id, name) values(:id, :name)";
Map<String, Object> args1 = CollectionUtils.asMap(
                                "id", 1,
                                "name", "David"
                            );
Map<String, Object> args1 = CollectionUtils.asMap(
                                "id", 2,
                                "name", "Kevin"
                            );

int[] result = jdbc.executeBatch(sql, new Map[] { args1, args2 });
```

```java title='使用 PreparedStatement'
String     sql = "insert into users values (?,?)";
Object[][] arg = new Object[][] { 
                    new Object[] { 1, "David"},
                    new Object[] { 2, "Kevin"}
                };

BatchPreparedStatementSetter setter = new BatchPreparedStatementSetter() {
    public void setValues(PreparedStatement ps, int i) throws SQLException {
        ps.setInt(1, (int)arg[i][0]);
        ps.setString(1, (String)arg[i][1]);
    }

    public int getBatchSize() {
        return arg.length;
    }
};

int[] result = jdbc.executeBatch(sql, setter);
```

:::info
- dbVisitor 支持多种参数传递方式，为了便于理解下面执行 SQL 的案例中选择常用的 **无参数**、**位置参数**、**名称参数** 三种。
- 想要了解更多参数传递内容请到 **[参数传递](../../args/about)** 页面查看。
:::

## 返回值

返回值会有如下几种种情况：
- **大于** 或 **等于0** 的数字，表示命令已成功处理，是一个更新计数，给出执行命令时影响数据库中的行数。
- **SUCCESS_NO_INFO** 的值，表示命令处理成功，但受影响的行数未知（参考：java.sql.Statement.SUCCESS_NO_INFO）。
- **EXECUTE_FAILED** 的值，表示命令未能成功执行，只有在命令失败后驱动程序继续处理命令时才会发生。

如果发送到数据库的命令之一未能正确执行或试图返回结果集，该方法会抛出 **BatchUpdateException** 异常。

:::info
尽管根据 JDBC 规范行为应当如上述情况所描述，但在实际实践过程中开发者仍然需要注意 Driver 驱动程序对 JDBC 批量操作的具体行为。

这部分资料需要参考应用程序所选择的驱动程序。
:::
