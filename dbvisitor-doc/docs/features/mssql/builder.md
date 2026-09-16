---
id: builder
sidebar_position: 30
title: 构造器 API
---

## 写入冲突 {#conflicts}

`Ignore` 和 `Update` 使用 `MERGE`，按映射的主键列判断匹配。前者只插入新记录，后者还会更新已有记录。示例见[插入冲突](conflict.mdx)。

## 分页查询 {#pagination}

构造器使用 `ROW_NUMBER() OVER(ORDER BY ...)` 编号并筛选当前页，总数单独查询。未指定排序时使用兜底表达式，不保证翻页顺序；应明确设置稳定排序。

示例见[分页查询](pagination.mdx)。

## 排序 {#ordering}

支持升降序、多字段排序和空值排序。每次调用都是追加排序条件，不是覆盖前一次配置：

```java
// 按年龄降序，年龄相同时按 ID 升序。
lambda.query(UserInfo.class)
        .desc("age").asc("id")
        .queryForList();
```

不要写 `asc("age").desc("age")`：SQL Server 会拒绝重复的排序字段。需要改变方向时，修改原来的排序调用，只保留一次。
