---
id: builder
sidebar_position: 30
title: 构造器 API
---

## 写入操作 {#writes}

构造器支持新增、更新和删除，但 JDBC 返回的影响条数不保证等于实际匹配条数。不能用 `doUpdate() == 0` 或 `doDelete() == 0` 判断记录不存在。

需要确认最终结果时，等待写入完成后按条件查询。Mutation 的等待方式见[数据写入](write.mdx#affected-rows)。

## 写入冲突 {#conflicts}

ClickHouse 方言不支持 `Ignore`、`Update` 插入冲突策略。普通 MergeTree 的主键用于排序，不阻止重复键写入。

不要用更换策略的方式去重；表引擎和去重方案见[插入冲突](conflict.mdx)。

## 分页查询 {#pagination}

构造器使用 `LIMIT offset, size` 截取当前页，总数单独查询。分页迭代逐页执行查询，需要稳定顺序时按唯一字段排序。示例见[分页查询](pagination.mdx)。

## 条件构造器 {#predicates}

集合条件可用，但不要依赖 `NOT IN (..., NULL)` 表达“排除这些值及空值”。请移除输入集合中的 null，再明确排除空字段：

```java
lambda.query(UserInfo.class)
        .notIn(UserInfo::getAge, List.of(20))
        .isNotNull(UserInfo::getAge)
        .queryForList();
```

这会查询年龄非空且不等于 20 的记录。

## 条件参数 {#parameter-values}

`String` 没有 `VARCHAR(100)` 那样的字符长度约束。绑定超过 100 字符的字符串不会因此报错，构造器也不会截断它。

如果业务字段有长度上限，在调用写入 API 前校验字符串长度。
