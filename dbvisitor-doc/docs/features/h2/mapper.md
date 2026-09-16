---
id: mapper
sidebar_position: 20
title: Mapper API
---

## 主键策略 {#keys}

支持手工赋值、IDENTITY 主键回填和复合主键。方法注解通过 `useGeneratedKeys`、`keyProperty` 配置回填；BaseMapper 使用实体的主键配置。

默认从 JDBC 生成键读取。只有写入语句本身返回所需列的结果集时，才使用 `generatedKeySource="resultSet"`。配置示例见[主键生成](generated-keys.mdx)。

## 分页查询 {#pagination}

方法注解、BaseMapper 和按 statement ID 调用文件 Mapper 均可使用分页。页码从 0 开始；分页时分别查询总数和当前页。分页语句及注意事项见[分页查询](pagination.mdx)。
