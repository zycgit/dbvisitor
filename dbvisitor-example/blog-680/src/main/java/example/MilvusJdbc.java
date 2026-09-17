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

public class MilvusJdbc {
    public static void main(String[] args) throws Exception {
        try (Connection conn = Connections.milvus(); Statement ddl = conn.createStatement()) {
            // Fail if the collection already exists. Only remove a collection created by this run.
            ddl.executeUpdate("""
                    CREATE TABLE blog_jdbc_articles (
                        id INT64 PRIMARY KEY, title VARCHAR(256),
                        category VARCHAR(32), embedding FLOAT_VECTOR(2)
                    ) WITH (consistency_level='Strong')
                    """);
            try {
                ddl.executeUpdate("""
                        CREATE INDEX idx_embedding ON blog_jdbc_articles(embedding)
                        USING AUTOINDEX WITH (metric_type='L2')
                        """);
                ddl.executeUpdate("LOAD TABLE blog_jdbc_articles");
                try (PreparedStatement insert = conn.prepareStatement("""
                        INSERT INTO blog_jdbc_articles(id,title,category,embedding) VALUES (?,?,?,?)
                        """)) {
                    Object[][] articles = { { 1L, "Vector introduction", "java", new float[] { 1, 0 } }, { 2L, "Mapper guide", "java", new float[] { 0, 1 } }, { 3L, "Other category", "python", new float[] { 1, 0 } } };
                    for (Object[] article : articles) {
                        for (int i = 0; i < article.length; i++) {
                            insert.setObject(i + 1, article[i]);
                        }
                        insert.executeUpdate();
                    }
                }
                try (PreparedStatement search = conn.prepareStatement("""
                        SELECT id,title,score FROM blog_jdbc_articles
                        WHERE category = ? ORDER BY embedding <-> ? LIMIT 2
                        """)) {
                    search.setString(1, "java");
                    search.setObject(2, new float[] { 1, 0 });
                    try (ResultSet rows = search.executeQuery()) {
                        while (rows.next()) {
                            System.out.println(rows.getLong("id") + " | " + rows.getString("title") + " | " + rows.getFloat("score"));
                        }
                    }
                }
            } finally {
                ddl.executeUpdate("DROP TABLE blog_jdbc_articles");
            }
        }
    }
}
