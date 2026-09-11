package net.hasor.dbvisitor.adapter.milvus.commands;

import java.nio.ByteBuffer;
import java.sql.*;
import java.util.*;
import io.milvus.grpc.*;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.SearchIteratorReqV2;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.UpsertReq;
import io.milvus.v2.utils.DataUtils;
import io.milvus.v2.utils.SchemaUtils;
import io.milvus.v2.utils.VectorUtils;
import net.hasor.dbvisitor.adapter.milvus.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Test;
import static net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.v2Response;
import static org.junit.Assert.*;

/** JDBC -> official SDK -> protobuf -> SDK -> JDBC, without a running server. */
public class MilvusTypesTest extends AbstractJdbcTest {
    private DescribeCollectionResp schema;
    private List<FieldData>        stored = Collections.emptyList();
    private SearchRequest          searched;
    private SearchIteratorReqV2    iterated;

    private Connection connect() throws SQLException {
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            switch (method.getName()) {
                case "createCollection":
                    CreateCollectionReq create = (CreateCollectionReq) args[0];
                    schema = DescribeCollectionResp.builder().collectionName("t").collectionSchema(create.getCollectionSchema()).build();
                    return null;
                case "describeCollection":
                    return schema;
                case "insert":
                    stored = new DataUtils.InsertBuilderWrapper().convertGrpcInsertRequest((InsertReq) args[0], schema).getFieldsDataList();
                    return v2Response("insert", MutationResult.newBuilder().setInsertCnt(1).build());
                case "upsert":
                    stored = new DataUtils.InsertBuilderWrapper().convertGrpcUpsertRequest((UpsertReq) args[0], schema).getFieldsDataList();
                    return v2Response("upsert", MutationResult.newBuilder().setUpsertCnt(1).build());
                case "query":
                case "queryIterator":
                    QueryResults.Builder query = QueryResults.newBuilder();
                    for (FieldData field : stored) {
                        if (field.getType() == DataType.JSON && field.getValidDataCount() > field.getScalars().getJsonData().getDataCount()) {
                            JSONArray.Builder json = JSONArray.newBuilder();
                            int source = 0;
                            for (boolean valid : field.getValidDataList()) {
                                json.addData(valid ? field.getScalars().getJsonData().getData(source++) : com.google.protobuf.ByteString.copyFromUtf8("null"));
                            }
                            field = field.toBuilder().setScalars(field.getScalars().toBuilder().setJsonData(json)).build();
                        }
                        if (field.getType() == DataType.Bool && field.getValidDataCount() > field.getScalars().getBoolData().getDataCount()) {
                            BoolArray.Builder bools = BoolArray.newBuilder();
                            int source = 0;
                            for (boolean valid : field.getValidDataList()) {
                                bools.addData(valid && field.getScalars().getBoolData().getData(source++));
                            }
                            field = field.toBuilder().setScalars(field.getScalars().toBuilder().setBoolData(bools)).build();
                        }
                        // INSERT omits ArrayArray.element_type; the server supplies it on reads.
                        if (field.getType() == DataType.Array) {
                            String name = field.getFieldName();
                            io.milvus.grpc.FieldSchema definition = schema.getCollectionSchema().getFieldSchemaList().stream().filter(f -> f.getName().equals(name)).map(SchemaUtils::convertToGrpcFieldSchema).findFirst().orElseThrow();
                            field = field.toBuilder().setScalars(field.getScalars().toBuilder().setArrayData(field.getScalars().getArrayData().toBuilder().setElementType(definition.getElementType()))).build();
                            // Query responses carry one Array row per entity, including NULL placeholders.
                            if (field.getValidDataCount() > field.getScalars().getArrayData().getDataCount()) {
                                ArrayArray.Builder array = field.getScalars().getArrayData().toBuilder().clearData();
                                int source = 0;
                                for (boolean valid : field.getValidDataList()) {
                                    array.addData(valid ? field.getScalars().getArrayData().getData(source++) : ScalarField.getDefaultInstance());
                                }
                                field = field.toBuilder().setScalars(field.getScalars().toBuilder().setArrayData(array)).build();
                            }
                        }
                        query.addFieldsData(field);
                    }
                    return v2Response(method.getName(), query.build());
                case "search":
                    searched = new VectorUtils().ConvertToGrpcSearchRequest((SearchReq) args[0]);
                    return v2Response("search", SearchResults.newBuilder().setResults(SearchResultData.newBuilder().setNumQueries(1).setTopK(0)).build());
                case "searchIteratorV2":
                    iterated = (SearchIteratorReqV2) args[0];
                    io.milvus.orm.iterator.SearchIteratorV2 iterator = org.mockito.Mockito.mock(io.milvus.orm.iterator.SearchIteratorV2.class);
                    org.mockito.Mockito.when(iterator.next()).thenReturn(Collections.emptyList());
                    return iterator;
                default:
                    return null;
            }
        });
        Properties props = new Properties();
        props.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        props.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://test:19530", props);
    }

    @After
    public void cleanup() {
        MilvusCommandInterceptor.resetInterceptor();
    }

    @Test
    public void jdbcTemporalValuesShouldRoundTripAsStableVarcharText() throws Exception {
        java.sql.Date date = java.sql.Date.valueOf("2026-09-10");
        Time time = Time.valueOf("08:09:10");
        Timestamp timestamp = Timestamp.valueOf("2026-09-10 08:09:10.123456789");
        java.util.Date utilDate = new java.util.Date(timestamp.getTime());
        try (Connection conn = connect(); Statement statement = conn.createStatement()) {
            statement.executeUpdate("CREATE TABLE t (id INT64 PRIMARY KEY,d VARCHAR(128),tm VARCHAR(128),ts VARCHAR(128),u VARCHAR(128))");
            for (String operation : Arrays.asList("INSERT", "UPSERT")) {
                try (PreparedStatement insert = conn.prepareStatement(operation + " INTO t (id,d,tm,ts,u) VALUES (1,?,?,?,?)")) {
                    insert.setDate(1, date);
                    insert.setTime(2, time);
                    insert.setTimestamp(3, timestamp);
                    insert.setObject(4, utilDate);
                    assertEquals(1, insert.executeUpdate());
                }
                try (ResultSet rows = statement.executeQuery("SELECT id,d,tm,ts,u FROM t LIMIT 1")) {
                    assertTrue(rows.next());
                    assertEquals(date.toString(), rows.getString("d"));
                    assertEquals(time.toString(), rows.getString("tm"));
                    assertEquals(timestamp.toString(), rows.getString("ts"));
                    assertEquals(new Timestamp(utilDate.getTime()).toString(), rows.getString("u"));
                    assertEquals(date, rows.getDate("d"));
                    assertEquals(time, rows.getTime("tm"));
                    assertEquals(timestamp, rows.getTimestamp("ts"));
                    assertEquals(utilDate.getTime(), rows.getTimestamp("u").getTime());
                    assertEquals(Types.VARCHAR, rows.getMetaData().getColumnType(4));
                }
            }
        }
    }

    @Test
    public void allVectorFormatsRoundTripThroughOfficialWireEncoder() throws Exception {
        String[] types = { "BINARY_VECTOR(16)", "FLOAT16_VECTOR(2)", "BFLOAT16_VECTOR(2)", "SPARSE_FLOAT_VECTOR", "INT8_VECTOR(2)" };
        Object[] values = { new byte[] { 1, -1 }, new float[] { 1, -2 }, new double[] { 1, -2 }, Map.of(3, 1F, 900L, -2F), new byte[] { -128, 127 } };
        PlaceholderType[] placeholders = { PlaceholderType.BinaryVector, PlaceholderType.Float16Vector, PlaceholderType.BFloat16Vector, PlaceholderType.SparseFloatVector, PlaceholderType.Int8Vector };
        for (int i = 0; i < types.length; i++) {
            try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE TABLE t (id INT64 PRIMARY KEY, v " + types[i] + ")");
                for (String op : Arrays.asList("INSERT", "UPSERT")) {
                    try (PreparedStatement ps = conn.prepareStatement(op + " INTO t (id,v) VALUES (1,?)")) {
                        ps.setObject(1, values[i]);
                        assertEquals(1, ps.executeUpdate());
                    }
                    for (String limit : Arrays.asList(" LIMIT 1", "")) {
                        try (ResultSet rs = stmt.executeQuery("SELECT id,v FROM t" + limit)) {
                            assertTrue(rs.next());
                            assertEquals(1, rs.getLong("id"));
                            if (i == 3) {
                                assertEquals(new TreeMap<>(Map.of(3L, 1F, 900L, -2F)), rs.getObject("v"));
                                assertEquals(Types.OTHER, rs.getMetaData().getColumnType(2));
                            } else {
                                byte[] bytes = rs.getBytes("v");
                                assertEquals(i == 0 || i == 4 ? 2 : 4, bytes.length);
                                if (i == 0) {
                                    assertArrayEquals(new byte[] { 1, -1 }, bytes);
                                }
                                if (i == 1) {
                                    assertArrayEquals(new byte[] { 0, 60, 0, -64 }, bytes);
                                }
                                if (i == 2) {
                                    assertArrayEquals(new byte[] { -128, 63, 0, -64 }, bytes);
                                }
                                if (i == 4) {
                                    assertArrayEquals(new byte[] { -128, 127 }, bytes);
                                    assertEquals(Types.VARBINARY, rs.getMetaData().getColumnType(2));
                                }
                                // Packed bytes are accepted unchanged by both write and search paths.
                                values[i] = ByteBuffer.wrap(bytes);
                            }
                            assertFalse(rs.next());
                        }
                    }
                    try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM t ORDER BY v " + (i == 0 ? "~=" : i == 3 ? "<#>" : "<->") + " ? LIMIT 2")) {
                        ps.setObject(1, values[i]);
                        try (ResultSet rs = ps.executeQuery()) {
                            assertFalse(rs.next());
                        }
                        assertEquals(placeholders[i], PlaceholderGroup.parseFrom(searched.getPlaceholderGroup()).getPlaceholders(0).getType());
                    }
                    String distance = i == 0 ? "~=" : i == 3 ? "<#>" : "<->";
                    for (String command : Arrays.asList("SELECT id FROM t", "UPDATE t SET v = ?", "DELETE FROM t")) {
                        for (String predicate : Arrays.asList(" ORDER BY v " + distance + " ?", " WHERE v " + distance + " ? " + (i == 3 ? "> 0" : "< 10"))) {
                            try (PreparedStatement ps = conn.prepareStatement(command + predicate)) {
                                int arg = 1;
                                if (command.startsWith("UPDATE")) {
                                    ps.setObject(arg++, values[i]);
                                }
                                ps.setObject(arg, values[i]);
                                if (command.startsWith("SELECT")) {
                                    try (ResultSet rs = ps.executeQuery()) {
                                        assertFalse(rs.next());
                                    }
                                } else {
                                    assertEquals(0, ps.executeUpdate());
                                }
                                SearchReq query = SearchReq.builder().collectionName("t").annsField("v").topK(1).data(iterated.getVectors()).build();
                                assertEquals(placeholders[i], PlaceholderGroup.parseFrom(new VectorUtils().ConvertToGrpcSearchRequest(query).getPlaceholderGroup()).getPlaceholders(0).getType());
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    public void nullableArraysMetadataAndShowCreateRoundTrip() throws Exception {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE t (id INT64 PRIMARY KEY, enabled BOOL NULL DEFAULT true, tags ARRAY<VARCHAR(8)>(3) NULL, nums ARRAY<INT16>(3), v FLOAT_VECTOR(2))");
            String script;
            try (ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE t")) {
                assertTrue(rs.next());
                script = rs.getString("CREATE SCRIPT");
            }
            List<io.milvus.grpc.FieldSchema> original = new ArrayList<>();
            schema.getCollectionSchema().getFieldSchemaList().forEach(f -> original.add(SchemaUtils.convertToGrpcFieldSchema(f)));
            stmt.executeUpdate(script);
            for (int i = 0; i < original.size(); i++) {
                assertEquals(original.get(i), SchemaUtils.convertToGrpcFieldSchema(schema.getCollectionSchema().getFieldSchemaList().get(i)));
            }
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO t (id,enabled,tags,nums,v) VALUES (1,NULL,?,?,[1,2])")) {
                ps.setArray(1, conn.createArrayOf("STRING", new String[] { "a", "b" }));
                ps.setObject(2, new short[] { 1, -2 });
                assertEquals(1, ps.executeUpdate());
            }
            try (ResultSet rs = stmt.executeQuery("SELECT id,enabled,tags,nums FROM t WHERE tags IS NOT NULL LIMIT 1")) {
                assertTrue(rs.next());
                assertEquals(ResultSetMetaData.columnNoNulls, rs.getMetaData().isNullable(1));
                assertEquals(ResultSetMetaData.columnNullable, rs.getMetaData().isNullable(3));
                assertEquals(Types.SMALLINT, rs.getArray("nums").getBaseType());
                assertArrayEquals(new Object[] { (short) 1, (short) -2 }, (Object[]) rs.getArray("nums").getArray());
                assertArrayEquals(new Object[] { "a", "b" }, (Object[]) rs.getArray("tags").getArray());
            }
            for (String ddl : Arrays.asList("CREATE TABLE x (id INT64 PRIMARY KEY NULL)", "CREATE TABLE x (a ARRAY)", "CREATE TABLE x (a ARRAY<INT8>(0))")) {
                try {
                    stmt.executeUpdate(ddl);
                    fail(ddl);
                } catch (SQLException expected) {
                    assertNotNull(expected.getMessage());
                }
            }
        }
    }

    @Test
    public void invalidVectorAndArrayValuesFailBeforeMutation() throws Exception {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE t (id INT64 PRIMARY KEY, v FLOAT16_VECTOR(2), a ARRAY<INT8>(2))");
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO t (id,v,a) VALUES (1,?,?)")) {
                // @formatter:off
                Object[][] invalid = {
                    { new byte[] { 1 }, new byte[] { 1 } },
                    { new float[] { Float.NaN, 2 }, new byte[] { 1 } },
                    { new float[] { 1, 2 }, new int[] { 128 } },
                    { new float[] { 1, 2 }, new byte[] { 1, 2, 3 } }
                };
                // @formatter:on
                for (Object[] row : invalid) {
                    ps.setObject(1, row[0]);
                    ps.setObject(2, row[1]);
                    try {
                        ps.executeUpdate();
                        fail("invalid value accepted");
                    } catch (SQLException expected) {
                        assertNotNull(expected.getMessage());
                    }
                }
                assertTrue(stored.isEmpty());
            }
        }
    }

    @Test
    public void int8InputsShouldPreserveSignedBytesAndBufferWindow() throws Exception {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE t (id INT64 PRIMARY KEY, int8_vector INT8_VECTOR(2))");
            try (ResultSet ddl = stmt.executeQuery("SHOW CREATE TABLE t")) {
                assertTrue(ddl.next());
                assertTrue(ddl.getString("CREATE SCRIPT").toLowerCase(Locale.ROOT).contains("int8_vector(2)"));
            }
            ByteBuffer buffer = ByteBuffer.wrap(new byte[] { 42, -128, 127, 42 });
            buffer.position(1);
            buffer.limit(3);
            Object[] inputs = { buffer, List.of(-128, 127), new byte[] { -128, 127 }, new short[] { -128, 127 }, new int[] { -128, 127 }, new long[] { -128, 127 }, new float[] { -128, 127 }, new double[] { -128, 127 } };
            for (Object input : inputs) {
                try (PreparedStatement insert = conn.prepareStatement("INSERT INTO t (id,int8_vector) VALUES (1,?)")) {
                    insert.setObject(1, input);
                    assertEquals(1, insert.executeUpdate());
                }
                FieldData vector = stored.stream().filter(field -> field.getFieldName().equals("int8_vector")).findFirst().orElseThrow();
                assertArrayEquals(new byte[] { -128, 127 }, vector.getVectors().getInt8Vector().toByteArray());
                for (String metric : List.of("<->", "<=>", "<#>")) {
                    try (PreparedStatement search = conn.prepareStatement("SELECT id FROM t ORDER BY int8_vector " + metric + " ? LIMIT 1")) {
                        search.setObject(1, input);
                        try (ResultSet ignored = search.executeQuery()) {
                            PlaceholderValue query = PlaceholderGroup.parseFrom(searched.getPlaceholderGroup()).getPlaceholders(0);
                            assertEquals(PlaceholderType.Int8Vector, query.getType());
                            assertArrayEquals(new byte[] { -128, 127 }, query.getValues(0).toByteArray());
                        }
                    }
                }
            }
            assertEquals(1, buffer.position());
            assertEquals(3, buffer.limit());
        }
    }

    @Test
    public void int8InputsShouldRejectOutOfRangeFractionalAndNonNumericValues() throws Exception {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE t (id INT64 PRIMARY KEY, v INT8_VECTOR(2))");
            Object[] invalid = { List.of(128, 0), List.of(-129, 0), List.of(1.5, 0), List.of(Double.NaN, 0), List.of(Double.POSITIVE_INFINITY, 0), List.of("1", "2"), Arrays.asList(null, 1), List.of(List.of(1, 2), List.of(3, 4)), new boolean[] { true, false }, new char[] { 'a', 'b' }, new byte[0], new byte[] { 1 }, new byte[] { 1, 2, 3 } };
            for (Object value : invalid) {
                for (String sql : List.of("INSERT INTO t(id,v) VALUES (1,?)", "SELECT id FROM t ORDER BY v <-> ? LIMIT 1")) {
                    try (PreparedStatement statement = conn.prepareStatement(sql)) {
                        statement.setObject(1, value);
                        assertThrows(SQLException.class, statement::execute);
                    }
                }
            }
            assertTrue(stored.isEmpty());
            assertNull(searched);
            assertThrows(SQLException.class, () -> stmt.executeQuery("SELECT id FROM t ORDER BY v ~= [1,2] LIMIT 1"));
        }
    }

    @Test
    public void scalarAndArrayNullRemainDistinctFromEmptyArrays() throws Exception {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE t (id INT64 PRIMARY KEY, optional BOOL NULL, tags ARRAY<BOOL>(3) NULL, v FLOAT_VECTOR(2))");
            stmt.executeUpdate("INSERT INTO t (id,optional,tags,v) VALUES (1,NULL,NULL,[1,2])");
            try (ResultSet rs = stmt.executeQuery("SELECT optional,tags FROM t LIMIT 1")) {
                assertTrue(rs.next());
                assertNull(rs.getObject("optional"));
                assertTrue(rs.wasNull());
                assertNull(rs.getArray("tags"));
                assertTrue(rs.wasNull());
            }
            stmt.executeUpdate("UPSERT INTO t (id,optional,tags,v) VALUES (1,false,[],[1,2])");
            try (ResultSet rs = stmt.executeQuery("SELECT tags FROM t LIMIT 1")) {
                assertTrue(rs.next());
                assertEquals(0, ((Object[]) rs.getArray(1).getArray()).length);
                assertFalse(rs.wasNull());
                assertEquals(Types.BOOLEAN, rs.getArray(1).getBaseType());
            }
        }
    }

    @Test
    public void nullableVectorsPreserveNullWireValuesAndMetadata() throws Exception {
        // Protocol coverage; server support starts at 2.6.18, not the 2.6.2 scalar baseline.
        for (String type : Arrays.asList("FLOAT_VECTOR(2)", "BINARY_VECTOR(16)", "FLOAT16_VECTOR(2)", "BFLOAT16_VECTOR(2)", "SPARSE_FLOAT_VECTOR")) {
            try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE TABLE t (id INT64 PRIMARY KEY, v " + type + " NULL)");
                stmt.executeUpdate("INSERT INTO t (id,v) VALUES (1,NULL)");
                FieldData vector = stored.stream().filter(f -> "v".equals(f.getFieldName())).findFirst().orElseThrow();
                assertEquals(type, Collections.singletonList(false), vector.getValidDataList());
                try (ResultSet rs = stmt.executeQuery("SELECT v FROM t LIMIT 1")) {
                    assertEquals(ResultSetMetaData.columnNullable, rs.getMetaData().isNullable(1));
                    assertTrue(rs.next());
                    assertNull(rs.getObject(1));
                    assertTrue(rs.wasNull());
                    assertFalse(rs.next());
                }
            }
        }
    }

    @Test
    public void unsupportedScalarOrderingNeverFallsThroughToDml() throws Exception {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            for (String sql : Arrays.asList("UPDATE t SET v=[1,2] ORDER BY id LIMIT 1", "DELETE FROM t WHERE id>0 ORDER BY id LIMIT 1")) {
                try {
                    stmt.executeUpdate(sql);
                    fail(sql);
                } catch (SQLFeatureNotSupportedException expected) {
                    assertTrue(expected.getMessage().contains("Scalar ORDER BY"));
                }
            }
            assertTrue(stored.isEmpty());
            assertNull(iterated);
        }
    }

    @Test
    public void nullableJsonUsesJdbcNullButKeepsNestedJsonNulls() throws Exception {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE t (id INT64 PRIMARY KEY, document JSON NULL, v FLOAT_VECTOR(2))");
            stmt.executeUpdate("INSERT INTO t (id,document,v) VALUES (1,NULL,[1,2])");
            try (ResultSet rs = stmt.executeQuery("SELECT document FROM t LIMIT 1")) {
                assertTrue(rs.next());
                assertNull(rs.getObject(1));
                assertTrue(rs.wasNull());
            }
            stmt.executeUpdate("UPSERT INTO t (id,document,v) VALUES (1,'{\"value\":null}',[1,2])");
            try (ResultSet rs = stmt.executeQuery("SELECT document FROM t LIMIT 1")) {
                assertTrue(rs.next());
                assertEquals("{\"value\":null}", rs.getObject(1).toString());
                assertFalse(rs.wasNull());
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE t SET document=? WHERE id=1 LIMIT 1")) {
                ps.setObject(1, Collections.singletonMap("updated", null));
                assertEquals(1, ps.executeUpdate());
            }
            try (ResultSet rs = stmt.executeQuery("SELECT document FROM t LIMIT 1")) {
                assertTrue(rs.next());
                assertEquals("{\"updated\":null}", rs.getObject(1).toString());
            }
        }
    }

    @Test
    public void everyArrayElementTypeUsesItsJdbcType() throws Exception {
        String[] types = { "BOOL", "INT8", "INT16", "INT32", "INT64", "FLOAT", "DOUBLE", "VARCHAR(12)" };
        // @formatter:off
        Object[] inputs = {
            new boolean[] { true },
            new byte[] { -2 },
            new short[] { -2 },
            new int[] { -2 },
            new long[] { -2 },
            new float[] { 0.5F },
            new double[] { 0.5D },
            new String[] { "中文" }
        };
        // @formatter:on
        Object[] expected = { true, (byte) -2, (short) -2, -2, -2L, 0.5F, 0.5D, "中文" };
        int[] jdbc = { Types.BOOLEAN, Types.TINYINT, Types.SMALLINT, Types.INTEGER, Types.BIGINT, Types.FLOAT, Types.DOUBLE, Types.VARCHAR };
        for (int i = 0; i < types.length; i++) {
            try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE TABLE t (id INT64 PRIMARY KEY, a ARRAY<" + types[i] + ">(2), v FLOAT_VECTOR(2))");
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO t (id,a,v) VALUES (1,?,[1,2])")) {
                    ps.setObject(1, inputs[i]);
                    assertEquals(1, ps.executeUpdate());
                }
                try (ResultSet rs = stmt.executeQuery("SELECT a FROM t LIMIT 1")) {
                    assertTrue(rs.next());
                    assertEquals(types[i], jdbc[i], rs.getArray(1).getBaseType());
                    assertArrayEquals(types[i], new Object[] { expected[i] }, (Object[]) rs.getArray(1).getArray());
                }
                try (ResultSet rs = stmt.executeQuery("SHOW TABLE t")) {
                    while (rs.next()) {
                        if ("a".equals(rs.getString("FIELD"))) {
                            assertEquals(2, rs.getInt("MAX_CAPACITY"));
                            assertFalse(rs.getBoolean("NULLABLE"));
                            assertFalse(rs.getString("ELEMENT_TYPE").isEmpty());
                        }
                    }
                }
            }
        }
    }
}
