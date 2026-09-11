/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.type;

import java.sql.SQLException;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class BasicBitValueJdbcContractTest extends BasicTypeJdbcSupport {
    @Test
    @Capability(CapabilityId.TYPE_BASIC_BIT_CAST_NULL)
    public void explicitBitAndBooleanTypes_shouldExposeDatabaseValues() throws SQLException {
        requiresNxnFeature(FeatureId.BIT_CAST_NULL_VALUE);

        int id = baseId() + 4;
        int nullId = baseId() + 5;
        String insertSql = isDataSource("mysql") || isOracle() //
                ? "INSERT INTO basic_types_explicit_test (id, bool_bit, bool_boolean) VALUES (?, ?, ?)" //
                : "INSERT INTO basic_types_explicit_test (id, bool_bit, bool_boolean) VALUES (?, CAST(? AS BIT), ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { id, 1, isOracle() ? 0 : false });
        jdbcTemplate.executeUpdate(insertSql, new Object[] { nullId, null, null });

        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT bool_bit, bool_boolean FROM basic_types_explicit_test WHERE id = ?", new Object[] { id });
        Map<String, Object> nullRow = jdbcTemplate.queryForMap("SELECT bool_bit, bool_boolean FROM basic_types_explicit_test WHERE id = ?", new Object[] { nullId });

        assertTrue(booleanValue(value(row, "bool_bit")));
        assertFalse(booleanValue(value(row, "bool_boolean")));
        assertNull(value(nullRow, "bool_bit"));
        assertNull(value(nullRow, "bool_boolean"));
    }
}
