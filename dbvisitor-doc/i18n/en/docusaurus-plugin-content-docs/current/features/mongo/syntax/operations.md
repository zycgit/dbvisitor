---
id: operations
sidebar_position: 8
title: Native Operations and Results
---


`updateOne` and `updateMany` return modifiedCount, not matchedCount. Assigning an existing value can return 0; an upsert insertion cannot be inferred from this count either. `replaceOne` replaces the document rather than assigning individual fields with `$set`. `bulkWrite` returns the sum of inserted, modified and deleted counts, excluding the separate upsert count. It is one native bulk command, not JDBC batch or a multi-document transaction.

Use native update operators instead of reading a document and modifying it in Java:

```text
db.user_info.updateOne({uid: ?}, {$inc: {loginCount: ?}})
db.user_info.find({$or: [{status: ?}, {age: {$gte: ?}}]})
db.user_info.find({name: {$regex: ?}})
```

The `$regex` parameter is a regular expression, not an SQL LIKE pattern. Escape regex metacharacters when the application intends literal-text matching. MongoDB performs these operations; dbVisitor does not filter or increment values in memory.

### Aggregation Pipelines

This example expects string status values and numeric amount values in orders:

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

The server filters paid orders, sums amount by customerId, sorts and returns the first ten groups. The output `_id` is a group key, not an order primary key. Grouping does not itself sort results; see [MongoDB $group](https://www.mongodb.com/docs/manual/reference/operator/aggregation/group/). The total field is not a separate JDBC column; parse it from `_JSON`.

Supported aggregation options are allowDiskUse, maxTimeMS, maxAwaitTimeMS, bypassDocumentValidation, collation, comment and hint. The hint option is an index-hint object, not a command-prefix pagination Hint. batchSize is not connected to the SDK and cannot control retrieval or memory. Page's find override Hints do not rewrite pipelines; explicitly use `$skip`/`$limit` for aggregation pagination.

### Integration Boundaries

Native indexes, views, aggregation stages and runCommand requests depend on MongoDB version, permissions and deployment. `runCommand` returns one command response; it does not implement getMore for a cursor inside that response or turn every server command into a paginated entity query. Use the official SDK for change streams, client sessions and transactions. The driver neither executes JavaScript programs nor simulates these interfaces.
