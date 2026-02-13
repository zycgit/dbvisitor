---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: 数据库事务
description: dbVisitor 内置了一个轻量化本地事务管理器，支持完整的事务传播行为和隔离级别。
---

dbVisitor 内置了一个轻量化本地事务管理器，支持完整的 7 种事务传播行为和 5 种隔离级别。
您可以通过三种方式控制事务，具体选择取决于项目需求。

## 使用指引

原理
- [本地事务管理器](./manager/about)，了解 dbVisitor 事务管理器的工作原理和事务栈机制。
- [传播行为](./propagation)，了解 7 种传播行为的区别与适用场景。
- [隔离级别](./isolation)，了解不同隔离级别对并发读写的影响。

使用方式
- [编程式事务](./manager/program)，手动调用 `begin/commit/rollBack` 控制事务，灵活但代码较多。
- [模版事务](./manager/template)，通过 `TransactionTemplate` 执行事务代码块，自动处理提交/回滚。
- [注解式事务](./manager/annotation)，通过 `@Transactional` 注解声明式控制事务，代码最简洁。
