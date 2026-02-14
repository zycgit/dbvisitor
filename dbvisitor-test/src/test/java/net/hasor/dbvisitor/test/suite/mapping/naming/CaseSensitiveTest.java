package net.hasor.dbvisitor.test.suite.mapping.naming;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialectRegister;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.model.naming.CaseTestLower;
import net.hasor.dbvisitor.test.model.naming.CaseTestUpperCI;
import net.hasor.dbvisitor.test.model.naming.UpperCaseColumnStrictUser;
import net.hasor.dbvisitor.test.model.naming.UpperCaseColumnUser;
import org.junit.Assume;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Case Sensitive Test — 验证 caseInsensitive 属性在不同场景下的行为。
 * <h3>测试分为两大类：</h3>
 * <ol>
 *   <li><b>SQL 验证（所有数据库都执行）</b> — 通过 getBoundSql() 检查生成的 SQL 是否正确</li>
 *   <li><b>真实数据库验证（需要 case_sensitivity 策略支持）</b> — 通过两张大小写不同的表验证实际行为</li>
 * </ol>
 * <h3>策略机制：</h3>
 * <ul>
 *   <li>{@code SUPPORTED} — 数据库支持大小写敏感标识符（如 PostgreSQL 使用双引号），执行真实 DB 测试</li>
 *   <li>{@code UNSUPPORTED} — 数据库不支持大小写敏感标识符（如 MySQL lower_case_table_names=1），跳过真实 DB 测试</li>
 *   <li>未注册的方言 — 抛出异常，提醒开发者为新数据库添加策略配置</li>
 * </ul>
 * <h3>真实数据库测试表：</h3>
 * <ul>
 *   <li>{@code case_test_lower} — 全小写表名和列名（id, name, age, memo）</li>
 *   <li>{@code "Case_Test_Upper"} — 带引号的混合大小写表名和列名（"Id", "Name", "Age", "Memo"）</li>
 * </ul>
 */
public class CaseSensitiveTest extends AbstractOneApiTest {

    // ==================== Case-Sensitivity Strategy ====================

    /** 方言 → 策略映射表。新数据库接入时必须在此注册。 */
    private static final Map<String, CaseSensitivitySupport> DIALECT_STRATEGY = new HashMap<String, CaseSensitivitySupport>();

    static {
        DIALECT_STRATEGY.put("pg", CaseSensitivitySupport.SUPPORTED);       // PostgreSQL: 双引号保留大小写
        DIALECT_STRATEGY.put("h2", CaseSensitivitySupport.SUPPORTED);       // H2: 双引号保留大小写
        DIALECT_STRATEGY.put("mysql", CaseSensitivitySupport.UNSUPPORTED);  // MySQL: 取决于 lower_case_table_names，默认不支持
        DIALECT_STRATEGY.put("es7", CaseSensitivitySupport.UNSUPPORTED);    // Elasticsearch: 无传统表/列概念
        DIALECT_STRATEGY.put("milvus", CaseSensitivitySupport.UNSUPPORTED); // Milvus: 无传统表/列概念
    }

    /**
     * 获取当前方言的策略。方言未注册时抛出异常。
     */
    private CaseSensitivitySupport getCaseSensitivitySupport() {
        String dialect = OneApiDataSourceManager.getDbDialect();
        CaseSensitivitySupport support = DIALECT_STRATEGY.get(dialect);
        if (support == null) {
            String msg = "Unknown dialect '" + dialect + "' for case-sensitivity testing. " + "Please register it in CaseSensitiveTest.DIALECT_STRATEGY map.";
            throw new UnsupportedOperationException(msg);
        }
        return support;
    }

    /**
     * 仅在当前方言支持大小写敏感时执行，否则跳过测试。
     */
    private void requireCaseSensitivitySupport() {
        CaseSensitivitySupport support = getCaseSensitivitySupport();
        String msg = "Skipping: dialect '" + OneApiDataSourceManager.getDbDialect() + "' does not support case-sensitive identifiers";
        Assume.assumeTrue(msg, support == CaseSensitivitySupport.SUPPORTED);
    }

    /** 从当前数据库连接自动检测方言，返回对应的 SqlDialect 实例 */
    private SqlDialect detectDialect() throws SQLException {
        Connection conn = dataSource.getConnection();
        try {
            return SqlDialectRegister.findDialect(null, conn);
        } finally {
            conn.close();
        }
    }

