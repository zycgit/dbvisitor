---
id: lambda_builder
sidebar_position: 11
title: 调用构造器 API
description: 在 Mapper 接口中调用构造器 API，适合需要代码化组合条件的查询、更新和删除。
---

# 调用构造器 API

Mapper 接口继承 `BaseMapper<T>` 后，可以在默认方法或业务代码中调用 `query()`、`update()`、`delete()`、`insert()` 等构造器能力。

## 适合场景

- 条件由 Java 代码组合，比写动态 SQL 更自然。
- 需要复用实体映射，避免手写表名和列名。
- 希望把复杂查询封装成 Mapper 接口上的默认方法。

## 不适合场景

- SQL 已经很稳定且较短，使用 [方法注解](./about#method-annotations) 更直观。
- SQL 很长或需要复杂 XML 映射，使用 [Mapper 文件](./file_statement) 更合适。
- 只是常规单表 CRUD，直接使用 [BaseMapper](./about#base-mapper) 的 CRUD 方法即可。

## Mapper 中的模式

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

调用方仍然只面对 Mapper 接口：

```java title='调用 Mapper'
UserMapper mapper = session.createMapper(UserMapper.class);
List<User> users = mapper.listActiveUsers("alice", 18);
```

## 常见实例

```java title='新增'
mapper.insert()
        .applyEntity(user)
        .executeSumResult();
```

```java title='修改'
mapper.update()
        .eq(User::getId, 1)
        .updateTo(User::getName, "Mary")
        .updateTo(User::getStatus, 2)
        .doUpdate();
```

```java title='删除'
mapper.delete()
        .eq(User::getId, 1)
        .doDelete();
```

```java title='查询'
List<User> result = mapper.query()
        .le(User::getId, 100)
        .queryForList();
```

:::tip
Session 的获取方式取决于项目架构，详见 [框架整合](../../yourproject/buildtools#integration)。
:::

## 深入阅读

- [LambdaTemplate 类使用指引](../lambda/about#guide) — 构造器 API 的完整能力。
- [BaseMapper](./about#base-mapper) — Mapper 继承后获得的通用 CRUD 能力。
- [对象映射](../mapping/about) — 构造器如何根据实体映射生成 SQL。
