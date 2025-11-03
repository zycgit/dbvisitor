---
id: usecase
sidebar_position: 1
hide_table_of_contents: true
title: 如何使用
description: 展示如何使用 jdbc-redis 以 JDBC 的方式访问 Redis 数据库。
---

### 引入依赖

```xml title='Maven 依赖'
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>jdbc-redis</artifactId>
  <version>最新版本的版本号</version>
</dependency>
```

## 连接创建

使用标准的 JDBC URL 格式连接 Redis：

```java
String url = "jdbc:dbvisitor:jedis://server:port?database=0&param1=value1&param2=value2";
Properties props = new Properties();
props.setProperty("username", "user");
props.setProperty("password", "pass");

Class.forName("net.hasor.dbvisitor.driver.JdbcDriver");
Connection conn = DriverManager.getConnection(url, props);
```

主要的连接参数包括：
- server：Redis 服务地址，格式为 ip 或 ip:port，集群模式为 ip:port;ip:port。
- username/password：认证信息。
- database：默认数据库索引，默认为 0。
- connectTimeout：连接超时时间（毫秒），默认 5000。
- socketTimeout：套接字超时时间（秒），默认 10。

## 命令执行

```java
// 创建连接
Connection conn = DriverManager.getConnection("jdbc:dbvisitor:jedis://localhost:6379");

// 执行命令
Statement stmt = conn.createStatement();
ResultSet rs = stmt.executeQuery("SET mykey hello");

// 处理结果
rs = stmt.executeQuery("GET mykey");
if (rs.next()) {
    System.out.println(rs.getString(1)); // 输出: hello
}

// 参数化查询
PreparedStatement pstmt = conn.prepareStatement("HSET ? ? ?");
pstmt.setString(1, "myhash");
pstmt.setString(2, "field1");
pstmt.setString(3, "value1");
ResultSet rs = pstmt.executeQuery();
```
