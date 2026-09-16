---
id: builder
slug: /features/mongo/builder
sidebar_position: 30
title: 构造器 API
---

## 写入冲突 {#conflicts}

`Ignore` 和 `Update` 使用 MongoDB 的 upsert，按实体映射的主键字段定位文档。只加 `@Column(primary = true)` 不会创建唯一索引；需要防止重复时，为该字段建立唯一索引。

用法及写入条数见[数据写入](write.mdx#insert-conflict)。

## 查询操作 {#queries}

普通构造器查询生成 `find`。计算列和聚合表达式使用 MongoDB 表达式，而不是 SQL 函数，例如分组计数使用下面的 `$sum`，不是 `COUNT(*)`。

## 分页查询 {#pagination}

普通查询使用 `skip`、`limit`；总数单独查询。分页迭代重复读取各页，稳定翻页需要按唯一字段排序。自定义聚合管道的分页用法见[分页查询](pagination.mdx)。

## 条件构造器 {#predicates}

不要把 `notIn` 中的 null 当作 SQL 的三值逻辑条件。需要排除指定年龄及空值时，先移除输入集合中的 null，再使用：

```java
lambda.query(UserInfo.class)
        .notIn(UserInfo::getAge, List.of(20))
        .isNotNull(UserInfo::getAge)
        .queryForList();
```

## 分组 {#grouping}

`groupBy` 生成聚合管道的 `$group`。通过 `applySelect` 传入 MongoDB 聚合表达式：

```java
List<Map<String, Object>> rows = lambda.query(UserInfo.class)
        .applySelect("{cnt: {$sum: 1}}")
        .groupBy("age").orderBy("age")
        .queryForMapList();
```

结果包含 `age` 和 `cnt`。完整管道与投影的用法见[查询操作](query.mdx#aggregation)。

## 排序 {#ordering}

支持升降序和多字段排序；MongoDB 方言尚不支持 `OrderNullsStrategy.FIRST`、`LAST`。默认排序遵循 MongoDB 的空值顺序，不应将它当作显式空值排序策略。

需要指定空值位置时，使用聚合管道增加排序辅助字段，再执行 `$sort`。
