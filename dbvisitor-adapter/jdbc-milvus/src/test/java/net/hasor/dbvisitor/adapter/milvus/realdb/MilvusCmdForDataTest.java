/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.adapter.milvus.realdb;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import io.milvus.client.MilvusServiceClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.grpc.GetLoadStateResponse;
import io.milvus.grpc.LoadState;
import io.milvus.grpc.QueryResults;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.collection.GetLoadStateParam;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.QueryParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.QueryResultsWrapper;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.messages.Bucket;
import net.hasor.cobble.ResourcesUtils;
import net.hasor.cobble.io.IOUtils;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MilvusCmdForDataTest extends AbstractMilvusCmdForTest {
    private static final String STRONG_CONSISTENCY_HINT = "/*+ consistency_level=Strong */ ";
    @Test
    public void testInsert() throws Exception {
        if (!milvusReady) {
            return;
        }

        try {
            dropCollection(TEST_COLLECTION);
            createCollection(TEST_COLLECTION);

            // 1. JDBC Insert
            String insertSQL = "INSERT INTO " + TEST_COLLECTION + " (book_id, word_count, book_intro) VALUES (?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(MILVUS_URL);//
                 PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                pstmt.setLong(1, 1L);
                pstmt.setLong(2, 1000L);
                pstmt.setObject(3, java.util.Arrays.asList(0.1f, 0.2f));
                int affected = pstmt.executeUpdate();
                assertEquals(1, affected);
            }

            // Verify Insert using SDK
            MilvusServiceClient client = newClient();

            // Need to load to query? Query usually requires loaded collection in Milvus 2.x?
            // Yes, let's load it just in case.
            client.createIndex(CreateIndexParam.newBuilder()//
                    .withCollectionName(TEST_COLLECTION)    //
                    .withFieldName("book_intro")            //
                    .withIndexName("idx_book_intro")        //
                    .withIndexType(IndexType.IVF_FLAT)      //
                    .withMetricType(MetricType.L2)          //
                    .withExtraParam("{\"nlist\":1024}")     //
                    .build());
            client.loadCollection(LoadCollectionParam.newBuilder()//
                    .withCollectionName(TEST_COLLECTION)//
                    .build());

            R<QueryResults> queryRes = client.query(QueryParam.newBuilder()//
                    .withCollectionName(TEST_COLLECTION)//
                    .withExpr("book_id == 1")//
                    .withOutFields(Collections.singletonList("word_count"))//
                    .build());

            assertEquals(R.Status.Success.getCode(), queryRes.getStatus().intValue());
            QueryResultsWrapper wrapper = new QueryResultsWrapper(queryRes.getData());
            assertEquals(1, wrapper.getRowRecords().size());
            assertEquals(1000L, wrapper.getRowRecords().get(0).get("word_count"));

            client.close();
        } finally {
            dropCollection(TEST_COLLECTION);
        }
    }

    @Test
    public void testUpdateSimple() throws Exception {
        if (!milvusReady) {
            return;
        }

        String tableName = "test_update_tb_" + System.currentTimeMillis();

        try {
            // 1. Create Collection (Fields: book_id, word_count, book_intro)
            createCollection(tableName);

            // 2. Insert Data
            try (Connection conn = DriverManager.getConnection(MILVUS_URL);//
                 Statement stmt = conn.createStatement()) {
                String insertSql = "INSERT INTO " + tableName + " (book_id, word_count, book_intro) VALUES (1, 100, [0.1, 0.2])";
                stmt.executeUpdate(insertSql);
            }

            // 3. Create Index and Load (Required for Query/Update)
            MilvusServiceClient client = newClient();
            try {
                client.createIndex(CreateIndexParam.newBuilder()//
                        .withCollectionName(tableName)//
                        .withFieldName("book_intro")//
                        .withIndexName("idx_book_intro")//
                        .withIndexType(IndexType.IVF_FLAT)//
                        .withMetricType(MetricType.L2)//
                        .withExtraParam("{\"nlist\":1024}")//
                        .build());
                client.loadCollection(LoadCollectionParam.newBuilder().withCollectionName(tableName).build());
            } finally {
                client.close();
            }

            // 4. Update Data
            try (Connection conn = DriverManager.getConnection(MILVUS_URL); //
                 Statement stmt = conn.createStatement()) {

                // Standard SQL Update
                String updateSql = "UPDATE " + tableName + " SET word_count = 999 WHERE book_id = 1";
                int affected = stmt.executeUpdate(updateSql);
                assertEquals(1, affected);
            }
            // 5. Verify Update
            try (Connection conn = DriverManager.getConnection(MILVUS_URL);//
                 Statement stmt = conn.createStatement()) {
                // Query via JDBC to verify
                try (ResultSet rs = stmt.executeQuery(STRONG_CONSISTENCY_HINT + "SELECT word_count FROM " + tableName + " WHERE book_id = 1")) {
                    if (rs.next()) {
                        long count = rs.getLong("word_count");
                        assertEquals(999L, count);
                    } else {
                        throw new RuntimeException("Record not found");
                    }
                }
            }

        } finally {
            dropCollection(tableName);
        }
    }

    @Test
    public void testUpdateByVectorSearch() throws Exception {
        if (!milvusReady) {
            return;
        }
        String tableName = "test_upd_vec_" + System.currentTimeMillis();
        try {
            createCollection(tableName);
            MilvusServiceClient client = newClient();
            try {
                // Insert data: id=1 vec=[0.1, 0.1], id=2 vec=[0.9, 0.9]
                // Target: Update nearest to [0.1, 0.1]
                client.insert(InsertParam.newBuilder().withCollectionName(tableName).withFields(Arrays.asList(//
                        new InsertParam.Field("book_id", Arrays.asList(1L, 2L)),//
                        new InsertParam.Field("word_count", Arrays.asList(100L, 200L)),//
                        new InsertParam.Field("book_intro", Arrays.asList(Arrays.asList(0.1f, 0.1f), Arrays.asList(0.9f, 0.9f)))//
                )).build());
                client.createIndex(CreateIndexParam.newBuilder()//
                        .withCollectionName(tableName)//
                        .withFieldName("book_intro")//
                        .withIndexName("idx_intro")//
                        .withIndexType(IndexType.IVF_FLAT)//
                        .withMetricType(MetricType.L2)//
                        .withExtraParam("{\"nlist\":1024}")//
                        .build());
                client.loadCollection(LoadCollectionParam.newBuilder()//
                        .withCollectionName(tableName)//
                        .build());
            } finally {
                client.close();
            }

            try (Connection conn = DriverManager.getConnection(MILVUS_URL);//
                 Statement stmt = conn.createStatement()) {
                // UPDATE nearest to [0.1, 0.1] -> should be id=1
                int affected = stmt.executeUpdate("UPDATE " + tableName + " SET word_count = 999 ORDER BY book_intro <-> [0.1, 0.1] LIMIT 1");
                assertEquals(1, affected);
            }
            try (Connection conn = DriverManager.getConnection(MILVUS_URL);//
                 Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(STRONG_CONSISTENCY_HINT + "SELECT word_count FROM " + tableName + " WHERE book_id = 1")) {
                    assertTrue(rs.next());
                    assertEquals(999L, rs.getLong("word_count"));
                }
                try (ResultSet rs = stmt.executeQuery(STRONG_CONSISTENCY_HINT + "SELECT word_count FROM " + tableName + " WHERE book_id = 2")) {
                    assertTrue(rs.next());
                    assertEquals(200L, rs.getLong("word_count")); // Unchanged
                }
            }
        } finally {
            dropCollection(tableName);
        }
    }

    @Test
    public void testUpdateByVectorRange() throws Exception {
        if (!milvusReady) {
            return;
        }
        String tableName = "test_upd_rng_" + System.currentTimeMillis();
        try {
            createCollection(tableName);
            MilvusServiceClient client = newClient();
            try {
                client.insert(InsertParam.newBuilder()//
                        .withCollectionName(tableName)//
                        .withFields(Arrays.asList(//
                                new InsertParam.Field("book_id", Arrays.asList(1L, 2L, 3L)),//
                                new InsertParam.Field("word_count", Arrays.asList(100L, 200L, 300L)),//
                                new InsertParam.Field("book_intro", Arrays.asList(//
                                        Arrays.asList(0.1f, 0.1f), // dist=0 to [0.1,0.1]
                                        Arrays.asList(0.11f, 0.11f), // dist very small
                                        Arrays.asList(0.9f, 0.9f) // far
                                ))//
                        )).build());
                client.createIndex(CreateIndexParam.newBuilder()//
                        .withCollectionName(tableName)//
                        .withFieldName("book_intro")//
                        .withIndexName("idx_intro")//
                        .withIndexType(IndexType.IVF_FLAT)//
                        .withMetricType(MetricType.L2)//
                        .withExtraParam("{\"nlist\":1024}")//
                        .build());
                client.loadCollection(LoadCollectionParam.newBuilder()//
                        .withCollectionName(tableName)//
                        .build());
            } finally {
                client.close();
            }

            try (Connection conn = DriverManager.getConnection(MILVUS_URL);//
                 Statement stmt = conn.createStatement()) {
                // UPDATE range [0.1,0.1], radius=0.1
                // Should match 1 and 2
                int affected = stmt.executeUpdate("UPDATE " + tableName + " SET word_count = 888 WHERE vector_range(book_intro, [0.1, 0.1], 0.1)");
                assertEquals(2, affected);
            }
            try (Connection conn = DriverManager.getConnection(MILVUS_URL);//
                 Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(STRONG_CONSISTENCY_HINT + "SELECT word_count FROM " + tableName + " WHERE book_id IN (1, 2) AND word_count = 888")) {
                    int count = 0;
                    while (rs.next())
                        count++;
                    assertEquals(2, count);
                }
                try (ResultSet rs = stmt.executeQuery(STRONG_CONSISTENCY_HINT + "SELECT word_count FROM " + tableName + " WHERE book_id = 3")) {
                    assertTrue(rs.next());
                    assertEquals(300L, rs.getLong("word_count")); // Unchanged
                }
            }
        } finally {
            dropCollection(tableName);
        }
    }

    @Test
    public void testUpdateComplex() throws Exception {
        if (!milvusReady) {
            return;
        }

        String tableName = "test_update_complex_tb_" + System.currentTimeMillis();

        try {
            createCollection(tableName);

            // 1. Insert Multiple Data
            MilvusServiceClient client = newClient();
            client.insert(InsertParam.newBuilder()//
                    .withCollectionName(tableName)//
                    .withFields(Arrays.asList(//
                            new InsertParam.Field("book_id", Arrays.asList(1L, 2L, 3L, 4L)),//
                            new InsertParam.Field("word_count", Arrays.asList(1000L, 2000L, 3000L, 4000L)),//
                            new InsertParam.Field("book_intro", Arrays.asList(//
                                    Arrays.asList(0.1f, 0.1f),//
                                    Arrays.asList(0.2f, 0.2f),//
                                    Arrays.asList(0.3f, 0.3f),//
                                    Arrays.asList(0.4f, 0.4f)//
                            ))//
                    )).build());

            client.createIndex(CreateIndexParam.newBuilder()//
                    .withCollectionName(tableName)//
                    .withFieldName("book_intro")//
                    .withIndexName("idx_book_intro")//
                    .withIndexType(IndexType.IVF_FLAT)//
                    .withMetricType(MetricType.L2)//
                    .withExtraParam("{\"nlist\":1024}")//
                    .build());
            client.loadCollection(LoadCollectionParam.newBuilder().withCollectionName(tableName).build());
            client.close();

            // 2. JDBC Update with Complex WHERE
            // Condition: book_id IN [1, 2] OR (word_count > 2500 AND book_id = 3)
            // Matches: 1, 2, 3. (4 is excluded)
            // Update: word_count = 9999

            String updateSql = "UPDATE " + tableName + " SET word_count = 9999 WHERE book_id IN [1, 2] OR (word_count > 2500 AND book_id = 3)";

            try (Connection conn = DriverManager.getConnection(MILVUS_URL);//
                 Statement stmt = conn.createStatement()) {
                int affected = stmt.executeUpdate(updateSql);
                assertEquals(3, affected); // 1, 2, 3
            }

            // 3. Verify
            try (Connection conn = DriverManager.getConnection(MILVUS_URL); //
                 Statement stmt = conn.createStatement()) {

                // Check ID 1 (Updated)
                try (ResultSet rs = stmt.executeQuery(STRONG_CONSISTENCY_HINT + "SELECT word_count FROM " + tableName + " WHERE book_id = 1")) {
                    if (rs.next())
                        assertEquals(9999L, rs.getLong("word_count"));
                }
                // Check ID 3 (Updated)
                try (ResultSet rs = stmt.executeQuery(STRONG_CONSISTENCY_HINT + "SELECT word_count FROM " + tableName + " WHERE book_id = 3")) {
                    if (rs.next())
                        assertEquals(9999L, rs.getLong("word_count"));
                }
                // Check ID 4 (Not Updated)
                try (ResultSet rs = stmt.executeQuery(STRONG_CONSISTENCY_HINT + "SELECT word_count FROM " + tableName + " WHERE book_id = 4")) {
                    if (rs.next())
                        assertEquals(4000L, rs.getLong("word_count"));
                }
            }

        } finally {
            dropCollection(tableName);
        }
    }

    @Test
    public void testDelete() throws Exception {
        if (!milvusReady) {
            return;
        }

        try {
            dropCollection(TEST_COLLECTION);
            createCollection(TEST_COLLECTION);

            // 1. Prepare Data using SDK (Isolation)
            MilvusServiceClient client = newClient();
            client.insert(InsertParam.newBuilder()//
                    .withCollectionName(TEST_COLLECTION)//
                    .withFields(java.util.Arrays.asList(//
                            new InsertParam.Field("book_id", Collections.singletonList(1L)),//
                            new InsertParam.Field("word_count", Collections.singletonList(1000L)),//
                            new InsertParam.Field("book_intro", Collections.singletonList(java.util.Arrays.asList(0.1f, 0.2f)))//
                    )).build());

            client.createIndex(CreateIndexParam.newBuilder()//
                    .withCollectionName(TEST_COLLECTION)//
                    .withFieldName("book_intro")//
                    .withIndexName("idx_book_intro")//
                    .withIndexType(IndexType.IVF_FLAT)//
                    .withMetricType(MetricType.L2)//
                    .withExtraParam("{\"nlist\":1024}")//
                    .build());
            client.loadCollection(LoadCollectionParam.newBuilder().withCollectionName(TEST_COLLECTION).build());

            // 2. JDBC Delete
            try (Connection conn = DriverManager.getConnection(MILVUS_URL); java.sql.Statement stmt = conn.createStatement()) {
                int affected = stmt.executeUpdate("DELETE FROM " + TEST_COLLECTION + " WHERE book_id = 1");
                assertEquals(1, affected);
            }

            // Verify Delete using SDK
            R<QueryResults> queryResAfter = client.query(QueryParam.newBuilder()//
                    .withCollectionName(TEST_COLLECTION)//
                    .withExpr("book_id == 1")//
                    .withOutFields(Collections.singletonList("word_count"))//
                    .withConsistencyLevel(ConsistencyLevelEnum.STRONG)//
                    .build());

            assertEquals(R.Status.Success.getCode(), queryResAfter.getStatus().intValue());
            QueryResultsWrapper wrapper2 = new QueryResultsWrapper(queryResAfter.getData());
            assertTrue(wrapper2.getRowRecords().isEmpty());

            client.close();
        } finally {
            dropCollection(TEST_COLLECTION);
        }
    }

    @Test
    public void testDeleteComplex() throws Exception {
        if (!milvusReady) {
            return;
        }

        try {
            dropCollection(TEST_COLLECTION);
            createCollection(TEST_COLLECTION);

            // 1. Prepare Data
            MilvusServiceClient client = newClient();
            client.insert(InsertParam.newBuilder()//
                    .withCollectionName(TEST_COLLECTION)//
                    .withFields(java.util.Arrays.asList(//
                            new InsertParam.Field("book_id", java.util.Arrays.asList(1L, 2L, 3L, 4L)),//
                            new InsertParam.Field("word_count", java.util.Arrays.asList(1000L, 2000L, 3000L, 4000L)),//
                            new InsertParam.Field("book_intro", java.util.Arrays.asList(//
                                    java.util.Arrays.asList(0.1f, 0.1f),//
                                    java.util.Arrays.asList(0.2f, 0.2f),//
                                    java.util.Arrays.asList(0.3f, 0.3f),//
                                    java.util.Arrays.asList(0.4f, 0.4f)//
                            ))//
                    )).build());

            client.createIndex(CreateIndexParam.newBuilder()//
                    .withCollectionName(TEST_COLLECTION)//
                    .withFieldName("book_intro")//
                    .withIndexName("idx_book_intro")//
                    .withIndexType(IndexType.IVF_FLAT)//
                    .withMetricType(MetricType.L2)//
                    .withExtraParam("{\"nlist\":1024}")//
                    .build());
            client.loadCollection(LoadCollectionParam.newBuilder().withCollectionName(TEST_COLLECTION).build());

            // 2. JDBC Complex Delete
            // Delete if id in [1,2] OR (word_count > 2500 AND book_id = 3)
            // Note: Milvus Adapter Grammar uses [...] for lists, including IN clause.
            String deleteSQL = "DELETE FROM " + TEST_COLLECTION + " WHERE book_id IN [1, 2] OR (word_count > 2500 AND book_id = 3)";
            try (Connection conn = DriverManager.getConnection(MILVUS_URL); //
                 Statement stmt = conn.createStatement()) {
                int affected = stmt.executeUpdate(deleteSQL);
                assertEquals(3, affected);
            }

            // 3. Verify
            R<QueryResults> queryRes = client.query(QueryParam.newBuilder()//
                    .withCollectionName(TEST_COLLECTION)//
                    .withExpr("book_id > 0")//
                    .withOutFields(Collections.singletonList("book_id"))//
                    .withConsistencyLevel(ConsistencyLevelEnum.STRONG)//
                    .build());

            QueryResultsWrapper wrapper = new QueryResultsWrapper(queryRes.getData());
            assertEquals(1, wrapper.getRowRecords().size());
            assertEquals(4L, wrapper.getRowRecords().get(0).get("book_id"));

            client.close();
        } finally {
            dropCollection(TEST_COLLECTION);
        }
    }

    @Test
    public void testDeleteByVectorSearch() throws Exception {
        if (!milvusReady) {
            return;
        }

        try {
            dropCollection(TEST_COLLECTION);
            createCollection(TEST_COLLECTION);

            // 1. Prepare Data
            MilvusServiceClient client = newClient();
            client.insert(InsertParam.newBuilder()//
                    .withCollectionName(TEST_COLLECTION)//
                    .withFields(java.util.Arrays.asList(//
                            new InsertParam.Field("book_id", java.util.Arrays.asList(1L, 2L, 3L)),//
                            new InsertParam.Field("word_count", java.util.Arrays.asList(1000L, 2000L, 3000L)),//
                            new InsertParam.Field("book_intro", java.util.Arrays.asList(//
                                    java.util.Arrays.asList(0.1f, 0.1f),// Target likely matches this
                                    java.util.Arrays.asList(0.11f, 0.11f),// close
                                    java.util.Arrays.asList(0.9f, 0.9f)// far
                            ))//
                    )).build());

            client.createIndex(CreateIndexParam.newBuilder()//
                    .withCollectionName(TEST_COLLECTION)//
                    .withFieldName("book_intro")//
                    .withIndexName("idx_book_intro")//
                    .withIndexType(IndexType.IVF_FLAT)//
                    .withMetricType(MetricType.L2)//
                    .withExtraParam("{\"nlist\":1024}")//
                    .build());
            client.loadCollection(LoadCollectionParam.newBuilder().withCollectionName(TEST_COLLECTION).build());

            try (Connection conn = DriverManager.getConnection(MILVUS_URL); //
                 Statement stmt = conn.createStatement()) {
                String deleteSQL = "DELETE FROM " + TEST_COLLECTION + " ORDER BY book_intro <-> [0.1, 0.1] LIMIT 2";

                int affected = stmt.executeUpdate(deleteSQL);
                assertEquals(2, affected);
            }

            // 3. Verify
            R<QueryResults> queryRes = client.query(QueryParam.newBuilder()//
                    .withCollectionName(TEST_COLLECTION)//
                    .withExpr("book_id > 0")//
                    .withOutFields(Collections.singletonList("book_id"))//
                    .withConsistencyLevel(ConsistencyLevelEnum.STRONG)//
                    .build());

            QueryResultsWrapper wrapper = new QueryResultsWrapper(queryRes.getData());
            assertEquals(1, wrapper.getRowRecords().size());
            assertEquals(3L, wrapper.getRowRecords().get(0).get("book_id")); // Only 3 remains

            client.close();
        } finally {
            dropCollection(TEST_COLLECTION);
        }
    }

    @Test
    public void testDeleteByVectorRange() throws Exception {
        if (!milvusReady) {
            return;
        }

        try {
            dropCollection(TEST_COLLECTION);
            createCollection(TEST_COLLECTION);

            // 1. Prepare Data
            MilvusServiceClient client = newClient();
            client.insert(InsertParam.newBuilder()//
                    .withCollectionName(TEST_COLLECTION)//
                    .withFields(java.util.Arrays.asList(//
                            new InsertParam.Field("book_id", java.util.Arrays.asList(1L, 2L, 3L)),//
                            new InsertParam.Field("word_count", java.util.Arrays.asList(100L, 200L, 300L)),//
                            new InsertParam.Field("book_intro", java.util.Arrays.asList(//
                                    java.util.Arrays.asList(0.1f, 0.1f),// Target (almost)
                                    java.util.Arrays.asList(0.9f, 0.9f),// Far
                                    java.util.Arrays.asList(0.11f, 0.11f)// Close
                            ))//
                    )).build());

            client.createIndex(CreateIndexParam.newBuilder()//
                    .withCollectionName(TEST_COLLECTION)//
                    .withFieldName("book_intro")//
                    .withIndexName("idx_book_intro")//
                    .withIndexType(IndexType.IVF_FLAT)//
                    .withMetricType(MetricType.L2)//
                    .withExtraParam("{\"nlist\":1024}")//
                    .build());
            client.loadCollection(LoadCollectionParam.newBuilder().withCollectionName(TEST_COLLECTION).build());

            // 2. Range Delete: DISTANCE(book_intro, [0.1, 0.1]) < 0.1
            // 0.1,0.1 vs 0.1,0.1 -> dist 0
            // 0.1,0.1 vs 0.11,0.11 -> diff 0.01, 0.01. L2 = 0.0002.
            // 0.9,0.9 vs 0.1,0.1 -> diff 0.8, 0.8. L2 = 1.28.

            // If we use < 0.1, we should delete ID 1 and 3. ID 2 remains.

            String deleteSQL = "DELETE FROM " + TEST_COLLECTION + " WHERE book_intro <-> [0.1, 0.1] < 0.1";
            try (Connection conn = DriverManager.getConnection(MILVUS_URL);//
                 Statement stmt = conn.createStatement()) {
                int affected = stmt.executeUpdate(deleteSQL);
                assertEquals(2, affected);
            }

            // 3. Verify
            R<QueryResults> queryRes = client.query(QueryParam.newBuilder()//
                    .withCollectionName(TEST_COLLECTION)//
                    .withExpr("book_id > 0")//
                    .withOutFields(Collections.singletonList("book_id"))//
                    .withConsistencyLevel(ConsistencyLevelEnum.STRONG)//
                    .build());

            QueryResultsWrapper wrapper = new QueryResultsWrapper(queryRes.getData());
            assertEquals(1, wrapper.getRowRecords().size());
            assertEquals(2L, wrapper.getRowRecords().get(0).get("book_id"));

            client.close();
        } finally {
            dropCollection(TEST_COLLECTION);
        }
    }

    @Test
    public void testDeleteByMixedConditions() throws Exception {
        if (!milvusReady) {
            return;
        }
        try {
            dropCollection(TEST_COLLECTION);
            createCollection(TEST_COLLECTION);

            // 1. Prepare Data
            MilvusServiceClient client = newClient();
            client.insert(InsertParam.newBuilder()//
                    .withCollectionName(TEST_COLLECTION)//
                    .withFields(java.util.Arrays.asList(//
                            new InsertParam.Field("book_id", java.util.Arrays.asList(1L, 2L, 3L)),//
                            new InsertParam.Field("word_count", java.util.Arrays.asList(100L, 200L, 300L)),// values
                            new InsertParam.Field("book_intro", java.util.Arrays.asList(//
                                    java.util.Arrays.asList(0.1f, 0.1f),// ID 1: Dist=0, Count=100
                                    java.util.Arrays.asList(0.9f, 0.9f),// ID 2: Dist=Large, Count=200
                                    java.util.Arrays.asList(0.11f, 0.11f)// ID 3: Dist=Small, Count=300
                            ))//
                    )).build());

            client.createIndex(CreateIndexParam.newBuilder()//
                    .withCollectionName(TEST_COLLECTION)//
                    .withFieldName("book_intro")//
                    .withIndexName("idx_book_intro")//
                    .withIndexType(IndexType.IVF_FLAT)//
                    .withMetricType(MetricType.L2)//
                    .withExtraParam("{\"nlist\":1024}")//
                    .build());
            client.loadCollection(LoadCollectionParam.newBuilder().withCollectionName(TEST_COLLECTION).build());

            // 2. Mixed Delete: Dist < 0.1 AND word_count > 200
            // ID 1: Dist OK (0 < 0.1), Count Fail (100 > 200 is False) -> Keep
            // ID 2: Dist Fail, Count Fail -> Keep
            // ID 3: Dist OK (approx 0.0002 < 0.1), Count OK (300 > 200 is True) -> DELETE

            String deleteSQL = "DELETE FROM " + TEST_COLLECTION + " WHERE book_intro <-> [0.1, 0.1] < 0.1 AND word_count > 200";
            try (Connection conn = DriverManager.getConnection(MILVUS_URL); java.sql.Statement stmt = conn.createStatement()) {
                int affected = stmt.executeUpdate(deleteSQL);
                assertEquals(1, affected);
            }

            // 3. Verify
            R<QueryResults> queryRes = client.query(QueryParam.newBuilder()//
                    .withCollectionName(TEST_COLLECTION)//
                    .withExpr("book_id > 0")//
                    .withOutFields(Collections.singletonList("book_id"))//
                    .withConsistencyLevel(ConsistencyLevelEnum.STRONG)//
                    .build());

            QueryResultsWrapper wrapper = new QueryResultsWrapper(queryRes.getData());
            assertEquals(2, wrapper.getRowRecords().size());

            // Check IDs 1 and 2 exist
            java.util.Set<Long> remainingIds = new java.util.HashSet<>();
            wrapper.getRowRecords().forEach(r -> remainingIds.add((Long) r.get("book_id")));
            assertTrue(remainingIds.contains(1L));
            assertTrue(remainingIds.contains(2L));

            client.close();
        } finally {
            dropCollection(TEST_COLLECTION);
        }
    }

    @Test
    public void testLoadReleaseCollection() throws Exception {
        if (!milvusReady) {
            return;
        }
        try {
            dropCollection(TEST_COLLECTION);
            createCollection(TEST_COLLECTION);
            MilvusServiceClient client = newClient();
            // Create Index (Required for Load)
            client.createIndex(CreateIndexParam.newBuilder()//
                    .withCollectionName(TEST_COLLECTION)//
                    .withFieldName("book_intro")//
                    .withIndexName("idx_book_intro")//
                    .withIndexType(IndexType.IVF_FLAT)//
                    .withMetricType(MetricType.L2)//
                    .withExtraParam("{\"nlist\":1024}")//
                    .build());

            // Verify Before: Not Load
            R<GetLoadStateResponse> stateBefore = client.getLoadState(GetLoadStateParam.newBuilder()//
                    .withCollectionName(TEST_COLLECTION).build());
            assertEquals(LoadState.LoadStateNotLoad, stateBefore.getData().getState());
            client.close();

            try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
                // 1. Load Collection
                stmt.execute("LOAD TABLE " + TEST_COLLECTION);

                // Verify After Load: Loaded
                client = newClient();
                R<GetLoadStateResponse> stateLoaded = client.getLoadState(GetLoadStateParam.newBuilder()//
                        .withCollectionName(TEST_COLLECTION).build());
                assertEquals(LoadState.LoadStateLoaded, stateLoaded.getData().getState());
                client.close();

                // 2. Release Collection
                stmt.execute("RELEASE TABLE " + TEST_COLLECTION);

                // Verify After Release: Not Load
                client = newClient();
                R<GetLoadStateResponse> stateReleased = client.getLoadState(GetLoadStateParam.newBuilder()//
                        .withCollectionName(TEST_COLLECTION).build());
                assertEquals(LoadState.LoadStateNotLoad, stateReleased.getData().getState());
                client.close();
            }
        } finally {
            dropCollection(TEST_COLLECTION);
        }
    }

    @Test
    public void testLoadReleasePartition() throws Exception {
        if (!milvusReady) {
            return;
        }
        try {
            dropCollection(TEST_COLLECTION);
            createCollection(TEST_COLLECTION);
            MilvusServiceClient client = newClient();
            client.createIndex(CreateIndexParam.newBuilder()//
                    .withCollectionName(TEST_COLLECTION)//
                    .withFieldName("book_intro")//
                    .withIndexName("idx_book_intro")//
                    .withIndexType(IndexType.IVF_FLAT)//
                    .withMetricType(MetricType.L2)//
                    .withExtraParam("{\"nlist\":1024}")//
                    .build());
            client.close();

            try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE PARTITION p1 ON TABLE " + TEST_COLLECTION);

                // Verify Before: Partition Not Load
                client = newClient();
                R<GetLoadStateResponse> stateBefore = client.getLoadState(GetLoadStateParam.newBuilder() //
                        .withCollectionName(TEST_COLLECTION).withPartitionNames(Collections.singletonList("p1")).build());
                assertEquals(LoadState.LoadStateNotLoad, stateBefore.getData().getState());
                client.close();

                // 1. Load Partition
                stmt.execute("LOAD TABLE " + TEST_COLLECTION + " PARTITION p1");

                // Verify After Load: Partition Loaded
                client = newClient();
                R<GetLoadStateResponse> stateLoaded = client.getLoadState(GetLoadStateParam.newBuilder() //
                        .withCollectionName(TEST_COLLECTION).withPartitionNames(Collections.singletonList("p1")).build());
                assertEquals(LoadState.LoadStateLoaded, stateLoaded.getData().getState());
                client.close();

                // 2. Release Partition
                stmt.execute("RELEASE TABLE " + TEST_COLLECTION + " PARTITION p1");

                // Verify After Release: Partition Not Load
                client = newClient();
                R<GetLoadStateResponse> stateReleased = client.getLoadState(GetLoadStateParam.newBuilder() //
                        .withCollectionName(TEST_COLLECTION).withPartitionNames(Collections.singletonList("p1")).build());
                assertEquals(LoadState.LoadStateNotLoad, stateReleased.getData().getState());
                client.close();
            }
        } finally {
            dropCollection(TEST_COLLECTION);
        }
    }

    @Test
    public void testImport() throws Exception {
        if (!milvusReady) {
            return;
        }
        try {
            dropCollection(TEST_COLLECTION);
            createCollection(TEST_COLLECTION);

            // 1. Prepare Data locally
            File tempFile = File.createTempFile("milvus_import_", ".json");
            try (InputStream inStream = ResourcesUtils.getResourceAsStream("test_data/import_data.json");//
                 OutputStream outStream = new FileOutputStream(tempFile)) {
                IOUtils.copy(inStream, outStream);
            }

            // 2. Upload to MinIO
            MinioClient minioClient = MinioClient.builder()//
                    .endpoint("http://127.0.0.1:19001")//
                    .credentials("minioadmin", "minioadmin")//
                    .build();

            String objectName = "import_test_" + System.currentTimeMillis() + ".json";
            List<Bucket> buckets = minioClient.listBuckets();
            if (buckets.isEmpty())
                throw new IllegalStateException("No buckets in MinIO");

            for (Bucket bucket : buckets) {
                minioClient.uploadObject(UploadObjectArgs.builder()//
                        .bucket(bucket.name())//
                        .object(objectName)//
                        .filename(tempFile.getAbsolutePath())//
                        .build());
                // Try files/ prefix as well, common default rootPath
                minioClient.uploadObject(UploadObjectArgs.builder()//
                        .bucket(bucket.name())//
                        .object("files/" + objectName)//
                        .filename(tempFile.getAbsolutePath())//
                        .build());
            }

            // Cleanup local file
            tempFile.delete();

            // 3. Execute IMPORT (Server-side via MinIO object path)
            try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
                stmt.execute("IMPORT FROM '" + objectName + "' INTO " + TEST_COLLECTION);
            }

            // 4. Verify Import using SDK
            MilvusServiceClient client = newClient();

            // Create Index & Load (Required for Query)
            client.createIndex(CreateIndexParam.newBuilder()//
                    .withCollectionName(TEST_COLLECTION)    //
                    .withFieldName("book_intro")            //
                    .withIndexName("idx_book_intro")        //
                    .withIndexType(IndexType.IVF_FLAT)      //
                    .withMetricType(MetricType.L2)          //
                    .withExtraParam("{\"nlist\":1024}")     //
                    .build());

            // Wait loop for import data visibility
            long endTime = System.currentTimeMillis() + 60000; // 60s
            boolean dataFound = false;

            // Retry loading and querying
            while (System.currentTimeMillis() < endTime) {
                try {
                    R<GetLoadStateResponse> loadState = client.getLoadState(GetLoadStateParam.newBuilder().withCollectionName(TEST_COLLECTION).build());
                    if (loadState.getData().getState() != LoadState.LoadStateLoaded) {
                        client.loadCollection(LoadCollectionParam.newBuilder().withCollectionName(TEST_COLLECTION).build());
                    }

                    R<QueryResults> queryRes = client.query(QueryParam.newBuilder()//
                            .withCollectionName(TEST_COLLECTION)//
                            .withExpr("book_id > 0")//
                            .withOutFields(Collections.singletonList("word_count"))//
                            .build());

                    if (queryRes.getStatus() == R.Status.Success.getCode()) {
                        QueryResultsWrapper wrapper = new QueryResultsWrapper(queryRes.getData());
                        if (!wrapper.getRowRecords().isEmpty()) {
                            dataFound = true;
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Thread.sleep(500);
            }

            // Cleanup MinIO finally
            for (Bucket bucket : buckets) {
                try {
                    minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket.name()).object(objectName).build());
                    minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket.name()).object("files/" + objectName).build());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            assertTrue("Data should be imported", dataFound);
            client.close();

        } finally {
            dropCollection(TEST_COLLECTION);
        }
    }
}
