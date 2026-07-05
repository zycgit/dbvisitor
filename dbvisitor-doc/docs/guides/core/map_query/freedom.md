---
id: freedom
sidebar_position: 3
title: 自由 Map 模式
description: 不依赖实体类和对象映射，直接使用表名、列名和 Map 数据构造 SQL。
---

# 自由 Map 模式

自由 Map 模式不需要实体类，也不读取对象映射。它和映射 Map 模式同属于 Map API 入口，区别是表名、列名都由调用者直接提供。

## 适合场景

- 没有实体类，或不希望为临时表、动态表、同步任务维护实体映射。
- 表名、列名来自配置中心、任务元数据或运行时规则。
- 操作对象是 Redis、MongoDB、ElasticSearch、Milvus 这类非传统表结构的数据源，同时仍希望使用统一的构造器 API。
- 需要按数据库列名直接组织 Map，而不是按 Java 属性名组织 Map。

## 不适合场景

- 需要复用实体字段上的 TypeHandler、写入策略、主键配置或字段过滤规则。
- 希望字段名有编译期检查或对象映射校验。
- 表名、列名直接来自用户输入，且无法建立白名单。

## 模式关系

```text title='自由 Map 模式'
调用者提供表名
lambda.queryFreedom("users")
        |
        v
调用者提供列名
eq("login_name", "alice")
        |
        v
SQL 标识符直接进入语句
```

自由 Map 模式复用构造器 API 的条件、排序、分组、分页和危险更新保护，但不读取实体类映射。

## 入口写法

```java title='自由 Map 入口'
MapQuery query = lambda.queryFreedom("users");
MapInsert insert = lambda.insertFreedom("users");
MapUpdate update = lambda.updateFreedom("users");
MapDelete delete = lambda.deleteFreedom("users");
```

需要明确 catalog 或 schema 时，可以使用三参数入口：

```java
MapQuery query = lambda.queryFreedom("catalog_name", "schema_name", "users");
```

## 按数据库列名查询

自由 Map 模式中的字段名默认按数据库列名理解，不会去实体映射里校验字段是否存在，也不会自动过滤未知列。

```java title='按数据库列名查询'
List<Map<String, Object>> rows = lambda.queryFreedom("users")
        .eq("login_name", "alice")
        .select("id", "login_name", "email")
        .queryForList();
```

SQL 形态类似：

```sql
SELECT id, login_name, email FROM users WHERE login_name = ?
```

如果启用了驼峰转下划线配置，内部会按配置把 Map key 转成列名；否则 key 会按传入内容作为列名使用。

## 写入、更新、删除

```java title='自由模式写入'
Map<String, Object> row = new HashMap<>();
row.put("id", 1001);
row.put("login_name", "alice");
row.put("email", "alice@example.com");

int rows = lambda.insertFreedom("users")
        .applyMap(row)
        .executeSumResult();
```

```java title='自由模式更新'
Map<String, Object> row = new HashMap<>();
row.put("login_name", "alice_new");

int rows = lambda.updateFreedom("users")
        .eq("id", 1001)
        .updateRow(row)
        .doUpdate();
```

```java title='自由模式删除'
int rows = lambda.deleteFreedom("users")
        .eq("id", 1001)
        .doDelete();
```

更多写入和更新差异见 [Map 写入、更新和删除](./write)。

## 标识符安全

SQL 参数值仍按参数绑定处理；表名、列名属于 SQL 标识符，不能像值一样用 `?` 绑定。

```text title='参数绑定边界'
值:       eq("id", 1001)      -> 使用参数绑定
列名:     eq("id", 1001)      -> id 是 SQL 标识符
表名:     queryFreedom("users") -> users 是 SQL 标识符
```

外部输入的表名、列名应先做白名单校验，尤其是配置驱动或用户可控字段。

## 边界

- Map 中的任意 key 都可能成为列名。
- 写错列名时，错误通常由数据库在执行 SQL 时返回。
- 类型处理以 Map 值和运行时类型为主，不会使用实体字段上的 TypeHandler 配置。
- 没有 WHERE 的更新和删除默认会被拦截，需要显式 `allowEmptyWhere()`。

## 深入阅读

- [映射 Map 模式](./mapped) — 复用对象映射的 Map 入口。
- [Map 写入、更新和删除](./write) — Map 数据变更操作。
- [条件构造器](../lambda/where_builder) — 普通条件和动态条件。
