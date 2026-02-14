package net.hasor.dbvisitor.test.suite.mapping.naming;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialectRegister;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.naming.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Delimited Identifier Test — 验证 useDelimited 属性与关键字自动检测的行为。
 * <h3>测试分为三大类：</h3>
 * <ol>
 *   <li><b>SQL 验证</b> — 通过 getBoundSql() 检查生成的 SQL 标识符是否被方言限定符正确包裹</li>
 *   <li><b>关键字自动检测</b> — 验证 useDelimited=false 时，方言 fmtName 仍会为 SQL 关键字列名/表名自动添加限定符</li>
 *   <li><b>真实数据库验证</b> — 通过实际 CRUD 操作验证限定后的 SQL 在数据库上正确执行</li>
 * </ol>
 * <h3>限定符来源：</h3>
 * <p>所有 SQL 断言中的限定符字符从 {@link SqlDialect#leftQualifier()} 和
 * {@link SqlDialect#rightQualifier()} 获取，不硬编码具体字符。</p>
 * <h3>方言特殊功能 — 关键字自动限定：</h3>
 * <p>{@link net.hasor.dbvisitor.dialect.provider.AbstractDialect#fmtName(boolean, String)} 在以下情况自动添加限定符：</p>
 * <ul>
 *   <li>标识符名称是 SQL 关键字（如 ORDER, SELECT）— 通过 {@link SqlDialect#keywords()} 检测</li>
 *   <li>标识符首字符不合法或包含特殊字符</li>
 *   <li>标识符包含右限定符字符（自动转义）</li>
 * </ul>
 */
public class DelimitedIdentifierTest extends AbstractOneApiTest {

    // ==================== 方言工具方法 ====================

    /** 从当前数据库连接自动检测方言，返回对应的 SqlDialect 实例 */
    private SqlDialect detectDialect() throws SQLException {
        Connection conn = dataSource.getConnection();
        try {
            return SqlDialectRegister.findDialect(null, conn);
        } finally {
            conn.close();
        }
    }

    // ==================== 第一类：SQL 验证（useDelimited=true） ====================

    /**
     * 验证 useDelimited=true 时 INSERT SQL 中所有标识符被限定符包裹。
     * DelimitedUser: @Table("user_info", useDelimited=true)
     * 预期: INSERT INTO L user_info R (L id R, L name R, ...) VALUES (...)
     */
    @Test
    public void testSql_UseDelimited_InsertQuotesAllIdentifiers() throws SQLException {
        SqlDialect dialect = detectDialect();
        String L = dialect.leftQualifier();
        String R = dialect.rightQualifier();

        DelimitedUser user = new DelimitedUser();
        user.setId(41001);
        user.setName("SqlDelimited");
        user.setAge(30);
        user.setEmail("sql@test.com");
        user.setCreateTime(new Date());

        BoundSql boundSql = lambdaTemplate.insert(DelimitedUser.class)//
                .applyEntity(user)//
                .getBoundSql();
        String sql = boundSql.getSqlString();

        assertTrue("INSERT should quote table: " + sql, sql.contains(L + "user_info" + R));
        assertTrue("INSERT should quote column id: " + sql, sql.contains(L + "id" + R));
        assertTrue("INSERT should quote column name: " + sql, sql.contains(L + "name" + R));
        assertTrue("INSERT should quote column age: " + sql, sql.contains(L + "age" + R));
        assertTrue("INSERT should quote column email: " + sql, sql.contains(L + "email" + R));
        assertTrue("INSERT should quote column create_time: " + sql, sql.contains(L + "create_time" + R));
    }

    /**
     * 验证 useDelimited=true 时 SELECT WHERE SQL 中标识符被限定。
     * 预期: SELECT * FROM L user_info R WHERE L name R = ? AND L age R = ?
     */
    @Test
    public void testSql_UseDelimited_SelectWhereQuotesIdentifiers() throws SQLException {
        SqlDialect dialect = detectDialect();
        String L = dialect.leftQualifier();
        String R = dialect.rightQualifier();

        BoundSql boundSql = lambdaTemplate.query(DelimitedUser.class)//
                .eq(DelimitedUser::getName, "test")//
                .eq(DelimitedUser::getAge, 25)//
                .getBoundSql();
        String sql = boundSql.getSqlString();

        assertTrue("SELECT should quote table: " + sql, sql.contains(L + "user_info" + R));
        assertTrue("WHERE should quote name: " + sql, sql.contains(L + "name" + R));
        assertTrue("WHERE should quote age: " + sql, sql.contains(L + "age" + R));
    }

    /**
     * 验证 useDelimited=true 时 UPDATE SQL 中 SET 和 WHERE 标识符被限定。
     * 预期: UPDATE L user_info R SET L name R = ? WHERE L id R = ?
     */
    @Test
    public void testSql_UseDelimited_UpdateQuotesIdentifiers() throws SQLException {
        SqlDialect dialect = detectDialect();
        String L = dialect.leftQualifier();
        String R = dialect.rightQualifier();

        BoundSql boundSql = lambdaTemplate.update(DelimitedUser.class)//
                .eq(DelimitedUser::getId, 1)//
                .updateTo(DelimitedUser::getName, "updated")//
                .getBoundSql();
        String sql = boundSql.getSqlString();

        assertTrue("UPDATE should quote table: " + sql, sql.contains(L + "user_info" + R));
        assertTrue("UPDATE SET should quote name: " + sql, sql.contains(L + "name" + R));
        assertTrue("UPDATE WHERE should quote id: " + sql, sql.contains(L + "id" + R));
    }

    /**
     * 验证 useDelimited=true 时 DELETE SQL 中 WHERE 标识符被限定。
     * 预期: DELETE FROM L user_info R WHERE L id R = ?
     */
    @Test
    public void testSql_UseDelimited_DeleteQuotesIdentifiers() throws SQLException {
        SqlDialect dialect = detectDialect();
        String L = dialect.leftQualifier();
        String R = dialect.rightQualifier();

        BoundSql boundSql = lambdaTemplate.delete(DelimitedUser.class)//
                .eq(DelimitedUser::getId, 1)//
                .getBoundSql();
        String sql = boundSql.getSqlString();

        assertTrue("DELETE should quote table: " + sql, sql.contains(L + "user_info" + R));
        assertTrue("DELETE WHERE should quote id: " + sql, sql.contains(L + "id" + R));
    }

    // ==================== 第二类：关键字自动检测（useDelimited=false） ====================

    /**
     * 验证方言关键字表中包含 ORDER 和 SELECT。
     * 这是自动检测的前提条件。
     */
    @Test
    public void testDialect_KeywordsContainOrderAndSelect() throws SQLException {
        SqlDialect dialect = detectDialect();
        Set<String> keywords = dialect.keywords();

        assertTrue("Dialect keywords should contain ORDER", keywords.contains("ORDER"));
        assertTrue("Dialect keywords should contain SELECT", keywords.contains("SELECT"));
    }

    /**
     * 验证 useDelimited=false 时，关键字列名 "order"/"select" 被方言自动加限定符。
     * KeywordColumnNoDelimitedEntity: @Table("naming_keyword_test") 不带 useDelimited
     * 预期: 非关键字列(id, name)不加限定符，关键字列(order, select)自动加限定符
     */
    @Test
    public void testSql_KeywordColumn_AutoDetected_WithoutUseDelimited() throws SQLException {
        SqlDialect dialect = detectDialect();
        String L = dialect.leftQualifier();
        String R = dialect.rightQualifier();

        KeywordColumnNoDelimitedEntity entity = new KeywordColumnNoDelimitedEntity();
        entity.setId(1);
        entity.setOrderValue("test-order");
        entity.setSelectValue("test-select");
        entity.setName("test");

        BoundSql boundSql = lambdaTemplate.insert(KeywordColumnNoDelimitedEntity.class)//
                .applyEntity(entity)//
                .getBoundSql();
        String sql = boundSql.getSqlString();

        // 关键字列应被自动限定
        assertTrue("Keyword column 'order' should be auto-quoted: " + sql, sql.contains(L + "order" + R));
        assertTrue("Keyword column 'select' should be auto-quoted: " + sql, sql.contains(L + "select" + R));

        // 非关键字列不被限定（useDelimited=false）
        assertFalse("Non-keyword column 'id' should NOT be quoted: " + sql, sql.contains(L + "id" + R));
        assertFalse("Non-keyword column 'name' should NOT be quoted: " + sql, sql.contains(L + "name" + R));

        // 表名也不是关键字，不应被限定
        assertFalse("Non-keyword table should NOT be quoted: " + sql, sql.contains(L + "naming_keyword_test" + R));
    }

    /**
     * 验证 useDelimited=false 时，关键字表名 "order" 被方言自动加限定符。
     * KeywordTableNoDelimitedEntity: @Table("order") 不带 useDelimited
     * 预期: INSERT INTO L order R (...) VALUES (...)
     */
    @Test
    public void testSql_KeywordTable_AutoDetected_WithoutUseDelimited() throws SQLException {
        SqlDialect dialect = detectDialect();
        String L = dialect.leftQualifier();
        String R = dialect.rightQualifier();

        KeywordTableNoDelimitedEntity entity = new KeywordTableNoDelimitedEntity();
        entity.setId(1);
        entity.setName("test");
        entity.setDescription("desc");

        BoundSql boundSql = lambdaTemplate.insert(KeywordTableNoDelimitedEntity.class)//
                .applyEntity(entity)//
                .getBoundSql();
        String sql = boundSql.getSqlString();

        // 关键字表名应被自动限定
        assertTrue("Keyword table 'order' should be auto-quoted: " + sql, sql.contains(L + "order" + R));

        // 非关键字列不被限定
        assertFalse("Non-keyword column 'id' should NOT be quoted: " + sql, sql.contains(L + "id" + R));
        assertFalse("Non-keyword column 'name' should NOT be quoted: " + sql, sql.contains(L + "name" + R));
    }

    /**
     * 对比 useDelimited=true 和 useDelimited=false 对关键字列的处理差异。
     * - useDelimited=true: 全部标识符加限定符（包括非关键字的 id, name）
     * - useDelimited=false: 仅关键字标识符自动加限定符（order, select）
     */
    @Test
    public void testSql_KeywordColumn_Contrast_DelimitedVsAutoDetect() throws SQLException {
        SqlDialect dialect = detectDialect();
        String L = dialect.leftQualifier();
        String R = dialect.rightQualifier();

        // useDelimited=true 版本
        KeywordColumnEntity delimitedEntity = new KeywordColumnEntity();
        delimitedEntity.setId(1);
        delimitedEntity.setOrderValue("v1");
        delimitedEntity.setSelectValue("v2");
        delimitedEntity.setName("test");

        BoundSql delimitedSql = lambdaTemplate.insert(KeywordColumnEntity.class)//
                .applyEntity(delimitedEntity)//
                .getBoundSql();
        String dSql = delimitedSql.getSqlString();

        // useDelimited=false 版本
        KeywordColumnNoDelimitedEntity autoEntity = new KeywordColumnNoDelimitedEntity();
        autoEntity.setId(1);
        autoEntity.setOrderValue("v1");
        autoEntity.setSelectValue("v2");
        autoEntity.setName("test");

        BoundSql autoSql = lambdaTemplate.insert(KeywordColumnNoDelimitedEntity.class)//
                .applyEntity(autoEntity)//
                .getBoundSql();
        String aSql = autoSql.getSqlString();

        // 两者都应该限定关键字列
        assertTrue("Delimited: order should be quoted: " + dSql, dSql.contains(L + "order" + R));
        assertTrue("AutoDetect: order should be quoted: " + aSql, aSql.contains(L + "order" + R));
        assertTrue("Delimited: select should be quoted: " + dSql, dSql.contains(L + "select" + R));
        assertTrue("AutoDetect: select should be quoted: " + aSql, aSql.contains(L + "select" + R));

        // useDelimited=true 还应该限定非关键字列，useDelimited=false 不应该
        assertTrue("Delimited: id should be quoted: " + dSql, dSql.contains(L + "id" + R));
        assertFalse("AutoDetect: id should NOT be quoted: " + aSql, aSql.contains(L + "id" + R));
        assertTrue("Delimited: name should be quoted: " + dSql, dSql.contains(L + "name" + R));
        assertFalse("AutoDetect: name should NOT be quoted: " + aSql, aSql.contains(L + "name" + R));
    }

    /**
     * 验证 useDelimited=true + mapUnderscoreToCamelCase=true 组合生成的 SQL。
     * AllNamingOptionsUser: useDelimited=true, mapUnderscoreToCamelCase=true
     * 预期: Java 属性 createTime → 列名 create_time → SQL 中 L create_time R
     */
    @Test
    public void testSql_UseDelimited_CombinedWithCamelCase() throws SQLException {
        SqlDialect dialect = detectDialect();
        String L = dialect.leftQualifier();
        String R = dialect.rightQualifier();

        AllNamingOptionsUser user = new AllNamingOptionsUser();
        user.setId(1);
        user.setName("test");
        user.setAge(25);
        user.setEmail("test@test.com");
        user.setCreateTime(new Date());

        BoundSql boundSql = lambdaTemplate.insert(AllNamingOptionsUser.class)//
                .applyEntity(user)//
                .getBoundSql();
        String sql = boundSql.getSqlString();

        assertTrue("CamelCase+Delimited: table should be quoted: " + sql, sql.contains(L + "user_info" + R));
        assertTrue("CamelCase+Delimited: create_time should be quoted: " + sql, sql.contains(L + "create_time" + R));
        assertTrue("CamelCase+Delimited: id should be quoted: " + sql, sql.contains(L + "id" + R));
    }

    /**
     * 验证 Options.useDelimited(true) 全局设置对 freedom 模式生成的 SQL 的影响。
     * 对比 useDelimited=true 和 useDelimited=false 的 freedom 模式 SQL。
     */
    @Test
    public void testSql_OptionsUseDelimited_FreedomMode() throws SQLException {
        SqlDialect dialect = detectDialect();
        String L = dialect.leftQualifier();
        String R = dialect.rightQualifier();

        Options delimitedOpts = Options.of().useDelimited(true);
        LambdaTemplate delimitedLambda = new LambdaTemplate(dataSource, delimitedOpts);

        BoundSql delimitedSql = delimitedLambda.queryFreedom("user_info")//
                .eq("id", 1)//
                .getBoundSql();
        String dSql = delimitedSql.getSqlString();

        Options normalOpts = Options.of().useDelimited(false);
        LambdaTemplate normalLambda = new LambdaTemplate(dataSource, normalOpts);

        BoundSql normalSql = normalLambda.queryFreedom("user_info")//
                .eq("id", 1)//
                .getBoundSql();
        String nSql = normalSql.getSqlString();

        // useDelimited=true 时标识符被限定
        assertTrue("Delimited freedom: table should be quoted: " + dSql, dSql.contains(L + "user_info" + R));
        assertTrue("Delimited freedom: column should be quoted: " + dSql, dSql.contains(L + "id" + R));

        // useDelimited=false 时非关键字标识符不被限定
        assertFalse("Normal freedom: table should NOT be quoted: " + nSql, nSql.contains(L + "user_info" + R));
        assertFalse("Normal freedom: column should NOT be quoted: " + nSql, nSql.contains(L + "id" + R));
    }

    // ==================== 第三类：真实数据库验证 ====================

    /**
     * 场景：@Table(useDelimited=true) 普通表 — 完整 CRUD
     * 验证：限定后的 SQL 在真实数据库上正确执行
     */
    @Test
    public void testDb_Delimited_NormalTable_InsertAndQuery() throws SQLException {
        DelimitedUser user = new DelimitedUser();
        user.setId(41001);
        user.setName("DelimitedNormal");
        user.setAge(30);
        user.setEmail("delim@test.com");
        user.setCreateTime(new Date());

        int rows = lambdaTemplate.insert(DelimitedUser.class)//
                .applyEntity(user)//
                .executeSumResult();
        assertEquals(1, rows);

        DelimitedUser loaded = lambdaTemplate.query(DelimitedUser.class)//
                .eq(DelimitedUser::getId, 41001)//
                .queryForObject();

        assertNotNull("Normal table with useDelimited should work", loaded);
        assertEquals("DelimitedNormal", loaded.getName());
        assertEquals(Integer.valueOf(30), loaded.getAge());
        assertNotNull("createTime should be mapped", loaded.getCreateTime());
    }

    /**
     * 场景：@Table(useDelimited=true) + UPDATE + DELETE
     * 验证：限定后的 UPDATE/DELETE SQL 在真实数据库上正确执行
     */
    @Test
    public void testDb_Delimited_UpdateAndDelete() throws SQLException {
        DelimitedUser user = new DelimitedUser();
        user.setId(41003);
        user.setName("BeforeDelimUpdate");
        user.setAge(28);
        user.setEmail("delimup@test.com");
        user.setCreateTime(new Date());

        lambdaTemplate.insert(DelimitedUser.class).applyEntity(user).executeSumResult();

        // UPDATE
        int updated = lambdaTemplate.update(DelimitedUser.class)//
                .eq(DelimitedUser::getId, 41003)//
                .updateTo(DelimitedUser::getName, "AfterDelimUpdate")//
                .doUpdate();
        assertEquals(1, updated);

        DelimitedUser loaded = lambdaTemplate.query(DelimitedUser.class)//
                .eq(DelimitedUser::getId, 41003)//
                .queryForObject();
        assertEquals("AfterDelimUpdate", loaded.getName());

        // DELETE
        int deleted = lambdaTemplate.delete(DelimitedUser.class)//
                .eq(DelimitedUser::getId, 41003)//
                .doDelete();
        assertEquals(1, deleted);

        long count = lambdaTemplate.query(DelimitedUser.class)//
                .eq(DelimitedUser::getId, 41003)//
                .queryForCount();
        assertEquals(0, count);
    }

    /**
     * 场景：SQL 关键字列名 + useDelimited=true — INSERT/Query/Update
     * 验证：'order' 和 'select' 作为列名，带限定符后在真实数据库上正确执行
     */
    @Test
    public void testDb_KeywordColumn_CRUD() throws SQLException {
        ensureKeywordTestTableExists();

        // INSERT
        KeywordColumnEntity entity = new KeywordColumnEntity();
        entity.setId(41005);
        entity.setOrderValue("ORDER-001");
        entity.setSelectValue("SELECT-001");
        entity.setName("KeywordCol");

        int rows = lambdaTemplate.insert(KeywordColumnEntity.class)//
                .applyEntity(entity)//
                .executeSumResult();
        assertEquals(1, rows);

        // QUERY
        KeywordColumnEntity loaded = lambdaTemplate.query(KeywordColumnEntity.class)//
                .eq(KeywordColumnEntity::getId, 41005)//
                .queryForObject();

        assertNotNull("Keyword columns should work with useDelimited", loaded);
        assertEquals("ORDER-001", loaded.getOrderValue());
        assertEquals("SELECT-001", loaded.getSelectValue());
        assertEquals("KeywordCol", loaded.getName());

        // UPDATE keyword column
        int updated = lambdaTemplate.update(KeywordColumnEntity.class)//
                .eq(KeywordColumnEntity::getId, 41005)//
                .updateTo(KeywordColumnEntity::getOrderValue, "NEW-ORDER")//
                .updateTo(KeywordColumnEntity::getSelectValue, "NEW-SELECT")//
                .doUpdate();
        assertEquals(1, updated);

        KeywordColumnEntity reloaded = lambdaTemplate.query(KeywordColumnEntity.class)//
                .eq(KeywordColumnEntity::getId, 41005)//
                .queryForObject();
        assertEquals("NEW-ORDER", reloaded.getOrderValue());
        assertEquals("NEW-SELECT", reloaded.getSelectValue());
    }

    /**
     * 场景：SQL 关键字表名 "order" + useDelimited=true — CRUD
     * 验证：关键字作为表名，带限定符后在真实数据库上正确执行
     */
    @Test
    public void testDb_KeywordTable_CRUD() throws SQLException {
        ensureOrderTableExists();

        // INSERT
        KeywordTableEntity entity = new KeywordTableEntity();
        entity.setId(41007);
        entity.setName("KeywordTable");
        entity.setDescription("Table named 'order'");

        int rows = lambdaTemplate.insert(KeywordTableEntity.class)//
                .applyEntity(entity)//
                .executeSumResult();
        assertEquals(1, rows);

        // QUERY
        KeywordTableEntity loaded = lambdaTemplate.query(KeywordTableEntity.class)//
                .eq(KeywordTableEntity::getId, 41007)//
                .queryForObject();

        assertNotNull("Keyword table name should work with useDelimited", loaded);
        assertEquals("KeywordTable", loaded.getName());
        assertEquals("Table named 'order'", loaded.getDescription());

        // DELETE
        int deleted = lambdaTemplate.delete(KeywordTableEntity.class)//
                .eq(KeywordTableEntity::getId, 41007)//
                .doDelete();
        assertEquals(1, deleted);

        long count = lambdaTemplate.query(KeywordTableEntity.class)//
                .eq(KeywordTableEntity::getId, 41007)//
                .queryForCount();
        assertEquals(0, count);
    }

    /**
     * 场景：关键字自动检测 + Map 模式（freedom）
     * 验证：即使 useDelimited=false，fmtName 也会自动检测关键字并加限定符
     */
    @Test
    public void testDb_KeywordAutoDetection_MapMode() throws SQLException {
        ensureKeywordTestTableExists();

        // 直接用 JDBC 插入
        jdbcTemplate.executeUpdate(//
                "INSERT INTO naming_keyword_test (id, \"order\", \"select\", name) " +//
                        "VALUES (41008, 'AUTO-ORDER', 'AUTO-SELECT', 'AutoDetect')");

        // Map 模式：fmtName 应该自动检测 'order' 和 'select' 是关键字并加限定符
        Map<String, Object> row = lambdaTemplate.queryFreedom("naming_keyword_test")//
                .eq("id", 41008)//
                .queryForObject();

        assertNotNull("Map mode query should work on keyword table", row);
        assertEquals("AutoDetect", row.get("name"));
        assertEquals("AUTO-ORDER", row.get("order"));
        assertEquals("AUTO-SELECT", row.get("select"));
    }

    /**
     * 场景：Options.useDelimited(true) 全局设置
     * 验证：通过 Options 在全局层面启用限定符
     */
    @Test
    public void testDb_GlobalOptions_UseDelimited() throws SQLException {
        jdbcTemplate.executeUpdate(//
                "INSERT INTO user_info (id, name, age, email, create_time) " +//
                        "VALUES (41009, 'GlobalDelim', 27, 'gdelim@test.com', CURRENT_TIMESTAMP)");

        Options opts = Options.of().useDelimited(true);
        LambdaTemplate optionsLambda = new LambdaTemplate(dataSource, opts);

        Map<String, Object> row = optionsLambda.queryFreedom("user_info")//
                .eq("id", 41009)//
                .queryForObject();

        assertNotNull("Global Options useDelimited should work", row);
        assertEquals("GlobalDelim", row.get("name"));
    }

    /**
     * 场景：useDelimited=true + mapUnderscoreToCamelCase=true 组合
     * 验证：驼峰转换后的列名也被正确限定，在数据库上正确执行
     */
    @Test
    public void testDb_UseDelimited_CombinedWithCamelCase() throws SQLException {
        AllNamingOptionsUser user = new AllNamingOptionsUser();
        user.setId(41014);
        user.setName("DelimCamel");
        user.setAge(32);
        user.setEmail("delimcamel@test.com");
        user.setCreateTime(new Date());

        int rows = lambdaTemplate.insert(AllNamingOptionsUser.class)//
                .applyEntity(user)//
                .executeSumResult();
        assertEquals(1, rows);

        AllNamingOptionsUser loaded = lambdaTemplate.query(AllNamingOptionsUser.class)//
                .eq(AllNamingOptionsUser::getId, 41014)//
                .queryForObject();

        assertNotNull("Delimited + CamelCase combined should work", loaded);
        assertEquals("DelimCamel", loaded.getName());
        assertNotNull("createTime camelCase+delimited should map correctly", loaded.getCreateTime());
    }

    // ==================== Helper ====================

    private void ensureKeywordTestTableExists() {
        try {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM naming_keyword_test WHERE 1=0", Integer.class);
            jdbcTemplate.executeUpdate("DELETE FROM naming_keyword_test");
        } catch (Exception e) {
            try {
                jdbcTemplate.executeUpdate("CREATE TABLE naming_keyword_test (" + "id INT PRIMARY KEY, " + "\"order\" VARCHAR(100), " + "\"select\" VARCHAR(100), " + "name VARCHAR(100))");
            } catch (Exception ex) {
                throw new RuntimeException("Failed to create naming_keyword_test table", ex);
            }
        }
    }

    private void ensureOrderTableExists() {
        try {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM \"order\" WHERE 1=0", Integer.class);
            jdbcTemplate.executeUpdate("DELETE FROM \"order\"");
        } catch (Exception e) {
            try {
                jdbcTemplate.executeUpdate("CREATE TABLE \"order\" (" + "id INT PRIMARY KEY, " + "name VARCHAR(100), " + "description VARCHAR(200))");
            } catch (Exception ex) {
                throw new RuntimeException("Failed to create \"order\" table", ex);
            }
        }
    }
}
