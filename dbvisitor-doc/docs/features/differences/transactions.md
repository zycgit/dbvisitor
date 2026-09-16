---
id: transactions
sidebar_position: 10
title: 10. 数据库事务
hide_table_of_contents: true
---

import CapabilityTable from '@site/src/components/CapabilityTable';
import matrix from '@site/src/data/capabilities/tables/transactions.json';

通用用法见核心 API 的[10. 数据库事务](../../guides/transaction/about)，状态链接说明各数据源的具体差异。

<CapabilityTable matrix={matrix} />

- 跨 API 事务：JdbcTemplate、Session 与构造器 API 共享同一个事务。
- 事务传播：`REQUIRED`、`REQUIRES_NEW`、`NESTED`、`SUPPORTS`、`NOT_SUPPORTED`、`MANDATORY`、`NEVER`，共 7 种。
- 传播计数：按完整支持的属性计数；全部相关场景通过才计入分子。悬停查看各属性状态，部分支持的限制见状态链接。
- 有限：部分调用可用，但受 JDBC 或数据库事务能力限制；具体可用行为及限制见对应数据源说明。
- 隔离级别：表示能否通过 dbVisitor 设置该数据源的事务隔离级别，不表示所有数据库接受相同的值；具体取值与行为见状态链接。