    /**
     * 验证 caseInsensitive 属性不影响生成的 SQL。
     * caseInsensitive 控制的是 Java 侧 ResultSet 列名到属性的匹配方式，不改变 SQL 本身。
     */
    @Test
    public void testSql_CaseInsensitive_DoesNotAffectSqlGeneration() throws SQLException {
        // caseInsensitive=true 的实体
        BoundSql ciInsert = lambdaTemplate.insert(UpperCaseColumnUser.class)//
                .applyEntity(newUpperCaseUser(99001, "SqlCI"))//
                .getBoundSql();

        // caseInsensitive=false 的实体（相同 @Column 定义）
        BoundSql csInsert = lambdaTemplate.insert(UpperCaseColumnStrictUser.class)//
                .applyEntity(newUpperCaseStrictUser(99002, "SqlCS"))//
                .getBoundSql();

        // 两者的 INSERT SQL 应该一样（因为 @Column 值相同，caseInsensitive 不影响 SQL）
        assertEquals("caseInsensitive should not affect INSERT SQL", ciInsert.getSqlString(), csInsert.getSqlString());
    }

    // ==================== 第一类：SQL 验证（不依赖数据库能力） ====================

    /**
     * 验证 useDelimited=true 时，SQL 中表名/列名带引号。
     * 限定符字符从方言字典动态获取（如 PG 用 "，MySQL 用 `）。
     * 对比 useDelimited=false（UpperCaseColumnUser）的 SQL 差异。
     */
    @Test
    public void testSql_UseDelimited_QuotesIdentifiers() throws SQLException {
        SqlDialect dialect = detectDialect();
        String L = dialect.leftQualifier();
        String R = dialect.rightQualifier();

        // CaseTestUpperCI: useDelimited=true → 标识符应被限定符包裹
        BoundSql delimitedSql = lambdaTemplate.insert(CaseTestUpperCI.class)//
                .applyEntity(newCaseTestUpperCI(99003, "Delimited"))//
                .getBoundSql();
        String sql = delimitedSql.getSqlString();

        assertTrue("Delimited INSERT should quote table name: " + sql, sql.contains(L + "Case_Test_Upper" + R));
        assertTrue("Delimited INSERT should quote column Id: " + sql, sql.contains(L + "Id" + R));
        assertTrue("Delimited INSERT should quote column Name: " + sql, sql.contains(L + "Name" + R));
        assertTrue("Delimited INSERT should quote column Age: " + sql, sql.contains(L + "Age" + R));
        assertTrue("Delimited INSERT should quote column Memo: " + sql, sql.contains(L + "Memo" + R));

        // UpperCaseColumnUser: useDelimited=false → 标识符不被限定符包裹
        BoundSql nonDelimitedSql = lambdaTemplate.insert(UpperCaseColumnUser.class)//
                .applyEntity(newUpperCaseUser(99004, "NonDel"))//
                .getBoundSql();
        String nonDelSql = nonDelimitedSql.getSqlString();

        assertFalse("Non-delimited should NOT quote table: " + nonDelSql, nonDelSql.contains(L + "user_info" + R));
    }

    /**
     * 验证 SELECT 的 WHERE 条件中，@Column 定义的列名被正确使用。
     * caseInsensitive 不影响 WHERE 子句的生成。
     */
    @Test
    public void testSql_WhereClause_UsesColumnAnnotationName() throws SQLException {
        // caseInsensitive=true
        BoundSql ciQuery = lambdaTemplate.query(UpperCaseColumnUser.class)//
                .eq(UpperCaseColumnUser::getName, "test")//
                .getBoundSql();
        // caseInsensitive=false
        BoundSql csQuery = lambdaTemplate.query(UpperCaseColumnStrictUser.class)//
                .eq(UpperCaseColumnStrictUser::getName, "test")//
                .getBoundSql();

        // WHERE 子句应相同
        assertEquals("WHERE clause should be identical regardless of caseInsensitive", ciQuery.getSqlString(), csQuery.getSqlString());

        // 列名应来自 @Column("NAME")
        assertTrue("WHERE should use @Column name: " + ciQuery.getSqlString(), ciQuery.getSqlString().contains("NAME"));
    }

