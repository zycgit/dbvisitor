---
id: operations
sidebar_position: 8
title: 原生操作与返回语义
---


`updateOne`、`updateMany` 返回 modifiedCount，不是 matchedCount。设置为已有值时可返回 0；upsert 新增文档也不能用该计数推断插入数。`replaceOne` 是整篇文档替换，与 `$set` 更新部分字段不同。`bulkWrite` 返回插入、修改、删除计数之和，不包含独立的 upsert 数量；它是一个原生批量命令，不是 JDBC batch，也不是跨文档事务。

可以使用原生更新操作符，避免先读取文档再在 Java 中修改：

```text
db.user_info.updateOne({uid: ?}, {$inc: {loginCount: ?}})
db.user_info.find({$or: [{status: ?}, {age: {$gte: ?}}]})
db.user_info.find({name: {$regex: ?}})
```

`$regex` 的参数是正则表达式，不是 SQL LIKE 模式；如果应用要按普通文本匹配，应先按正则规则处理元字符。以上操作都由 MongoDB 执行，不由 dbVisitor 在内存中过滤或累加。

### 聚合管道

以下示例要求 orders 中的 status 为字符串、amount 为数值：

```java
String command = """
        db.orders.aggregate([
            {$match: {status: ?}},
            {$group: {_id: '$customerId', total: {$sum: '$amount'}}},
            {$sort: {total: -1, _id: 1}},
            {$limit: 10}
        ], {allowDiskUse: true, maxTimeMS: 5000})
        """;
try (PreparedStatement ps = conn.prepareStatement(command)) {
    ps.setString(1, "paid");
    try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            System.out.println(rs.getString("_JSON"));
        }
    }
}
```

服务端先筛选已付款订单，再按 customerId 汇总 amount、排序并返回前 10 组。`$group` 输出中的 `_id` 是分组键，不是订单主键；聚合自身不保证排序，参阅[MongoDB $group](https://www.mongodb.com/docs/manual/reference/operator/aggregation/group/)。结果中的 total 不是独立 JDBC 列，应从 `_JSON` 中解析。

聚合选项已接入 allowDiskUse、maxTimeMS、maxAwaitTimeMS、bypassDocumentValidation、collation、comment、hint。其中 hint 是索引提示对象，不是命令前的分页 Hint。batchSize 没有接入；不能借此控制驱动取数或内存。Page 的 find 覆盖 Hint 不会改写管道，分页必须在管道中显式加入 `$skip`、`$limit`。

### 接入边界

原生索引、视图、聚合阶段及 runCommand 请求仍受 MongoDB 版本、权限和部署方式限制。`runCommand` 返回一次命令响应，不自动实现响应内 cursor 的 getMore 协议，也不将任意服务端命令包装成可分页的实体查询。需要 change stream、客户端会话或事务时使用官方 SDK；驱动不执行 JavaScript 程序或自动模拟这些接口。
