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
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.test.contract.material.model.naming.AllNamingOptionsUser;
import net.hasor.dbvisitor.test.contract.material.model.naming.CaseTestUpperCI;
import net.hasor.dbvisitor.test.contract.material.model.naming.DelimitedUser;
import net.hasor.dbvisitor.test.contract.material.model.naming.KeywordColumnEntity;
import net.hasor.dbvisitor.test.contract.material.model.naming.KeywordColumnNoDelimitedEntity;
import net.hasor.dbvisitor.test.contract.material.model.naming.KeywordTableEntity;
import net.hasor.dbvisitor.test.contract.material.model.naming.KeywordTableNoDelimitedEntity;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class IdentifierQuotingCase extends NamingMappingSupport {
    @Test
    @Capability(CapabilityId.NAMING_DELIMITED_SQL)
    public void delimitedSql_shouldQuoteIdentifiersWithDialectQualifiers() throws SQLException {
        SqlDialect dialect = detectDialect();
        String left = dialect.leftQualifier();
        String right = dialect.rightQualifier();

        BoundSql insertSql = lambdaTemplate.insert(AllNamingOptionsUser.class)//
                .applyEntity(allNamingUser(baseId() + 6, "NXN-DelimitedSql"))//
                .getBoundSql();
        String sql = insertSql.getSqlString();

        assertTrue(sql, sql.contains(left + "user_info" + right));
        assertTrue(sql, sql.contains(left + "id" + right));
        assertTrue(sql, sql.contains(left + "name" + right));
        assertTrue(sql, sql.contains(left + "create_time" + right));

        BoundSql caseSql = lambdaTemplate.query(CaseTestUpperCI.class)//
                .eq(CaseTestUpperCI::getId, 1)//
                .getBoundSql();
        assertTrue(caseSql.getSqlString(), caseSql.getSqlString().contains(left + "Case_Test_Upper" + right));
        assertTrue(caseSql.getSqlString(), caseSql.getSqlString().contains(left + "Id" + right));
    }

    @Test
    @Capability(CapabilityId.NAMING_DELIMITED_CRUD)
    public void delimitedCrud_shouldRoundTripAgainstStandardUserInfo() throws SQLException {
        requiresNxnFeature(FeatureId.DELIMITED_LOWERCASE_STANDARD_TABLE);
        int id = baseId() + 7;
        DelimitedUser user = new DelimitedUser();
        user.setId(id);
        user.setName("NXN-DelimitedCrud");
        user.setAge(37);
        user.setEmail("delimited@nxn.test");
        user.setCreateTime(new Date());

        int inserted = lambdaTemplate.insert(DelimitedUser.class).applyEntity(user).executeSumResult();
        int updated = lambdaTemplate.update(DelimitedUser.class)//
                .eq(DelimitedUser::getId, id)//
                .updateTo(DelimitedUser::getName, "NXN-DelimitedUpdated")//
                .doUpdate();
        DelimitedUser loaded = lambdaTemplate.query(DelimitedUser.class)//
                .eq(DelimitedUser::getId, id)//
                .queryForObject();
        int deleted = lambdaTemplate.delete(DelimitedUser.class)//
                .eq(DelimitedUser::getId, id)//
                .doDelete();
        long count = lambdaTemplate.query(DelimitedUser.class)//
                .eq(DelimitedUser::getId, id)//
                .queryForCount();

        assertEquals(1, inserted);
        assertEquals(1, updated);
        assertNotNull(loaded);
        assertEquals("NXN-DelimitedUpdated", loaded.getName());
        assertEquals(1, deleted);
        assertEquals(0, count);
    }

    @Test
    @Capability(CapabilityId.NAMING_KEYWORD_COLUMN_SQL)
    public void keywordColumnSql_shouldQuoteOnlyKeywordColumnsWhenAutoDetected() throws SQLException {
        SqlDialect dialect = detectDialect();
        String left = dialect.leftQualifier();
        String right = dialect.rightQualifier();

        KeywordColumnNoDelimitedEntity autoEntity = keywordColumnNoDelimited(1, "ORDER-A", "SELECT-A", "AutoKeyword");
        BoundSql autoSql = lambdaTemplate.insert(KeywordColumnNoDelimitedEntity.class).applyEntity(autoEntity).getBoundSql();
        String sql = autoSql.getSqlString();

        assertTrue(sql, sql.contains(left + "order" + right));
        assertTrue(sql, sql.contains(left + "select" + right));
        assertTrue(sql, !sql.contains(left + "id" + right));
        assertTrue(sql, !sql.contains(left + "name" + right));
    }

    @Test
    @Capability(CapabilityId.NAMING_KEYWORD_TABLE_SQL)
    public void keywordTableSql_shouldQuoteKeywordTableWhenAutoDetected() throws SQLException {
        SqlDialect dialect = detectDialect();
        String left = dialect.leftQualifier();
        String right = dialect.rightQualifier();

        KeywordTableNoDelimitedEntity entity = keywordTableNoDelimited(1, "AutoKeywordTable", "desc");
        BoundSql sql = lambdaTemplate.insert(KeywordTableNoDelimitedEntity.class).applyEntity(entity).getBoundSql();

        assertTrue(sql.getSqlString(), sql.getSqlString().contains(left + "order" + right));
        assertTrue(sql.getSqlString(), !sql.getSqlString().contains(left + "id" + right));
        assertTrue(sql.getSqlString(), !sql.getSqlString().contains(left + "name" + right));
    }

    @Test
    @Capability(CapabilityId.NAMING_KEYWORD_COLUMN_CRUD)
    public void keywordColumnCrud_shouldRoundTripWithDelimitedKeywordColumns() throws SQLException {
        ensureKeywordColumnTable();

        KeywordColumnEntity entity = keywordColumn(940001, "ORDER-001", "SELECT-001", "KeywordCol");
        assertEquals(1, lambdaTemplate.insert(KeywordColumnEntity.class).applyEntity(entity).executeSumResult());

        KeywordColumnEntity loaded = lambdaTemplate.query(KeywordColumnEntity.class)//
                .eq(KeywordColumnEntity::getId, 940001)//
                .queryForObject();
        assertNotNull(loaded);
        assertEquals("ORDER-001", loaded.getOrderValue());
        assertEquals("SELECT-001", loaded.getSelectValue());

        int updated = lambdaTemplate.update(KeywordColumnEntity.class)//
                .eq(KeywordColumnEntity::getId, 940001)//
                .updateTo(KeywordColumnEntity::getOrderValue, "ORDER-002")//
                .updateTo(KeywordColumnEntity::getSelectValue, "SELECT-002")//
                .doUpdate();
        assertEquals(1, updated);
    }

    @Test
    @Capability(CapabilityId.NAMING_KEYWORD_TABLE_CRUD)
    public void keywordTableCrud_shouldRoundTripWithDelimitedKeywordTable() throws SQLException {
        ensureKeywordTable();

        KeywordTableEntity entity = keywordTable(940002, "KeywordTable", "Table named order");
        assertEquals(1, lambdaTemplate.insert(KeywordTableEntity.class).applyEntity(entity).executeSumResult());

        KeywordTableEntity loaded = lambdaTemplate.query(KeywordTableEntity.class)//
                .eq(KeywordTableEntity::getId, 940002)//
                .queryForObject();
        assertNotNull(loaded);
        assertEquals("KeywordTable", loaded.getName());

        int deleted = lambdaTemplate.delete(KeywordTableEntity.class)//
                .eq(KeywordTableEntity::getId, 940002)//
                .doDelete();
        assertEquals(1, deleted);
    }
}
