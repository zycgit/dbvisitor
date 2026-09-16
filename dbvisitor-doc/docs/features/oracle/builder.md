---
id: builder
sidebar_position: 30
title: 构造器 API
---

## 写入冲突 {#conflicts}

`Ignore` 和 `Update` 使用 `MERGE`，按实体映射的主键列匹配。前者只插入新记录，后者还会更新已有记录。

主键映射应与实际表约束一致。示例见[插入冲突](conflict.mdx)。

## 分页查询 {#pagination}

构造器通过嵌套查询和 `ROWNUM` 截取当前页；总数单独查询。分页迭代逐页执行此查询，不是一次加载全部数据。

用法与生成的 SQL 见[分页查询](pagination.mdx)。

## 条件构造器 {#predicates}

支持比较、区间、集合、LIKE、NULL 和组合条件。下面两种情况需要按 Oracle 的规则使用。

### 查询空字符串

Oracle 将字符空字符串存为 NULL。查询这些记录使用 `isNull`：

```java
lambda.query(UserInfo.class)
        .isNull(UserInfo::getName)
        .queryForList();
```

`eq(UserInfo::getName, "")` 仍然是等值条件，不会变成 `IS NULL`，也不会查出这些记录。

### 查询大量 ID

构造器不自动拆分 `IN` 列表。超过所用 Oracle 版本的单条列表限制时，由调用方分批查询：

```java
List<UserInfo> rows = new ArrayList<>();
for (int start = 0; start < ids.size(); start += 500) {
    List<Integer> batch = ids.subList(start, Math.min(start + 500, ids.size()));
    rows.addAll(lambda.query(UserInfo.class)
            .in(UserInfo::getId, batch)
            .queryForList());
}
```

这里的 500 是应用选择的每批数量，不是驱动自动分页。输入 ID 应去重，避免跨批重复收集同一条记录。
