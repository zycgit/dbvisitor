---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: 接收结果
description: 在 dbVisitor 中通常基于对象映射查询数据，但在一些特殊需求情况下可以通过实现特定接口来决定数据如何获取。
---

在 dbVisitor 中通常基于对象映射查询数据，但在一些特殊需求情况下可以通过实现特定接口来决定数据如何获取。

## 使用指引

- 通过 [ResultSetExtractor](./for_extractor) 接口，自定义 ResultSet 结果集如何读取。
- 通过 [RowMapper](./for_mapper) 接口，将结果集的一行记录进行转换。
- 使用 [List/Map 结构](./for_map) 来接收数据。
- 通过 [RowCallbackHandler](./row_callback) 接口，以回调的方式处理每一行数据。
