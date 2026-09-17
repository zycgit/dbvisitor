---
slug: milvus-data-ingestion
topics: [vectors]
title: "Milvus Ingestion: Choosing a Write Method"
description: "Use jdbc-milvus for small writes, iterator-based paged ingestion and server-side Import, with distinct handling for counts, job IDs and failures."
authors: [ZhaoYongChun]
tags: [dbVisitor, Milvus, JDBC]
---

A few online records, an incoming stream of rows and a prepared data file should not all be handled as individual INSERT statements.

jdbc-milvus offers three entry points. Start with where your data is and how it is produced, then tune the amount sent in each request.

<!-- truncate -->

## Choosing a Write Method {#choose-the-workflow-first}

| Input | Suggested approach | Result to observe |
| --- | --- | --- |
| One or a few business records | INSERT or multiple VALUES tuples | Write response |
| Records produced in Java | VALUES ? bound to an Iterator | Confirmed pages and failure progress |
| Import files accessible to the server | IMPORT | Job ID, state and failure reason |

None of these implies JDBC Batch or a cross-request transaction.

## INSERT Writes {#small-writes-specify-the-fields}

The example collection has id INT64 PRIMARY KEY, body VARCHAR(1000) and dense FLOAT_VECTOR(2):

```sql
INSERT INTO blog_ingest_articles(id,body,dense) VALUES
(1,'first article',[1,0]),
(2,'second article',[0,1]);
```

Bind real application text and vectors as parameters. If an existing primary key should be replaced, choose UPSERT or partial updates according to business semantics. Milvus INSERT is not a relational unique-key conflict check.

## Iterator Writes {#streams-no-need-to-collect-everything-into-a-list}

VALUES ? accepts an iterator. This generates five rows and uses a page size of two to exercise page boundaries:

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

Map keys are collection field names. The result is written=5. fetchSize=2 controls each page, not the total number of rows.

Tune page size using record size, request cost and server limits. The small value is for demonstration. Callers remain responsible for closing input streams or other owned resources.

For AutoID, request RETURN_GENERATED_KEYS and read getGeneratedKeys(). Collecting all generated keys uses memory proportional to the key count; do not assume the entire operation always uses constant memory.

## File Import {#file-import-submit-then-observe}

Milvus reads import files on the server side. Naming a client-side file in SQL does not upload it.

First create a separate collection with Statement:

```sql
CREATE TABLE blog_import_articles (
    id INT64 PRIMARY KEY, body VARCHAR(1000), dense FLOAT_VECTOR(2)
) WITH (consistency_level='Strong');
```

Prepare a JSON import file:

```json
[
  {"id":101,"body":"first imported article","dense":[1.0,0.0]},
  {"id":102,"body":"second imported article","dense":[0.0,1.0]}
]
```

Place it in **the storage configured for the Milvus server**. Use an accessible object path for object storage, or a server-side path for local storage. The connection endpoint must also expose the Import REST service.

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

Replace the sample path before running. Persist jobId, then let a background task query progress:

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

Completed means the import finished. Read REASON when it reaches Failed. An ordinary IMPORT update count is not an imported-row count.

The complete example.MilvusImport uses polling with a deadline. If polling times out, it preserves the job ID rather than automatically cancelling or resubmitting. After a successful import, create an index, load the collection and query to verify data.

## Failure Handling {#on-failure-first-identify-what-has-completed}

:::note
Earlier pages may have succeeded when a paged write fails, and an unconfirmed page may also have reached the server. Do not automatically replay the entire iterator. Ordinary INSERT/UPSERT writes are not automatically retried.
:::

Inspect confirmed pages, confirmed rows and current-page information before deciding where to resume. For Import, query the saved job ID first. A network timeout does not prove that no task was created.

Before implementing recovery using business IDs, also decide which UPSERT replacement semantics are appropriate. The driver does not promise exactly-once writes.

See the example project ([GitHub](https://github.com/zycgit/dbvisitor/tree/main/dbvisitor-example/blog-680) / [Gitee](https://gitee.com/zycgit/dbvisitor/tree/main/dbvisitor-example/blog-680)): example.MilvusIngest demonstrates paged writes. example.MilvusImport requires an explicit existing collection and server file path; it does not import by default or delete that collection.

References: [INSERT](/docs/features/milvus/sql/insert) · [IMPORT](/docs/features/milvus/sql/import) · [SHOW IMPORT](/docs/features/milvus/show/import).
