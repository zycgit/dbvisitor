/*
 * Copyright 2015-2022 the original author or authors.
 * Licensed under the Apache License, Version 2.0.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package example;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.List;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;

public class MilvusDao {
    @Table("blog_dao_articles")
    public static class Article {
        @Column(primary = true)
        private Long        id;
        private String      title;
        private String      category;
        private List<Float> embedding;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public List<Float> getEmbedding() {
            return embedding;
        }

        public void setEmbedding(List<Float> embedding) {
            this.embedding = embedding;
        }
    }

    @SimpleMapper
    public interface ArticleMapper extends BaseMapper<Article> {
        default List<Article> nearest(String category, List<Float> vector) throws SQLException {
            return query().eq(Article::getCategory, category).orderByL2(Article::getEmbedding, vector).initPage(2, 0).queryForList();
        }

        @Query("""
                SELECT id,title,category,embedding FROM blog_dao_articles
                WHERE category = #{category}
                ORDER BY embedding <-> #{vector} LIMIT 2
                """)
        List<Article> search(@Param("category") String category, @Param("vector") List<Float> vector);
    }

    public static void main(String[] args) throws Exception {
        try (Connection conn = Connections.milvus(); Statement ddl = conn.createStatement(); Session session = new Configuration().newSession(conn)) {
            ddl.executeUpdate("""
                    CREATE TABLE blog_dao_articles (id INT64 PRIMARY KEY, title VARCHAR(256),
                        category VARCHAR(32), embedding FLOAT_VECTOR(2))
                    WITH (consistency_level='Strong')
                    """);
            try {
                ddl.executeUpdate("CREATE INDEX idx_v ON blog_dao_articles(embedding) USING AUTOINDEX WITH(metric_type='L2')");
                ddl.executeUpdate("LOAD TABLE blog_dao_articles");
                Article article = new Article();
                article.setId(1L);
                article.setTitle("Vector introduction");
                article.setCategory("java");
                article.setEmbedding(List.of(1F, 0F));
                LambdaTemplate lambda = new LambdaTemplate(conn);
                lambda.insert(Article.class).applyEntity(article).executeSumResult();
                ArticleMapper mapper = session.createMapper(ArticleMapper.class);
                System.out.println("builder=" + mapper.nearest("java", List.of(1F, 0F)).get(0).getTitle());
                System.out.println("annotation=" + mapper.search("java", List.of(1F, 0F)).get(0).getTitle());
                mapper.update().eq(Article::getId, 1L).updateTo(Article::getTitle, "Revised guide").doUpdate();
                Article loaded = mapper.selectById(1L);
                System.out.println("updated=" + loaded.getTitle() + "; vector=" + loaded.getEmbedding());
            } finally {
                ddl.executeUpdate("DROP TABLE blog_dao_articles");
            }
        }
    }
}
