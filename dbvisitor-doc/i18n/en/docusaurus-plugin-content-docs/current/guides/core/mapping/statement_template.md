---
id: statement_template
sidebar_position: 7
title: Statement Templates
description: Statement templates control how SQL fragments are generated when using the Fluent API.
---

# Statement Templates

:::warning[Note]
- Templates become SQL structure and must be defined by application code, not external input. Bind data values through `?`; switching to JdbcTemplate still requires binding.
:::

When using the [Fluent API](../../core/lambda/about), statement templates shape the generated SQL fragments.

Example: for MySQL tables with `point` columns, templates let you wrap reads/writes with `ST_GeomFromText` and `ST_AsText`.

```mysql title='Example: table'
create table user_points
(
    id    int auto_increment primary key,
    point point,
    text  varchar(50)
);
```

```java title='Example: mapping'
@Table("user_points")
public class UserPoints {
    private Integer id;

    @Column(selectTemplate   = "ST_AsText(point)",  // Generates select ST_AsText(point) as point
            insertTemplate   = "ST_GeomFromText(?)",// Generates insert ... values (ST_GeomFromText(?))
            setValueTemplate = "ST_GeomFromText(?)",// Generates update ... set point = ST_GeomFromText(?)
            whereColTemplate = "ST_AsText(point)"   // Generates ... where ST_AsText(point) = ?
    )
    private String point;
}
```

```java title='Example: INSERT and generated SQL'
UserPoints point = new UserPoints();
point.setId(1);
point.setPoint("POINT(1 2)");

LambdaTemplate lambda = ...
int result = lambda.insert(UserPoints.class)
                   .applyEntity(point)
                   .executeSumResult();

// SQL: INSERT INTO user_points (id, point) VALUES (?, ST_GeomFromText(?))
```

```java title='Example: UPDATE and generated SQL'
LambdaTemplate lambda = ...
int result = lambda.update(UserPoints.class)
                   .eq(UserPoints::getId, 1)                     // Matching condition
                   .updateTo(UserPoints::getPoint, "POINT(1 2)") // Update field
                   .doUpdate();

// SQL: UPDATE user_points SET point = ST_GeomFromText(?) WHERE ( id = ? )
```

```java title='Example: DELETE and generated SQL'
LambdaTemplate lambda = ...
int result = lambda.delete(UserPoints.class)
                   .eq(UserPoints::getPoint, "POINT(1 2)") // Matching condition
                   .doDelete();

// SQL: DELETE FROM user_points WHERE ( ST_AsText(point) = ? )
```

### Template attributes

| Attribute          | Description                                               |
|--------------------|-----------------------------------------------------------|
| selectTemplate     | Column expression in SELECT. Empty = column name.         |
| insertTemplate     | Argument expression in INSERT. Default `?`.              |
| setValueTemplate   | Argument expression in UPDATE SET. Default `?`.          |
| whereColTemplate   | Column expression in WHERE (update/delete). Empty = name. |
| whereValueTemplate | Argument expression in WHERE (update/delete). Default `?`.| 
| orderByColTemplate | Column expression in ORDER BY. Empty = column name.        |
