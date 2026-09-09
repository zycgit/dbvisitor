---
id: params
sidebar_position: 3
title: Connection Parameters
description: Redis JDBC properties, timeout units, cluster settings and command separators.
---

```text title='JDBC URL Format'
jdbc:dbvisitor:jedis://server?database=0&param1=value1&param2=value2
```

| Parameter | Type | Default | Description |
|---|---|---|---|
| server | string | | Redis server address. Uses `6379` as default port if not specified. Format: `ip` or `ip:port`. Cluster mode: `ip:port;ip:port` or `ip;ip;ip` |
| username | string | | Username |
| password | string | | Password |
| database | int | 0 | Default database |
| connectTimeout | int | 5000 | Connection timeout (milliseconds) |
| socketTimeout | int | 10 | Socket timeout (seconds) |
| timeZone | string | UTC | Driver time zone for time type conversion, for example `+08:00`. |
| clientName | string | Jedis-JDBC-Client | Client name |
| uncheckNumKeys | boolean | false | Whether to disable key count checking. When set to true, the driver will not check the number of keys required by the executed command, such as numfields in HEXPIRE/HTTL, and numkeys in ZMPOP. |
| separatorChar | char | '\n' | Command separator. Default is '\n'. Can be set to (\n, ";" semicolon). The driver will split multiple commands by this character, e.g., `SET mykey hello; GET mykey`. |
| interceptor | class | | Command interceptor for intercepting JedisCluster or Jedis object command calls. Must implement java.lang.reflect.InvocationHandler |
| customJedis | class | | Custom creation of JedisCluster or Jedis objects outside the driver. Must implement net.hasor.dbvisitor.adapter.redis.CustomJedis |
| maxTotal | int | 8 | (Cluster) Value of the pool's "maxTotal" configuration property. |
| maxIdle | int | 8 | (Cluster) Value of the pool's "maxIdle" configuration property. |
| minIdle | int | 0 | (Cluster) Value of the pool's "minIdle" configuration property. |
| testWhileIdle | boolean | false | (Cluster) Value of the pool's "testWhileIdle" configuration property. |
| maxAttempts | int | 5 | (Cluster) Maximum number of attempts for executing commands. |

- The uncheckNumKeys parameter affects the following commands:
  - HEXPIRE, HEXPIREAT, HEXPIRETIME, HPEXPIRE, HPEXPIREAT, HPEXPIRETIME, HPERSIST, HTTL, HPTTL
  - ZMPOP, BZMPOP, ZDIFF, ZDIFFSTORE, ZINTER, ZINTERCARD, ZINTERSTORE, ZUNION, ZUNIONSTORE
  - LMPOP, BLMPOP, SINTERCARD
- When separatorChar is set to ";" (semicolon):
  - Content containing semicolons must be enclosed in double quotes to avoid script parsing errors.

`user` is the registered property; `username` is an alias, with user taking precedence. `adapterName` and `server` normally come from the URL. socketTimeout uses seconds, unlike connectTimeout in milliseconds.

Multiple hosts create JedisCluster, not broadcast writes to independent Redis instances. database cannot bypass Redis Cluster's database restrictions. The default client does not enable TLS; use customJedis for advanced client settings. The JDBC connection closes the returned client.

The current implementation applies testWhileIdle only when minIdle is also supplied; setting testWhileIdle alone leaves the pool default in effect.
