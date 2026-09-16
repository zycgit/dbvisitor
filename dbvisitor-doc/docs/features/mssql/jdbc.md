---
id: jdbc
slug: /features/mssql/programmatic
sidebar_position: 10
title: 编程式 API
---

## 存储过程与函数 {#routines}

支持存储过程的输入、输出参数，以及标量函数和表值函数查询。不支持把游标声明为 JDBC REF_CURSOR OUT 参数来接收；需要返回记录时，让过程通过 SELECT 返回结果集。

参数配置及调用示例见[存储过程调用](procedures.md#parameters)。
