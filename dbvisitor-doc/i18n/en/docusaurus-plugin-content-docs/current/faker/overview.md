---
id: overview
sidebar_position: 1
title: 介绍
description: faker 是一款测试数据生成工具，它的目标是提供针对特定数据库随机生成 insert、update、delete 语句。并且灵活控制各种随机因子。
---
# 介绍

是一款测试数据生成工具，它的目标是提供针对特定数据库随机生成 insert、update、delete 语句。并且灵活控制各种随机因子，比如：随机事务、随机 where、随机 set、随机 insert 等等。

## 目标
- 数据库的全类型支持
- 数据类型的边界值涵盖
- 使用简单方便
- 高效率
- 灵活可扩展

## 功能特性
语句类型
- 支持 Insert、Update、Delete 语句的生成
- 支持 各类语句随机占比 配置
- 支持 生成的语句表名/列明 是否携带限定符

事务
- 支持 生成随机事务
- 支持 单个事务中随机 DML 语句数控制
- 支持 事物间写入等待延迟 控制

执行器
- 支持 多线程并发写入
- 支持 多线程并发随机产生 DML 语句
- 支持 多线程模式下总体写入 QPS 限流
- 支持 设置超时时间
- 支持 特定报错信息忽略