    /**
     * 验证 UPDATE SET 子句中列名来自 @Column 定义，不受 caseInsensitive 影响。
     */
    @Test
    public void testSql_UpdateSet_UsesColumnAnnotationName() throws SQLException {
        BoundSql ciUpdate = lambdaTemplate.update(UpperCaseColumnUser.class)//
                .eq(UpperCaseColumnUser::getId, 1)//
                .updateTo(UpperCaseColumnUser::getName, "newName")//
                .getBoundSql();
        String sql = ciUpdate.getSqlString();

        assertTrue("UPDATE SET should use @Column('NAME'): " + sql, sql.contains("NAME"));
        assertTrue("UPDATE WHERE should use @Column('ID'): " + sql, sql.contains("ID"));
    }

    /**
     * 验证 useDelimited + caseInsensitive 组合时的 SELECT SQL。
     * CaseTestUpperCI: useDelimited=true, @Column("Id","Name",...) → 限定符包裹。
     */
    @Test
    public void testSql_Delimited_SelectWhere() throws SQLException {
        SqlDialect dialect = detectDialect();
        String L = dialect.leftQualifier();
        String R = dialect.rightQualifier();

        BoundSql querySql = lambdaTemplate.query(CaseTestUpperCI.class)//
                .eq(CaseTestUpperCI::getId, 1)//
                .getBoundSql();
        String sql = querySql.getSqlString();

        assertTrue("SELECT should quote table: " + sql, sql.contains(L + "Case_Test_Upper" + R));
        assertTrue("WHERE should quote Id: " + sql, sql.contains(L + "Id" + R));
    }

    /**
     * 验证 Options.caseInsensitive 对 freedom 模式 SQL 无影响。
     * freedom 模式 SQL 直接使用传入的表名/列名字符串。
     */
    @Test
    public void testSql_Options_FreedomMode_NoEffect() throws SQLException {
        Options ciOpts = Options.of().caseInsensitive(true);
        LambdaTemplate ciLambda = new LambdaTemplate(dataSource, ciOpts);

        Options csOpts = Options.of().caseInsensitive(false);
        LambdaTemplate csLambda = new LambdaTemplate(dataSource, csOpts);

        BoundSql ciSql = ciLambda.queryFreedom("user_info")//
                .eq("id", 1)//
                .getBoundSql();
        BoundSql csSql = csLambda.queryFreedom("user_info")//
                .eq("id", 1)//
                .getBoundSql();

        assertEquals("Freedom SQL should be the same regardless of caseInsensitive", ciSql.getSqlString(), csSql.getSqlString());
    }

    /**
     * 验证 caseInsensitive=true 时，混合大小写的 @Column 能匹配 ResultSet 列名。
     * 使用 "Case_Test_Upper" 表（带引号标识符）和 CaseTestUpperCI 实体。
     */
    @Test
    public void testDb_CaseInsensitiveTrue_MixedCaseTable_CRUD() throws SQLException {
        requireCaseSensitivitySupport();

        // INSERT — 通过实体写入 "Case_Test_Upper" 表
        CaseTestUpperCI entity = newCaseTestUpperCI(50001, "InsertCI");
        int rows = lambdaTemplate.insert(CaseTestUpperCI.class)//
                .applyEntity(entity)//
                .executeSumResult();
        assertEquals(1, rows);

        // SELECT — caseInsensitive=true → "Id" 匹配 ResultSet 的 "Id"
        CaseTestUpperCI loaded = lambdaTemplate.query(CaseTestUpperCI.class)//
                .eq(CaseTestUpperCI::getId, 50001)//
                .queryForObject();
        assertNotNull("CaseInsensitive=true should map mixed-case columns", loaded);
        assertEquals("InsertCI", loaded.getName());
        assertEquals(Integer.valueOf(25), loaded.getAge());
        assertEquals("CI-memo", loaded.getMemo());

        // UPDATE
        int updated = lambdaTemplate.update(CaseTestUpperCI.class)//
                .eq(CaseTestUpperCI::getId, 50001)//
                .updateTo(CaseTestUpperCI::getName, "UpdatedCI")//
                .doUpdate();
        assertEquals(1, updated);

        loaded = lambdaTemplate.query(CaseTestUpperCI.class)//
                .eq(CaseTestUpperCI::getId, 50001)//
                .queryForObject();
        assertEquals("UpdatedCI", loaded.getName());

        // DELETE
        int deleted = lambdaTemplate.delete(CaseTestUpperCI.class)//
                .eq(CaseTestUpperCI::getId, 50001)//
                .doDelete();
        assertEquals(1, deleted);
    }

