---
id: params
sidebar_position: 3
hide_table_of_contents: true
title: 连接参数
description: jdbc-redis 驱动程序可以设置的 JDBC 参数。
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
| timeZone       | string  |                   | 驱动在处理时间类型时使用的本地时区。                                                                                      |
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