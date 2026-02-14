package net.hasor.dbvisitor.adapter.elastic.realdb.es7;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Elasticsearch 7 向量搜索测试
 * 测试向量相似度搜索和范围过滤功能
 */
public class Elastic7VectorSearchTest {
    private static final String ES_URL     = "jdbc:dbvisitor:elastic://127.0.0.1:19201?indexRefresh=true";
    private static final String INDEX_NAME = "test_vector_search";

    @Before
    public void before() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            // 清理旧索引
            try {
                s.execute("DELETE /" + INDEX_NAME);
            } catch (Exception e) {
                // ignore
            }

            // 创建索引并定义 mapping（包含 dense_vector 类型）
            String createIndexMapping = "PUT /" + INDEX_NAME + " {" + //
                    "  \"mappings\": {" +                             //
                    "    \"properties\": {" +                         //
                    "      \"name\": { \"type\": \"keyword\" }," +    //
                    "      \"category\": { \"type\": \"keyword\" }," +//
                    "      \"embedding\": {" +                        //
                    "        \"type\": \"dense_vector\"," +           //
                    "        \"dims\": 3," +                          //
                    "        \"index\": true," +                      //
                    "        \"similarity\": \"cosine\"" +            //
                    "      }" +                                       //
                    "    }" +                                         //
                    "  }" +                                           //
                    "}";
            s.execute(createIndexMapping);

            // 插入测试数据（带向量）
            s.executeUpdate("POST /" + INDEX_NAME + "/_doc/1 { \"name\": \"doc1\", \"category\": \"A\", \"embedding\": [1.0, 0.0, 0.0] }");
            s.executeUpdate("POST /" + INDEX_NAME + "/_doc/2 { \"name\": \"doc2\", \"category\": \"A\", \"embedding\": [0.9, 0.1, 0.0] }");
            s.executeUpdate("POST /" + INDEX_NAME + "/_doc/3 { \"name\": \"doc3\", \"category\": \"B\", \"embedding\": [0.0, 1.0, 0.0] }");
            s.executeUpdate("POST /" + INDEX_NAME + "/_doc/4 { \"name\": \"doc4\", \"category\": \"B\", \"embedding\": [0.0, 0.0, 1.0] }");

