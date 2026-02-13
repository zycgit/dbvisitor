---
id: lambda_api
sidebar_position: 4
hide_table_of_contents: true
title: 4.4 构造器 API
description: 构造器为创建和运行数据库查询提供了方便、流畅的界面。它可以用于执行应用程序中的大多数数据库操作，并与 dbVisitor 支持的所有数据库系统完美配合。
---

# 构造器 API

dbVisitor 的 [构造器](../core/lambda/about) 为创建和运行数据库操作提供了流畅的链式界面。
**无需编写任何 SQL**，即可完成 CRUD、条件查询、分页、聚合等操作，并自动适配所有支持的数据库方言。

:::tip[特点]
- **不写 SQL 的通用数据库访问**，通过链式 API 构建查询条件，自动生成跨数据库兼容的 SQL。
- 提供 **Entity 模式**（基于实体类 + Lambda 方法引用）和 **Freedom 模式**（基于表名 + 字符串列名）两种使用方式。
- 丰富的条件方法：`eq`、`ne`、`gt`、`ge`、`lt`、`le`、`like`、`in`、`between`、`isNull`、嵌套 `and/or` 等。
- 支持 **样本查询**（`eqBySample`）：传入实体对象，非空属性自动作为等值条件。
- 内置 **分页**（`initPage` / `usePage`）、**排序**（`orderBy`）、**分组聚合**（`groupBy` / `having`）。
- 内置 **主键冲突策略**：`Into`（默认抛异常）、`Ignore`（静默忽略）、`Update`（存在则更新，即 Upsert）。
- 所有参数均通过 **预编译占位符** 传递，天然防止 SQL 注入。
:::

## 声明实体类

```java title='使用注解建立表映射'
@Table("users")
public class User {
    @Column(name = "id", primary = true, keyType = KeyTypeEnum.Auto)
    private Long id;
    @Column("name")
    private String name;
    @Column("age")
    private Integer age;
    ...
}
```

## 创建

```java title='通过 DataSource 或 Connection 创建'
LambdaTemplate lambda = new LambdaTemplate(dataSource);
// 或
LambdaTemplate lambda = new LambdaTemplate(connection);
```

## 插入

```java title='单条 / 多条 / 冲突策略'
// 单条插入
lambda.insert(User.class)
      .applyEntity(user1)
      .executeSumResult();

// 多条插入
lambda.insert(User.class)
      .applyEntity(user1, user2, user3)
      .executeSumResult();

// 主键冲突时更新（Upsert）
lambda.insert(User.class)
      .onDuplicateStrategy(DuplicateKeyStrategy.Update)
      .applyEntity(user1)
      .executeSumResult();
```

## 更新与删除

```java title='条件更新 / 删除'
// 条件更新
lambda.update(User.class)
      .eq(User::getId, 1)
      .updateTo(User::getName, "新名字")
      .updateTo(User::getAge, 30)
      .doUpdate();

// 条件删除
lambda.delete(User.class)
      .eq(User::getId, 1)
      .doDelete();
```

## 条件查询

```java title='链式条件 + 分页 + 排序'
// 多条件等值查询
List<User> users = lambda.query(User.class)
                         .eq(User::getName, "Alice")
                         .ge(User::getAge, 20)
                         .queryForList();

// 样本查询：非空属性自动作为条件
User sample = new User();
sample.setName("Alice");
List<User> users = lambda.query(User.class)
                         .eqBySample(sample)
                         .queryForList();

// 分页查询
List<User> page1 = lambda.query(User.class)
                         .like(User::getName, "User%")
                         .initPage(10, 0)     // 每页 10 条，第 0 页
                         .orderBy("id")
                         .queryForList();
```

## Freedom 模式

```java title='不依赖实体类，使用表名和字符串列名操作'
// 插入
Map<String, Object> row = new HashMap<>();
row.put("name", "FreedomUser");
row.put("age", 25);
lambda.insertFreedom("user_info")
      .applyMap(row)
      .executeSumResult();

// 查询
Map<String, Object> result = lambda.queryFreedom("user_info")
                                   .eq("name", "FreedomUser")
                                   .queryForObject();
```

:::info[有关构造器 API 的详细信息，请参阅：]
- [LambdaTemplate 类](../core/lambda/about)
- [对象映射](../core/mapping/about)
:::
