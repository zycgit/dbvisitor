---
id: template
sidebar_position: 7
hide_table_of_contents: true
title: 语句模版
description: 在使用构造器 API进行数据库操作时，语句模版可以决定生成的 SQL 的语句元素内容。
---

在使用 [构造器 API](../../core/wrapper/about) 进行数据库操作时，语句模版可以决定生成的 SQL 的语句元素内容。

例如，对带有 point 类型的 MySQL 表进行读写操作时可以利用语句模版特性在生成的语句中使用数据库 `PointFromText`、`AsText` 函数。

```mysql title='例：表'
create table user_points
(
    id    int auto_increment primary key,
    point point,
    text  varchar(50)
);
```

```java title='例：映射'
@Table("user_points")
public class UserPoints {
    private Integer id;

    @Column(selectTemplate   = "AsText(point)",  // 会生成 select AsText(point) as point
            insertTemplate   = "GeomFromText(?)",// 会生成 insert ... values (GeomFromText(?))
            setValueTemplate = "GeomFromText(?)",// 会生成 update ... set point = GeomFromText(?)
            whereColTemplate = "AsText(point)"   // 会生成 ... where AsText(point) = ?
    )
    private String point;
}
```

```java title='例：INSERT 操作和对应语句'
UserPoints point = new UserPoints();
point.setId(1);
point.setPoint("point(1,2)");

WrapperAdapter adapter = ...
int result = adapter.insertByEntity(UserPoints.class)
                    .applyEntity(point);
                    .executeSumResult();

// 语句为：INSERT INTO user_points (id, point) VALUES (?, GeomFromText(?))
```

```java title='例：UPDATE 操作和对应语句'
WrapperAdapter adapter = ...
int result = adapter.updateByEntity(UserPoints.class)
        .eq(UserPoints::getId, 1)                     // 匹配条件
        .updateTo(UserPoints::getPoint, "point(1,2)") // 更新字段
        .doUpdate();

// 语句为：UPDATE user_points SET point = GeomFromText(?) WHERE ( id = ? )
```

```java title='例：DELETE 操作和对应语句'
WrapperAdapter adapter = ...
int result = adapter.deleteByEntity(User.class)
        .eq(UserPoints::getPoint, "point(1,2)") // 匹配条件
        .doDelete();

// 语句为：DELETE FROM user_points WHERE ( AsText(point) = ? )
```

### 模版属性

| 属性名                | 描述                                             |
|--------------------|------------------------------------------------|
| selectTemplate     | 用作 select 语句中的列名。默认是空，表示列名本身。                  |
| insertTemplate     | 用作 insert 语句时参数写法，默认是 ?。                       |
| setColTemplate     | 用作 update 的 set 语句时列名的写法。默认是空，表示列名本身。          |
| setValueTemplate   | 用作 update set 语句时参数写法，默认是 ?。                   |
| whereColTemplate   | 用作 update/delete 的 where 语句时列名的写法。默认是空，表示列名本身。 |
| whereValueTemplate | 用作 update/delete 的 where 语句时参数写法，默认是 ?。        |
| groupByColTemplate | 用作 group by 语句时列名的写法。默认是空，表示列名本身。              |
| orderByColTemplate | 用作 order by 语句时列名的写法。默认是空，表示列名本身。              |