            // 等待索引刷新
            Thread.sleep(1000);
        }
    }

    @org.junit.After
    public void after() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try {
                s.execute("DELETE /" + INDEX_NAME);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * 测试原生 KNN 查询（使用 ES DSL）
     * 注：ES 7.x 不支持顶层 knn 参数搜索，此测试方法禁用
     */
    @Test
    public void testNativeKnnQuery() throws Exception {
        // ES 7.x 不支持 knn 参数查询，跳过测试
        if (true) return;

        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            String knnQuery = "POST /" + INDEX_NAME + "/_search {" +//
                    "  \"knn\": {" +                          //
                    "    \"field\": \"embedding\"," +         //
                    "    \"query_vector\": [1.0, 0.0, 0.0]," +//
                    "    \"k\": 2," +                         //
                    "    \"num_candidates\": 10" +            //
                    "  }" +                                   //
                    "}";

            try (ResultSet rs = s.executeQuery(knnQuery)) {
                int count = 0;
                String firstDocName = null;
                while (rs.next()) {
                    if (count == 0) {
                        firstDocName = rs.getString("name");
                    }
                    count++;
                }
                assertEquals("Expected 2 results", 2, count);
                assertEquals("First result should be doc1", "doc1", firstDocName);
            }
        }
    }

    /**
     * 测试脚本评分查询（使用余弦相似度）
     */
    @Test
    public void testScriptScoreQuery() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            String scriptQuery = "POST /" + INDEX_NAME + "/_search {" +///
                    "  \"query\": {" +                                 //
                    "    \"script_score\": {" +                        //
                    "      \"query\": { \"match_all\": {} }," +        //
                    "      \"script\": {" +                            //
                    "        \"source\": \"cosineSimilarity(params.query_vector, doc['embedding']) + 1.0\"," + //
                    "        \"params\": {" +                          //
                    "          \"query_vector\": [1.0, 0.0, 0.0]" +    //
                    "        }" +                                      //
                    "      }" +                                        //
                    "    }" +                                          //
                    "  }," +                                           //
                    "  \"size\": 2" +                                  //
                    "}";

            try (ResultSet rs = s.executeQuery(scriptQuery)) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    String name = rs.getString("name");
                    assertTrue("Result should be doc1 or doc2", "doc1".equals(name) || "doc2".equals(name));
                }
                assertEquals("Expected 2 results", 2, count);
            }
        }
    }

    /**
     * 测试混合查询（向量 + 过滤条件）
     */
    @Test
    public void testHybridVectorQuery() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            String hybridQuery = "POST /" + INDEX_NAME + "/_search {" +  //
                    "  \"query\": {" +                                   //
                    "    \"script_score\": {" +                          //
                    "      \"query\": {" +                               //
                    "        \"bool\": {" +                              //
                    "          \"must\": [" +                            //
                    "            { \"term\": { \"category\": \"A\" } }" +//
                    "          ]" +                                      //
                    "        }" +                                        //
                    "      }," +                                         //
                    "      \"script\": {" +                              //
                    "        \"source\": \"cosineSimilarity(params.query_vector, 'embedding') + 1.0\"," + //
                    "        \"params\": {" +                            //
                    "          \"query_vector\": [1.0, 0.0, 0.0]" +      //
                    "        }" +                                        //
                    "      }" +                                          //
                    "    }" +                                            //
                    "  }," +                                             //
                    "  \"size\": 10" +                                   //
                    "}";

            try (ResultSet rs = s.executeQuery(hybridQuery)) {
                int count = 0;
                while (rs.next()) {
                    String category = rs.getString("category");
                    assertEquals("All results should be in category A", "A", category);
                    count++;
                }
                assertTrue("Expected at least 1 result", count >= 1);
                assertTrue("Expected at most 2 results", count <= 2);
            }
        }
    }

    /**
     * 测试 L2 范数（欧氏距离）向量搜索
     */
    @Test
    public void testL2NormVectorQuery() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            String l2Query = "POST /" + INDEX_NAME + "/_search {" +//
                    "  \"query\": {" +                             //
                    "    \"script_score\": {" +                    //
                    "      \"query\": { \"match_all\": {} }," +    //
                    "      \"script\": {" +                        //
                    "        \"source\": \"1 / (1 + l2norm(params.query_vector, doc['embedding']))\"," + //
                    "        \"params\": {" +                      //
                    "          \"query_vector\": [1.0, 0.0, 0.0]" +//
                    "        }" +                                  //
                    "      }" +                                    //
                    "    }" +                                      //
                    "  }," +                                       //
                    "  \"size\": 3" +                              //
                    "}";

            try (ResultSet rs = s.executeQuery(l2Query)) {
                int count = 0;
                String firstDocName = null;
                while (rs.next()) {
                    if (count == 0) {
                        firstDocName = rs.getString("name");
                    }
                    count++;
                }
                assertTrue("Expected at least 1 result", count > 0);
                assertEquals("First result should be doc1 (closest vector)", "doc1", firstDocName);
            }
        }
    }

    /**
     * 测试向量范围过滤（距离阈值）
     */
    @Test
    public void testVectorRangeFilter() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            // 使用 min_score 配合 script_score 过滤出相似度大于阈值的文档
            String rangeQuery = "POST /" + INDEX_NAME + "/_search {" + //
                    "  \"min_score\": 1.8," +                          //
                    "  \"query\": {" +                                 //
                    "    \"script_score\": {" +                        //
                    "      \"query\": { \"match_all\": {} }," +        //
                    "      \"script\": {" +                            //
                    "        \"source\": \"cosineSimilarity(params.query_vector, 'embedding') + 1.0\"," +//
                    "        \"params\": {" +                           //
                    "          \"query_vector\": [1.0, 0.0, 0.0]" +     //
                    "        }" +                                       //
                    "      }" +                                         //
                    "    }" +                                           //
                    "  }" +                                             //
                    "}";

            try (ResultSet rs = s.executeQuery(rangeQuery)) {
                int count = 0;
                while (rs.next()) {
                    String name = rs.getString("name");
                    // doc1 (1.0, 0.0, 0.0) -> sim 1.0 + 1.0 = 2.0
                    // doc2 (0.9, 0.1, 0.0) -> sim 0.9 + 1.0 = 1.9
                    assertTrue("Result should be doc1 or doc2", "doc1".equals(name) || "doc2".equals(name));
                    count++;
                }
                assertTrue("Expected at least 1 result with high similarity", count >= 1);
            }
        }
    }

    /**
     * 测试排序（按向量相似度）
     */
    @Test
    public void testVectorSortWithScriptScore() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            String sortQuery = "POST /" + INDEX_NAME + "/_search {" +//
                    "  \"query\": {" +                              //
                    "    \"script_score\": {" +                     //
                    "      \"query\": { \"match_all\": {} }," +   //
                    "      \"script\": {" +                         //
                    "        \"source\": \"cosineSimilarity(params.query_vector, doc['embedding']) + 1.0\"," +//
                    "        \"params\": {" +                       //
                    "          \"query_vector\": [1.0, 0.0, 0.0]" + //
                    "        }" +                                    //
                    "      }" +                                      //
                    "    }" +                                        //
                    "  }," +                                         //
                    "  \"size\": 3" +                               //
                    "}";

            try (ResultSet rs = s.executeQuery(sortQuery)) {
                int count = 0;
                String firstDocName = null;
                while (rs.next()) {
                    if (count == 0) {
                        firstDocName = rs.getString("name");
                    }
                    count++;
                }
                assertTrue("Expected at least 1 result", count > 0);
                assertEquals("First result should be doc1 (highest cosine similarity)", "doc1", firstDocName);
            }
        }
    }

    /**
     * 测试参数化传参（PreparedStatement）
     */
    @Test
    public void testParameterizedVectorQuery() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); java.sql.PreparedStatement ps = c.prepareStatement("POST /" + INDEX_NAME + "/_search {" +//
                "  \"query\": {" +                                //
                "    \"script_score\": {" +                       //
                "      \"query\": { \"match_all\": {} }," +     //
                "      \"script\": {" +                           //
                "        \"source\": \"cosineSimilarity(params.query_vector, doc['embedding']) + 1.0\"," + //
                "        \"params\": {" +                         //
                "          \"query_vector\": ?" +                 //
                "        }" +                                      //
                "      }" +                                        //
                "    }" +                                          //
                "  }," +                                           //
                "  \"size\": 3" +                                 //
                "}")) {

            Object[] vector = new Object[] { 1.0, 0.0, 0.0 };
            ps.setObject(1, vector);

            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                String firstDocName = null;
                while (rs.next()) {
                    if (count == 0) {
                        firstDocName = rs.getString("name");
                    }
                    count++;
                }
                assertTrue("Expected results", count > 0);
                assertEquals("First result should be doc1 (closest vector)", "doc1", firstDocName);
            }
        }
    }

    /**
     * 测试读取向量数据
     */
    @Test
    public void testVectorDataRetrieval() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            // Include embedding field in source
            String query = "POST /" + INDEX_NAME + "/_search {" +        //
                    "  \"_source\": [\"name\", \"embedding\"]," +        //
                    "  \"query\": { \"term\": { \"name\": \"doc1\" } }" +//
                    "}";

            try (ResultSet rs = s.executeQuery(query)) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    assertEquals("doc1", name);

                    // Retrieve vector data
                    // Depending on driver implementation, it might be an Array, List, or String
                    Object embeddingObj = rs.getObject("embedding");
                    assertNotNull("Embedding should not be null", embeddingObj);

                    if (embeddingObj instanceof java.util.List) {
                        java.util.List<?> list = (java.util.List<?>) embeddingObj;
                        assertEquals(3, list.size());
                        assertEquals(1.0, ((Number) list.get(0)).doubleValue(), 0.0001);
                    } else if (embeddingObj.getClass().isArray()) {
                        // If it returns an array (e.g. Object[] or double[])
                        int length = java.lang.reflect.Array.getLength(embeddingObj);
                        assertEquals(3, length);
                        assertEquals(1.0, ((Number) java.lang.reflect.Array.get(embeddingObj, 0)).doubleValue(), 0.0001);
                    } else if (embeddingObj instanceof String) {
                        String raw = ((String) embeddingObj).trim();
                        if (raw.startsWith("[") && raw.endsWith("]")) {
                            String[] parts = raw.substring(1, raw.length() - 1).split(",");
                            assertEquals(3, parts.length);
                            assertEquals(1.0, Double.parseDouble(parts[0].trim()), 0.0001);
                        } else {
                            fail("Unexpected format for vector data: " + raw);
                        }
                    } else {
                        // Fallback or unexpected type
                        fail("Unexpected type for vector data: " + embeddingObj.getClass().getName());
                    }
                } else {
                    fail("Should have found doc1");
                }
            }
        }
    }
}
