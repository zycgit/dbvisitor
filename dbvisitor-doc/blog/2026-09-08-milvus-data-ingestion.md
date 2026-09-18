---
slug: milvus-data-ingestion
title: "Milvus 数据入库：三种写入方式与选择"
description: "用 jdbc-milvus 分别处理少量写入、迭代器分页写入和服务端 Import，区分返回计数、任务 ID 与失败处理。"
authors: [ZhaoYongChun]
tags: [dbVisitor, Milvus, JDBC]
topics: [vectors]
---

几条在线数据、持续到达的一批记录、已经准备好的大型文件，并不适合一律逐条 INSERT。

jdbc-milvus 提供三种不同的入口。选择的依据首先是数据在哪里、怎样产生，其次才是调整单次发送多少条。

<!-- truncate -->

## 选择写入方式 {#先选写入方式}

| 你的数据 | 建议方式 | 需要关注的结果 |
| --- | --- | --- |
| 一条或几条业务记录 | INSERT 或多个 VALUES 元组 | 写入响应 |
| Java 中持续产生的记录 | VALUES ? 绑定 Iterator | 已确认页面与失败进度 |
| 服务端可以访问的导入文件 | IMPORT | 任务 ID、状态和失败原因 |

这些方式都不等于 JDBC Batch，也不提供跨请求事务。

## INSERT 写入 {#少量数据明确写入字段}

示例集合的字段为 `id INT64 PRIMARY KEY`、`body VARCHAR(1000)` 和 `dense FLOAT_VECTOR(2)`：

```sql
INSERT INTO blog_ingest_articles(id,body,dense) VALUES
(1,'first article',[1,0]),
(2,'second article',[0,1]);
```

真实业务中的正文和向量使用参数传递。相同主键需要覆盖时，应按业务含义选择 UPSERT 或部分更新；不要把 Milvus INSERT 当成关系库的唯一键冲突检查。

## 迭代器写入 {#记录流不用先收集成一个大-list}

`VALUES ?` 可以接收迭代器。下面逐次生成五条数据，设置每页两条，用很少的数据展示跨页写入：

```java
try (PreparedStatement insert = conn.prepareStatement(
        "INSERT INTO blog_ingest_articles VALUES ?")) {
    insert.setFetchSize(2);
    insert.setObject(1, LongStream.rangeClosed(1, 5)
            .mapToObj(id -> Map.<String, Object>of(
                    "id", id,
                    "body", "article-" + id,
                    "dense", List.of(1F, 0F)))
            .iterator());
    System.out.println("written=" + insert.executeLargeUpdate());
}
```

这里 Map 的键就是集合字段名。返回 `written=5`；`fetchSize=2` 限制单页实体数，不把总写入量限制为两条。

生产环境按单条数据大小、请求成本及服务端限制调整页大小，不要直接照搬演示值。来源如果是文件流或其它可关闭资源，由调用方负责关闭。

需要 AutoID 主键时，可以通过 `RETURN_GENERATED_KEYS` 和 `getGeneratedKeys()` 获取；请求全部生成键会积累相应主键数据，不应因此假定整体内存始终固定。

## 文件导入 {#文件导入先提交再观察}

Import 由 Milvus 服务端读取文件。客户端磁盘上的文件不会因为写进 SQL 就被上传。

以独立集合 `blog_import_articles` 为例，先用 Statement 执行：

```sql
CREATE TABLE blog_import_articles (
    id INT64 PRIMARY KEY, body VARCHAR(1000), dense FLOAT_VECTOR(2)
) WITH (consistency_level='Strong');
```

准备一个符合 Milvus Import 格式的 JSON 文件：

```json
[
  {"id":101,"body":"first imported article","dense":[1.0,0.0]},
  {"id":102,"body":"second imported article","dense":[0.0,1.0]}
]
```

将它放到 **Milvus 服务端配置的存储** 中：对象存储部署使用可访问的对象路径，本地存储部署使用服务端路径。连接入口也必须提供 Import REST 服务。

```java
String jobId;
try (PreparedStatement submit = conn.prepareStatement("""
        /*+ sync=false */ IMPORT FROM ? INTO blog_import_articles RETURNING JOB_ID
        """)) {
    submit.setString(1, "prepared/articles.json");
    try (ResultSet result = submit.executeQuery()) {
        result.next();
        jobId = result.getString("JOB_ID");
    }
}
```

实际运行时替换文件路径。先将 `jobId` 持久化，再由后台任务查询进度：

```java
try (PreparedStatement progress = conn.prepareStatement("SHOW IMPORT ?")) {
    progress.setString(1, jobId);
    try (ResultSet result = progress.executeQuery()) {
        result.next();
        String state = result.getString("STATE");
        String reason = result.getString("REASON");
        System.out.println(state + " | " + reason);
    }
}
```

任务进入 `Completed` 才表示导入完成；`Failed` 时读取 `REASON`。普通 IMPORT 的更新计数不是导入行数，不能拿它判断成功导入了多少数据。

完整 `example.MilvusImport` 提供有截止时间的轮询：超时后停止观察并保留任务编号，不自动撤销或重新提交。导入完成后，创建索引、加载集合，再查询确认数据。

## 失败处理 {#失败后先弄清楚哪部分已经完成}

:::note
分页写入失败时，前面的页面可能已经成功，当前未确认页面也可能已被服务端处理。不要直接从头重放整个 Iterator。普通 INSERT/UPSERT 不自动重试。
:::

检查异常中的已确认页数、记录数和当前页信息，再决定从哪里恢复。Import 则优先使用保存的任务编号查询；网络超时不等于任务没有创建。

采用按业务 ID 保存等恢复策略前，也要明确 UPSERT 的覆盖语义。驱动不承诺精确一次写入。

示例工程（[GitHub](https://github.com/zycgit/dbvisitor/tree/main/dbvisitor-example/blog-680) / [Gitee](https://gitee.com/zycgit/dbvisitor/tree/main/dbvisitor-example/blog-680)）：`example.MilvusIngest` 可直接验证分页写入；`example.MilvusImport` 需要显式提供已存在的集合与服务端文件路径，不会默认发起导入或删除该集合。

详细参考：[INSERT](/docs/features/milvus/sql/insert) · [IMPORT](/docs/features/milvus/sql/import) · [SHOW IMPORT](/docs/features/milvus/show/import)。
