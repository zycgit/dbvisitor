package net.hasor.dbvisitor.test.contract.feature.type;

import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.types.ArrayTypesAnnotationModel;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.handler.array.ArrayTypeHandler;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public abstract class ArrayTypeJdbcContractTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 690000;
    }

    @Test
    @Capability(CapabilityId.TYPE_ARRAY_JDBC_ARRAY)
    public void arrayJdbcArray_shouldRoundTripIntegerArray() throws SQLException {
        requiresNxnFeature(FeatureId.ARRAY);
        int id = baseId() + 1;
        Integer[] expected = new Integer[] { 10, 20, 30, 40, 50 };

        try (Connection conn = dataSource.getConnection()) {
            Array sqlArray = conn.createArrayOf("INTEGER", expected);
            jdbcTemplate.executeUpdate("INSERT INTO array_types_test (id, int_array) VALUES (?, ?)", new Object[] { id, sqlArray });
        }

        Integer[] loaded = jdbcTemplate.queryForObject("SELECT int_array FROM array_types_test WHERE id = ?", new Object[] { id }, Integer[].class);

        assertNotNull(loaded);
        assertArrayEquals(expected, loaded);
    }

    @Test
    @Capability(CapabilityId.TYPE_ARRAY_SQLARG)
    public void arraySqlArg_shouldRoundTripIntegerAndFloatArrays() throws SQLException {
        requiresNxnFeature(FeatureId.ARRAY);
        int id = baseId() + 2;
        Integer[] ints = new Integer[] { 100, 200, 300 };
        Float[] floats = new Float[] { 1.1f, 2.2f, 3.3f };

        jdbcTemplate.executeUpdate(//
                "INSERT INTO array_types_test (id, int_array, float_array) VALUES (?, ?, ?)", //
                new Object[] { id, new SqlArg(ints, Types.ARRAY, new ArrayTypeHandler()), new SqlArg(floats, Types.ARRAY, new ArrayTypeHandler()) });

        Integer[] loadedInts = jdbcTemplate.queryForObject("SELECT int_array FROM array_types_test WHERE id = ?", new Object[] { id }, Integer[].class);
        Float[] loadedFloats = jdbcTemplate.queryForObject("SELECT float_array FROM array_types_test WHERE id = ?", new Object[] { id }, Float[].class);

        assertArrayEquals(ints, loadedInts);
        assertEquals(floats.length, loadedFloats.length);
        for (int i = 0; i < floats.length; i++) {
            assertEquals(floats[i], loadedFloats[i], 0.0001f);
        }
    }

    @Test
    @Capability(CapabilityId.TYPE_ARRAY_NAMED_PARAMETER)
    public void arrayNamedParameter_shouldRoundTripStringArray() throws SQLException {
        requiresNxnFeature(FeatureId.ARRAY);
        int id = baseId() + 3;
        String[] expected = new String[] { "你好", "世界", "こんにちは", "🎉", "Emoji🚀Test" };

        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("array", new SqlArg(expected, Types.ARRAY, new ArrayTypeHandler()));
        jdbcTemplate.executeUpdate("INSERT INTO array_types_test (id, string_array) VALUES (:id, :array)", params);

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("id", id);
        String[] loaded = jdbcTemplate.queryForObject("SELECT string_array FROM array_types_test WHERE id = :id", queryParams, String[].class);

        assertNotNull(loaded);
        assertArrayEquals(expected, loaded);
    }

    @Test
    @Capability(CapabilityId.TYPE_ARRAY_UPDATE)
    public void arrayUpdate_shouldReplaceArrayValues() throws SQLException {
        requiresNxnFeature(FeatureId.ARRAY);
        int id = baseId() + 4;
        Integer[] original = new Integer[] { 1, 2, 3 };
        Integer[] updated = new Integer[] { 10, 20, 30, 40, 50 };

        jdbcTemplate.executeUpdate("INSERT INTO array_types_test (id, int_array) VALUES (?, ?)", //
                new Object[] { id, new SqlArg(original, Types.ARRAY, new ArrayTypeHandler()) });

        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("array", new SqlArg(updated, Types.ARRAY, new ArrayTypeHandler()));
        int rows = jdbcTemplate.executeUpdate("UPDATE array_types_test SET int_array = :array WHERE id = :id", params);

        Integer[] loaded = jdbcTemplate.queryForObject("SELECT int_array FROM array_types_test WHERE id = ?", new Object[] { id }, Integer[].class);

        assertEquals(1, rows);
        assertNotNull(loaded);
        assertArrayEquals(updated, loaded);
    }

    @Test
    @Capability(CapabilityId.TYPE_ARRAY_BATCH)
    public void arrayBatch_shouldInsertMultipleArrayRows() throws SQLException {
        requiresNxnFeature(FeatureId.ARRAY);
        int firstId = baseId() + 5;
        Object[][] batchArgs = new Object[][] { //
                { firstId, new SqlArg(new Integer[] { 1, 2 }, Types.ARRAY, new ArrayTypeHandler()), new SqlArg(new String[] { "A", "B" }, Types.ARRAY, new ArrayTypeHandler()) }, //
                { firstId + 1, new SqlArg(new Integer[] { 3, 4 }, Types.ARRAY, new ArrayTypeHandler()), new SqlArg(new String[] { "C", "D" }, Types.ARRAY, new ArrayTypeHandler()) }, //
                { firstId + 2, new SqlArg(new Integer[] { 5, 6 }, Types.ARRAY, new ArrayTypeHandler()), new SqlArg(new String[] { "E", "F" }, Types.ARRAY, new ArrayTypeHandler()) } //
        };

        int[] rows = jdbcTemplate.executeBatch("INSERT INTO array_types_test (id, int_array, string_array) VALUES (?, ?, ?)", batchArgs);
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM array_types_test WHERE id >= ? AND id <= ?", new Object[] { firstId, firstId + 2 }, Integer.class);

        assertEquals(3, rows.length);
        for (int row : rows) {
            assertEquals(1, row);
        }
        assertEquals(Integer.valueOf(3), count);
    }

    @Test
    @Capability(CapabilityId.TYPE_ARRAY_NULL)
    public void arrayNull_shouldRemainNull() throws SQLException {
        int id = baseId() + 8;

        jdbcTemplate.executeUpdate("INSERT INTO array_types_test (id, int_array) VALUES (?, ?)", new Object[] { id, null });

        Integer[] loaded = jdbcTemplate.queryForObject("SELECT int_array FROM array_types_test WHERE id = ?", new Object[] { id }, Integer[].class);

        assertNull(loaded);
    }

    @Test
    @Capability(CapabilityId.TYPE_ARRAY_ANNOTATION_MAPPING)
    public void arrayAnnotationMapping_shouldHonorAllColumnConfigurationsTogether() throws SQLException {
        requiresNxnFeature(FeatureId.ARRAY);
        int id = baseId() + 20;
        Integer[] expected = new Integer[] { 1, 2, 3, 4, 5 };

        ArrayTypesAnnotationModel model = new ArrayTypesAnnotationModel();
        model.setId(id);
        model.setArrayNoAnnotation(expected);
        model.setArrayJdbcType(expected);
        model.setArrayTypeHandler(expected);
        model.setArrayNumberSpecial(expected);
        model.setArrayFullAnnotated(expected);

        int rows = lambdaTemplate.insert(ArrayTypesAnnotationModel.class)//
                .applyEntity(model)//
                .executeSumResult();
        ArrayTypesAnnotationModel loaded = lambdaTemplate.query(ArrayTypesAnnotationModel.class)//
                .eq(ArrayTypesAnnotationModel::getId, id)//
                .queryForObject();

        assertEquals(1, rows);
        assertNotNull(loaded);
        assertArrayEquals(expected, loaded.getArrayNoAnnotation());
        assertArrayEquals(expected, loaded.getArrayJdbcType());
        assertArrayEquals(expected, loaded.getArrayTypeHandler());
        assertNumberArrayEquals(expected, loaded.getArrayNumberSpecial());
        assertNumberArrayEquals(expected, loaded.getArrayFullAnnotated());
    }

    private void assertNumberArrayEquals(Integer[] expected, Number[] actual) {
        assertNotNull(actual);
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].intValue(), actual[i].intValue());
        }
    }
}
