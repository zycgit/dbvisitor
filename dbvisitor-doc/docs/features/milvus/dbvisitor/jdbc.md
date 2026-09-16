---
id: jdbc
slug: /features/milvus/programmatic
sidebar_position: 10
title: 编程式 API
---

## 更新 {#updates}

`executeUpdate` 执行 Milvus SQL 的 INSERT、UPDATE、DELETE。修改指定字段使用 Partial Upsert；需要先选取记录的修改和删除会分页执行。返回条数不能总用于判断记录原先是否存在，见[数据写入](write.mdx#write-behavior)。

## 查询 {#queries}

SELECT 通过 Milvus 查询或搜索接口执行，结果可接收为实体、Map、单值或列表。向量排序和字段映射见[查询操作](query.mdx)。

## 批量化 {#batch}

`executeBatch` 逐条执行，不使用 JDBC batch，也不提供整体回滚。错误命令会报告异常，但 INSERT 不保证对重复主键报错，不能靠异常检测重复数据。写入策略见[插入冲突](write.mdx#insert-conflict)。

## 存储过程与函数 {#routines}

不支持 SQL 存储过程、标量函数或表函数调用。BM25 等集合 Function 用于字段计算，不是 `call` 可调用的存储例程，见[函数定义](../ddl/functions.md)。
