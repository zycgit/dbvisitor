/*
 * Copyright 2015-2022 the original author or authors.
 * Licensed under the Apache License, Version 2.0.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;

public class MilvusIngest {
    public static void main(String[] args) throws Exception {
        try (Connection conn = Connections.milvus(); Statement ddl = conn.createStatement()) {
            ddl.executeUpdate("""
                    CREATE TABLE blog_ingest_articles (id INT64 PRIMARY KEY, body VARCHAR(1000), dense FLOAT_VECTOR(2))
                    WITH (consistency_level='Strong')
                    """);
            try {
                try (PreparedStatement insert = conn.prepareStatement("INSERT INTO blog_ingest_articles VALUES ?")) {
                    // Demonstrate pagination without allocating a list of every input row.
                    insert.setFetchSize(2);
                    insert.setObject(1, LongStream.rangeClosed(1, 5).mapToObj(id -> Map.<String, Object>of("id", id, "body", "article-" + id, "dense", List.of(1F, 0F))).iterator());
                    System.out.println("written=" + insert.executeLargeUpdate());
                }
                ddl.executeUpdate("CREATE INDEX idx_v ON blog_ingest_articles(dense) USING AUTOINDEX WITH(metric_type='L2')");
                ddl.executeUpdate("LOAD TABLE blog_ingest_articles");
                try (ResultSet rows = ddl.executeQuery("SELECT COUNT(*) FROM blog_ingest_articles")) {
                    rows.next();
                    System.out.println("count=" + rows.getLong(1));
                }
            } finally {
                ddl.executeUpdate("DROP TABLE blog_ingest_articles");
            }
        }
    }
}
