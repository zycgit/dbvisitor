---
id: write_policy
sidebar_position: 5
hide_table_of_contents: true
title: 写入策略
description: 使用 dbVisitor ORM 工具操作数据库时使用不通的写入策略。
---

当在使用 [构造器 API](../../api/wrapper_api) 操作数据库时，可以在列上配置写入策略来影响 INSERT、UPDATE 的行为。

```java title='列不允许更新：不会参与 update set 语句生成'
@Table
public class Users {
    ...
    @Column(name = "create_time", update = false) // 列不参与更新
    private Date    createTime;
    ...
}
```

```java title='列不允许新增：不会参与 insert 的插入'
@Table
public class Users {
    ...
    @Column(name = "create_time", insert = false) // 列不参与新增
    private Date    createTime;
    ...
}
```
