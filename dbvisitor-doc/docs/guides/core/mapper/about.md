---
id: about
sidebar_position: 1
hide_table_of_contents: true
title: Mapper 接口
description: 通用 BaseMapper 接口提供零 SQL 的 CRUD 能力，基于对象映射自动生成操作语句。
---

# Mapper 接口（BaseMapper）

`BaseMapper<T>` 是 dbVisitor 提供的通用数据访问接口，基于 [对象映射](../mapping/about) 自动生成 CRUD 语句，**无需编写任何 SQL**。

```java title='快速上手'
BaseMapper<User> mapper = session.createBaseMapper(User.class);

// 写入
mapper.insert(user);

// 按主键查询
User found = mapper.selectById(1L);

// 分页查询
Page page = PageObject.of(0, 20);
PageResult<User> result = mapper.pageBySample(null, page);
```

:::tip[提示]
Session 的获取方式取决于项目架构，详见 **[框架整合](../../yourproject/buildtools#integration)**。
:::

## 使用指引

- [基础操作](./common)，CRUD、按主键操作、分页查询。
- [调用构造器 API](./lambda)，在 BaseMapper 上使用条件构造器进行复杂查询。
- [执行 Mapper 文件中的命令](./file)，引用 Mapper XML 文件中定义的 SQL 命令。
