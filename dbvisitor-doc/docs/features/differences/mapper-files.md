---
id: mapper-files
sidebar_position: 4
title: 4. Mapper 文件
hide_table_of_contents: true
---

import CapabilityTable from '@site/src/components/CapabilityTable';
import matrix from '@site/src/data/capabilities/tables/mapper-files.json';

通用用法见核心 API 的[5.4 Mapper 文件](../../guides/core/file/about)，状态链接说明各数据源的具体差异。共用参数能力集中见[参数传递与规则](./parameters.md)。

<CapabilityTable matrix={matrix} showCounts={false} />

- 命令执行：加载文件、查找语句并执行原生命令，包含连续调用和未命中时的返回值。
- 执行选项：`statementType`、`fetchSize`、`timeout`、`resultSetType` 及滚动结果。
- 主键策略：手工赋值、生成键回填及 `selectKey` 查询回填。
- 存储过程调用：输入、输出、INOUT 与游标参数。
- sql 标签：通过 `<sql>`、`<include>` 复用命令片段。
- 动态 SQL：通过条件、循环等标签生成命令。
- 映射结果集：`resultType`、`resultMap` 对实体、Map 和标量的映射；自定义接收接口见[结果接收](./results.md)。
- 分页查询：获取页数据与总数。
- 调用文件 Mapper：Java 接口通过 `@RefMapper` 调用文件中的命令、动态内容和结果映射。
