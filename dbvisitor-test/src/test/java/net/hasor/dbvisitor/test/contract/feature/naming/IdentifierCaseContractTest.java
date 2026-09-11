/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.naming;

import java.sql.SQLException;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.test.contract.material.model.naming.CaseTestLower;
import net.hasor.dbvisitor.test.contract.material.model.naming.CaseTestUpperCI;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class IdentifierCaseContractTest extends NamingMappingSupport {
    @Test
    @Capability(CapabilityId.NAMING_CASE_INSENSITIVE_MIXED_CASE_CRUD)
    public void caseInsensitiveMixedCaseTable_shouldRoundTripWhenIdentifiersAreCaseSensitive() throws SQLException {
        requiresNxnFeature(FeatureId.CASE_SENSITIVE_IDENTIFIERS);
        ensureCaseSensitivityTables();
        int id = baseId() + 16;

        CaseTestUpperCI entity = caseTestUpper(id, "NXN-MixedCaseCrud", "mixed-crud");
        assertEquals(1, lambdaTemplate.insert(CaseTestUpperCI.class).applyEntity(entity).executeSumResult());

        CaseTestUpperCI loaded = lambdaTemplate.query(CaseTestUpperCI.class)//
                .eq(CaseTestUpperCI::getId, id)//
                .queryForObject();
        assertNotNull(loaded);
        assertEquals("NXN-MixedCaseCrud", loaded.getName());
        assertEquals(Integer.valueOf(25), loaded.getAge());
        assertEquals("mixed-crud", loaded.getMemo());

        int updated = lambdaTemplate.update(CaseTestUpperCI.class)//
                .eq(CaseTestUpperCI::getId, id)//
                .updateTo(CaseTestUpperCI::getName, "NXN-MixedCaseUpdated")//
                .doUpdate();
        assertEquals(1, updated);

        CaseTestUpperCI updatedEntity = lambdaTemplate.query(CaseTestUpperCI.class)//
                .eq(CaseTestUpperCI::getId, id)//
                .queryForObject();
        assertEquals("NXN-MixedCaseUpdated", updatedEntity.getName());

        assertEquals(1, lambdaTemplate.delete(CaseTestUpperCI.class)//
                .eq(CaseTestUpperCI::getId, id)//
                .doDelete());
    }

    @Test
    @Capability(CapabilityId.NAMING_CASE_SENSITIVE_TABLE_ISOLATION)
    public void caseSensitiveIdentifiers_shouldKeepLowerAndMixedCaseTablesIsolated() throws SQLException {
        requiresNxnFeature(FeatureId.CASE_SENSITIVE_IDENTIFIERS);
        ensureCaseSensitivityTables();
        int id = baseId() + 18;

        CaseTestLower lower = new CaseTestLower();
        lower.setId(id);
        lower.setName("NXN-LowerData");
        lower.setAge(20);
        lower.setMemo("from-lower");
        assertEquals(1, lambdaTemplate.insert(CaseTestLower.class).applyEntity(lower).executeSumResult());

        CaseTestUpperCI upper = caseTestUpper(id, "NXN-UpperData", "from-upper");
        assertEquals(1, lambdaTemplate.insert(CaseTestUpperCI.class).applyEntity(upper).executeSumResult());

        CaseTestLower loadedLower = lambdaTemplate.query(CaseTestLower.class)//
                .eq(CaseTestLower::getId, id)//
                .queryForObject();
        CaseTestUpperCI loadedUpper = lambdaTemplate.query(CaseTestUpperCI.class)//
                .eq(CaseTestUpperCI::getId, id)//
                .queryForObject();

        assertNotNull(loadedLower);
        assertNotNull(loadedUpper);
        assertEquals("NXN-LowerData", loadedLower.getName());
        assertEquals("from-lower", loadedLower.getMemo());
        assertEquals("NXN-UpperData", loadedUpper.getName());
        assertEquals("from-upper", loadedUpper.getMemo());
        assertTrue(!loadedLower.getName().equals(loadedUpper.getName()));
    }

    @Test
    @Capability(CapabilityId.NAMING_CASE_SENSITIVE_FREEDOM_MIXED_CASE)
    public void caseSensitiveFreedomQuery_shouldPreserveMixedCaseResultKeys() throws SQLException {
        requiresNxnFeature(FeatureId.CASE_SENSITIVE_IDENTIFIERS);
        ensureCaseSensitivityTables();
        int id = baseId() + 21;
        jdbcTemplate.executeUpdate("INSERT INTO " + qualified("Case_Test_Upper") + " (" + qualified("Id") + ", " + qualified("Name") + ", " + qualified("Age") + ", " + qualified("Memo") + ") VALUES (?, ?, ?, ?)", //
                new Object[] { id, "NXN-FreedomMixed", 40, "freedom-mixed" });

        LambdaTemplate optLambda = optionsLambda(Options.of().caseInsensitive(false).useDelimited(true));
        Map<String, Object> row = optLambda.queryFreedom(null, null, "Case_Test_Upper")//
                .eq("Id", id)//
                .queryForObject();

        assertNotNull(row);
        assertEquals("NXN-FreedomMixed", row.get("Name"));
        assertNull(row.get("name"));
    }

    @Test
    @Capability(CapabilityId.NAMING_CASE_INSENSITIVE_BATCH_MAPPING)
    public void caseInsensitiveMixedCaseTable_shouldMapMultipleRows() throws SQLException {
        requiresNxnFeature(FeatureId.CASE_SENSITIVE_IDENTIFIERS);
        ensureCaseSensitivityTables();

        for (int i = 1; i <= 3; i++) {
            CaseTestUpperCI entity = caseTestUpper(baseId() + 30 + i, "NXN-Batch" + i, "batch-" + i);
            lambdaTemplate.insert(CaseTestUpperCI.class).applyEntity(entity).executeSumResult();
        }

        java.util.List<CaseTestUpperCI> list = lambdaTemplate.query(CaseTestUpperCI.class)//
                .ge(CaseTestUpperCI::getId, baseId() + 31)//
                .le(CaseTestUpperCI::getId, baseId() + 33)//
                .queryForList();

        assertEquals(3, list.size());
        for (CaseTestUpperCI entity : list) {
            assertNotNull(entity.getName());
            assertNotNull(entity.getAge());
        }
    }
}
