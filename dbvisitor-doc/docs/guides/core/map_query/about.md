---
id: about
sidebar_position: 1
title: 5.5 Map 查询模式
description: Map 查询模式使用 Map 作为数据载体，包含依赖对象映射的映射 Map 模式和不依赖对象映射的自由 Map 模式。
---

# 5.5 Map 查询模式

Map 查询模式使用 `Map<String, Object>` 作为数据载体。它包含两个同级入口：**映射 Map 模式** 复用实体对象映射，**自由 Map 模式** 直接使用表名和列名。

## 适合场景

- 数据来自动态表单、导入任务、消息体或中间转换结果，天然就是 `Map`。
- 需要在同一套构造器 API 下完成查询、写入、更新、删除。
- 已经有对象映射，但业务层不想维护实体对象。
- 没有实体类或不想维护对象映射，表名、列名来自配置或运行时规则。

## 不适合场景

- 希望获得编译期字段检查；应使用 [构造器 API Entity 模式](../lambda/about) 和方法引用。
- 只是按主键操作单行数据；优先看 [BaseMapper](../mapper/about#base-mapper) 的通用 CRUD。
- SQL 结构很复杂，包含大量 JOIN、子查询或数据库专有语法；直接写 [编程式 API](../jdbc/about) 或 [Mapper 文件](../file/about) 通常更清楚。

## 两种 Map 模式

| 模式 | 入口 | 是否需要对象映射 | 字段名如何解释 | 详细说明 |
| --- | --- | --- | --- | --- |
| 映射 Map 模式 | `lambda.query(User.class).asMap()` | 需要 | Java 属性名，按映射转换为数据库列名 | [映射 Map 模式](./mapped) |
| 自由 Map 模式 | `lambda.queryFreedom("users")` | 不需要 | 表名和列名直接来自字符串 | [自由 Map 模式](./freedom) |

```text title='模式选择'
已有实体映射
        |
        +-- 需要 Map 作为数据载体
        |       |
        |       +-- 使用 asMap()
        |
        +-- 需要实体对象和方法引用
                |
                +-- 使用 Entity 模式

没有实体映射
        |
        +-- 表名、列名由调用者提供
                |
                +-- 使用 *Freedom()
```

两种模式都会使用 `MapQuery`、`MapInsert`、`MapUpdate`、`MapDelete` 这组 API。共同能力包括条件、排序、分组、分页、写入、更新、删除和危险更新保护。

## 查询和写入入口

| 操作 | 映射 Map 模式 | 自由 Map 模式 |
| --- | --- | --- |
| 查询 | `lambda.query(User.class).asMap()` | `lambda.queryFreedom("users")` |
| 写入 | `lambda.insert(User.class).asMap()` | `lambda.insertFreedom("users")` |
| 更新 | `lambda.update(User.class).asMap()` | `lambda.updateFreedom("users")` |
| 删除 | `lambda.delete(User.class).asMap()` | `lambda.deleteFreedom("users")` |

写入、更新、删除的具体差异见 [Map 写入、更新和删除](./write)。

## 字段来源

```text title='字段解释规则'
映射 Map 模式
Map key = Java 属性名
loginName -> 对象映射 -> login_name

自由 Map 模式
Map key = 调用者提供的列名
login_name -> SQL 标识符
```

映射 Map 模式会读取对象映射，因此可以复用列名转换、字段过滤、TypeHandler 和写入策略。自由 Map 模式不读取实体映射，表名和列名由调用者直接提供。

## 深入阅读

- [映射 Map 模式](./mapped) — 已有实体映射，但使用 Map 作为数据载体。
- [自由 Map 模式](./freedom) — 不维护实体类，直接提供表名和列名。
- [Map 写入、更新和删除](./write) — 两种模式下的写入和变更操作。
- [对象映射](../mapping/about) — 映射 Map 模式的字段过滤、列名转换和类型处理基础。
