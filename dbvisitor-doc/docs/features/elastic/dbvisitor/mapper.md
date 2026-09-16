---
id: mapper
slug: /features/elastic/mapper
sidebar_position: 20
title: Mapper API
---

## 方法注解 {#annotations}

注解内容使用 Elasticsearch 的请求方法、路径和 JSON 请求体，SQL 与这些请求在 Mapper 中都是待执行命令。示例见[查询操作](query.mdx#query-apis)与[数据写入](write.mdx#write-apis)。

## Mapper 读写 {#operations}

BaseMapper 根据实体映射生成索引读写请求，支持按主键操作、样本查询及 Map 参数写入。

Elasticsearch 的文本字段没有 `VARCHAR(n)` 式长度限制。例如，Java 属性上的 JDBC 类型不能要求索引拒绝超长文本；业务有长度要求时应在写入前校验。其它写入差异见[数据写入](write.mdx#write-apis)。

## 主键策略 {#keys}

可提供 `_id` 或接收服务端生成的 `_id`。方法注解通过生成键回填，保持默认回填来源，不使用 `generatedKeySource="resultSet"`。配置见[主键生成](generated-keys.mdx#key-source)。

## 分页查询 {#pagination}

方法注解、BaseMapper 和文件调用均支持分页，按请求中的 from / size 获取当前页。总数与深分页的处理见[分页查询](pagination.mdx)。

## 执行选项 {#options}

使用默认或 `FORWARD_ONLY` 结果集。不支持 JDBC 的 `SCROLL_INSENSITIVE`、`SCROLL_SENSITIVE`；Elasticsearch 的 scroll 搜索也不等于可滚动 JDBC 结果集。

需要重复读取时重新查询，或先接收到 List。参数位置见核心 API 的[执行选项](../../../guides/core/mapper/annotation_query.mdx#options)。
