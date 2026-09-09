---
id: write
sidebar_position: 4
title: Map 写入、更新和删除
description: 使用 MapInsert、MapUpdate 和 MapDelete 完成 Map 数据的写入、更新和删除。
---

# Map 写入、更新和删除

Map 写入和变更操作由 `MapInsert`、`MapUpdate`、`MapDelete` 提供。映射 Map 模式和自由 Map 模式使用同一组 API，但字段解释规则不同。

## 适合场景

- 数据已经是 `Map<String, Object>`，需要直接写入或更新数据库。
- 需要批量导入、任务同步或动态字段写入。
- 需要在更新、删除时保留构造器 API 的条件能力和危险操作保护。

## 不适合场景

- 字段固定且希望使用编译期检查；应使用 Entity 模式。
- SQL 变更逻辑很复杂，需要多表更新、子查询或数据库专有语法；可使用 [编程式 API](../jdbc/about) 或 [Mapper 文件](../file/about)。
- 只是按主键做实体 CRUD；优先看 [BaseMapper](../mapper/about#base-mapper)。

## 操作模式

| 操作 | 映射 Map 模式 | 自由 Map 模式 |
| --- | --- | --- |
| 写入 | `lambda.insert(User.class).asMap()` | `lambda.insertFreedom("users")` |
| 更新 | `lambda.update(User.class).asMap()` | `lambda.updateFreedom("users")` |
| 删除 | `lambda.delete(User.class).asMap()` | `lambda.deleteFreedom("users")` |

```text title='字段处理差异'
映射 Map 模式
Map key -> 对象映射过滤和转换 -> 数据库列

自由 Map 模式
Map key -> 按列名处理 -> 数据库列
```

## 写入 Map

```java title='映射 Map 写入'
Map<String, Object> data = new HashMap<>();
data.put("id", 1001);
data.put("loginName", "alice");
data.put("email", "alice@example.com");

int rows = lambda.insert(User.class)
        .asMap()
        .applyMap(data)
        .executeSumResult();
```

```java title='自由 Map 写入'
Map<String, Object> data = new HashMap<>();
data.put("id", 1001);
data.put("login_name", "alice");
data.put("email", "alice@example.com");

int rows = lambda.insertFreedom("users")
        .applyMap(data)
        .executeSumResult();
```

映射 Map 模式会根据对象映射过滤和转换字段；自由 Map 模式直接按调用方提供的列名生成 SQL。

## 更新 Map

映射模式下，`updateToSample` 只更新样本中非空且可更新的字段。自由模式会处理传入的 key，包括 null 值；需要跳过 null 时应先过滤 Map。

```java title='样本式更新'
Map<String, Object> sample = new HashMap<>();
sample.put("loginName", "alice_new");
sample.put("email", null);

int rows = lambda.update(User.class)
        .asMap()
        .eq("id", 1001)
        .updateToSample(sample)
        .doUpdate();
```

`updateRow` 适合整行式更新。映射模式下，缺少的可更新属性会按 null 处理；自由模式只处理传入的 key。自由模式没有主键元数据，调用前需显式 `allowUpdateKey()`，并自行保证 Map 不包含不应修改的主键。

```java title='整行式更新'
Map<String, Object> row = new HashMap<>();
row.put("login_name", "alice_new");
row.put("email", "alice_new@example.com");

int rows = lambda.updateFreedom("users")
        .eq("id", 1001)
        .allowUpdateKey()
        .updateRow(row)
        .doUpdate();
```

## 选择更新方法

| 方法 | 语义 | 常见用途 |
| --- | --- | --- |
| `updateToSample(map)` | 样本式更新，跳过不参与更新的字段 | 局部更新、按表单提交内容更新 |
| `updateRow(map)` | 行数据更新，按行对象生成更新字段 | 同步整行数据、覆盖式更新 |
| `updateTo("field", value)` | 明确指定单个字段 | 少量字段、条件更新 |

没有任何更新字段时会报 `there nothing to update.`。没有 WHERE 的更新默认会被拦截，需要显式 `allowEmptyWhere()`。

## 删除 Map

Map 删除不需要提供行数据，只需要构造删除条件。

```java title='映射 Map 删除'
int rows = lambda.delete(User.class)
        .asMap()
        .eq("id", 1001)
        .doDelete();
```

```java title='自由 Map 删除'
int rows = lambda.deleteFreedom("users")
        .eq("id", 1001)
        .doDelete();
```

没有 WHERE 的删除默认会被拦截，需要显式 `allowEmptyWhere()`。

## 批量写入

批量写入适合导入任务和同步任务。

```java title='批量写入'
List<Map<String, Object>> rows = Arrays.asList(row1, row2, row3);

int total = lambda.insert(User.class)
        .asMap()
        .applyMap(rows)
        .executeSumResult();
```

批量数据中的字段集合应保持一致。映射 Map 模式会按对象映射过滤字段；自由 Map 模式会按 Map key 生成列。

## 深入阅读

- [映射 Map 模式](./mapped) — 复用对象映射的 Map 操作。
- [自由 Map 模式](./freedom) — 不依赖对象映射的 Map 操作。
- [构造器 API Update](../lambda/update) — Entity 模式下的更新语义。
