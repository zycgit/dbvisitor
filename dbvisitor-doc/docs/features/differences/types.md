---
id: types
sidebar_position: 8
title: 8. 类型处理器
hide_table_of_contents: true
---

import CapabilityTable from '@site/src/components/CapabilityTable';
import matrix from '@site/src/data/capabilities/tables/types.json';

通用用法见核心 API 的[8. 类型处理器](../../guides/types/about)，状态链接说明各数据源的具体差异。

<CapabilityTable matrix={matrix} showCounts={false} />

- 基础类型：数字、布尔、字符及其空值处理。
- 时间类型：日期、时间、时区、年月、精度及空值处理。
- 自定义处理器：通过字段配置的处理器完成实体写入和读取。
- JSON 序列化：Map、List、Set、Bean 及空值转换。
- 数组类型：数组绑定、读取、修改及空值往返。

向量类型映射与检索的支持情况见[向量操作](./vectors)。
