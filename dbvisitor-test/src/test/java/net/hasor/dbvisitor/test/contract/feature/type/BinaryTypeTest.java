package net.hasor.dbvisitor.test.contract.feature.type;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public abstract class BinaryTypeTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 670000;
    }

    @Test
    @Capability(CapabilityId.TYPE_BINARY_BYTES)
    public void binaryBytesFixedAndVariableValues() throws SQLException {
        requiresNxnFeature(FeatureId.BINARY);

        int id = baseId() + 1;
        byte[] binaryValue = new byte[] { 0, 1, 2, 3, 4, 5 };
        byte[] varbinaryValue = new byte[1024];
        for (int i = 0; i < varbinaryValue.length; i++) {
            varbinaryValue[i] = (byte) (i % 251);
        }

        jdbcTemplate.executeUpdate(//
                "INSERT INTO binary_types_explicit_test (id, binary_value, varbinary_value) VALUES (?, ?, ?)", //
                new Object[] { id, binaryValue, varbinaryValue });

        byte[] loadedBinary = jdbcTemplate.queryForObject("SELECT binary_value FROM binary_types_explicit_test WHERE id = ?", new Object[] { id }, byte[].class);
        byte[] loadedVarbinary = jdbcTemplate.queryForObject("SELECT varbinary_value FROM binary_types_explicit_test WHERE id = ?", new Object[] { id }, byte[].class);

        assertArrayEquals(binaryValue, loadedBinary);
        assertArrayEquals(varbinaryValue, loadedVarbinary);
    }

    @Test
    @Capability(CapabilityId.TYPE_BINARY_BLOB)
    public void binaryBlobLargeBytesAndInputStream() throws SQLException {
        requiresNxnFeature(FeatureId.BINARY);

        int id = baseId() + 2;
        int streamId = baseId() + 3;
        byte[] blobValue = new byte[8192];
        for (int i = 0; i < blobValue.length; i++) {
            blobValue[i] = (byte) (i * 7 % 256);
        }
        byte[] streamValue = new byte[] { 10, 20, 30, 40, 50 };

        jdbcTemplate.executeUpdate("INSERT INTO binary_types_explicit_test (id, blob_value) VALUES (?, ?)", new Object[] { id, blobValue });
        jdbcTemplate.executeUpdate("INSERT INTO binary_types_explicit_test (id, blob_value) VALUES (?, ?)", new Object[] { streamId, new ByteArrayInputStream(streamValue) });

        byte[] loadedBlob = jdbcTemplate.queryForObject("SELECT blob_value FROM binary_types_explicit_test WHERE id = ?", new Object[] { id }, byte[].class);
        byte[] loadedStream = jdbcTemplate.queryForObject("SELECT blob_value FROM binary_types_explicit_test WHERE id = ?", new Object[] { streamId }, byte[].class);

        assertNotNull(loadedBlob);
        assertEquals(blobValue.length, loadedBlob.length);
        assertArrayEquals(blobValue, loadedBlob);
        assertArrayEquals(streamValue, loadedStream);
    }

    @Test
    @Capability(CapabilityId.TYPE_BINARY_LARGE_PATTERN)
    public void binaryLargePatternBlobContent() throws SQLException {
        requiresNxnFeature(FeatureId.BINARY);

        int id = baseId() + 4;
        byte[] pattern = new byte[100 * 1024];
        for (int i = 0; i < pattern.length; i++) {
            pattern[i] = (byte) (i % 256);
        }

        jdbcTemplate.executeUpdate("INSERT INTO binary_types_explicit_test (id, blob_value) VALUES (?, ?)", new Object[] { id, pattern });

        byte[] loaded = jdbcTemplate.queryForObject("SELECT blob_value FROM binary_types_explicit_test WHERE id = ?", new Object[] { id }, byte[].class);

        assertNotNull(loaded);
        assertEquals(pattern.length, loaded.length);
        assertArrayEquals(pattern, loaded);
    }

    @Test
    @Capability(CapabilityId.TYPE_BINARY_NULL)
    public void binaryNullsRemainNull() throws SQLException {
        int id = baseId() + 5;
        jdbcTemplate.executeUpdate(//
                "INSERT INTO binary_types_explicit_test (id, binary_value, varbinary_value, longvarbinary_value, blob_value) VALUES (?, NULL, NULL, NULL, NULL)", //
                new Object[] { id });

        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT binary_value, varbinary_value, longvarbinary_value, blob_value FROM binary_types_explicit_test WHERE id = ?", new Object[] { id });

        assertNull(value(row, "binary_value"));
        assertNull(value(row, "varbinary_value"));
        assertNull(value(row, "longvarbinary_value"));
        assertNull(value(row, "blob_value"));
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
