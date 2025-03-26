---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: 数据库事务
description: dbVisitor 内置了一个轻量化本地事务管理器，它支持完整的事务传播行为和隔离级别。
---

dbVisitor 内置了一个轻量化本地事务管理器，它支持完整的事务传播行为和隔离级别。
您可以通过三种形式控制事务，具体如何选择取决于您具体情况。

## 使用指引

原理
- [本地事务管理器](./manager/about)，了解 dbVisitor 的事务管理器工作原理。
- [传播行为](./propagation)，了解传播行为的基本概念。
- [隔离级别](./isolation)，了解隔离级别的影响。

使用方式
- [编程方式](./manager/program)，使用编程方式手动控制事务
- [模版模式](./manager/template)，使用固定模版方法执行事务代码块。
- [注解方式](./manager/annotation)，使用注解来对方法中用到的数据源进行事务控制。
