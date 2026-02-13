---
id: params
sidebar_position: 4
hide_table_of_contents: true
title: 连接参数
description: jdbc-elastic 支持的 JDBC 连接参数列表。
---

`jdbc-elastic` 支持在 JDBC URL 中配置多种参数，用于控制连接行为、认证方式、超时设置等。

```text title='JDBC URL 格式'
jdbc:dbvisitor:elastic://host:port?param1=value1&param2=value2
```

## 基本参数

| 参数名 | 说明 | 默认值 |
| --- | --- | --- |
| `username` | 认证用户名 | 无 |
| `password` | 认证密码 | 无 |

## 网络与超时

| 参数名 | 说明 | 默认值 |
| --- | --- | --- |
| `connectTimeout` | 连接超时时间（毫秒） | - |
| `socketTimeout` | Socket 读取超时时间（毫秒） | - |

## 行为控制

| 参数名 | 说明 | 默认值 |
| --- | --- | --- |
| `indexRefresh` | 是否在写入操作后自动刷新索引 | `false` |

## 预读配置 (Pre-Read)

这些参数用于控制大文件或大量数据的预读行为，在开启预读功能后，查询结果集将会展示文档中所有字段，而不是仅展示 _ID 、_DOC 字段。

| 参数名 | 说明 | 默认值 |
| --- | --- | --- |
| `preRead` | 是否开启预读 | `true` |
| `preReadThreshold` | 预读阈值（字节），超过该大小触发文件缓存 | `5MB` |
| `preReadMaxFileSize` | 预读最大文件大小 | `20MB` |
| `preReadCacheDir` | 预读缓存目录 | 系统临时目录 |
