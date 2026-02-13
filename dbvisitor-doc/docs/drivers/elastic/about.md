---
id: about
sidebar_position: 1
title: 简介
description: jdbc-elastic 是一个 ElasticSearch 的 JDBC 驱动适配器，它允许开发者使用标准的 JDBC 接口和 ElasticSearch 原生 REST API 风格的命令来操作 ElasticSearch。
---

jdbc-elastic 是一个 ElasticSearch 的 JDBC 驱动适配器，它允许开发者使用标准的 JDBC 接口和 ElasticSearch 原生 REST API 风格的命令来操作 ElasticSearch。
目的是通过熟悉 JDBC 编程模型，使开发者能够无缝地集成和使用 ElasticSearch。

## 核心特性

- **原生命令支持**：支持 ElasticSearch 的常用 REST API 命令，包括 `GET`, `POST`, `PUT`, `DELETE`, `HEAD`。
- **JDBC 标准接口**：支持 `Connection`, `Statement`, `PreparedStatement`, `ResultSet` 等标准接口。
- **参数占位符**：支持在 URL 路径和 JSON Body 中使用 `?` 占位符，并使用 `PreparedStatement` 设置参数。
- **结果集映射**：自动将 ElasticSearch 的 JSON 响应映射为 JDBC `ResultSet`。
- **自动主键返回**：支持 `Statement.RETURN_GENERATED_KEYS`，在执行插入操作时自动返回生成的 `_id`。
- **多命令支持**：支持 `_mget`, `_msearch` 等批量操作。
- **索引管理**：支持索引的创建、删除、Mapping 设置、Settings 设置、别名管理等。
- **预读优化**：支持大结果集的预读配置，优化读取性能。
- **多版本兼容**：无需调整依赖，同时兼容 ES6/ES7/ES8/ES9。

## 架构设计

jdbc-elastic 内部通过解析 QueryDSL 命令，将其转换为底层的 REST 请求，并使用 ElasticSearch 官方 REST Client 进行通信。

## 适用场景

- 需要在 Java 项目中以统一的方式（JDBC）访问 ElasticSearch。
- 希望使用原生 REST API 风格操作 ElasticSearch，但又想利用 JDBC 的便利性。
- 需要将 ElasticSearch 集成到现有的基于 JDBC 的数据处理流程中。
