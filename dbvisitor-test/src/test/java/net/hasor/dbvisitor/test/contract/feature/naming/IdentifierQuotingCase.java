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
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.test.contract.material.model.naming.*;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class IdentifierQuotingCase extends NamingMappingSupport {
    protected String mappedTableName(String table) {
        return table;
    }

    protected void prepareDelimitedFixture() throws SQLException {
        // Most fixture schemas already use lowercase identifiers.
    }

    protected void prepareAutoKeywordColumnFixture() throws SQLException {
        ensureKeywordColumnTable();
    }

    protected void prepareAutoKeywordTableFixture() throws SQLException {
        ensureKeywordTable();
    }

    // 能力归属：对象映射 / 命名与名称敏感性 / 名称引用。
    @Test
    @Capability(value = CapabilityId.NAMING_DELIMITED_SQL, column = "mapping-keys/naming-and-case-sensitivity/identifiers")
    public void delimitedSql_shouldQuoteIdentifiersWithDialectQualifiers() throws SQLException {
        SqlDialect dialect = detectDialect();
        String left = dialect.leftQualifier();
        String right = dialect.rightQualifier();

        BoundSql insertSql = lambdaTemplate.insert(AllNamingOptionsUser.class)//
                .applyEntity(allNamingUser(baseId() + 6, "NXN-DelimitedSql"))//
                .getBoundSql();
        String sql = insertSql.getSqlString();

        assertTrue(sql, sql.contains(left + mappedTableName("user_info") + right));
        assertTrue(sql, sql.contains(left + "id" + right));
        assertTrue(sql, sql.contains(left + "name" + right));
        assertTrue(sql, sql.contains(left + "create_time" + right));

        BoundSql caseSql = lambdaTemplate.query(CaseTestUpperCI.class)//
                .eq(CaseTestUpperCI::getId, 1)//
                .getBoundSql();
        assertTrue(caseSql.getSqlString(), caseSql.getSqlString().contains(left + mappedTableName("Case_Test_Upper") + right));
        assertTrue(caseSql.getSqlString(), caseSql.getSqlString().contains(left + "Id" + right));
    }

    // 能力归属：对象映射 / 命名与名称敏感性 / 名称引用。
    @Test
    @Capability(value = CapabilityId.NAMING_DELIMITED_CRUD, column = "mapping-keys/naming-and-case-sensitivity/identifiers")
    public void delimitedCrud_shouldRoundTripAgainstStandardUserInfo() throws SQLException {
        prepareDelimitedFixture();
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

    // 能力归属：对象映射 / 命名与名称敏感性 / 名称引用。
    @Test
    @Capability(value = CapabilityId.NAMING_KEYWORD_COLUMN_SQL, column = "mapping-keys/naming-and-case-sensitivity/identifiers")
    public void keywordColumnSql_shouldQuoteOnlyKeywordColumnsWhenAutoDetected() throws SQLException {
        prepareAutoKeywordColumnFixture();
        SqlDialect dialect = detectDialect();
        String left = dialect.leftQualifier();
        String right = dialect.rightQualifier();

        KeywordColumnNoDelimitedEntity autoEntity = keywordColumnNoDelimited(1, "ORDER-A", "SELECT-A", "AutoKeyword");
        BoundSql autoSql = lambdaTemplate.insert(KeywordColumnNoDelimitedEntity.class).applyEntity(autoEntity).getBoundSql();
        String sql = autoSql.getSqlString();

        assertTrue(sql, sql.contains(left + "order" + right));
        assertTrue(sql, sql.contains(left + "select" + right));
        if (!left.isEmpty() || !right.isEmpty()) {
            assertFalse(sql, sql.contains(left + "id" + right));
            assertFalse(sql, sql.contains(left + "name" + right));
        }

        assertEquals(1, lambdaTemplate.insert(KeywordColumnNoDelimitedEntity.class).applyEntity(autoEntity).executeSumResult());
        KeywordColumnNoDelimitedEntity loaded = lambdaTemplate.query(KeywordColumnNoDelimitedEntity.class).eq(KeywordColumnNoDelimitedEntity::getId, 1).queryForObject();
        assertNotNull(loaded);
        assertEquals("ORDER-A", loaded.getOrderValue());
        assertEquals("SELECT-A", loaded.getSelectValue());
        assertEquals("AutoKeyword", loaded.getName());
    }

    // 能力归属：对象映射 / 命名与名称敏感性 / 名称引用。
    @Test
    @Capability(value = CapabilityId.NAMING_KEYWORD_TABLE_SQL, column = "mapping-keys/naming-and-case-sensitivity/identifiers")
    public void keywordTableSql_shouldQuoteKeywordTableWhenAutoDetected() throws SQLException {
        prepareAutoKeywordTableFixture();
        SqlDialect dialect = detectDialect();
        String left = dialect.leftQualifier();
        String right = dialect.rightQualifier();

        KeywordTableNoDelimitedEntity entity = keywordTableNoDelimited(1, "AutoKeywordTable", "desc");
        BoundSql sql = lambdaTemplate.insert(KeywordTableNoDelimitedEntity.class).applyEntity(entity).getBoundSql();

        assertTrue(sql.getSqlString(), sql.getSqlString().contains(left + mappedTableName("order") + right));
        if (!left.isEmpty() || !right.isEmpty()) {
            assertFalse(sql.getSqlString(), sql.getSqlString().contains(left + "id" + right));
            assertFalse(sql.getSqlString(), sql.getSqlString().contains(left + "name" + right));
        }

        assertEquals(1, lambdaTemplate.insert(KeywordTableNoDelimitedEntity.class).applyEntity(entity).executeSumResult());
        KeywordTableNoDelimitedEntity loaded = lambdaTemplate.query(KeywordTableNoDelimitedEntity.class).eq(KeywordTableNoDelimitedEntity::getId, 1).queryForObject();
        assertNotNull(loaded);
        assertEquals("AutoKeywordTable", loaded.getName());
    }

    // 能力归属：对象映射 / 命名与名称敏感性 / 名称引用。
    @Test
    @Capability(value = CapabilityId.NAMING_KEYWORD_COLUMN_CRUD, column = "mapping-keys/naming-and-case-sensitivity/identifiers")
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
        KeywordColumnEntity changed = lambdaTemplate.query(KeywordColumnEntity.class)//
                .eq(KeywordColumnEntity::getId, 940001)//
                .queryForObject();
        assertNotNull(changed);
        assertEquals("ORDER-002", changed.getOrderValue());
        assertEquals("SELECT-002", changed.getSelectValue());
        assertEquals("KeywordCol", changed.getName());
    }

    // 能力归属：对象映射 / 命名与名称敏感性 / 名称引用。
    @Test
    @Capability(value = CapabilityId.NAMING_KEYWORD_TABLE_CRUD, column = "mapping-keys/naming-and-case-sensitivity/identifiers")
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
        assertEquals(0, lambdaTemplate.query(KeywordTableEntity.class)//
                .eq(KeywordTableEntity::getId, 940002)//
                .queryForCount());
    }
}
