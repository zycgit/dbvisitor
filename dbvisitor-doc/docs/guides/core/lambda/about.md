---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: LambdaTemplate 类
description: 通过编程方式生成 SQL，屏蔽数据库方言差异，支持实体模式和自由模式。
---

# LambdaTemplate

`LambdaTemplate` 是 dbVisitor 的构造器 API 核心类，通过编程方式构建 SQL 语句，**自动处理数据库方言差异**。使用前需要为数据库表建立 [对象映射](../mapping/about)。

```java title='实体类映射'
@Table("users")
public class User {
    @Column(name = "id", primary = true, keyType = KeyTypeEnum.Auto)
    private Long id;
    @Column("name")
    private String name;
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
- [条件构造器](./where-builder)，构建复杂查询条件，用于 Update/Delete/Query。
- [分组](./groupby)，GROUP BY 分组查询。
- [排序](./orderby)，ORDER BY 查询排序。
- [专为 Map 设计的 API](./for-map)，以 Map 而非实体对象作为数据载体。
- [自由模式](./freedom)，无对象映射类，基于表名 + Map 直接操作。
