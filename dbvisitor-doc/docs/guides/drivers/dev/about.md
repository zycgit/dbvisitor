---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: 11.4 自定义适配器
description: jdbc-redis 是一个 Redis 的 JDBC 驱动适配器，它允许开发者使用标准的 JDBC 接口和命令的方式来操作 Redis 数据。
---

通过 dbvisitor-driver，开发者可以更轻松地扩展数据源支持，使应用程序能够以统一的方式访问各种不同类型的数据库。它被设计用来适用于以下场景：
- 将非关系型数据库集成到基于 JDBC 的应用程序中。
- 为自定义数据源提供 JDBC 接口的项目
- 为 dbVisitor 框架提供统一的数据库操作接口，使应用程序能够以一致的方式访问不同类型的数据库。

## 架构设计

项目采用了面向接口的设计模式，主要包含以下核心组件：

1. 驱动器入口
   - `net.hasor.dbvisitor.driver.JdbcDriver`，实现 `java.sql.Driver` 接口，负责 URL 解析和创建支持的链接。
   - 驱动入口类规定所有适配器都遵循使用 `jdbc:dbvisitor:<adapterName>//<server?param=value>` 格式的 JDBC URL
2. 适配器管理器
   - `net.hasor.dbvisitor.driver.AdapterManager` 类负责管理和加载适配器工厂，使用 Java SPI 机制发现和注册适配器。
3. JDBC 标准实现层
   - 位于 `net.hasor.dbvisitor.driver.*` 包下所有 Jdbc 为开头的类，这些类实现了 JDBC 标准接口。
4. 适配器层
   - `net.hasor.dbvisitor.driver.AdapterFactory` 接口是适配器连接工厂，负责创建 AdapterConnection 适配器链接。
   - `net.hasor.dbvisitor.driver.AdapterConnection` 抽象类是适配器链接，用于处理适配器的请求和响应。
5. 类型支持
   - TypeSupport、AdapterTypeSupport：提供数据类型转换支持
   - ConvertUtils：工具类，处理各种数据类型的转换

## 技术特点
- SPI 机制：通过 Java SPI 机制动态加载适配器，实现松耦合的插件化架构。
- 类型转换：提供灵活的类型转换系统，支持各种数据类型的映射。
- 请求/响应模型：采用请求/响应模型处理数据库操作，便于适配各种数据源。
- 兼容性：尽量兼容标准 JDBC 接口，但有一些限制。

