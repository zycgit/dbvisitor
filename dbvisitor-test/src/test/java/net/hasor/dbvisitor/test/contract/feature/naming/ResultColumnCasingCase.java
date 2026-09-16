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
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.test.contract.material.model.naming.UpperCaseColumnStrictUser;
import net.hasor.dbvisitor.test.contract.material.model.naming.UpperCaseColumnUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class ResultColumnCasingCase extends NamingMappingSupport {
    protected String resultProjection() {
        return "*";
    }

    // 能力归属：对象映射 / 命名与名称敏感性 / 结果列匹配。
    @Test
    @Capability(value = CapabilityId.NAMING_CASE_SENSITIVE_FIELD_MISMATCH, column = "mapping-keys/naming-and-case-sensitivity/result-columns")
    public void caseSensitiveMapping_shouldLeaveMismatchedUppercaseColumnsNull() throws SQLException {
        int id = baseId() + 17;
        jdbcTemplate.executeUpdate(insertCommand("user_info", "id, name, age, email, create_time"), //
                new Object[] { id, "NXN-StrictMismatch", 30, "strict-mismatch@nxn.test", new Date() });

        UpperCaseColumnUser matched = lambdaTemplate.query(UpperCaseColumnUser.class)//
                .applySelect(resultProjection())//
                .apply(rawIdCondition(), id)//
                .queryForObject();
        assertNotNull(matched);
        assertEquals(Integer.valueOf(id), matched.getId());
        assertEquals("NXN-StrictMismatch", matched.getName());
        assertEquals(Integer.valueOf(30), matched.getAge());
        assertNotNull(matched.getCreateTime());

        UpperCaseColumnStrictUser loaded = lambdaTemplate.query(UpperCaseColumnStrictUser.class)//
                .applySelect(resultProjection())//
                .apply(rawIdCondition(), id)//
                .queryForObject();

        assertNotNull(loaded);
        assertNull(loaded.getId());
        assertNull(loaded.getName());
        assertNull(loaded.getAge());
        assertNull(loaded.getCreateTime());
    }

    // 能力归属：对象映射 / 命名与名称敏感性 / 结果列匹配。
    @Test
    @Capability(value = CapabilityId.NAMING_CASE_INSENSITIVE_FREEDOM_MAP, column = "mapping-keys/naming-and-case-sensitivity/result-columns")
    public void caseInsensitiveFreedomMap_shouldAllowCaseInsensitiveKeyLookup() throws SQLException {
        int id = baseId() + 19;
        jdbcTemplate.executeUpdate(insertCommand("user_info", "id, name, age, email"), //
                new Object[] { id, "NXN-MapCI", 33, "map-ci@nxn.test" });

        LambdaTemplate optLambda = optionsLambda(Options.of().caseInsensitive(true));
        Map<String, Object> row = optLambda.queryFreedom(userTable())//
                .applySelect(resultProjection())//
                .eq("id", id)//
                .queryForObject();

        assertNotNull(row);
        assertEquals("NXN-MapCI", row.get("name"));
        assertEquals("NXN-MapCI", row.get("NAME"));
    }

    // 能力归属：对象映射 / 命名与名称敏感性 / 结果列匹配。
    @Test
    @Capability(value = CapabilityId.NAMING_CASE_SENSITIVE_FREEDOM_MAP, column = "mapping-keys/naming-and-case-sensitivity/result-columns")
    public void caseSensitiveFreedomMap_shouldRequireExactResultColumnCase() throws SQLException {
        int id = baseId() + 20;
        jdbcTemplate.executeUpdate(insertCommand("user_info", "id, name, age, email"), //
                new Object[] { id, "NXN-MapCS", 34, "map-cs@nxn.test" });

        LambdaTemplate optLambda = optionsLambda(Options.of().caseInsensitive(false));
        Map<String, Object> row = optLambda.queryFreedom(userTable())//
                .applySelect(resultProjection())//
                .eq("id", id)//
                .queryForObject();

        assertNotNull(row);
        assertEquals("NXN-MapCS", row.get("name"));
        assertNull(row.get("NAME"));
    }
}
