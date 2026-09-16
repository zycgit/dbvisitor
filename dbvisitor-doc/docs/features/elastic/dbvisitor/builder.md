---
id: builder
slug: /features/elastic/builder
sidebar_position: 30
title: 构造器 API
---

## 查询操作 {#queries}

构造器生成 Elasticsearch Query DSL。计算列或聚合通过 `applySelect` 传入 DSL，而不是 SQL 函数。例如统计匹配记录数：

```java
lambda.query(UserInfo.class)
        .applySelect("{\"aggs\":{\"count\":{\"filter\":{\"match_all\":{}}}}}")
        .queryForMapList();
```

字段选择及结果读取见[查询操作](query.mdx)。

## 分页查询 {#pagination}

普通查询使用 `from`、`size`；总数使用独立计数请求。构造器不会自动将深分页改为 Scroll 或 `search_after`。

分组查询使用 composite 聚合并通过 `after_key` 翻页。分页范围、总数含义与用法见[分页查询](pagination.mdx)。

## 条件构造器 {#predicates}

`eq` 生成的匹配条件仍受字段 mapping 影响。保存需要精确匹配的业务标识时使用 `keyword`，不要将分词的 `text` 字段当成普通字符串等值比较。

`NOT IN` 中的 null 不具有 SQL 三值逻辑。需要排除指定年龄及空值时，移除输入集合中的 null，并明确使用 `isNotNull`：

```java
lambda.query(UserInfo.class)
        .notIn(UserInfo::getAge, List.of(20))
        .isNotNull(UserInfo::getAge)
        .queryForList();
```

## 条件参数 {#parameter-values}

`text` 和 `keyword` 没有 `VARCHAR(100)` 那样的写入长度约束。长字符串会完整保存到 `_source`，构造器不会截断或校验长度。

`ignore_above` 影响索引，不等于拒绝写入。业务需要限制长度时，在写入前校验。

## 分组 {#grouping}

`groupBy` 对应 Elasticsearch 聚合，`applySelect` 中也必须使用聚合 DSL，不能写 SQL 的 `SUM(age)`。例如按年龄分组计数：

```java
lambda.query(UserInfo.class)
        .applySelect("{\"aggs\":{\"cnt\":{\"filter\":{\"match_all\":{}}}}}")
        .groupBy("age")
        .queryForMapList();
```

分组字段应可用于聚合，例如数值字段或 `keyword` 字段。
