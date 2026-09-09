---
id: file_statement
sidebar_position: 12
title: 调用文件 Mapper
description: 在 Mapper API 中调用 XML 文件里的 SQL，适合长 SQL、复杂动态 SQL 和集中维护的语句。
---

# 调用文件 Mapper

文件 Mapper 把 SQL 放到 XML 中维护，Mapper API 负责把接口方法或 statementId 调用映射到这些 SQL。

## 适合场景

- SQL 较长，写在注解中可读性差。
- 需要复用 [规则](../../rules/about)、动态 SQL 标签、resultMap 或 entity 映射。
- 希望 SQL 由专门文件集中维护，接口只保留调用签名。

## 不适合场景

- SQL 很短，直接使用 [方法注解](./about#method-annotations) 更清楚。
- 只是单表 CRUD，优先使用 [BaseMapper](./about#base-mapper)。
- 想用代码方式组合条件，优先使用 [构造器 API](./lambda_builder)。

## 接口调用（推荐）

使用 `@RefMapper` 将接口绑定到 XML 文件。XML 的 `namespace` 建议写接口全限定名，语句 `id` 对应接口方法名。

```java title='UserMapper.java'
@RefMapper("/mapper/userMapper.xml")
public interface UserMapper {
    List<User> listUsers(@Param("name") String name);
}
```

```xml title='mapper/userMapper.xml'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//dbvisitor.net//DTD Mapper 1.0//EN"
        "https://www.dbvisitor.net/schema/dbvisitor-mapper.dtd">
<mapper namespace="net.example.mapper.UserMapper">
    <select id="listUsers" resultType="net.example.dto.User">
        select * from users
        where 1 = 1
        @{and, name like concat('%', #{name}, '%')}
    </select>
</mapper>
```

```java title='调用 Mapper'
Session session = config.newSession(dataSource);
UserMapper mapper = session.createMapper(UserMapper.class);

List<User> users = mapper.listUsers("alice");
```

:::tip
Session 的获取方式取决于项目架构，详见 [框架整合](../../yourproject/buildtools#integration)。
:::

## 直接调用 statement

如果没有接口方法，也可以通过 statementId 直接调用 XML 中的 SQL。statementId 通常由 `namespace + "." + id` 组成。

```java title='通过 BaseMapper 调用'
BaseMapper<User> mapper = session.createBaseMapper(User.class);
List<User> users = mapper.queryStatement("net.example.mapper.UserMapper.listUsers", args);
```

```java title='通过 Session 调用'
List<User> users = session.queryStatement("net.example.mapper.UserMapper.listUsers", args);
```

:::note
接口调用更适合业务代码：方法签名就是契约。直接调用 statement 更适合框架封装、迁移兼容或少量低层场景。
:::

## 分页查询 {#page}

通过传递 `Page` 参数进行分页查询。BaseMapper 和 Session 都支持带分页的 `queryStatement`。

```java title='分页查询（返回 List）'
Page page = PageObject.of(0, 20);
BaseMapper<User> mapper = session.createBaseMapper(User.class);

List<User> users = mapper.queryStatement(
        "net.example.mapper.UserMapper.listUsers",
        args,
        page);
```

```java title='分页查询（返回 PageResult，仅 Session 支持）'
Page page = PageObject.of(0, 20);
PageResult<User> users = session.pageStatement(
        "net.example.mapper.UserMapper.listUsers",
        args,
        page);
```

`PageResult` 会包含原始分页信息、总记录数、总页数。

## 深入阅读

- [Mapper 文件](../file/about) — XML 文档结构、标签、动态 SQL、映射配置。
- [文档标签](../file/statements) — `<select>`、`<insert>`、`<update>`、`<delete>` 等标签。
- [分页查询](../file/paging) — 文件 Mapper 的分页能力。
- [参数传递](../../args/about) — Mapper 方法参数和 statement 参数如何绑定。
