/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.oracle.feature.type;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.feature.type.BinaryTypeJdbcCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;
import org.junit.Test;
import static org.junit.Assert.*;

public class OracleBinaryTypeJdbcTest extends BinaryTypeJdbcCase {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }

    // Oracle-specific locator binding is not the default byte[] parameter contract counted by NxN.
    @Test
    public void binaryEmptyBlobLocator_shouldRemainDistinctFromNull() throws SQLException {
        int emptyId = baseId() + 9;
        int nullId = baseId() + 10;
        jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
            Blob emptyBlob = connection.createBlob();
            try {
                assertEquals(0, emptyBlob.length());
                try (PreparedStatement statement = connection.prepareStatement(insertCommand("binary_types_explicit_test", "id, blob_value"))) {
                    statement.setInt(1, emptyId);
                    statement.setBlob(2, emptyBlob);
                    assertEquals(1, statement.executeUpdate());

                    statement.setInt(1, nullId);
                    statement.setNull(2, Types.BLOB);
                    assertEquals(1, statement.executeUpdate());
                }

                JdbcTemplate connectionJdbc = new JdbcTemplate(connection);
                byte[] empty = connectionJdbc.queryForObject(selectCommand("binary_types_explicit_test", "blob_value"), new Object[] { emptyId }, byte[].class);
                byte[] absent = connectionJdbc.queryForObject(selectCommand("binary_types_explicit_test", "blob_value"), new Object[] { nullId }, byte[].class);
                assertNotNull(empty);
                assertArrayEquals(new byte[0], empty);
                assertNull(absent);
                assertEquals(Long.valueOf(0), connectionJdbc.queryForObject(selectCommand("binary_types_explicit_test", "DBMS_LOB.GETLENGTH(blob_value)"), new Object[] { emptyId }, Long.class));
            } finally {
                emptyBlob.free();
            }
            return null;
        });
    }
}
