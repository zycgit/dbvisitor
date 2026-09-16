---
id: vectors
sidebar_position: 5
title: 5. 向量操作
hide_table_of_contents: true
---

import CapabilityTable from '@site/src/components/CapabilityTable';
import matrix from '@site/src/data/capabilities/tables/vectors.json';

通用用法见核心 API 的[5.6 向量查询](../../guides/core/vector_query/about)，状态链接说明各数据源的具体差异。

<CapabilityTable matrix={matrix} showCounts={false} />

- 向量类型映射：向量字段的映射与读写，配置见[8.9 向量类型处理器](../../guides/types/vector-handler)。
- 向量检索：近邻排序、范围过滤及向量与标量组合条件；支持向量读写不等于支持构造器向量检索。
