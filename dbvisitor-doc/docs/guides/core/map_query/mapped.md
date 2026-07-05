---
id: mapped
sidebar_position: 2
title: 映射 Map 模式
description: 在已有对象映射的基础上，用 Map 作为查询、写入和更新的数据载体。
---

# 映射 Map 模式

映射 Map 模式从 Entity 构造器切换而来，它仍然使用实体类上的对象映射。调用方传入 Map，但 Map 的 key 按 Java 属性名理解。

## 适合场景

- 项目已经维护了实体类和 `@Table` / `@Column` 映射。
- 需要复用列名转换、字段过滤、TypeHandler、写入策略等映射能力。
- 数据在业务层以 Map 流转，但数据库结构仍然和某个实体类对应。
- 查询结果希望返回 `List<Map<String, Object>>`，而不是实体对象列表。

## 不适合场景

- 没有实体类或不想维护对象映射；应使用 [自由 Map 模式](./freedom)。
- 字段名来自数据库列名，并且不希望经过对象映射转换。
- 表结构完全由运行时配置决定，实体映射无法覆盖这些字段。

## 模式关系

```text title='映射 Map 模式'
Entity 构造器
lambda.query(User.class)
        |
        | asMap()
        v
Map 构造器
Map key = User 的属性名
        |
        | 对象映射转换
        v
SQL column = 数据库列名
```

映射 Map 模式的核心价值是“数据载体换成 Map，但字段解释仍然由对象映射负责”。

## 入口写法

```java title='从 Entity 构造器切换到 Map'
LambdaTemplate lambda = new LambdaTemplate(dataSource);

MapQuery query = lambda.query(User.class).asMap();
MapInsert insert = lambda.insert(User.class).asMap();
MapUpdate update = lambda.update(User.class).asMap();
MapDelete delete = lambda.delete(User.class).asMap();
```

`asMap()` 只改变数据载体，不改变表映射来源。表名、列名、主键、TypeHandler 和写入策略仍来自 `User.class` 的对象映射。

## 按映射属性查询

实体中 `loginName` 映射到 `login_name` 时，查询条件仍写 `loginName`。

```java title='按映射属性查询'
List<Map<String, Object>> users = lambda.query(User.class)
        .asMap()
        .eq("loginName", "alice")
        .select("id", "loginName", "email")
        .queryForList();
```

SQL 形态类似：

```sql
SELECT id, login_name, email FROM users WHERE login_name = ?
```

## 查询结果

映射 Map 模式返回 `Map<String, Object>`。Map 的 key 仍按映射后的属性名组织，便于业务代码继续使用 Java 属性命名。

```java title='读取查询结果'
Map<String, Object> user = lambda.query(User.class)
        .asMap()
        .eq("id", 1001)
        .queryForObject();

Object loginName = user.get("loginName");
Object email = user.get("email");
```

## 写入和更新

写入、更新时，Map 中不存在于对象映射的 key 不会直接拼入 SQL。

```java title='按对象映射过滤写入字段'
Map<String, Object> data = new HashMap<>();
data.put("id", 1001);
data.put("loginName", "alice");
data.put("email", "alice@example.com");
data.put("unknownField", "ignored");

int rows = lambda.insert(User.class)
        .asMap()
        .applyMap(data)
        .executeSumResult();
```

```java title='按对象映射生成 SET'
Map<String, Object> setValues = new HashMap<>();
setValues.put("loginName", "alice_new");
setValues.put("email", null);
setValues.put("unknownField", "ignored");

int rows = lambda.update(User.class)
        .asMap()
        .eq("id", 1001)
        .updateToSample(setValues)
        .doUpdate();
```

更多写入和更新差异见 [Map 写入、更新和删除](./write)。

## 边界

- Map key 使用 Java 属性名，不是数据库列名。
- Map 中不存在映射的 key 会被过滤。
- 字段值会按对象映射中的 TypeHandler、写入策略等规则处理。
- 没有任何更新字段时会报 `there nothing to update.`。
- 没有 WHERE 的更新默认会被拦截，需要显式 `allowEmptyWhere()`。

## 深入阅读

- [自由 Map 模式](./freedom) — 不依赖对象映射的 Map 入口。
- [Map 写入、更新和删除](./write) — `applyMap`、`updateToSample` 和 `updateRow` 的选择。
- [对象映射](../mapping/about) — 映射 Map 模式复用的映射能力。
