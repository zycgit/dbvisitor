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

public class MilvusHybrid {
    public static void main(String[] args) throws Exception {
        try (Connection conn = Connections.milvus(); Statement ddl = conn.createStatement()) {
            ddl.executeUpdate("""
                    CREATE TABLE blog_hybrid_articles (
                        id INT64 PRIMARY KEY, category VARCHAR(32),
                        body VARCHAR(1000) WITH (enable_analyzer=true),
                        dense FLOAT_VECTOR(2), sparse SPARSE_FLOAT_VECTOR,
                        FUNCTION bm25_fn USING BM25 (body) INTO (sparse)
                    ) WITH (consistency_level='Strong')
                    """);
            try {
                ddl.executeUpdate("CREATE INDEX idx_dense ON blog_hybrid_articles(dense) USING AUTOINDEX WITH(metric_type='L2')");
                ddl.executeUpdate("CREATE INDEX idx_sparse ON blog_hybrid_articles(sparse) USING SPARSE_INVERTED_INDEX WITH(metric_type='BM25')");
                ddl.executeUpdate("""
                        INSERT INTO blog_hybrid_articles(id,category,body,dense) VALUES
                        (1,'java','milvus vector search',[1,0]),
                        (2,'java','mapper database guide',[0,1]),
                        (3,'python','milvus vector search',[1,0])
                        """);
                ddl.executeUpdate("FLUSH blog_hybrid_articles");
                ddl.executeUpdate("LOAD TABLE blog_hybrid_articles");
                try (PreparedStatement search = conn.prepareStatement("""
                        SELECT id,body,score FROM blog_hybrid_articles
                        WHERE category = ? ORDER BY HYBRID (
                            dense <-> ? LIMIT 3,
                            sparse <?> ? LIMIT 3
                        ) LIMIT 2 WITH(reranker='rrf',k=60)
                        """)) {
                    search.setString(1, "java");
                    search.setObject(2, new float[] { 1, 0 });
                    search.setString(3, "milvus");
                    try (ResultSet rows = search.executeQuery()) {
                        while (rows.next()) {
                            System.out.println("hybrid=" + rows.getLong("id") + " | " + rows.getString("body") + " | " + rows.getDouble("score"));
                        }
                    }
                }
            } finally {
                ddl.executeUpdate("DROP TABLE blog_hybrid_articles");
            }
        }
    }
}