    // ==================== 第二类：真实数据库验证 ====================

    /**
     * 验证 caseInsensitive=false 时，大小写不匹配的 @Column 无法映射 ResultSet 列名。
     * 原理：PG 不加引号的标识符自动折叠为小写，ResultSet 返回 "name","age" 等。
     * UpperCaseColumnStrictUser 的 @Column("NAME") + caseInsensitive=false → 严格匹配 "NAME"≠"name" → null。
     * （注意：@Column 值用于 SQL 生成时，PG 自动将无引号的 NAME 折叠为 name，所以 SQL 正常执行）
     */
    @Test
    public void testDb_CaseInsensitiveFalse_MismatchCase_FieldsNull() throws SQLException {
        requireCaseSensitivitySupport();

        jdbcTemplate.executeUpdate(//
                "INSERT INTO user_info (id, name, age, email, create_time) " +//
                        "VALUES (50002, 'StrictCS', 30, 'cs@test.com', CURRENT_TIMESTAMP)");

        // UpperCaseColumnStrictUser: @Column("NAME","AGE",...) + caseInsensitive=false
        // PG 返回小写列名 → 严格匹配 "NAME"≠"name" → 字段为 null
        UpperCaseColumnStrictUser loaded = lambdaTemplate.query(UpperCaseColumnStrictUser.class)//
                .eq(UpperCaseColumnStrictUser::getId, 50002)//
                .queryForObject();

        assertNotNull("Object exists (PG folded WHERE ID to id), but fields null due to case mismatch", loaded);
        assertNull("'NAME' ≠ 'name' (case-sensitive) → null", loaded.getName());
        assertNull("'AGE' ≠ 'age' (case-sensitive) → null", loaded.getAge());
        assertNull("'CREATE_TIME' ≠ 'create_time' (case-sensitive) → null", loaded.getCreateTime());
    }

    /**
     * 使用两张同结构但大小写不同的表，存入不同数据，验证 caseInsensitive 的实际隔离效果。
     * - case_test_lower: 存 "LowerData"
     * - "Case_Test_Upper": 存 "UpperData"
     * 用不同实体分别查询，确认数据来自正确的表。
     */
    @Test
    public void testDb_TwoTablesWithDifferentCase_DataIsolation() throws SQLException {
        requireCaseSensitivitySupport();

        // 写入 case_test_lower
        CaseTestLower lower = new CaseTestLower();
        lower.setId(50003);
        lower.setName("LowerData");
        lower.setAge(20);
        lower.setMemo("from-lower-table");
        lambdaTemplate.insert(CaseTestLower.class).applyEntity(lower).executeSumResult();

        // 写入 "Case_Test_Upper"
        CaseTestUpperCI upper = newCaseTestUpperCI(50003, "UpperData");
        upper.setMemo("from-upper-table");
        lambdaTemplate.insert(CaseTestUpperCI.class).applyEntity(upper).executeSumResult();

        // 从 case_test_lower 查
        CaseTestLower loadedLower = lambdaTemplate.query(CaseTestLower.class)//
                .eq(CaseTestLower::getId, 50003)//
                .queryForObject();
        assertNotNull(loadedLower);
        assertEquals("LowerData", loadedLower.getName());
        assertEquals("from-lower-table", loadedLower.getMemo());

        // 从 "Case_Test_Upper" 查
        CaseTestUpperCI loadedUpper = lambdaTemplate.query(CaseTestUpperCI.class)//
                .eq(CaseTestUpperCI::getId, 50003)//
                .queryForObject();
        assertNotNull(loadedUpper);
        assertEquals("UpperData", loadedUpper.getName());
        assertEquals("from-upper-table", loadedUpper.getMemo());

        // 确认数据完全隔离
        assertNotEquals("Two tables should have different data", loadedLower.getName(), loadedUpper.getName());
    }

