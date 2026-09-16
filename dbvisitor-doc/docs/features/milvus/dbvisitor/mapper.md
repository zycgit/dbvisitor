---
id: mapper
slug: /features/milvus/mapper
sidebar_position: 20
title: Mapper API
---

## 方法注解 {#annotations}

注解使用驱动的 Milvus SQL，支持实体、Map、标量和列表结果，以及 `COUNT(*)`。不支持 `DISTINCT` 和通用 SQL 聚合投影。示例见[查询操作](query.mdx#query-apis)。

方法注解可以写入和删除数据，但普通 INSERT 不以重复主键异常阻止写入。删除已知主键时，返回条数可能包括不存在的 ID，不能仅凭返回值判断是否删除了记录。

## Mapper 读写 {#operations}

BaseMapper 支持新增、按主键及样本查询、更新、替换、upsert 和 Map 参数写入。更新使用 Partial Update；未修改字段不会被完整实体覆盖。

普通 INSERT 及多条 INSERT 不保证以重复主键异常拒绝数据。删除不存在的主键也不保证返回 0。需要确认存在性时单独查询；重复写入与删除计数见[数据写入](write.mdx#delete-count)。

## 主键策略 {#keys}

可向关闭 AutoID 的集合写入已有 ID，或回填 AutoID 生成的主键。方法注解使用 `useGeneratedKeys` 和默认回填来源，不使用 `generatedKeySource="resultSet"`。

Milvus 只有一个数据库主键字段，不支持数据库复合主键约束；实体中组合多个条件不等于建立复合唯一约束。也不提供供 `selectKey` 取值的数据库序列。配置见[主键生成](generated-keys.mdx)。

## 分页查询 {#pagination}

方法注解、BaseMapper 和文件调用均支持分页。普通字段不能用于排序；需要有序分页时使用向量距离排序。查询总数与向量窗口限制见[分页查询](pagination.mdx)。

## 执行选项 {#options}

使用默认或 `FORWARD_ONLY` 结果集。不支持 `SCROLL_INSENSITIVE`、`SCROLL_SENSITIVE`；需要反复读取时重新查询，或先接收到 List。

`fetchSize` 控制分批读取大小，不代表分页大小或总条数限制。参数位置见核心 API 的[执行选项](../../../guides/core/mapper/annotation_query.mdx#options)。

## 引用文件 Mapper {#file-mapper}

按 statement ID 调用文件中的 Milvus SQL，可查询、写入并读取返回值。DELETE 仍沿用驱动的计数规则：删除已知但不存在的主键时，返回值不保证为 0。用法见[数据写入](write.mdx#delete-count)。
