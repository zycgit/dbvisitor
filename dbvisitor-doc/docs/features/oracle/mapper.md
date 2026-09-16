---
id: mapper
sidebar_position: 20
title: Mapper API
---

## 主键策略 {#keys}

支持手工赋值、IDENTITY、序列和复合主键。方法注解回填 IDENTITY 时设置 `useGeneratedKeys=true`、`keyProperty` 和 `keyColumn`；使用序列时可通过 `selectKey` 先取值。

不要把 `RETURNING ... INTO` 当作当前结果集回填，它使用 OUT 参数。配置与示例见[主键生成](generated-keys.mdx)。

## 分页查询 {#pagination}

方法注解、BaseMapper 和按 statement ID 调用文件 Mapper 均可使用分页。页码从 0 开始；分页时分别查询总数和当前页。分页语句及注意事项见[分页查询](pagination.mdx)。
