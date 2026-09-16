/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.type;

import java.sql.Array;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.material.model.types.ArrayTypesAnnotationModel;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.handler.array.ArrayTypeHandler;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class ArrayTypeJdbcCase extends TypeJdbcCommandSupport {
    protected int baseId() {
        return 690000;
    }

    // 能力归属：类型处理器 / 数组处理器 / 数组读写。
    @Test
    @Capability(value = CapabilityId.TYPE_ARRAY_JDBC_ARRAY, column = "types/array-handlers/arrays")
    public void arrayJdbcArray_shouldRoundTripIntegerArray() throws SQLException {
        requiresNxnFeature(FeatureId.ARRAY);
        int id = baseId() + 1;
        Integer[] expected = new Integer[] { 10, 20, 30, 40, 50 };

        jdbcTemplate.execute((ConnectionCallback<Void>) conn -> {
            Array sqlArray = conn.createArrayOf("INTEGER", expected);
            try {
                JdbcTemplate connectionJdbc = new JdbcTemplate(conn);
                assertEquals(1, connectionJdbc.executeUpdate(insertCommand("array_types_test", "id, int_array"), new Object[] { id, sqlArray }));
            } finally {
                sqlArray.free();
            }
            return null;
        });

        Integer[] loaded = jdbcTemplate.queryForObject(selectCommand("array_types_test", "int_array"), new Object[] { id }, Integer[].class);

        assertNotNull(loaded);
        assertArrayEquals(expected, loaded);
    }

    // 能力归属：类型处理器 / 数组处理器 / 数组读写。
    @Test
    @Capability(value = CapabilityId.TYPE_ARRAY_SQLARG, column = "types/array-handlers/arrays")
    public void arraySqlArg_shouldRoundTripIntegerAndFloatArrays() throws SQLException {
        requiresNxnFeature(FeatureId.ARRAY);
        int id = baseId() + 2;
        Integer[] ints = new Integer[] { 100, 200, 300 };
        Float[] floats = new Float[] { 1.1f, 2.2f, 3.3f };

        jdbcTemplate.executeUpdate(//
                insertCommand("array_types_test", "id, int_array, float_array"), //
                new Object[] { id, new SqlArg(ints, Types.ARRAY, new ArrayTypeHandler()), new SqlArg(floats, Types.ARRAY, new ArrayTypeHandler()) });

        Integer[] loadedInts = jdbcTemplate.queryForObject(selectCommand("array_types_test", "int_array"), new Object[] { id }, Integer[].class);
        Float[] loadedFloats = jdbcTemplate.queryForObject(selectCommand("array_types_test", "float_array"), new Object[] { id }, Float[].class);

        assertArrayEquals(ints, loadedInts);
        assertEquals(floats.length, loadedFloats.length);
        for (int i = 0; i < floats.length; i++) {
            assertEquals(floats[i], loadedFloats[i], 0.0001f);
        }
    }

    // 能力归属：类型处理器 / 数组处理器 / 数组读写。
    @Test
    @Capability(value = CapabilityId.TYPE_ARRAY_NAMED_PARAMETER, column = "types/array-handlers/arrays")
    public void arrayNamedParameter_shouldRoundTripStringArray() throws SQLException {
        requiresNxnFeature(FeatureId.ARRAY);
        int id = baseId() + 3;
        String[] expected = new String[] { "你好", "世界", "こんにちは", "🎉", "Emoji🚀Test" };

        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("array", new SqlArg(expected, Types.ARRAY, new ArrayTypeHandler()));
        jdbcTemplate.executeUpdate(insertCommand("array_types_test", "id, string_array", ":id", ":array"), params);

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("id", id);
        String[] loaded = jdbcTemplate.queryForObject(selectNamedCommand("array_types_test", "string_array"), queryParams, String[].class);

        assertNotNull(loaded);
        assertArrayEquals(expected, loaded);
    }

    // 能力归属：类型处理器 / 数组处理器 / 数组读写。
    @Test
    @Capability(value = CapabilityId.TYPE_ARRAY_UPDATE, column = "types/array-handlers/arrays")
    public void arrayUpdate_shouldReplaceArrayValues() throws SQLException {
        requiresNxnFeature(FeatureId.ARRAY);
        int id = baseId() + 4;
        Integer[] original = new Integer[] { 1, 2, 3 };
        Integer[] updated = new Integer[] { 10, 20, 30, 40, 50 };

        jdbcTemplate.executeUpdate(insertCommand("array_types_test", "id, int_array"), //
                new Object[] { id, new SqlArg(original, Types.ARRAY, new ArrayTypeHandler()) });

        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("array", new SqlArg(updated, Types.ARRAY, new ArrayTypeHandler()));
        int rows = jdbcTemplate.executeUpdate(updateArrayCommand(), params);

        Integer[] loaded = jdbcTemplate.queryForObject(selectCommand("array_types_test", "int_array"), new Object[] { id }, Integer[].class);

        assertEquals(1, rows);
        assertNotNull(loaded);
        assertArrayEquals(updated, loaded);
    }

    // 能力归属：类型处理器 / 数组处理器 / 数组读写。
    @Test
    @Capability(value = CapabilityId.TYPE_ARRAY_BATCH, column = "types/array-handlers/arrays")
    public void arrayBatch_shouldInsertMultipleArrayRows() throws SQLException {
        requiresNxnFeature(FeatureId.ARRAY);
        int firstId = baseId() + 5;
        // @formatter:off
        Object[][] batchArgs = new Object[][] {
            { firstId, new SqlArg(new Integer[] { 1, 2 }, Types.ARRAY, new ArrayTypeHandler()), new SqlArg(new String[] { "A", "B" }, Types.ARRAY, new ArrayTypeHandler()) },
            { firstId + 1, new SqlArg(new Integer[] { 3, 4 }, Types.ARRAY, new ArrayTypeHandler()), new SqlArg(new String[] { "C", "D" }, Types.ARRAY, new ArrayTypeHandler()) },
            { firstId + 2, new SqlArg(new Integer[] { 5, 6 }, Types.ARRAY, new ArrayTypeHandler()), new SqlArg(new String[] { "E", "F" }, Types.ARRAY, new ArrayTypeHandler()) }
        };
        // @formatter:on

        int[] rows = jdbcTemplate.executeBatch(insertCommand("array_types_test", "id, int_array, string_array"), batchArgs);
        Integer count = jdbcTemplate.queryForObject(countRangeCommand(), new Object[] { firstId, firstId + 2 }, Integer.class);

        assertEquals(3, rows.length);
        for (int row : rows) {
            assertEquals(1, row);
        }
        assertEquals(Integer.valueOf(3), count);
        Integer[][] expectedInts = { { 1, 2 }, { 3, 4 }, { 5, 6 } };
        String[][] expectedStrings = { { "A", "B" }, { "C", "D" }, { "E", "F" } };
        for (int i = 0; i < batchArgs.length; i++) {
            Integer[] loadedInts = jdbcTemplate.queryForObject(selectCommand("array_types_test", "int_array"), new Object[] { firstId + i }, Integer[].class);
            String[] loadedStrings = jdbcTemplate.queryForObject(selectCommand("array_types_test", "string_array"), new Object[] { firstId + i }, String[].class);
            assertArrayEquals(expectedInts[i], loadedInts);
            assertArrayEquals(expectedStrings[i], loadedStrings);
        }
    }

    // 能力归属：类型处理器 / 数组处理器 / 数组读写。
    @Test
    @Capability(value = CapabilityId.TYPE_ARRAY_NULL, column = "types/array-handlers/arrays")
    public void arrayNull_shouldRemainNull() throws SQLException {
        int id = baseId() + 8;

        jdbcTemplate.executeUpdate(insertCommand("array_types_test", "id, int_array"), new Object[] { id, null });

        Integer[] loaded = jdbcTemplate.queryForObject(selectCommand("array_types_test", "int_array"), new Object[] { id }, Integer[].class);

        assertNull(loaded);
    }

    // 能力归属：类型处理器 / 数组类型 / 空数组保留零长度且不变成 NULL。
    @Test
    @Capability(value = CapabilityId.TYPE_ARRAY_EMPTY, column = "types/array-handlers/arrays")
    public void arrayEmptyValues_shouldRemainEmptyArrays() throws SQLException {
        requiresNxnFeature(FeatureId.ARRAY);
        int id = baseId() + 9;
        jdbcTemplate.executeUpdate(insertCommand("array_types_test", "id, int_array, string_array"), //
                new Object[] { id, new SqlArg(new Integer[0], Types.ARRAY, new ArrayTypeHandler()), new SqlArg(new String[0], Types.ARRAY, new ArrayTypeHandler()) });

        // ArrayTypeHandler returns Object[] when the driver cannot identify an empty array's element type.
        Object[] loadedInts = jdbcTemplate.queryForObject(selectCommand("array_types_test", "int_array"), selectParameters(id), Object[].class);
        Object[] loadedStrings = jdbcTemplate.queryForObject(selectCommand("array_types_test", "string_array"), selectParameters(id), Object[].class);
        assertNotNull(loadedInts);
        assertNotNull(loadedStrings);
        assertArrayEquals(new Integer[0], loadedInts);
        assertArrayEquals(new String[0], loadedStrings);
    }

    // 能力归属：类型处理器 / 数组类型 / NULL 元素位置与数组整体 NULL 的区别。
    @Test
    @Capability(value = CapabilityId.TYPE_ARRAY_NULL_ELEMENTS, column = "types/array-handlers/arrays")
    public void arrayNullElements_shouldPreserveElementPositions() throws SQLException {
        requiresNxnFeature(FeatureId.ARRAY);
        int id = baseId() + 10;
        Integer[] expectedInts = new Integer[] { null, -1, 0, 7, null };
        String[] expectedStrings = new String[] { null, "", "中文\"\\", null };
        jdbcTemplate.executeUpdate(insertCommand("array_types_test", "id, int_array, string_array"), //
                new Object[] { id, new SqlArg(expectedInts, Types.ARRAY, nullableElementArrayHandler()), new SqlArg(expectedStrings, Types.ARRAY, nullableElementArrayHandler()) });

        Integer[] loadedInts = jdbcTemplate.queryForObject(selectCommand("array_types_test", "int_array"), selectParameters(id), Integer[].class);
        String[] loadedStrings = jdbcTemplate.queryForObject(selectCommand("array_types_test", "string_array"), selectParameters(id), String[].class);
        assertArrayEquals(expectedInts, loadedInts);
        assertArrayEquals(expectedStrings, loadedStrings);
    }

    protected ArrayTypeHandler nullableElementArrayHandler() {
        return new ArrayTypeHandler();
    }

    // 能力归属：类型处理器 / 数组处理器 / 数组读写。
    @Test
    @Capability(value = CapabilityId.TYPE_ARRAY_ANNOTATION_MAPPING, column = "types/array-handlers/arrays")
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

    protected String selectNamedCommand(String table, String column) throws SQLException {
        return "SELECT " + column + " FROM " + table + " WHERE id = :id";
    }

    protected String updateArrayCommand() throws SQLException {
        return "UPDATE array_types_test SET int_array = :array WHERE id = :id";
    }

    protected String countRangeCommand() throws SQLException {
        return "SELECT COUNT(*) FROM array_types_test WHERE id >= ? AND id <= ?";
    }

    private void assertNumberArrayEquals(Integer[] expected, Number[] actual) {
        assertNotNull(actual);
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].intValue(), actual[i].intValue());
        }
    }
}
