/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.naming;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.test.contract.material.model.naming.UpperCaseColumnStrictUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@NxnContract
public abstract class ResultColumnCasingCase extends NamingMappingSupport {
    @Test
    @Capability(CapabilityId.NAMING_CASE_SENSITIVE_FIELD_MISMATCH)
    public void caseSensitiveMapping_shouldLeaveMismatchedUppercaseColumnsNull() throws SQLException {
        requiresNxnFeature(FeatureId.LOWERCASE_STANDARD_RESULT_COLUMNS);
        int id = baseId() + 17;
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, "NXN-StrictMismatch", 30, "strict-mismatch@nxn.test", new Date() });

        UpperCaseColumnStrictUser loaded = lambdaTemplate.query(UpperCaseColumnStrictUser.class)//
                .apply("id = ?", id)//
                .queryForObject();

        assertNotNull(loaded);
        assertNull(loaded.getName());
        assertNull(loaded.getAge());
        assertNull(loaded.getCreateTime());
    }

    @Test
    @Capability(CapabilityId.NAMING_CASE_INSENSITIVE_FREEDOM_MAP)
    public void caseInsensitiveFreedomMap_shouldAllowCaseInsensitiveKeyLookup() throws SQLException {
        int id = baseId() + 19;
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email) VALUES (?, ?, ?, ?)", //
                new Object[] { id, "NXN-MapCI", 33, "map-ci@nxn.test" });

        LambdaTemplate optLambda = optionsLambda(Options.of().caseInsensitive(true));
        Map<String, Object> row = optLambda.queryFreedom("user_info")//
                .eq("id", id)//
                .queryForObject();

        assertNotNull(row);
        assertEquals("NXN-MapCI", row.get("name"));
        assertEquals("NXN-MapCI", row.get("NAME"));
    }

    @Test
    @Capability(CapabilityId.NAMING_CASE_SENSITIVE_FREEDOM_MAP)
    public void caseSensitiveFreedomMap_shouldRequireExactResultColumnCase() throws SQLException {
        requiresNxnFeature(FeatureId.LOWERCASE_STANDARD_RESULT_COLUMNS);
        int id = baseId() + 20;
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email) VALUES (?, ?, ?, ?)", //
                new Object[] { id, "NXN-MapCS", 34, "map-cs@nxn.test" });

        LambdaTemplate optLambda = optionsLambda(Options.of().caseInsensitive(false));
        Map<String, Object> row = optLambda.queryFreedom("user_info")//
                .eq("id", id)//
                .queryForObject();

        assertNotNull(row);
        assertEquals("NXN-MapCS", row.get("name"));
        assertNull(row.get("NAME"));
    }
}
