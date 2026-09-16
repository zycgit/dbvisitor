---
id: jdbc
sidebar_position: 1
title: 1. 编程式 API
hide_table_of_contents: true
---

import CapabilityTable from '@site/src/components/CapabilityTable';
import matrix from '@site/src/data/capabilities/tables/jdbc.json';

通用用法见核心 API 的[5.1 编程式 API](../../guides/core/jdbc/about)；共用能力见[参数传递与规则](./parameters.md)和[结果接收](./results.md)，表格中的状态链接说明各数据源的具体差异。

<CapabilityTable matrix={matrix} showCounts={false} />

- 更新：执行新增、修改、删除命令并读取影响条数；SQL 和数据库原生命令均可使用。
- 查询：读取实体、Map、单值、列表和计数，包括列名匹配与连续查询。
- 查询键值对：`queryForPairs` 将前两列读取为 Map 的键和值。
- 批量化：多条写入、空批次、较大批次及异常处理；不保证使用 JDBC batch，也不保证原子性。
- 多结果：`multipleExecute` 与 `call` 收集命令返回的结果集，包括结果命名和映射；支持 `call` 读取结果不代表支持数据库存储过程。
- 存储过程与函数：传入参数并读取标量、记录、表或游标；各数据源可用的调用方式见状态链接。
