---
id: params
sidebar_position: 3
title: Parameter Configuration
description: Connection parameter names, accepted values, defaults and constraints.
---

<span id="properties" />

| Parameter | Accepted values / type | Default | Description and constraints |
| --- | --- | --- | --- |
| `server` | `host[:port][;host[:port]...]` | Unset | Redis server address. Uses `6379` as default port if not specified. Format: `ip` or `ip:port`. Cluster mode: `ip:port;ip:port` or `ip;ip;ip` |
| `user` / `username` | String | Unset | Username user takes precedence over username. |
| `password` | String | Unset | Password |
| `database` | Integer database index | 0 | Default database Redis Cluster supports database 0 only. |
| `connectTimeout` | Integer (milliseconds) | 5000 | Connection timeout (milliseconds) |
| `socketTimeout` | Integer (seconds) | 10 | Socket timeout (seconds) |
| `timeZone` | Time-zone ID or offset, e.g. UTC, +08:00 | UTC | Driver time zone for time type conversion, for example `+08:00`. |
| `clientName` | String | Jedis-JDBC-Client | Client name |
| `uncheckNumKeys` | `true` / `false` | false | Whether to disable key count checking. When set to true, the driver will not check the number of keys required by the executed command, such as numfields in HEXPIRE/HTTL, and numkeys in ZMPOP. |
| `separatorChar` | One character, e.g. newline or semicolon | '\n' | Command separator. Quote values containing the separator or bind them with PreparedStatement. |
| `interceptor` | Fully qualified class name | Unset | Command interceptor for intercepting JedisCluster or Jedis object command calls. Must implement java.lang.reflect.InvocationHandler |
| `customJedis` | Fully qualified class name | Unset | Custom creation of JedisCluster or Jedis objects outside the driver. Must implement net.hasor.dbvisitor.adapter.redis.CustomJedis |
| `maxTotal` | Integer | 8 | (Cluster) Value of the pool's "maxTotal" configuration property. |
| `maxIdle` | Integer | 8 | (Cluster) Value of the pool's "maxIdle" configuration property. |
| `minIdle` | Integer | 0 | (Cluster) Value of the pool's "minIdle" configuration property. |
| `testWhileIdle` | `true` / `false` | false | (Cluster) Value of the pool's "testWhileIdle" configuration property. Requires minIdle to be supplied as well. |
| `maxAttempts` | Integer | 5 | (Cluster) Maximum number of attempts for executing commands. |
| `adapterName` | `jedis` | From JDBC URL | Adapter identifier supplied by the JDBC URL. |
