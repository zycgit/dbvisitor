---
id: jdbc
slug: /features/mysql/programmatic
sidebar_position: 10
title: 编程式 API
---

## 存储过程与函数 {#routines}

- 存储过程：支持 IN / OUT / INOUT 标量参数和直接返回的结果集；不使用 JDBC REF_CURSOR OUT 参数接收游标。
- 存储函数：返回单个值，不返回表，也不通过 OUT 参数返回多个字段。需要多字段结果时使用存储过程。

完整示例见[存储过程参数](procedures.md#parameters)和[读取函数值](procedures.md#functions)。
