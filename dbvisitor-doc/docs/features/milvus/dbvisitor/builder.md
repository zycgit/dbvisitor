---
id: builder
slug: /features/milvus/builder
sidebar_position: 30
title: 构造器 API
---

## 写入操作 {#writes}

新增使用 Insert，更新使用原生 Partial Update；不再读回完整实体后覆盖未修改的字段。多页操作不保证整体提交或回滚。

删除已知主键时，返回条数可能包含不存在的主键，不能单靠返回值判断记录是否存在。按条件或向量选择后删除的行为见[数据写入](write.mdx#delete-count)。

分批删除时，每次取剩余数据的第一页并删除这些主键；不需要按普通字段排序，也不要一边删除一边递增页码。

## 写入冲突 {#conflicts}

| 策略 | Milvus 行为 |
| --- | --- |
| 默认 / `Into` | 普通 Insert，不保证以重复主键异常阻止写入 |
| `Ignore` | 不支持，没有原子“仅在不存在时插入”的操作 |
| `Update` | Partial Upsert，更新已有实体的指定字段或插入新实体 |

配置见[数据写入](write.mdx#insert-conflict)。不要用普通 Insert 的异常代替重复主键检查。

## 查询操作 {#queries}

支持字段选择、实体和 Map 结果、标量读取及 `COUNT(*)`。不支持计算投影、`DISTINCT` 和通用 SQL 聚合。

`queryForObject()` 返回第一条匹配记录；没有普通字段排序保证时，不要把它当作“主键最小的一条”。需要确定记录时使用主键条件。示例见[查询操作](query.mdx#query-apis)。

## Map 查询模式 {#map-mode}

映射 Map 使用 Java 属性名，自由 Map 使用集合字段名。例如属性 `wordCount` 映射到 `word_count`，两种模式应分别使用相应名称。

实体和字段映射见[查询操作](query.mdx#entity-mapping)。Map 模式不会增加普通字段排序、计算列或分组能力。

## 分页查询 {#pagination}

普通查询使用 `LIMIT / OFFSET`，总数单独查询；驱动按需读取数据。分页迭代沿用分页调用，`fetchSize` 控制驱动分批读取的大小，不改变页码和总条数。

普通字段不能用于排序。向量查询按距离排序，其分页方式见[分页查询](pagination.mdx)。

## 条件构造器 {#predicates}

比较、区间、IN、NULL 和 AND/OR 条件可用。包含 null 的集合应先移除 null，再使用 `isNull` 或 `isNotNull` 明确表达空值条件。

Milvus 2.6.2 暂不支持以下参数化条件：

- LIKE 模式参数，包括 `like`、`likeLeft`、`likeRight` 及其取反形式。
- 整数参数比较外层的 NOT、重复 NOT，以及部分半开区间取反。单值排除可用 `ne`，区间外条件可用两侧的 OR 比较。

驱动不会改为拼接字符串来绕过参数绑定。

普通条件示例：

```java
lambda.query(BookVector.class)
        .ge(BookVector::getWordCount, 1000)
        .isNotNull(BookVector::getTitle)
        .queryForList();
```

## 条件参数 {#parameter-values}

比较、集合、区间及 `apply` 的值通过 SDK 参数绑定传递；字符串中的引号无需自行拼接或转义。LIKE 模式参数暂不支持，范围见[条件构造器](#predicates)。

不要把其他数据库的 SQL 片段原样放入 `apply`，例如用 `1=1` 拼动态条件。可选条件使用 API 的布尔开关：

```java
lambda.query(BookVector.class)
        .eq(title != null, BookVector::getTitle, title)
        .queryForList();
```

`apply` 中的表达式必须符合 [Milvus 表达式语法](../basics/operators.md)。

## 分组 {#grouping}

当前方言不支持构造器的 `groupBy` 和分组聚合。普通计数使用 `queryForCount()`；向量检索的分组搜索是另一项能力，不能代替 SQL 分组聚合。

## 排序 {#ordering}

当前方言不支持普通字段的 `asc`、`desc`、`orderBy`，也不支持其空值排序策略。向量距离排序使用 `orderByL2`、`orderByCosine` 或 `orderByIP`，见[向量操作](vectors.mdx)。
