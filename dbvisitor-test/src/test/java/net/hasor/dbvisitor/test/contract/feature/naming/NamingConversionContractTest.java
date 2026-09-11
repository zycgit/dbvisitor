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

import org.junit.Test;

import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.test.contract.material.model.naming.CamelCaseColumnOverrideUser;
import net.hasor.dbvisitor.test.contract.material.model.naming.CamelCaseDisabledUser;
import net.hasor.dbvisitor.test.contract.material.model.naming.CamelCaseEnabledUser;
import net.hasor.dbvisitor.test.contract.material.model.naming.PlainUser;
import net.hasor.dbvisitor.test.contract.material.model.naming.UpperCaseColumnStrictUser;
import net.hasor.dbvisitor.test.contract.material.model.naming.UpperCaseColumnUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class NamingConversionContractTest extends NamingMappingSupport {
    @Test
    @Capability(CapabilityId.NAMING_CAMELCASE_ENTITY)
    public void camelCaseEntity_shouldMapCreateTimeToCreateTimeColumn() throws SQLException {
        int id = baseId() + 1;
        CamelCaseEnabledUser user = new CamelCaseEnabledUser();
        user.setId(id);
        user.setName("NXN-Camel");
        user.setAge(31);
        user.setEmail("camel@nxn.test");
        user.setCreateTime(new Date());

        int rows = lambdaTemplate.insert(CamelCaseEnabledUser.class).applyEntity(user).executeSumResult();
        CamelCaseEnabledUser loaded = lambdaTemplate.query(CamelCaseEnabledUser.class)//
                .eq(CamelCaseEnabledUser::getId, id)//
                .queryForObject();

        assertEquals(1, rows);
        assertNotNull(loaded);
        assertEquals("NXN-Camel", loaded.getName());
        assertNotNull(loaded.getCreateTime());
    }

    @Test
    @Capability(CapabilityId.NAMING_CAMELCASE_DISABLED)
    public void camelCaseDisabled_shouldNotMapCreateTimeColumnWithoutFallbackOptions() throws SQLException {
        int id = baseId() + 11;
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, "NXN-CamelDisabled", 28, "disabled@nxn.test", new Date() });

        CamelCaseEnabledUser enabled = lambdaTemplate.query(CamelCaseEnabledUser.class)//
                .eq(CamelCaseEnabledUser::getId, id)//
                .queryForObject();
        CamelCaseDisabledUser disabled = lambdaTemplate.query(CamelCaseDisabledUser.class)//
                .eq(CamelCaseDisabledUser::getId, id)//
                .queryForObject();

        assertNotNull(enabled.getCreateTime());
        assertNull(disabled.getCreateTime());
        assertEquals(enabled.getName(), disabled.getName());
    }

    @Test
    @Capability(CapabilityId.NAMING_CAMELCASE_OPTIONS)
    public void camelCaseOptions_shouldApplyToPlainEntityAndAnnotationDefaults() throws SQLException {
        ensurePlainUserTable();
        int plainId = baseId() + 12;
        jdbcTemplate.executeUpdate("INSERT INTO plain_user (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { plainId, "NXN-PlainOptions", 29, "plain@nxn.test", new Date() });

        LambdaTemplate optionsLambda = optionsLambda(Options.of().mapUnderscoreToCamelCase(true));
        PlainUser plain = optionsLambda.query(PlainUser.class)//
                .eq(PlainUser::getId, plainId)//
                .queryForObject();
        assertNotNull(plain);
        assertNotNull(plain.getCreateTime());

        int defaultId = baseId() + 13;
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { defaultId, "NXN-AnnotationDefault", 31, "default@nxn.test", new Date() });
        CamelCaseDisabledUser fallback = optionsLambda.query(CamelCaseDisabledUser.class)//
                .eq(CamelCaseDisabledUser::getId, defaultId)//
                .queryForObject();
        assertNotNull(fallback.getCreateTime());

        LambdaTemplate noFallbackLambda = optionsLambda(Options.of());
        CamelCaseEnabledUser explicit = noFallbackLambda.query(CamelCaseEnabledUser.class)//
                .eq(CamelCaseEnabledUser::getId, defaultId)//
                .queryForObject();
        assertNotNull(explicit.getCreateTime());
    }

    @Test
    @Capability(CapabilityId.NAMING_CAMELCASE_UPDATE)
    public void camelCaseUpdate_shouldUseMappedCreateTimeColumn() throws SQLException {
        int id = baseId() + 14;
        CamelCaseEnabledUser user = camelCaseUser(id, "NXN-CamelUpdate", 22);
        lambdaTemplate.insert(CamelCaseEnabledUser.class).applyEntity(user).executeSumResult();

        int rows = lambdaTemplate.update(CamelCaseEnabledUser.class)//
                .eq(CamelCaseEnabledUser::getId, id)//
                .updateTo(CamelCaseEnabledUser::getCreateTime, new Date(System.currentTimeMillis() + 1000))//
                .doUpdate();
        CamelCaseEnabledUser loaded = lambdaTemplate.query(CamelCaseEnabledUser.class)//
                .eq(CamelCaseEnabledUser::getId, id)//
                .queryForObject();

        assertEquals(1, rows);
        assertNotNull(loaded.getCreateTime());
    }

    @Test
    @Capability(CapabilityId.NAMING_CAMELCASE_DELETE)
    public void camelCaseDelete_shouldUseMappedCreateTimeCondition() throws SQLException {
        int id = baseId() + 15;
        CamelCaseEnabledUser user = camelCaseUser(id, "NXN-CamelDelete", 29);
        lambdaTemplate.insert(CamelCaseEnabledUser.class).applyEntity(user).executeSumResult();

        int rows = lambdaTemplate.delete(CamelCaseEnabledUser.class)//
                .eq(CamelCaseEnabledUser::getId, id)//
                .isNotNull(CamelCaseEnabledUser::getCreateTime)//
                .doDelete();
        long count = lambdaTemplate.query(CamelCaseEnabledUser.class)//
                .eq(CamelCaseEnabledUser::getId, id)//
                .queryForCount();

        assertEquals(1, rows);
        assertEquals(0, count);
    }

    @Test
    @Capability(CapabilityId.NAMING_COLUMN_OVERRIDE)
    public void columnAnnotation_shouldOverrideCamelCaseName() throws SQLException {
        int id = baseId() + 2;
        CamelCaseColumnOverrideUser user = new CamelCaseColumnOverrideUser();
        user.setId(id);
        user.setUserName("NXN-ColumnOverride");
        user.setAge(32);
        user.setEmail("override@nxn.test");
        user.setCreateTime(new Date());

        int rows = lambdaTemplate.insert(CamelCaseColumnOverrideUser.class).applyEntity(user).executeSumResult();
        CamelCaseColumnOverrideUser loaded = lambdaTemplate.query(CamelCaseColumnOverrideUser.class)//
                .eq(CamelCaseColumnOverrideUser::getId, id)//
                .queryForObject();

        assertEquals(1, rows);
        assertNotNull(loaded);
        assertEquals("NXN-ColumnOverride", loaded.getUserName());
        assertNotNull(loaded.getCreateTime());
    }

    @Test
    @Capability(CapabilityId.NAMING_LAMBDA_PROPERTY_REF)
    public void lambdaPropertyRef_shouldUseMappedColumnNames() throws SQLException {
        int id = baseId() + 3;
        CamelCaseColumnOverrideUser user = new CamelCaseColumnOverrideUser();
        user.setId(id);
        user.setUserName("NXN-LambdaRef");
        user.setAge(33);
        user.setEmail("lambda-ref@nxn.test");
        user.setCreateTime(new Date());
        lambdaTemplate.insert(CamelCaseColumnOverrideUser.class).applyEntity(user).executeSumResult();

        CamelCaseColumnOverrideUser loaded = lambdaTemplate.query(CamelCaseColumnOverrideUser.class)//
                .eq(CamelCaseColumnOverrideUser::getUserName, "NXN-LambdaRef")//
                .isNotNull(CamelCaseColumnOverrideUser::getCreateTime)//
                .queryForObject();

        assertNotNull(loaded);
        assertEquals(Integer.valueOf(id), loaded.getId());
    }

    @Test
    @Capability(CapabilityId.NAMING_CASE_INSENSITIVE_SQL)
    public void caseInsensitive_shouldNotChangeGeneratedSql() throws SQLException {
        BoundSql ciInsert = lambdaTemplate.insert(UpperCaseColumnUser.class)//
                .applyEntity(newUpperCaseUser(baseId() + 4, "NXN-CI"))//
                .getBoundSql();
        BoundSql csInsert = lambdaTemplate.insert(UpperCaseColumnStrictUser.class)//
                .applyEntity(newUpperCaseStrictUser(baseId() + 5, "NXN-CS"))//
                .getBoundSql();

        assertEquals(ciInsert.getSqlString(), csInsert.getSqlString());
        assertTrue(ciInsert.getSqlString().contains("NAME"));
    }

    @Test
    @Capability(CapabilityId.NAMING_CASE_INSENSITIVE_FREEDOM_SQL)
    public void caseInsensitiveOptions_shouldNotChangeFreedomSqlGeneration() throws SQLException {
        LambdaTemplate ciLambda = optionsLambda(Options.of().caseInsensitive(true));
        LambdaTemplate csLambda = optionsLambda(Options.of().caseInsensitive(false));

        BoundSql ciSql = ciLambda.queryFreedom("user_info")//
                .eq("id", 1)//
                .getBoundSql();
        BoundSql csSql = csLambda.queryFreedom("user_info")//
                .eq("id", 1)//
                .getBoundSql();

        assertEquals(ciSql.getSqlString(), csSql.getSqlString());
    }
}
