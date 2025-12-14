---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: LambdaTemplate 类
description: 了解使用 LambdaTemplate 访问数据库的准备工作和必要的概念。
---

## 准备工作

LambdaTemplate 类在使用之前需要像 [JdbcTemplate](../jdbc/about) 一样为其准备好数据源，也可以将 JdbcTemplate 作为参数传递给 LambdaTemplate 的构造方法使其共享同一个数据库链接。

- 在使用 LambdaTemplate 时需还要为数据库中的表建立对象映射。
- 使用注解配置映射关系只是其中一种方式，[点击这里](../mapping/about) 可以了解更加详细的对象映射内容。

```java title='1. 注解方式为 users 表创建对象映射'
@Table("users")
public class User {
    @Column(name = "id", primary = true, keyType = KeyTypeEnum.Auto)
    private Long id;
    @Column("name")
    private String name;
    ...
}
```

- `@Table` 注解用来声明类型为一个实体，并且指定了这个实体对应的数据库表名。
- `@Column` 注解用来声明类型的字段和列之间的映射关系。
- 在上例中 `id` 字段被标记为主键，并指示该列含有数据库自增特性。

```java title='2. 创建构造器'
DataSource dataSource = ...
LambdaTemplate lambda = new LambdaTemplate(dataSource);

或者

Connection conn = ...
LambdaTemplate lambda = new LambdaTemplate(conn);
```

- 通过 lambda 对象提供的各类方法即可实现数据库读写。
- 默认情况下 LambdaTemplate 会使用 RegistryManager.DEFAULT 注册器为其管理类型映射信息。

## 原理 {#principle}

核心实现原理是通过 LambdaTemplate 类提供的 insert、update、delete、query、freedom 系列方法提供对表的操作。

- insert 用于生成和执行 INSERT 语句。
- update 用于生成和执行 UPDATE 语句。
- delete 用于生成和执行 DELETE 语句。
- query 用于生成和执行 SELECT 语句。

开发者通过编程方式指示 SQL 语句的生成逻辑，在执行时会根据逻辑生成 SQL 语句和参数，最终交给 JdbcTemplate 进行执行并获得返回值。

## 使用指引 {#guide}

- [Insert](./insert)，了解如何写入数据、如何利用批量化写入方式提升效率。并且处理写入过程中的数据冲突问题。
- [Update](./update)，了解更新数据的三种方法以及如何处理一些不安全的更新操作。
- [Delete](./delete)，了解使用 LambdaTemplate 如何删除数据。
- [Query](./query)，了解如何使用 dbVisitor 的构造器提供的查询方法共数据查询使用。 
- [条件构造器](./where-builder)，通过使用条件构造器可以构建出丰富的查询条件，并用于 Update、Delete、Query 操作中。
- [分组](./groupby)，进行分组查询。
- [排序](./orderby)，进行查询排序。
- [专为 Map 结构而设计的 API](./for-map)，虽然有了实体对象承载数据库表映射，但某些时候 Map 结构依然是一个不错的数据载体。
- [自由模式](./freedom)，自由模式下基于 Map 结构可以在无对象映射类的情况下生成和执行 SQL 语句。
