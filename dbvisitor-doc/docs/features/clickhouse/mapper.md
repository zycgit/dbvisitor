---
id: mapper
sidebar_position: 20
title: Mapper API
---

## 方法注解 {#annotations}

方法注解直接执行 ClickHouse SQL。命令错误会抛出异常，但重复主键不会因此抛错，UPDATE / DELETE 的返回值也不保证是实际影响条数。

例如，更新不存在的 ID 后，不能只用返回值是否为 0 判断记录是否存在；应查询目标记录。等待变更完成的方法见[等待数据变更完成](write.mdx#wait-for-writes)。

## Mapper 读写 {#operations}

BaseMapper 支持新增、查询、修改、删除、样本查询和 Map 参数写入，具体修改方式见[构造器 API](builder.md#writes)。

需要自行处理以下业务校验：

- 重复主键：MergeTree 的主键不强制唯一，不能依赖重复主键异常，包括多条插入。
- 字符串长度：`String` 不采用 `VARCHAR(n)` 的长度限制，长度要求应在写入前校验。
- 修改数量：驱动返回值不能当作实际命中条数；修改后按需回读数据。

## 主键策略 {#keys}

插入前由应用提供 ID，或先查询生成值再写入。ClickHouse 不提供这里的自增主键回填；不要依赖 `useGeneratedKeys` 或当前结果集回填生成键。示例见[主键生成](generated-keys.mdx)。

## 分页查询 {#pagination}

方法注解、BaseMapper 和按 statement ID 调用文件 Mapper 均可使用分页。页码从 0 开始；分页时分别查询总数和当前页。分页语句及注意事项见[分页查询](pagination.mdx)。
