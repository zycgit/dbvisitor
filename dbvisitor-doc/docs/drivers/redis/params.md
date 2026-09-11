---
id: params
sidebar_position: 3
title: 参数配置
description: 连接参数名称、可配置值、默认值及约束。
---

<span id="properties" />

| 参数名 | 可配置值 / 类型 | 默认值 | 作用与约束 |
| --- | --- | --- | --- |
| `server` | `host[:port][;host[:port]...]` | 未设置 | redis 服务地址，如果没有指定端口号将会采用 `6379` 作为默认端口，格式为：`ip` 或 `ip:port`，集群模式为 `ip:port;ip:port` 或 `ip;ip;ip ` |
| `user` / `username` | 字符串 | 未设置 | 用户名 同时设置时 user 优先于 username。 |
| `password` | 字符串 | 未设置 | 密码 |
| `database` | 整数数据库索引 | 0 | 默认数据库 Redis Cluster 仅支持数据库 0。 |
| `connectTimeout` | 整数（毫秒） | 5000 | 连接超时时间（毫秒） |
| `socketTimeout` | 整数（秒） | 10 | 套接字超时时间（秒） |
| `timeZone` | 时区 ID 或偏移量，如 UTC、+08:00 | UTC | 驱动用于时间类型转换的时区，例如 `+08:00`。 |
| `clientName` | 字符串 | Jedis-JDBC-Client | 客户端名称 |
| `uncheckNumKeys` | `true` / `false` | false | 是否禁用检查键数量，默认值为 false。当设置为 true 时，驱动将不会检查执行的命令中对于 Keys 数量的要求。如 HEXPIRE、HTTL 的 numfields，ZMPOP 的 numkeys。 |
| `separatorChar` | 单个字符，如换行符或分号 | '\n' | 多命令分隔符。值包含分隔符时使用引号或 PreparedStatement 参数绑定。 |
| `interceptor` | 类全限定名 | 未设置 | 命令拦截器，用于拦截 JedisCluster 或 Jedis 对象执行命令调用，需要实现 java.lang.reflect.InvocationHandler |
| `customJedis` | 类全限定名 | 未设置 | 在驱动外自定义 JedisCluster 或 Jedis 对象的创建过程，需要实现 net.hasor.dbvisitor.adapter.redis.CustomJedis |
| `maxTotal` | 整数 | 8 | (集群) 池中“maxTotal”配置属性的值。 |
| `maxIdle` | 整数 | 8 | (集群) 池中“maxIdle”配置属性的值。 |
| `minIdle` | 整数 | 0 | (集群) 池中“minIdle”配置属性的值。 |
| `testWhileIdle` | `true` / `false` | false | (集群) 池中“testWhileIdle”配置属性的值。 需同时配置 minIdle。 |
| `maxAttempts` | 整数 | 5 | (集群) 执行命令的最大尝试次数。 |
| `adapterName` | `jedis` | 来自 JDBC URL | 适配器标识，由 JDBC URL 提供。 |
