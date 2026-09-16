/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.type;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.util.Map;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class BinaryTypeJdbcCase extends TypeJdbcCommandSupport {
    protected int baseId() {
        return 670000;
    }

    protected void verifyAdditionalBinaryValues() throws SQLException {
    }

    // 能力归属：类型处理器 / 字符与字节数组 / 二进制。
    @Test
    @Capability(value = CapabilityId.TYPE_BINARY_BYTES, column = "types/characters-and-byte-arrays/binary")
    public void binaryBytes_shouldRoundTripFixedAndVariableValues() throws SQLException {
        requiresNxnFeature(FeatureId.BINARY);

        int id = baseId() + 1;
        byte[] binaryValue = new byte[] { 0, 1, 2, 3, 4, 5 };
        byte[] varbinaryValue = new byte[1024];
        for (int i = 0; i < varbinaryValue.length; i++) {
            varbinaryValue[i] = (byte) (i % 251);
        }

        executeInsert(//
                insertCommand("binary_types_explicit_test", "id, binary_value, varbinary_value"), //
                new Object[] { id, binaryValue, varbinaryValue });

        byte[] loadedBinary = jdbcTemplate.queryForObject(selectCommand("binary_types_explicit_test", "binary_value"), selectParameters(id), byte[].class);
        byte[] loadedVarbinary = jdbcTemplate.queryForObject(selectCommand("binary_types_explicit_test", "varbinary_value"), selectParameters(id), byte[].class);

        assertArrayEquals(binaryValue, loadedBinary);
        assertArrayEquals(varbinaryValue, loadedVarbinary);
    }

    // 能力归属：类型处理器 / 字符与字节数组 / 二进制。
    @Test
    @Capability(value = CapabilityId.TYPE_BINARY_BLOB, column = "types/characters-and-byte-arrays/binary")
    public void binaryBlob_shouldRoundTripLargeBytesAndInputStream() throws SQLException {
        requiresNxnFeature(FeatureId.BINARY);

        int id = baseId() + 2;
        int streamId = baseId() + 3;
        byte[] blobValue = new byte[8192];
        for (int i = 0; i < blobValue.length; i++) {
            blobValue[i] = (byte) (i * 7 % 256);
        }
        byte[] streamValue = new byte[] { 10, 20, 30, 40, 50 };

        executeInsert(insertCommand("binary_types_explicit_test", "id, blob_value"), new Object[] { id, blobValue });
        executeInsert(insertCommand("binary_types_explicit_test", "id, blob_value"), new Object[] { streamId, new ByteArrayInputStream(streamValue) });

        byte[] loadedBlob = jdbcTemplate.queryForObject(selectCommand("binary_types_explicit_test", "blob_value"), selectParameters(id), byte[].class);
        byte[] loadedStream = jdbcTemplate.queryForObject(selectCommand("binary_types_explicit_test", "blob_value"), selectParameters(streamId), byte[].class);

        assertNotNull(loadedBlob);
        assertEquals(blobValue.length, loadedBlob.length);
        assertArrayEquals(blobValue, loadedBlob);
        assertArrayEquals(streamValue, loadedStream);
    }

    // 能力归属：类型处理器 / 字符与字节数组 / 二进制。
    @Test
    @Capability(value = CapabilityId.TYPE_BINARY_LARGE_PATTERN, column = "types/characters-and-byte-arrays/binary")
    public void binaryLargePattern_shouldPreserveBlobContent() throws SQLException {
        requiresNxnFeature(FeatureId.BINARY);

        int id = baseId() + 4;
        byte[] pattern = new byte[100 * 1024];
        for (int i = 0; i < pattern.length; i++) {
            pattern[i] = (byte) (i % 256);
        }

        executeInsert(insertCommand("binary_types_explicit_test", "id, blob_value"), new Object[] { id, pattern });

        byte[] loaded = jdbcTemplate.queryForObject(selectCommand("binary_types_explicit_test", "blob_value"), selectParameters(id), byte[].class);

        assertNotNull(loaded);
        assertEquals(pattern.length, loaded.length);
        assertArrayEquals(pattern, loaded);
        verifyAdditionalBinaryValues();
    }

    // 能力归属：类型处理器 / 字符与字节数组 / 二进制。
    @Test
    @Capability(value = CapabilityId.TYPE_BINARY_NULL, column = "types/characters-and-byte-arrays/binary")
    public void binaryNulls_shouldRemainNull() throws SQLException {
        int id = baseId() + 5;
        executeInsert(//
                insertCommand("binary_types_explicit_test", "id, binary_value, varbinary_value, longvarbinary_value, blob_value", "?", "NULL", "NULL", "NULL", "NULL"), //
                new Object[] { id });

        Map<String, Object> row = jdbcTemplate.queryForMap(selectCommand("binary_types_explicit_test", "binary_value, varbinary_value, longvarbinary_value, blob_value"), new Object[] { id });

        assertNull(value(row, "binary_value"));
        assertNull(value(row, "varbinary_value"));
        assertNull(value(row, "longvarbinary_value"));
        assertNull(value(row, "blob_value"));

        for (String column : new String[] { "binary_value", "varbinary_value", "longvarbinary_value", "blob_value" }) {
            assertNull(column, jdbcTemplate.queryForObject(selectCommand("binary_types_explicit_test", column), selectParameters(id), byte[].class));
        }
    }

    // 能力归属：类型处理器 / 二进制类型 / 空字节数组与 NULL 的区别。
    @Test
    @Capability(value = CapabilityId.TYPE_BINARY_EMPTY, column = "types/characters-and-byte-arrays/binary")
    public void binaryEmptyBytes_shouldRemainAnEmptyValue() throws SQLException {
        requiresNxnFeature(FeatureId.BINARY);
        int id = baseId() + 7;
        byte[] expected = new byte[0];

        executeInsert(insertCommand("binary_types_explicit_test", "id, blob_value"), new Object[] { id, expected });

        byte[] loaded = jdbcTemplate.queryForObject(selectCommand("binary_types_explicit_test", "blob_value"), selectParameters(id), byte[].class);
        assertNotNull(loaded);
        assertArrayEquals(expected, loaded);
    }

    // 能力归属：类型处理器 / 二进制类型 / Byte[] 包装数组读写。
    @Test
    @Capability(value = CapabilityId.TYPE_BINARY_BOXED_BYTES, column = "types/characters-and-byte-arrays/binary")
    public void binaryBoxedBytes_shouldUseByteHandlersInsteadOfSqlArrayHandlers() throws SQLException {
        requiresNxnFeature(FeatureId.BINARY);
        int id = baseId() + 8;
        Byte[] expected = new Byte[] { Byte.MIN_VALUE, -1, 0, 1, Byte.MAX_VALUE };
        executeInsert(insertCommand("binary_types_explicit_test", "id, varbinary_value"), new Object[] { id, expected });

        Byte[] loaded = jdbcTemplate.queryForObject(selectCommand("binary_types_explicit_test", "varbinary_value"), selectParameters(id), Byte[].class);
        byte[] raw = jdbcTemplate.queryForObject(selectCommand("binary_types_explicit_test", "varbinary_value"), selectParameters(id), byte[].class);
        assertArrayEquals(expected, loaded);
        assertArrayEquals(new byte[] { Byte.MIN_VALUE, -1, 0, 1, Byte.MAX_VALUE }, raw);
    }

    private Object value(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        if (row.containsKey(key.toUpperCase())) {
            return row.get(key.toUpperCase());
        }
        return row.get(key.toLowerCase());
    }
}
