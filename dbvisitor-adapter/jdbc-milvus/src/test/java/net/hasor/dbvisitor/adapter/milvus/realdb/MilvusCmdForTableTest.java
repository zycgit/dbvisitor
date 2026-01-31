package net.hasor.dbvisitor.adapter.milvus.realdb;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MilvusCmdForTableTest extends AbstractMilvusCmdForTest {
    @Before
    public void setUp() {
        if (!milvusReady) {
            return;
        }
        dropCollection(TEST_COLLECTION);
        dropCollection(TEST_COLLECTION_NEW);
    }

    @After
    public void tearDown() {
        if (!milvusReady) {
            return;
        }
        dropCollection(TEST_COLLECTION);
        dropCollection(TEST_COLLECTION_NEW);
    }

    @Test
    public void testCreateTable() throws Exception {
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE " + TEST_COLLECTION + " (" +//
                    "book_id int64 primary key, " +                     //
                    "word_count int64, " +                              //
                    "book_intro float_vector(2)" +                      //
                    ")");

            assertTrue("Table should exist", hasCollection(TEST_COLLECTION));
        }
    }

    @Test
    public void testCreateTableIfNotExists() throws Exception {
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + TEST_COLLECTION + " (" +//
                    "book_id int64 primary key, " +                         //
                    "word_count int64, " +                                  //
                    "book_intro float_vector(2)" +                          //
                    ")";
            stmt.executeUpdate(sql);
            assertTrue(hasCollection(TEST_COLLECTION));

            // Should not fail
            stmt.executeUpdate(sql);
            assertTrue(hasCollection(TEST_COLLECTION));
        }
    }

    @Test
    public void testDropTable() throws Exception {
        createCollection(TEST_COLLECTION);
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            assertTrue(hasCollection(TEST_COLLECTION));

            stmt.executeUpdate("DROP TABLE " + TEST_COLLECTION);
            assertFalse("Table should be dropped", hasCollection(TEST_COLLECTION));
        }
    }

    @Test
    public void testDropTableIfExists() throws Exception {
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            // Should not fail if not exists
            stmt.executeUpdate("DROP TABLE IF EXISTS " + TEST_COLLECTION);

            createCollection(TEST_COLLECTION);
            stmt.executeUpdate("DROP TABLE IF EXISTS " + TEST_COLLECTION);

            assertFalse(hasCollection(TEST_COLLECTION));
        }
    }

    @Test
    public void testRenameTable() throws Exception {
        createCollection(TEST_COLLECTION);
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            assertTrue(hasCollection(TEST_COLLECTION));
            assertFalse(hasCollection(TEST_COLLECTION_NEW));

            stmt.executeUpdate("ALTER TABLE " + TEST_COLLECTION + " RENAME TO " + TEST_COLLECTION_NEW);

            assertFalse("Old table should not exist", hasCollection(TEST_COLLECTION));
            assertTrue("New table should exist", hasCollection(TEST_COLLECTION_NEW));
        }
    }

    @Test
    public void testShowTables() throws Exception {
        createCollection(TEST_COLLECTION);
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            boolean found = false;
            try (ResultSet rs = stmt.executeQuery("SHOW TABLES")) {
                while (rs.next()) {
                    if (TEST_COLLECTION.equals(rs.getString("TABLE"))) {
                        found = true;
                        break;
                    }
                }
            }
            assertTrue("Should find the test table in SHOW TABLES", found);
        }
    }

    @Test
    public void testShowTable() throws Exception {
        createCollection(TEST_COLLECTION);
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            boolean found = false;
            try (ResultSet rs = stmt.executeQuery("SHOW TABLE " + TEST_COLLECTION)) {
                while (rs.next()) {
                    if ("book_id".equals(rs.getString("FIELD"))) {
                        found = true;
                        assertTrue(rs.getBoolean("PRIMARY"));
                        break;
                    }
                }
            }
            assertTrue("Should find column book_id in SHOW TABLE", found);
        }
    }

    @Test
    public void testShowCreateTable() throws Exception {
        createCollection(TEST_COLLECTION);
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            boolean found = false;
            try (ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE " + TEST_COLLECTION)) {
                if (rs.next()) {
                    found = true;
                    String tableName = rs.getString("TABLE");
                    String createSql = rs.getString("CREATE SCRIPT");

                    assertTrue(TEST_COLLECTION.equalsIgnoreCase(tableName));
                    assertTrue(createSql.toUpperCase().startsWith("CREATE TABLE"));
                    assertTrue(createSql.contains(TEST_COLLECTION));
                    assertTrue(createSql.contains("int64"));
                    assertTrue(createSql.contains("float_vector(2)"));
                }
            }
            assertTrue("Should get result from SHOW CREATE TABLE", found);
        }
    }

    @Test
    public void testCreateTableWithComment() throws Exception {
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE " + TEST_COLLECTION + " (" + //
                    "book_id int64 primary key COMMENT 'ID of book', " + //
                    "word_count int64 COMMENT 'Count of words', " + //
                    "book_intro float_vector(2) COMMENT 'Intro vector'" + //
                    ")";
            stmt.executeUpdate(sql);

            assertTrue("Table should exist", hasCollection(TEST_COLLECTION));

            try (ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE " + TEST_COLLECTION)) {
                if (rs.next()) {
                    String createSql = rs.getString("CREATE SCRIPT");
                    assertTrue(createSql.contains("COMMENT 'ID of book'"));
                    assertTrue(createSql.contains("COMMENT 'Count of words'"));
                    assertTrue(createSql.contains("COMMENT 'Intro vector'"));
                } else {
                    throw new RuntimeException("SHOW CREATE TABLE failed");
                }
            }
        }
    }

    @Test
    public void testCreateTableWithSpecialComment() throws Exception {
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE " + TEST_COLLECTION + " (" + //
                    "book_id int64 primary key COMMENT 'ID''s of book', " + //
                    "word_count int64 COMMENT 'Count of words', " + //
                    "book_intro float_vector(2) COMMENT 'Intro vector'" + //
                    ")";
            stmt.executeUpdate(sql);

            assertTrue("Table should exist", hasCollection(TEST_COLLECTION));

            try (ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE " + TEST_COLLECTION)) {
                if (rs.next()) {
                    String createSql = rs.getString("CREATE SCRIPT");
                    // System.out.println(createSql);
                    assertTrue(createSql.contains("COMMENT 'ID''s of book'"));
                } else {
                    throw new RuntimeException("SHOW CREATE TABLE failed");
                }
            }
        }
    }

    @Test
    public void testCreateTableAllTypes() throws Exception {
        String collectionName = "test_all_types";
        dropCollection(collectionName);

        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE " + collectionName + " (" + //
                    "id int64 primary key auto_id COMMENT 'Primary Key', " + //
                    "col_bool bool COMMENT 'Boolean column', " + //
                    "col_int8 int8, " + //
                    "col_int16 int16, " + //
                    "col_int32 int32, " + //
                    "col_float float, " + //
                    "col_double double, " + //
                    "col_varchar varchar(256) COMMENT 'String column', " + //
                    "col_json json, " + //
                    "col_float_vector float_vector(128) COMMENT 'Float Vector', " + //
                    "col_binary_vector binary_vector(128), " + //
                    "col_float16_vector float16_vector(128), " + //
                    "col_bfloat16_vector bfloat16_vector(128) " + //
                    // "col_sparse_vector sparse_float_vector(100)" + // Sparse vector support might vary by Milvus version or SDK
                    ")";
            stmt.executeUpdate(sql);

            assertTrue("Table should exist", hasCollection(collectionName));

            // Verify with Milvus native SDK
            io.milvus.client.MilvusServiceClient client = newClient();
            try {
                io.milvus.param.collection.DescribeCollectionParam descParam = io.milvus.param.collection.DescribeCollectionParam.newBuilder().withCollectionName(collectionName).build();
                io.milvus.param.R<io.milvus.grpc.DescribeCollectionResponse> response = client.describeCollection(descParam);

                if (response.getStatus() != io.milvus.param.R.Status.Success.getCode()) {
                    throw new RuntimeException(response.getMessage());
                }

                io.milvus.grpc.CollectionSchema schema = response.getData().getSchema();
                java.util.Map<String, io.milvus.grpc.FieldSchema> fieldMap = new java.util.HashMap<>();
                for (io.milvus.grpc.FieldSchema field : schema.getFieldsList()) {
                    fieldMap.put(field.getName(), field);
                }

                // Verify ID
                io.milvus.grpc.FieldSchema idField = fieldMap.get("id");
                Assert.assertEquals(io.milvus.grpc.DataType.Int64, idField.getDataType());
                Assert.assertTrue(idField.getIsPrimaryKey());
                Assert.assertTrue(idField.getAutoID());
                Assert.assertEquals("Primary Key", idField.getDescription());

                // Verify Bool
                io.milvus.grpc.FieldSchema boolField = fieldMap.get("col_bool");
                Assert.assertEquals(io.milvus.grpc.DataType.Bool, boolField.getDataType());
                Assert.assertEquals("Boolean column", boolField.getDescription());

                // Verify Int8
                Assert.assertEquals(io.milvus.grpc.DataType.Int8, fieldMap.get("col_int8").getDataType());
                // Verify Int16
                Assert.assertEquals(io.milvus.grpc.DataType.Int16, fieldMap.get("col_int16").getDataType());
                // Verify Int32
                Assert.assertEquals(io.milvus.grpc.DataType.Int32, fieldMap.get("col_int32").getDataType());
                // Verify Float
                Assert.assertEquals(io.milvus.grpc.DataType.Float, fieldMap.get("col_float").getDataType());
                // Verify Double
                Assert.assertEquals(io.milvus.grpc.DataType.Double, fieldMap.get("col_double").getDataType());
                // Verify JSON
                Assert.assertEquals(io.milvus.grpc.DataType.JSON, fieldMap.get("col_json").getDataType());

                // Verify Varchar
                io.milvus.grpc.FieldSchema varcharField = fieldMap.get("col_varchar");
                Assert.assertEquals(io.milvus.grpc.DataType.VarChar, varcharField.getDataType());
                boolean foundMaxLength = false;
                for (io.milvus.grpc.KeyValuePair kv : varcharField.getTypeParamsList()) {
                    if ("max_length".equals(kv.getKey())) {
                        Assert.assertEquals("256", kv.getValue());
                        foundMaxLength = true;
                    }
                }
                Assert.assertTrue("Should have max_length parameter", foundMaxLength);
                Assert.assertEquals("String column", varcharField.getDescription());

                // Verify Float Vector
                io.milvus.grpc.FieldSchema floatVectorField = fieldMap.get("col_float_vector");
                Assert.assertEquals(io.milvus.grpc.DataType.FloatVector, floatVectorField.getDataType());
                boolean foundDim = false;
                for (io.milvus.grpc.KeyValuePair kv : floatVectorField.getTypeParamsList()) {
                    if ("dim".equals(kv.getKey())) {
                        Assert.assertEquals("128", kv.getValue());
                        foundDim = true;
                    }
                }
                Assert.assertTrue("Should have dim parameter", foundDim);
                Assert.assertEquals("Float Vector", floatVectorField.getDescription());

                // Verify Binary Vector (Binary vector dimension must be multiple of 8, 128 is fine)
                io.milvus.grpc.FieldSchema binaryVectorField = fieldMap.get("col_binary_vector");
                Assert.assertEquals(io.milvus.grpc.DataType.BinaryVector, binaryVectorField.getDataType());
                // Verify Float16 Vector
                Assert.assertEquals(io.milvus.grpc.DataType.Float16Vector, fieldMap.get("col_float16_vector").getDataType());
                // Verify BFloat16 Vector
                Assert.assertEquals(io.milvus.grpc.DataType.BFloat16Vector, fieldMap.get("col_bfloat16_vector").getDataType());

            } finally {
                client.close();
                dropCollection(collectionName);
            }
        }
    }
}
