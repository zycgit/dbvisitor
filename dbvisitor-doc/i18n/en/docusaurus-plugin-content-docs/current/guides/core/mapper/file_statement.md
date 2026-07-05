---
id: file_statement
sidebar_position: 12
title: Call File Mapper
description: Call SQL in XML files from Mapper API, suitable for long SQL, complex dynamic SQL, and centrally maintained statements.
---

# Call File Mapper

File Mapper keeps SQL in XML files for maintenance. Mapper API maps interface methods or statement IDs to those SQL statements.

## Suitable For

- SQL is long and hard to read inside annotations.
- You need to reuse [rules](../../rules/about), dynamic SQL tags, resultMap, or entity mappings.
- SQL should be centrally maintained in dedicated files, with interfaces keeping only method signatures.

## Not Suitable For

- Short SQL – use [method annotations](./about#method-annotations) for clarity.
- Simple single-table CRUD – prefer [BaseMapper](./about#base-mapper).
- Conditions composed in code – prefer [Fluent API](./lambda_builder).

## Interface Call (Recommended)

Use `@RefMapper` to bind an interface to an XML file. The XML `namespace` should be the fully qualified interface name, and the statement `id` should match the interface method name.

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
    <select id="listUsers" resultMap="user_resultMap">
        select * from users
        where 1 = 1
        @{and, name is not null, "name like concat('%', #{name}, '%')"}
    </select>
</mapper>
```

```java title='Using the Mapper'
Session session = config.newSession(dataSource);
UserMapper mapper = session.createMapper(UserMapper.class);

List<User> users = mapper.listUsers("alice");
```

:::tip
How to obtain a Session depends on your project architecture. See [Framework Integration](../../yourproject/buildtools#integration).
:::

## Direct Statement Call

If you don't have an interface method, you can also call XML SQL directly via statement ID. The statement ID is typically `namespace + "." + id`.

```java title='Call via BaseMapper'
BaseMapper<User> mapper = session.createBaseMapper(User.class);
List<User> users = mapper.queryStatement("net.example.mapper.UserMapper.listUsers", args);
```

```java title='Call via Session'
List<User> users = session.queryStatement("net.example.mapper.UserMapper.listUsers", args);
```

:::note
Interface calls are better for business code: the method signature is the contract. Direct statement calls are more suitable for framework wrappers, migration compatibility, or a few low-level scenarios.
:::

## Pagination Query {#page}

Pass a `Page` parameter for paginated queries. Both BaseMapper and Session support `queryStatement` with pagination.

```java title='Pagination query (returns List)'
PageObject page = PageObject.of(0, 20);
BaseMapper<User> mapper = session.createBaseMapper(User.class);

List<User> users = mapper.queryStatement(
        "net.example.mapper.UserMapper.listUsers",
        args,
        page);
```

```java title='Pagination query (returns PageResult, Session only)'
PageObject page = PageObject.of(0, 20);
PageResult<User> users = session.pageStatement(
        "net.example.mapper.UserMapper.listUsers",
        args,
        page);
```

`PageResult` contains the original pagination info, total record count, and total page count.

## Further Reading

- [Mapper File](../file/about) — XML document structure, tags, dynamic SQL, mapping configuration.
- [Statement Tags](../file/statements) — `<select>`, `<insert>`, `<update>`, `<delete>` and other tags.
- [Pagination Query](../file/paging) — Pagination capabilities of File Mapper.
- [Parameter Passing](../../args/about) — How Mapper method parameters and statement parameters are bound.
