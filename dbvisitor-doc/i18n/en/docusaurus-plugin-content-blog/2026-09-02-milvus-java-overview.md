---
slug: milvus-java-overview
title: "Milvus with SQL: Queries, ORM and Tools"
description: "Express Milvus writes, business filters and vector search in SQL, execute them through JDBC, then extend to dbVisitor object mapping, Mappers, DataGrip and DBeaver."
authors: [ZhaoYongChun]
tags: [dbVisitor, Milvus, JDBC, Vector]
topics: [vectors]
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

Java applications typically use the Milvus SDK to write data and search vectors. For relational database developers, SQL and JDBC are more familiar. [jdbc-milvus](/en/docs/drivers/milvus/about) brings that workflow to Milvus:

```sql
SELECT id, title, score FROM intro_articles -- score is squared L2 distance; smaller is closer
WHERE category = 'java'                    -- Search only within the Java category
ORDER BY embedding <-> [1,0] LIMIT 2;       -- Return the two nearest vectors
```

Add dbVisitor to express the same search with [entity mapping](/en/docs/features/milvus/query#entity-mapping) and a [query builder](/en/docs/features/milvus/builder):

```java
@Table("intro_articles")
public class Article {
    @Column(primary = true)
    private Long id;
    private String title;
    private String category;
    private List<Float> embedding;
    // Standard getters and setters omitted
}
```

```java
LambdaTemplate lambda = new LambdaTemplate(conn);
List<Article> articles = lambda.query(Article.class)
        .eq(Article::getCategory, "java")
        .orderByL2(Article::getEmbedding, List.of(1F, 0F))
        .initPage(2, 0)
        .queryForList();
```

<!-- truncate -->

<span id="search-with-business-scope" />

## Milvus through SQL {#familiar-apis-native-work}

jdbc-milvus is developed by the dbVisitor project and translates [supported SQL](/en/docs/features/milvus/about) into Milvus API calls. The following table maps SQL to Milvus concepts:

| Scenario | Solution | Meaning in Milvus |
| --- | --- | --- |
| Define the data structure | Define tables and columns with `CREATE TABLE` | Tables map to Collections, rows to Entities, and columns to Fields |
| Prepare a vector index | Specify a vector field and distance metric with `CREATE INDEX` | Build an index for similarity search |
| Write and import data | `INSERT`, `IMPORT` | Write entities or import files in bulk |
| Load a collection | `LOAD TABLE` | Make the collection available for queries and searches |
| Filter and find similar records | `WHERE` + `ORDER BY embedding <-> ... LIMIT K` | Find the K nearest vectors by L2 distance among matching entities |
| Combine search paths | `ORDER BY HYBRID` | Combine dense vectors, BM25 and other paths with RRF or Weighted reranking |

## Prepare Data

The example uses Milvus 2.6.2+. Execute these statements individually through JDBC, DataGrip or DBeaver:

```sql
-- Define the collection; FLOAT_VECTOR(2) is a two-dimensional vector field.
CREATE TABLE intro_articles (
    id INT64 PRIMARY KEY, title VARCHAR(256),
    category VARCHAR(32), embedding FLOAT_VECTOR(2)
) WITH (consistency_level='Strong');

-- Build a vector index: AUTOINDEX selects the index; the L2 metric matches <-> in queries.
CREATE INDEX idx_embedding ON intro_articles(embedding)
USING AUTOINDEX WITH (metric_type='L2');

-- Insert articles and their vectors.
INSERT INTO intro_articles(id,title,category,embedding) VALUES
(1,'Vector introduction','java',[1,0]),
(2,'Mapper guide','java',[0,1]),
(3,'Other category','python',[1,0]);

-- Load the collection for searches.
LOAD TABLE intro_articles;
```

## Connect through JDBC {#execute-through-jdbc}

[Maven dependencies](/en/docs/drivers/milvus/dependencies) for a Java 17+ application:

```xml
<!-- Milvus JDBC driver: access Milvus through SQL -->
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>jdbc-milvus</artifactId>
    <version>6.8.1</version>
</dependency>
<!-- Add for object mapping, query builders and Mappers -->
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>dbvisitor</artifactId>
    <version>6.8.0</version>
</dependency>
```

[Connection URL and parameters](/en/docs/drivers/milvus/connection):

```java
String url = "jdbc:dbvisitor:milvus://127.0.0.1:19530/default";
Properties props = new Properties();
props.setProperty("consistencyLevel", "Strong");
```

- **Configured**: With `Strong`, new data is visible immediately after a successful write, but queries may take a little longer.
- **Not configured**: The collection's setting applies. With the default `Bounded`, new data may take a short while to appear.

## Execute Queries

```java
try (Connection conn = DriverManager.getConnection(url, props);
     PreparedStatement search = conn.prepareStatement(
             "SELECT id,title,score FROM intro_articles WHERE category = ? ORDER BY embedding <-> ? LIMIT 2")) {
    search.setString(1, "java");
    search.setObject(2, new float[] {1, 0});
    try (ResultSet rows = search.executeQuery()) {
        while (rows.next()) {
            System.out.println(rows.getLong("id") + " | " + rows.getString("title") + " | " + rows.getFloat("score"));
        }
    }
}
```

Query results:

| id | title | score |
| --- | --- | --- |
| 1 | Vector introduction | 0.0 |
| 2 | Mapper guide | 2.0 |

## ORM Mapping {#a-business-method-next}

dbVisitor integrates with [Spring](/en/docs/guides/yourproject/with_spring), [Hasor](/en/docs/guides/yourproject/with_hasor), [Solon](/en/docs/guides/yourproject/with_solon) and [Guice](/en/docs/guides/yourproject/with_guice).

[Mapper method annotations](/en/docs/guides/core/mapper/annotation_query) wrap SQL in a business method. The result object `ArticleHit` contains `id`, `title` and `score` properties with getters/setters:

```java
@SimpleMapper
public interface ArticleMapper {
    @Query("""
        SELECT id,title,score FROM intro_articles
        WHERE category = #{category}
        ORDER BY embedding <-> #{vector} LIMIT 2
        """)
    List<ArticleHit> nearest(@Param("category") String category,
                             @Param("vector") List<Float> vector);
}
```

```java
try (Session session = new Configuration().newSession(DriverManager.getConnection(url, props))) {
    ArticleMapper mapper = session.createMapper(ArticleMapper.class);
    for (ArticleHit hit : mapper.nearest("java", List.of(1F, 0F))) {
        System.out.println(hit.getTitle());
    }
}
```

<span id="related-articles" />

The query builder combines filters and [vector ordering](/en/docs/features/milvus/vectors#knn-ordering), for example to find similar articles in the same category while excluding the current article:

```java
try (Connection conn = DriverManager.getConnection(url, props)) {
    LambdaTemplate lambda = new LambdaTemplate(conn);
    Article current = lambda.query(Article.class).eq(Article::getId, 1L).queryForObject();
    List<Article> related = lambda.query(Article.class)
            .eq(Article::getCategory, current.getCategory())
            .ne(Article::getId, current.getId())
            .orderByL2(Article::getEmbedding, current.getEmbedding())
            .initPage(5, 0)
            .queryForList();
}
```

For [MyBatis-style XML](/en/docs/guides/core/file/dynamic_sql), replace the interface annotation with [`@RefMapper`](/en/docs/guides/core/mapper/file_statement) and remove `@Query` from the method:

```java title="ArticleMapper.java"
package example;

@RefMapper("/mapper/articles.xml")
public interface ArticleMapper {
    List<ArticleHit> nearest(@Param("category") String category,
                             @Param("vector") List<Float> vector);
}
```

MyBatis-style XML configuration:

```xml title="mapper/articles.xml"
<mapper namespace="example.ArticleMapper">
    <select id="nearest" resultType="example.ArticleHit">
        SELECT id, title, score FROM intro_articles
        <where>
            <if test="category != null">
                category = #{category}
            </if>
        </where>
        ORDER BY embedding &lt;-> #{vector} LIMIT 2
    </select>
</mapper>
```

Passing `null` makes the XML `<if>` omit the category condition:

```java
ArticleMapper mapper = session.createMapper(ArticleMapper.class);
List<ArticleHit> javaHits = mapper.nearest("java", List.of(1F, 0F));
List<ArticleHit> allHits = mapper.nearest(null, List.of(1F, 0F));
```

## SQL Support {#supported-syntax}

- **[Manage collections, indexes and partitions](/en/docs/features/milvus/about#definition-statements)**: `CREATE / ALTER / DROP TABLE`, `CREATE / DROP INDEX`, `CREATE / DROP PARTITION`.
- **[Write and modify data](/en/docs/features/milvus/about#write-statements)**: `INSERT`, `UPSERT`, `UPDATE`, `DELETE`.
- **Import files in bulk**: [`IMPORT`](/en/docs/features/milvus/sql/import), with task status available through [`SHOW IMPORT`](/en/docs/features/milvus/show/import).
- **Query and count records**: [`SELECT ... WHERE ... LIMIT ...`](/en/docs/features/milvus/sql/select), [`SELECT COUNT(*)`](/en/docs/features/milvus/query/count).
- **Vector and hybrid search**: [`ORDER BY` distance operators](/en/docs/features/milvus/sql/select), [`ORDER BY HYBRID`](/en/docs/features/milvus/sql/hybrid); metrics include L2, COSINE and IP.
- **Load collections and inspect status**: [`LOAD`](/en/docs/features/milvus/sql/maintenance) / [`RELEASE TABLE`](/en/docs/features/milvus/admin/release), [`SHOW TABLES`](/en/docs/features/milvus/show/tables), [`SHOW INDEXES`](/en/docs/features/milvus/show/indexes), [`SHOW STATS`](/en/docs/features/milvus/show/stats).

## SQL Client {#query-in-datagrip}

SQL is not limited to application code. The JDBC driver also lets you prepare data, experiment with vector searches, and view tabular results in SQL clients such as DataGrip and DBeaver.

<div className="sql-client-screenshots">
<Tabs>
  <TabItem value="datagrip" label="DataGrip">

![Querying a Milvus collection and viewing results in DataGrip](/img/drivers/datagrip.png)

  </TabItem>
  <TabItem value="dbeaver" label="DBeaver">

![Viewing Milvus collection fields in DBeaver](/img/drivers/dbeaver.png)

  </TabItem>
</Tabs>
</div>

Download the `alone` driver from the [Milvus SQL Client](/en/docs/drivers/milvus/sql-client) page. The driver requires Java 17 or later.

| Setting | Value |
| --- | --- |
| Driver name | dbVisitor Milvus |
| Driver file | Downloaded `jdbc-milvus-6.8.1-alone.jar` |
| Driver class | `net.hasor.dbvisitor.driver.JdbcDriver` |
| Host | `127.0.0.1`, replaced with your Milvus address |
| Port | `19530` |
| Database | `default`, or another existing database |

<span id="datagrip" />

**DataGrip**

1. Open **File → Data Sources → Drivers → +**, name the driver, add the JAR under **Driver Files → + → Custom JARs**, and set **Class** to the driver class above.
2. Add the following template under **General → URL templates**, then click **Apply → Create Data Source**.
3. Select the template's connection type and enter the host, port, and database. Enter credentials if authentication is enabled, click **Test Connection**, and save. Set the query console's transaction mode to **Auto** and **Switch schema** to **Disable**.

```text title="DataGrip URL template"
jdbc:dbvisitor:milvus://{host}:{port}/{database}\?consistencyLevel=Strong
```

<span id="dbeaver" />

**DBeaver**

1. Open **Database → Driver Manager → New**, name the driver, and select **Generic**.
2. Add the JAR under **Libraries → Add File**. Set **Class Name** to the driver class above, **Default Port** to `19530`, and **URL Template** to the template below.
3. Save the driver and create a connection with it. Enter the host, port, database, and credentials. Click **Test Connection**, then open the SQL editor with **Auto-commit** enabled.

```text title="DBeaver URL template"
jdbc:dbvisitor:milvus://{host}:{port}/{database}?consistencyLevel=Strong
```

```text title="Example generated JDBC URL"
jdbc:dbvisitor:milvus://127.0.0.1:19530/default?consistencyLevel=Strong
```

Set additional driver parameters in DataGrip's **Advanced** tab or DBeaver's **Driver properties**; see [connection parameters](/en/docs/drivers/milvus/params) for names and values. For the configuration UI, see [DataGrip custom drivers](https://www.jetbrains.com/help/datagrip/other-databases.html) and the [DBeaver Driver Manager](https://dbeaver.com/docs/dbeaver/Driver-Manager/#main-parameters).

<span id="run-a-vector-query" />

With the sample data from earlier sections ready, run this in the console:

```sql
SELECT id, title, score FROM intro_articles
WHERE category = 'java'
ORDER BY embedding <-> [1, 0] LIMIT 2;
```

The result grid shows `Vector introduction` and `Mapper guide`, with distances of `0` and `2`. Change the category or query vector to compare results, then reuse the verified SQL in JDBC code or a Mapper.
