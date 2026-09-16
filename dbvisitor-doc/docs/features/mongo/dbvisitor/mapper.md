---
id: mapper
slug: /features/mongo/mapper
sidebar_position: 20
title: Mapper API
---

## 方法注解 {#annotations}

注解内容使用 MongoDB 命令，不写关系型 SQL。查询和写入示例见[查询操作](query.mdx#query-apis)与[数据写入](write.mdx#write-apis)。

查询不存在的集合返回空结果，不会产生“表不存在”异常。需要判断集合是否存在时，应单独查询集合信息；不能通过捕获该异常判断。

## 主键策略 {#keys}

可自行提供 `_id`，或接收插入生成的 `_id`。方法注解保持默认生成键来源，不配置 `generatedKeySource="resultSet"`：插入命令不返回 SQL RETURNING 式的结果集。

字段映射与回填示例见[主键生成](generated-keys.mdx#key-source)。

## 分页查询 {#pagination}

方法注解、BaseMapper 和文件调用均支持分页；驱动使用 MongoDB 的查询与计数命令。用法及 skip / limit 行为见[分页查询](pagination.mdx)。

## 执行选项 {#options}

使用默认或 `FORWARD_ONLY` 结果集。驱动不支持 `SCROLL_INSENSITIVE`、`SCROLL_SENSITIVE`，配置滚动类型后不能倒退或定位到任意行。

需要重新读取时，再次查询或先将结果接收到 List。参数位置见核心 API 的[执行选项](../../../guides/core/mapper/annotation_query.mdx#options)。
