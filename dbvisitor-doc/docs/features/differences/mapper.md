---
id: mapper
sidebar_position: 2
title: 2. Mapper API
hide_table_of_contents: true
---

import CapabilityTable from '@site/src/components/CapabilityTable';
import matrix from '@site/src/data/capabilities/tables/mapper.json';

通用用法见核心 API 的[5.2 Mapper API](../../guides/core/mapper/about)；共用能力见[参数传递与规则](./parameters.md)和[结果接收](./results.md)，表格中的状态链接说明各数据源的具体差异。

<CapabilityTable matrix={matrix} showCounts={false} />

- 方法注解：查询、写入、`@Execute`、多行命令拼接及异常处理。
- Mapper 读写：BaseMapper 增删改查、替换、upsert、样本查询及 Map 参数写入。
- 主键策略：手工赋值、默认主键、生成键回填、`selectKey` 及复合主键。
- 分页查询：方法注解、BaseMapper 和文件调用的分页。
- 执行选项：执行方式、超时、取数设置及结果集类型，包括滚动结果。
- 调用构造器：通过 Mapper 或 Session 获取并使用构造器。
- 引用文件 Mapper：按 statement ID 查询、写入及读取返回值。
- Session 管理：创建 Mapper、复用 Session 及跨 API 访问数据。
