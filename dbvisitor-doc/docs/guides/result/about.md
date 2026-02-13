---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: 接收结果
description: dbVisitor 提供了 RowMapper、ResultSetExtractor、RowCallbackHandler 等接口来自定义查询结果的处理方式。
---

dbVisitor 提供了多种方式来处理查询结果。默认情况下基于 ORM 映射即可完成大部分场景，
对于特殊需求可以通过以下接口自定义结果处理逻辑。

## 使用指引

- 通过 [RowMapper](./for_mapper) 接口，逐行映射结果集，每行返回一个对象。
- 使用 [List/Map 结构](./for_map) 接收数据，无需定义实体类。
- 通过 [RowCallbackHandler](./row_callback) 接口，逐行回调处理结果（不收集，适合流式场景）。
- 通过 [ResultSetExtractor](./for_extractor) 接口，完全控制 ResultSet 的遍历与转换。
