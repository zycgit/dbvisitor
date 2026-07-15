package net.hasor.dbvisitor.test.contract.feature.naming;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialectRegister;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.test.contract.material.model.naming.AllNamingOptionsUser;
import net.hasor.dbvisitor.test.contract.material.model.naming.CamelCaseColumnOverrideUser;
import net.hasor.dbvisitor.test.contract.material.model.naming.CamelCaseDisabledUser;
import net.hasor.dbvisitor.test.contract.material.model.naming.CamelCaseEnabledUser;
import net.hasor.dbvisitor.test.contract.material.model.naming.CaseTestLower;
import net.hasor.dbvisitor.test.contract.material.model.naming.CaseTestUpperCI;
import net.hasor.dbvisitor.test.contract.material.model.naming.DelimitedUser;
import net.hasor.dbvisitor.test.contract.material.model.naming.KeywordColumnEntity;
import net.hasor.dbvisitor.test.contract.material.model.naming.KeywordColumnNoDelimitedEntity;
import net.hasor.dbvisitor.test.contract.material.model.naming.KeywordTableEntity;
import net.hasor.dbvisitor.test.contract.material.model.naming.KeywordTableNoDelimitedEntity;
import net.hasor.dbvisitor.test.contract.material.model.naming.PlainUser;
import net.hasor.dbvisitor.test.contract.material.model.naming.UpperCaseColumnStrictUser;
import net.hasor.dbvisitor.test.contract.material.model.naming.UpperCaseColumnUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class NamingMappingContractTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 930000;
    }

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
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, @{macro, currentTimestamp})", //
                new Object[] { id, "NXN-CamelDisabled", 28, "disabled@nxn.test" });

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
        jdbcTemplate.executeUpdate("INSERT INTO plain_user (id, name, age, email, create_time) VALUES (?, ?, ?, ?, @{macro, currentTimestamp})", //
                new Object[] { plainId, "NXN-PlainOptions", 29, "plain@nxn.test" });

        LambdaTemplate optionsLambda = new LambdaTemplate(dataSource, Options.of().mapUnderscoreToCamelCase(true));
        PlainUser plain = optionsLambda.query(PlainUser.class)//
                .eq(PlainUser::getId, plainId)//
                .queryForObject();
        assertNotNull(plain);
        assertNotNull(plain.getCreateTime());

        int defaultId = baseId() + 13;
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, @{macro, currentTimestamp})", //
                new Object[] { defaultId, "NXN-AnnotationDefault", 31, "default@nxn.test" });
        CamelCaseDisabledUser fallback = optionsLambda.query(CamelCaseDisabledUser.class)//
                .eq(CamelCaseDisabledUser::getId, defaultId)//
                .queryForObject();
        assertNotNull(fallback.getCreateTime());

        LambdaTemplate noFallbackLambda = new LambdaTemplate(dataSource, Options.of());
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
        LambdaTemplate ciLambda = new LambdaTemplate(dataSource, Options.of().caseInsensitive(true));
        LambdaTemplate csLambda = new LambdaTemplate(dataSource, Options.of().caseInsensitive(false));

        BoundSql ciSql = ciLambda.queryFreedom("user_info")//
                .eq("id", 1)//
                .getBoundSql();
        BoundSql csSql = csLambda.queryFreedom("user_info")//
                .eq("id", 1)//
                .getBoundSql();

        assertEquals(ciSql.getSqlString(), csSql.getSqlString());
    }

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
    @Capability(CapabilityId.NAMING_CASE_SENSITIVE_FIELD_MISMATCH)
    public void caseSensitiveMapping_shouldLeaveMismatchedUppercaseColumnsNull() throws SQLException {
        requiresNxnFeature(FeatureId.CASE_SENSITIVE_IDENTIFIERS);
        requiresNxnFeature(FeatureId.LOWERCASE_STANDARD_RESULT_COLUMNS);
        int id = baseId() + 17;
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, @{macro, currentTimestamp})", //
                new Object[] { id, "NXN-StrictMismatch", 30, "strict-mismatch@nxn.test" });

        UpperCaseColumnStrictUser loaded = lambdaTemplate.query(UpperCaseColumnStrictUser.class)//
                .eq(UpperCaseColumnStrictUser::getId, id)//
                .queryForObject();

        assertNotNull(loaded);
        assertNull(loaded.getName());
        assertNull(loaded.getAge());
        assertNull(loaded.getCreateTime());
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
    @Capability(CapabilityId.NAMING_CASE_INSENSITIVE_FREEDOM_MAP)
    public void caseInsensitiveFreedomMap_shouldAllowCaseInsensitiveKeyLookup() throws SQLException {
        requiresNxnFeature(FeatureId.CASE_SENSITIVE_IDENTIFIERS);
        int id = baseId() + 19;
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email) VALUES (?, ?, ?, ?)", //
                new Object[] { id, "NXN-MapCI", 33, "map-ci@nxn.test" });

        LambdaTemplate optLambda = new LambdaTemplate(dataSource, Options.of().caseInsensitive(true));
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
        requiresNxnFeature(FeatureId.CASE_SENSITIVE_IDENTIFIERS);
        requiresNxnFeature(FeatureId.LOWERCASE_STANDARD_RESULT_COLUMNS);
        int id = baseId() + 20;
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email) VALUES (?, ?, ?, ?)", //
                new Object[] { id, "NXN-MapCS", 34, "map-cs@nxn.test" });

        LambdaTemplate optLambda = new LambdaTemplate(dataSource, Options.of().caseInsensitive(false));
        Map<String, Object> row = optLambda.queryFreedom("user_info")//
                .eq("id", id)//
                .queryForObject();

        assertNotNull(row);
        assertEquals("NXN-MapCS", row.get("name"));
        assertNull(row.get("NAME"));
    }

    @Test
    @Capability(CapabilityId.NAMING_CASE_SENSITIVE_FREEDOM_MIXED_CASE)
    public void caseSensitiveFreedomQuery_shouldPreserveMixedCaseResultKeys() throws SQLException {
        requiresNxnFeature(FeatureId.CASE_SENSITIVE_IDENTIFIERS);
        ensureCaseSensitivityTables();
        int id = baseId() + 21;
        jdbcTemplate.executeUpdate("INSERT INTO " + qualified("Case_Test_Upper") + " (" + qualified("Id") + ", " + qualified("Name") + ", " + qualified("Age") + ", " + qualified("Memo") + ") VALUES (?, ?, ?, ?)", //
                new Object[] { id, "NXN-FreedomMixed", 40, "freedom-mixed" });

        LambdaTemplate optLambda = new LambdaTemplate(dataSource, Options.of().caseInsensitive(false).useDelimited(true));
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
                .like(CaseTestUpperCI::getName, "NXN-Batch%")//
                .queryForList();

        assertEquals(3, list.size());
        for (CaseTestUpperCI entity : list) {
            assertNotNull(entity.getName());
            assertNotNull(entity.getAge());
        }
    }

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

    private SqlDialect detectDialect() throws SQLException {
        Connection conn = dataSource.getConnection();
        try {
            return SqlDialectRegister.findDialect(null, conn);
        } finally {
            conn.close();
        }
    }

    private UpperCaseColumnUser newUpperCaseUser(int id, String name) {
        UpperCaseColumnUser user = new UpperCaseColumnUser();
        user.setId(id);
        user.setName(name);
        user.setAge(30);
        user.setEmail(name.toLowerCase() + "@nxn.test");
        user.setCreateTime(new Date());
        return user;
    }

    private CamelCaseEnabledUser camelCaseUser(int id, String name, int age) {
        CamelCaseEnabledUser user = new CamelCaseEnabledUser();
        user.setId(id);
        user.setName(name);
        user.setAge(age);
        user.setEmail(name.toLowerCase() + "@nxn.test");
        user.setCreateTime(new Date());
        return user;
    }

    private void ensurePlainUserTable() throws SQLException {
        dropTableIfExists("plain_user");
        jdbcTemplate.executeUpdate("CREATE TABLE plain_user (" + primaryKeyColumn("id", "INT") + ", name VARCHAR(100), age INT, email VARCHAR(100), create_time " + profile().datetimeColumnType() + ")");
    }

    private UpperCaseColumnStrictUser newUpperCaseStrictUser(int id, String name) {
        UpperCaseColumnStrictUser user = new UpperCaseColumnStrictUser();
        user.setId(id);
        user.setName(name);
        user.setAge(30);
        user.setEmail(name.toLowerCase() + "@nxn.test");
        user.setCreateTime(new Date());
        return user;
    }

    private AllNamingOptionsUser allNamingUser(int id, String name) {
        AllNamingOptionsUser user = new AllNamingOptionsUser();
        user.setId(id);
        user.setName(name);
        user.setAge(36);
        user.setEmail(name.toLowerCase() + "@nxn.test");
        user.setCreateTime(new Date());
        return user;
    }

    private void ensureCaseSensitivityTables() throws SQLException {
        dropTableIfExists("case_test_lower");
        dropTableIfExists(qualified("Case_Test_Upper"));
        jdbcTemplate.executeUpdate("CREATE TABLE case_test_lower (" + primaryKeyColumn("id", "INT") + ", name VARCHAR(100), age INT, memo VARCHAR(200))");
        jdbcTemplate.executeUpdate("CREATE TABLE " + qualified("Case_Test_Upper") + " (" //
                + primaryKeyColumn(qualified("Id"), "INT") + ", " //
                + qualified("Name") + " VARCHAR(100), " //
                + qualified("Age") + " INT, " //
                + qualified("Memo") + " VARCHAR(200))");
    }

    private CaseTestUpperCI caseTestUpper(int id, String name, String memo) {
        CaseTestUpperCI entity = new CaseTestUpperCI();
        entity.setId(id);
        entity.setName(name);
        entity.setAge(25);
        entity.setMemo(memo);
        return entity;
    }

    private String qualified(String identifier) {
        return profile().leftQualifier() + identifier + profile().rightQualifier();
    }

    private void ensureKeywordColumnTable() throws SQLException {
        String left = profile().leftQualifier();
        String right = profile().rightQualifier();
        dropTableIfExists(left + "naming_keyword_test" + right);
        jdbcTemplate.executeUpdate("CREATE TABLE " + left + "naming_keyword_test" + right + " (" //
                + primaryKeyColumn(left + "id" + right, "INT") + ", " //
                + left + "order" + right + " VARCHAR(100), " //
                + left + "select" + right + " VARCHAR(100), " //
                + left + "name" + right + " VARCHAR(100))");
    }

    private void ensureKeywordTable() throws SQLException {
        String left = profile().leftQualifier();
        String right = profile().rightQualifier();
        dropTableIfExists(left + "order" + right);
        jdbcTemplate.executeUpdate("CREATE TABLE " + left + "order" + right + " (" //
                + primaryKeyColumn(left + "id" + right, "INT") + ", " //
                + left + "name" + right + " VARCHAR(100), " //
                + left + "description" + right + " VARCHAR(200))");
    }

    private KeywordColumnEntity keywordColumn(int id, String orderValue, String selectValue, String name) {
        KeywordColumnEntity entity = new KeywordColumnEntity();
        entity.setId(id);
        entity.setOrderValue(orderValue);
        entity.setSelectValue(selectValue);
        entity.setName(name);
        return entity;
    }

    private KeywordColumnNoDelimitedEntity keywordColumnNoDelimited(int id, String orderValue, String selectValue, String name) {
        KeywordColumnNoDelimitedEntity entity = new KeywordColumnNoDelimitedEntity();
        entity.setId(id);
        entity.setOrderValue(orderValue);
        entity.setSelectValue(selectValue);
        entity.setName(name);
        return entity;
    }

    private KeywordTableEntity keywordTable(int id, String name, String description) {
        KeywordTableEntity entity = new KeywordTableEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setDescription(description);
        return entity;
    }

    private KeywordTableNoDelimitedEntity keywordTableNoDelimited(int id, String name, String description) {
        KeywordTableNoDelimitedEntity entity = new KeywordTableNoDelimitedEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setDescription(description);
        return entity;
    }
}
