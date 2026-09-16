---
id: results
sidebar_position: 9
title: 9. 结果接收
hide_table_of_contents: true
---

import CapabilityTable from '@site/src/components/CapabilityTable';
import matrix from '@site/src/data/capabilities/tables/results.json';

通用用法见核心 API 的[9. 结果接收](../../guides/result/about)，状态链接说明各数据源的具体差异。

<CapabilityTable matrix={matrix} showCounts={false} />

- API 入口：编程式 API、Mapper API（方法注解）、构造器 API、Mapper 文件。
- 部分支持：点击状态查看该数据源哪些入口可用。查询入口不可用，不代表接口无法处理该数据源的结果。
- 结果映射：使用 `RowMapper` 将每行数据映射为对象。
- 逐行处理：使用 `RowCallbackHandler` 接收每行数据。
- 结果集提取：使用 `ResultSetExtractor` 处理整个结果集；[内置提取器](../../guides/result/for_extractor#inner)还可完成过滤和 Pairs 等操作。
