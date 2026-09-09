---
id: about
sidebar_position: 1
title: 5.3 构造器 API
description: 通过编程方式生成 SQL，屏蔽数据库方言差异，支持实体模式和 Map 查询模式。
---

# 5.3 构造器 API

`LambdaTemplate` 是 dbVisitor 的构造器 API 核心类，通过编程方式构建 SQL 语句，**自动处理数据库方言差异**。

## 先选模式

| 模式 | 是否需要对象映射 | 适合场景 |
| --- | --- | --- |
| Entity 模式 | 需要 | 有实体类，使用 `User::getName` 这类方法引用操作列。 |
| [映射 Map 模式](../map_query/mapped) | 需要 | 有表映射，但数据载体是 `Map`。 |
| [自由 Map 模式](../map_query/freedom) | 不需要 | 没有实体类或不想维护映射，直接使用表名和列名。 |

Entity 模式和 [映射 Map 模式](../map_query/mapped) 使用前需要为数据库表建立 [对象映射](../mapping/about)。[自由 Map 模式](../map_query/freedom) 不需要实体映射。

```java title='实体类映射'
@Table("users")
public class User {
    @Column(value = "id", primary = true, keyType = KeyType.Auto)
    private Long id;
    @Column("name")
    private String name;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
```

```java title='创建和使用'
LambdaTemplate lambda = new LambdaTemplate(dataSource);

// 查询示例
List<User> users = lambda.query(User.class)
        .eq(User::getName, "alice")
        .queryForList();
```

:::tip[提示]
LambdaTemplate 的获取方式取决于项目架构，详见 **[框架整合](../../yourproject/buildtools#integration)**。
:::

## 原理 {#principle}

通过 `insert`、`update`、`delete`、`query`、`freedom` 系列方法构建操作，开发者以编程方式描述 SQL 逻辑，运行时自动生成对应方言的 SQL 并通过 JdbcTemplate 执行。

## 使用指引 {#guide}

- [Insert](./insert)，写入数据、批量写入、冲突策略（Ignore/Update）。
- [Update](./update)，三种更新方式及不安全更新防护。
- [Delete](./delete)，删除数据。
- [Query](./query)，查询数据。
- [条件构造器](./where_builder)，构建复杂查询条件，用于 Update/Delete/Query。
- [分组](./group_by)，GROUP BY 分组查询。
- [排序](./order_by)，ORDER BY 查询排序。
- [映射 Map 模式](../map_query/mapped)，复用对象映射，但以 Map 作为数据载体。
- [自由 Map 模式](../map_query/freedom)，不依赖对象映射，直接使用表名和列名。
