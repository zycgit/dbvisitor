---
id: using_connection
sidebar_position: 10
title: 使用原始连接
description: dbVisitor ORM 工具使用最底层的 Connection 对象。
---

# 使用原始连接

有时候我们需要获取最原始的 `Connection` 那么可以采用下面方式：

```java
ConnectionCallback<List<TestUser>> callBack = new ConnectionCallback<List<TestUser>>() {
    public List<TestUser> doInConnection(Connection con) throws SQLException {
        List<TestUser> result = ...
        // do some thing
        return result;
    }
};

List<TestUser> resultList = jdbcTemplate.execute(callBack);
```
