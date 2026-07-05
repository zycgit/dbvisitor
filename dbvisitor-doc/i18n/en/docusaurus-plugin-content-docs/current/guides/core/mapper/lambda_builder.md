---
id: lambda_builder
sidebar_position: 11
title: Call Fluent API
description: Call Fluent API from Mapper interfaces for queries, updates, and deletes whose conditions are composed in Java code.
---

# Call Fluent API

After a Mapper interface extends `BaseMapper<T>`, it can call builder capabilities such as `query()`, `update()`, `delete()`, and `insert()` from default methods or business code.

## Suitable For

- Conditions are composed in Java code, which is more natural than writing dynamic SQL.
- Entity mappings should be reused, avoiding hand-written table and column names.
- Complex queries should be encapsulated as default methods on a Mapper interface.

## Not Suitable For

- SQL is already stable and short – use [method annotations](./about#method-annotations) for clarity.
- SQL is long or needs complex XML mappings – use [Mapper files](./file_statement).
- Simple single-table CRUD – use [BaseMapper](./about#base-mapper)'s CRUD methods directly.

## Pattern in Mapper

```java title='UserMapper.java'
@SimpleMapper
public interface UserMapper extends BaseMapper<User> {
    default List<User> listActiveUsers(String name, int minAge) {
        return query()
                .eq(User::getStatus, "ACTIVE")
                .like(name != null, User::getName, name)
                .ge(User::getAge, minAge)
                .orderByDesc(User::getCreateTime)
                .queryForList();
    }
}
```

The caller still only interacts with the Mapper interface:

```java title='Using the Mapper'
UserMapper mapper = session.createMapper(UserMapper.class);
List<User> users = mapper.listActiveUsers("alice", 18);
```

## Common Examples

```java title='Insert'
mapper.insert()
        .applyEntity(user)
        .executeSumResult();
```

```java title='Update'
mapper.update()
        .eq(User::getId, 1)
        .updateTo(User::getName, "Mary")
        .updateTo(User::getStatus, 2)
        .doUpdate();
```

```java title='Delete'
mapper.delete()
        .eq(User::getId, 1)
        .doDelete();
```

```java title='Query'
List<User> result = mapper.query()
        .le(User::getId, 100)
        .queryForList();
```

:::tip
How to obtain a Session depends on your project architecture. See [Framework Integration](../../yourproject/buildtools#integration).
:::

## Further Reading

- [LambdaTemplate Usage Guide](../lambda/about#guide) — The complete Fluent API capabilities.
- [BaseMapper](./about#base-mapper) — Common CRUD capabilities gained by extending BaseMapper.
- [Object Mapping](../mapping/about) — How the Fluent builder generates SQL from entity mappings.
