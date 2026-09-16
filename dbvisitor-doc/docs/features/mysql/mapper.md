---
id: mapper
sidebar_position: 20
title: Mapper API
---

## 主键策略 {#keys}

支持手工赋值、AUTO_INCREMENT 主键回填和复合主键。方法注解启用 `useGeneratedKeys`，并指定 `keyProperty`；保持默认回填来源，不使用 `generatedKeySource="resultSet"`。

配置示例见[主键生成](generated-keys.mdx#identity)。BaseMapper 使用实体的主键配置，方法注解使用注解自己的回填配置。

## 分页查询 {#pagination}

方法注解、BaseMapper 和按 statement ID 调用文件 Mapper 均可使用分页。页码从 0 开始；分页时分别查询总数和当前页。分页语句及注意事项见[分页查询](pagination.mdx)。
