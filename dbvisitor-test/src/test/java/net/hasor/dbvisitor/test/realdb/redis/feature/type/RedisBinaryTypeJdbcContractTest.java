/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.feature.type;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;

public class RedisBinaryTypeJdbcContractTest extends RedisNativeTypeSupport {
    @Test
    @Capability(CapabilityId.TYPE_BINARY_BYTES)
    public void binaryBytes_shouldRoundTripFixedAndVariableValues() throws SQLException {
        assertBinary(new byte[] { 0, 1, 2, 3, 4, 5 });
        byte[] value = new byte[1024];
        for (int i = 0; i < value.length; i++) {
            value[i] = (byte) (i % 251);
        }
        assertBinary(value);
    }

    @Test
    @Capability(CapabilityId.TYPE_BINARY_BLOB)
    public void binaryBlob_shouldRoundTripLargeBytesAndInputStream() throws SQLException {
        byte[] value = new byte[8192];
        for (int i = 0; i < value.length; i++) {
            value[i] = (byte) (i * 7 % 256);
        }
        assertBinary(value);
        byte[] stream = new byte[] { 10, 20, 30, 40, 50 };
        String key = key("stream");
        this.jdbcTemplate.executeUpdate("SET ? ?", new Object[] { key, new ByteArrayInputStream(stream) });
        assertArrayEquals(stream, readBinary(key));
    }

    @Test
    @Capability(CapabilityId.TYPE_BINARY_LARGE_PATTERN)
    public void binaryLargePattern_shouldPreserveBlobContent() throws SQLException {
        byte[] value = new byte[100 * 1024];
        for (int i = 0; i < value.length; i++) {
            value[i] = (byte) i;
        }
        assertBinary(value);
        assertBinary(new byte[0]);
    }

    private void assertBinary(byte[] expected) throws SQLException {
        String key = key("bytes-" + expected.length);
        this.jdbcTemplate.executeUpdate("SET ? ?", new Object[] { key, expected });
        assertArrayEquals(expected, readBinary(key));
    }

    private byte[] readBinary(String key) throws SQLException {
        return this.jdbcTemplate.queryForObject("GET ?",
                new Object[] { key.getBytes(StandardCharsets.UTF_8) }, byte[].class);
    }
}

