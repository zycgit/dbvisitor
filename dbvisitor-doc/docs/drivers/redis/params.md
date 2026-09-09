---
id: params
sidebar_position: 3
title: 连接参数
description: Redis JDBC 参数、超时单位、集群配置与命令分隔规则。
---

```text title='JDBC URL 格式'
jdbc:dbvisitor:jedis://server?database=0&param1=value1&param2=value2
```

| 参数名            | 类型      | 默认值               | 参数说明                                                                                                    |
|----------------|---------|-------------------|---------------------------------------------------------------------------------------------------------|
| server         | string  |                   | redis 服务地址，如果没有指定端口号将会采用 `6379` 作为默认端口，格式为：`ip` 或 `ip:port`，集群模式为 `ip:port;ip:port` 或 `ip;ip;ip `       |
| username       | string  |                   | 用户名                                                                                                     |
| password       | string  |                   | 密码                                                                                                      |
| database       | int     | 0                 | 默认数据库                                                                                                   |
| connectTimeout | int     | 5000              | 连接超时时间（毫秒）                                                                                              |
| socketTimeout  | int     | 10                | 套接字超时时间（秒）                                                                                              |
| timeZone       | string  | UTC               | 驱动用于时间类型转换的时区，例如 `+08:00`。 |
| clientName     | string  | Jedis-JDBC-Client | 客户端名称                                                                                                   |
| uncheckNumKeys | boolean | false             | 是否禁用检查键数量，默认值为 false。当设置为 true 时，驱动将不会检查执行的命令中对于 Keys 数量的要求。如 HEXPIRE、HTTL 的 numfields，ZMPOP 的 numkeys。 |
| separatorChar  | char    | '\n'              | 命令分隔符，默认值为 '\n'，可设置为（\n、“;”分号）驱动将根据该字符来分隔多个命令，如：`SET mykey hello; GET mykey`。                           |
| interceptor    | class   |                   | 命令拦截器，用于拦截 JedisCluster 或 Jedis 对象执行命令调用，需要实现 java.lang.reflect.InvocationHandler                       |
| customJedis    | class   |                   | 在驱动外自定义 JedisCluster 或 Jedis 对象的创建过程，需要实现 net.hasor.dbvisitor.adapter.redis.CustomJedis                 |
| maxTotal       | int     | 8                 | (集群) 池中“maxTotal”配置属性的值。                                                                                |
| maxIdle        | int     | 8                 | (集群) 池中“maxIdle”配置属性的值。                                                                                 |
| minIdle        | int     | 0                 | (集群) 池中“minIdle”配置属性的值。                                                                                 |
| testWhileIdle  | boolean | false             | (集群) 池中“testWhileIdle”配置属性的值。                                                                           |
| maxAttempts    | int     | 5                 | (集群) 执行命令的最大尝试次数。                                                                                       |

- 参数 uncheckNumKeys 将会影响的命令有：
  - HEXPIRE、HEXPIREAT、HEXPIRETIME、HPEXPIRE、HPEXPIREAT、HPEXPIRETIME、HPERSIST、HTTL、HPTTL
  - ZMPOP、BZMPOP、ZDIFF、ZDIFFSTORE、ZINTER、ZINTERCARD、ZINTERSTORE、ZUNION、ZUNIONSTORE
  - LMPOP、BLMPOP、SINTERCARD
- 参数 separatorChar 当设置为 “;” 分号时
  - 含有分号的内容需要用双引号进行处理，否则会导致脚本解析错误。

`user` 是注册参数名，`username` 是兼容别名，二者同时设置时 user 优先。`adapterName` 和 `server` 通常由 URL 提供。socketTimeout 单位是秒，不同于 connectTimeout 的毫秒。

多主机配置创建 JedisCluster，不是独立 Redis 节点的广播写模式；database 不能绕过 Redis Cluster 的数据库限制。默认客户端不启用 TLS，高级配置使用 customJedis 工厂。工厂返回的客户端由 JDBC 连接关闭。

当前实现仅在同时设置 minIdle 时才应用 testWhileIdle；仅设置 testWhileIdle 仍会沿用池默认值。