    /**
     * caseInsensitive=true vs false 对比：同一条数据，同一个表，仅 caseInsensitive 设置不同。
     * 验证该属性是唯一决定变量。
     * 使用 user_info 表：PG 返回小写列名 (name, age, create_time)。
     * UpperCaseColumnUser(@Column("NAME"), CI=true) → 匹配成功。
     * UpperCaseColumnStrictUser(@Column("NAME"), CI=false) → 匹配失败。
     */
    @Test
    public void testDb_SameData_CaseInsensitiveTrueVsFalse() throws SQLException {
        requireCaseSensitivitySupport();

        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) " +//
                "VALUES (50004, 'CompareMe', 35, 'compare@test.com', CURRENT_TIMESTAMP)");

        // caseInsensitive=true → "NAME" 匹配 PG 的 "name"
        UpperCaseColumnUser ci = lambdaTemplate.query(UpperCaseColumnUser.class)//
                .eq(UpperCaseColumnUser::getId, 50004)//
                .queryForObject();
        assertNotNull(ci);
        assertEquals("CompareMe", ci.getName());
        assertEquals(Integer.valueOf(35), ci.getAge());
        assertNotNull(ci.getCreateTime());

        // caseInsensitive=false → "NAME" ≠ "name" → null
        UpperCaseColumnStrictUser cs = lambdaTemplate.query(UpperCaseColumnStrictUser.class)//
                .eq(UpperCaseColumnStrictUser::getId, 50004)//
                .queryForObject();
        assertNotNull("Object exists but fields null due to case mismatch", cs);
        assertNull("CI=false: 'NAME' ≠ 'name'", cs.getName());
        assertNull("CI=false: 'AGE' ≠ 'age'", cs.getAge());
        assertNull("CI=false: 'CREATE_TIME' ≠ 'create_time'", cs.getCreateTime());
    }

    /**
     * 验证 caseInsensitive=true 在 user_info 表上的行为。
     * @Column("NAME") + PG 返回 "name" → caseInsensitive map 匹配成功。
     */
    @Test
    public void testDb_UserInfo_CaseInsensitive_UpperColumnMatchesLower() throws SQLException {
        requireCaseSensitivitySupport();

        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) " +//
                "VALUES (50005, 'UCMatch', 28, 'uc@test.com', CURRENT_TIMESTAMP)");

        UpperCaseColumnUser loaded = lambdaTemplate.query(UpperCaseColumnUser.class)//
                .eq(UpperCaseColumnUser::getId, 50005)//
                .queryForObject();

        assertNotNull("CI=true: 'NAME' should match PG's 'name'", loaded);
        assertEquals("UCMatch", loaded.getName());
        assertEquals(Integer.valueOf(28), loaded.getAge());
        assertNotNull(loaded.getCreateTime());
    }

    /**
     * 验证 caseInsensitive=false 在 user_info 表上的行为。
     * @Column("NAME") + PG 返回 "name" → 严格匹配失败。
     */
    @Test
    public void testDb_UserInfo_CaseSensitive_UpperColumnMismatchesLower() throws SQLException {
        requireCaseSensitivitySupport();

        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) " +//
                "VALUES (50006, 'UCMismatch', 29, 'ucm@test.com', CURRENT_TIMESTAMP)");

        UpperCaseColumnStrictUser loaded = lambdaTemplate.query(UpperCaseColumnStrictUser.class)//
                .eq(UpperCaseColumnStrictUser::getId, 50006)//
                .queryForObject();

        assertNotNull("Object returned but fields null", loaded);
        assertNull("CI=false: 'NAME' ≠ 'name'", loaded.getName());
        assertNull("CI=false: 'AGE' ≠ 'age'", loaded.getAge());
        assertNull("CI=false: 'CREATE_TIME' ≠ 'create_time'", loaded.getCreateTime());
    }

    /**
     * 验证 Options.caseInsensitive(true) 在 Map freedom 模式下的行为。
     * Map 的 key 来自 ResultSet 列名，caseInsensitive 决定 Map 是否大小写无关。
     */
    @Test
    public void testDb_Options_CaseInsensitive_FreedomMapMode() throws SQLException {
        requireCaseSensitivitySupport();

        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email) " + //
                "VALUES (50007, 'MapCI', 33, 'mapci@test.com')");

        Options opts = Options.of().caseInsensitive(true);
        LambdaTemplate optLambda = new LambdaTemplate(dataSource, opts);

        Map<String, Object> row = optLambda.queryFreedom("user_info")//
                .eq("id", 50007)//
                .queryForObject();

        assertNotNull(row);
        assertEquals("MapCI", row.get("name"));
        // caseInsensitive=true → Map key 大小写无关
        assertEquals("CI Map should match uppercase key", "MapCI", row.get("NAME"));
    }

    /**
     * 验证 Options.caseInsensitive(false) 在 Map freedom 模式下的行为。
     * PG 返回小写 key → 只有小写能访问。
     */
    @Test
    public void testDb_Options_CaseSensitive_FreedomMapMode() throws SQLException {
        requireCaseSensitivitySupport();

        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email) " +//
                "VALUES (50008, 'MapCS', 34, 'mapcs@test.com')");

        Options opts = Options.of().caseInsensitive(false);
        LambdaTemplate optLambda = new LambdaTemplate(dataSource, opts);

        Map<String, Object> row = optLambda.queryFreedom("user_info")//
                .eq("id", 50008)//
                .queryForObject();

        assertNotNull(row);
        assertEquals("MapCS", row.get("name"));
        // caseInsensitive=false → Map key 严格大小写 → 大写访问不到
        assertNull("CS Map should NOT match uppercase key", row.get("NAME"));
    }

    /**
     * 对 "Case_Test_Upper" 表做 freedom 模式查询，验证返回的 Map key 保留原始大小写。
     */
    @Test
    public void testDb_FreedomQuery_MixedCaseTable_PreservesColumnCase() throws SQLException {
        requireCaseSensitivitySupport();

        jdbcTemplate.executeUpdate("INSERT INTO \"Case_Test_Upper\" (\"Id\", \"Name\", \"Age\", \"Memo\") " + //
                "VALUES (50009, 'FreedomMixed', 40, 'freedom-memo')");

        Options opts = Options.of().caseInsensitive(false).useDelimited(true);
        LambdaTemplate optLambda = new LambdaTemplate(dataSource, opts);

        Map<String, Object> row = optLambda.queryFreedom(null, null, "Case_Test_Upper")//
                .eq("Id", 50009)//
                .queryForObject();

        assertNotNull(row);
        // PG 保留了引号内的大小写 → Map key 应该是 "Id", "Name" 等
        assertEquals("FreedomMixed", row.get("Name"));
        // 严格模式下小写 key 访问不到
        assertNull("CS mode: 'name' ≠ 'Name'", row.get("name"));
    }

    /**
     * 验证完整的多记录批量操作 + caseInsensitive=true。
     */
    @Test
    public void testDb_MultipleRecords_CaseInsensitiveMapping() throws SQLException {
        requireCaseSensitivitySupport();

        for (int i = 1; i <= 3; i++) {
            CaseTestUpperCI entity = newCaseTestUpperCI(50010 + i, "Batch" + i);
            lambdaTemplate.insert(CaseTestUpperCI.class).applyEntity(entity).executeSumResult();
        }

        java.util.List<CaseTestUpperCI> list = lambdaTemplate.query(CaseTestUpperCI.class)//
                .like(CaseTestUpperCI::getName, "Batch%")//
                .queryForList();

        assertEquals(3, list.size());
        for (CaseTestUpperCI u : list) {
            assertNotNull("Each record should have name mapped (CI=true)", u.getName());
            assertNotNull("Each record should have age mapped (CI=true)", u.getAge());
        }
    }

    private UpperCaseColumnUser newUpperCaseUser(int id, String name) {
        UpperCaseColumnUser u = new UpperCaseColumnUser();
        u.setId(id);
        u.setName(name);
        u.setAge(25);
        u.setEmail(name.toLowerCase() + "@test.com");
        u.setCreateTime(new Date());
        return u;
    }

    // ==================== 辅助方法 ====================

    private UpperCaseColumnStrictUser newUpperCaseStrictUser(int id, String name) {
        UpperCaseColumnStrictUser u = new UpperCaseColumnStrictUser();
        u.setId(id);
        u.setName(name);
        u.setAge(25);
        u.setEmail(name.toLowerCase() + "@test.com");
        u.setCreateTime(new Date());
        return u;
    }

    private CaseTestUpperCI newCaseTestUpperCI(int id, String name) {
        CaseTestUpperCI u = new CaseTestUpperCI();
        u.setId(id);
        u.setName(name);
        u.setAge(25);
        u.setMemo("CI-memo");
        return u;
    }

    /**
     * 大小写敏感支持策略。
     * 每个数据库方言必须在此注册，未注册的方言会导致测试报错，提醒接入。
     */
    private enum CaseSensitivitySupport {
        SUPPORTED,   // 数据库支持创建大小写敏感的标识符（需要带引号）
        UNSUPPORTED  // 数据库不支持或忽略标识符大小写
    }
}
