package net.hasor.dbvisitor.test.suite.mapping.naming;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.naming.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * CamelCase Mapping Test
 * 验证 mapUnderscoreToCamelCase 在不同场景下的行为：
 * - @Table(mapUnderscoreToCamelCase = true) 开启自动驼峰转下划线
 * - @Table(mapUnderscoreToCamelCase = false) 关闭（默认）
 * - @Column 显式指定列名覆盖驼峰转换
 * - Options 全局设置作为无注解实体的后备
 * - Lambda 属性引用的列名解析
 */
public class CamelCaseMappingTest extends AbstractOneApiTest {

    // ==================== @Table 注解场景 ====================

    /**
     * 场景：@Table(mapUnderscoreToCamelCase=true)
     * 验证：Java 属性 createTime 自动映射到数据库列 create_time
     * 预期：INSERT 和 SELECT 完整往返成功, createTime 值正确
     */
    @Test
    public void testCamelCaseEnabled_InsertAndQuery() throws SQLException {
        CamelCaseEnabledUser user = new CamelCaseEnabledUser();
        user.setId(40001);
        user.setName("CamelEnabled");
        user.setAge(30);
        user.setEmail("camel@test.com");
        user.setCreateTime(new Date());

        // CamelCaseEnabledUser: createTime → create_time (auto camelCase)
        int rows = lambdaTemplate.insert(CamelCaseEnabledUser.class)//
                .applyEntity(user)//
                .executeSumResult();
        assertEquals(1, rows);

        CamelCaseEnabledUser loaded = lambdaTemplate.query(CamelCaseEnabledUser.class)//
                .eq(CamelCaseEnabledUser::getId, 40001)//
                .queryForObject();

        assertNotNull("Should load entity by id", loaded);
        assertEquals("CamelEnabled", loaded.getName());
        assertEquals(Integer.valueOf(30), loaded.getAge());
        assertEquals("camel@test.com", loaded.getEmail());
        assertNotNull("createTime must be mapped to create_time column", loaded.getCreateTime());
    }

    /**
     * 场景：@Table(mapUnderscoreToCamelCase=false) — 默认行为
     * 验证：Java 属性 createTime 映射到列 "createTime"（原样），无法匹配 PG 的 create_time 列
     * 预期：使用 JDBC 插入数据后，通过该实体查询时 createTime 为 null
     */
    @Test
    public void testCamelCaseDisabled_CreateTimeNotMapped() throws SQLException {
        // 直接用 JDBC 插入（避免 CamelCaseDisabledUser INSERT 时引用不存在的 "createTime" 列）
        jdbcTemplate.executeUpdate(//
                "INSERT INTO user_info (id, name, age, email, create_time) " +//
                        "VALUES (40002, 'CamelDisabled', 28, 'disabled@test.com', CURRENT_TIMESTAMP)");

        // 用 CamelCaseDisabledUser（mapUnderscoreToCamelCase=false）查询
        CamelCaseDisabledUser loaded = lambdaTemplate.query(CamelCaseDisabledUser.class)//
                .eq(CamelCaseDisabledUser::getId, 40002)//
                .queryForObject();

        assertNotNull("Should load entity", loaded);
        assertEquals("CamelDisabled", loaded.getName());
        assertEquals(Integer.valueOf(28), loaded.getAge());
        // 关键断言：createTime 为 null，因为 "createTime" 不匹配 DB 列 "create_time"
        assertNull("createTime should be null — 'createTime' != 'create_time'", loaded.getCreateTime());
    }

    /**
     * 场景：对比 CamelCaseEnabled vs CamelCaseDisabled 在同一条数据上的效果
     * 验证：同一条数据，不同映射策略产生不同结果
     */
    @Test
    public void testCamelCase_EnabledVsDisabled_SameData() throws SQLException {
        // 用 JDBC 插入带 create_time 的数据
        jdbcTemplate.executeUpdate(//
                "INSERT INTO user_info (id, name, age, email, create_time) " +//
                        "VALUES (40003, 'CompareTest', 35, 'compare@test.com', CURRENT_TIMESTAMP)");

        // 用 CamelCaseEnabledUser 查询 — createTime 应该有值
        CamelCaseEnabledUser enabled = lambdaTemplate.query(CamelCaseEnabledUser.class)//
                .eq(CamelCaseEnabledUser::getId, 40003)//
                .queryForObject();

        // 用 CamelCaseDisabledUser 查询 — createTime 应该为 null
        CamelCaseDisabledUser disabled = lambdaTemplate.query(CamelCaseDisabledUser.class)//
                .eq(CamelCaseDisabledUser::getId, 40003)//
                .queryForObject();

        //
        assertNotNull("CamelCase enabled: createTime should be mapped", enabled.getCreateTime());
        assertNull("CamelCase disabled: createTime should NOT be mapped", disabled.getCreateTime());

        // 基础字段（id, name, age, email）在两种映射下都正常
        assertEquals(enabled.getName(), disabled.getName());
        assertEquals(enabled.getAge(), disabled.getAge());
        assertEquals(enabled.getEmail(), disabled.getEmail());
    }

    // ==================== @Column 覆盖场景 ====================

    /**
     * 场景：@Table(mapUnderscoreToCamelCase=true) + @Column("name") 显式指定
     * 验证：@Column 显式指定的列名不受驼峰转换影响
     * 预期：属性 userName 映射到列 "name"（@Column 优先），而非自动转换的 "user_name"
     */
    @Test
    public void testColumnAnnotation_OverridesCamelCase() throws SQLException {
        CamelCaseColumnOverrideUser user = new CamelCaseColumnOverrideUser();
        user.setId(40004);
        user.setUserName("ColumnOverride");
        user.setAge(25);
        user.setEmail("override@test.com");
        user.setCreateTime(new Date());

        // 如果 @Column 没覆盖，userName → user_name → 列不存在 → INSERT 失败
        // 正常执行说明 @Column("name") 覆盖了驼峰转换
        int rows = lambdaTemplate.insert(CamelCaseColumnOverrideUser.class)//
                .applyEntity(user)//
                .executeSumResult();
        assertEquals(1, rows);

        CamelCaseColumnOverrideUser loaded = lambdaTemplate.query(CamelCaseColumnOverrideUser.class)//
                .eq(CamelCaseColumnOverrideUser::getId, 40004)//
                .queryForObject();

        assertNotNull(loaded);
        assertEquals("ColumnOverride", loaded.getUserName()); // @Column("name") → 映射正确
        assertNotNull("createTime auto-mapped via camelCase", loaded.getCreateTime()); // 无 @Column → 自动转换
    }

    // ==================== Lambda 属性引用场景 ====================

    /**
     * 场景：Lambda 属性引用 + mapUnderscoreToCamelCase=true
     * 验证：eq(CamelCaseEnabledUser::getCreateTime, ...) 正确生成 WHERE create_time 条件
     */
    @Test
    public void testCamelCase_LambdaPropertyRef_InWhereClause() throws SQLException {
        Date now = new Date();
        CamelCaseEnabledUser user = new CamelCaseEnabledUser();
        user.setId(40005);
        user.setName("LambdaRef");
        user.setAge(27);
        user.setEmail("lambda@test.com");
        user.setCreateTime(now);

        lambdaTemplate.insert(CamelCaseEnabledUser.class)//
                .applyEntity(user)//
                .executeSumResult();

        // 使用 Lambda 引用 getCreateTime 作为 WHERE 条件
        List<CamelCaseEnabledUser> results = lambdaTemplate.query(CamelCaseEnabledUser.class)//
                .eq(CamelCaseEnabledUser::getId, 40005)//
                .isNotNull(CamelCaseEnabledUser::getCreateTime)//
                .queryForList();

        assertFalse("Should find entity with non-null createTime", results.isEmpty());
        assertEquals(40005, results.get(0).getId().intValue());
    }

    /**
     * 场景：Lambda 属性引用 + @Column 覆盖
     * 验证：eq(CamelCaseColumnOverrideUser::getUserName, ...) 使用 @Column("name") 指定的列名
     */
    @Test
    public void testCamelCase_LambdaRef_WithColumnOverride() throws SQLException {
        CamelCaseColumnOverrideUser user = new CamelCaseColumnOverrideUser();
        user.setId(40006);
        user.setUserName("LambdaOverride");
        user.setAge(33);
        user.setEmail("lambdaover@test.com");
        user.setCreateTime(new Date());

        lambdaTemplate.insert(CamelCaseColumnOverrideUser.class)//
                .applyEntity(user)//
                .executeSumResult();

        // getUserName() → property userName → @Column("name") → WHERE name = ?
        CamelCaseColumnOverrideUser loaded = lambdaTemplate.query(CamelCaseColumnOverrideUser.class)//
                .eq(CamelCaseColumnOverrideUser::getUserName, "LambdaOverride")//
                .queryForObject();

        assertNotNull("Lambda ref should use @Column name", loaded);
        assertEquals("LambdaOverride", loaded.getUserName());
    }

    // ==================== Options 全局设置场景 ====================

    /**
     * 场景：无 @Table 注解的 POJO + Options(mapUnderscoreToCamelCase=true)
     * 验证：Options 的全局设置被应用到无注解实体
     * 预期：PlainUser（无@Table）的表名 = humpToLine("PlainUser") = "plain_user"
     * 属性 createTime → 列 create_time
     */
    @Test
    public void testCamelCase_ViaGlobalOptions_PlainEntity() throws SQLException {
        // 动态创建 plain_user 表
        ensurePlainUserTableExists();

        // 用 JDBC 插入数据
        jdbcTemplate.executeUpdate(//
                "INSERT INTO plain_user (id, name, age, email, create_time) " +//
                        "VALUES (40007, 'PlainOpt', 29, 'plain@test.com', CURRENT_TIMESTAMP)");

        // 创建带 Options 的 LambdaTemplate
        Options opts = Options.of().mapUnderscoreToCamelCase(true);
        LambdaTemplate optionsLambda = new LambdaTemplate(dataSource, opts);

        // PlainUser（无@Table）: Options.mapUnderscoreToCamelCase=true 生效
        PlainUser loaded = optionsLambda.query(PlainUser.class)//
                .eq(PlainUser::getId, 40007)//
                .queryForObject();

        assertNotNull("Options should apply to plain entity", loaded);
        assertEquals("PlainOpt", loaded.getName());
        assertNotNull("createTime should be mapped via Options camelCase", loaded.getCreateTime());
    }

    /**
     * 场景：@Table 注解属性默认值 与 Options 全局设置的交互
     * 验证：当 @Table 未显式设置 mapUnderscoreToCamelCase（使用注解声明的默认值 false），
     * Options 的 true 作为 fallback 生效，因为框架无法区分"未设置"和"显式设为默认值"。
     * 这是 Java 注解的固有限制：annotation 属性的 default 值和显式指定同一值在运行时不可区分。
     * 框架的策略：annotation value == declared default → 使用 Options fallback
     */
    @Test
    public void testCamelCase_OptionsAppliesWhenAnnotationUsesDefault() throws SQLException {
        jdbcTemplate.executeUpdate(//
                "INSERT INTO user_info (id, name, age, email, create_time) " +//
                        "VALUES (40008, 'AnnotationDefault', 31, 'annotdef@test.com', CURRENT_TIMESTAMP)");

        // Options 设置 mapUnderscoreToCamelCase=true
        Options opts = Options.of().mapUnderscoreToCamelCase(true);
        LambdaTemplate optionsLambda = new LambdaTemplate(dataSource, opts);

        // CamelCaseDisabledUser 的 @Table("user_info") 未显式设置 mapUnderscoreToCamelCase
        // 注解默认值 false == 声明默认值 false → Options(true) 作为 fallback 生效
        CamelCaseDisabledUser loaded = optionsLambda.query(CamelCaseDisabledUser.class)//
                .eq(CamelCaseDisabledUser::getId, 40008)//
                .queryForObject();

        assertNotNull(loaded);
        // Options 的 true 生效 → createTime 被映射到 create_time
        assertNotNull("Options fallback applies when annotation uses default → createTime should be mapped", loaded.getCreateTime());
    }

    /**
     * 场景：@Table 显式设置 mapUnderscoreToCamelCase=true 覆盖 Options(false)
     * 验证：注解的非默认值优先于 Options
     */
    @Test
    public void testCamelCase_ExplicitAnnotation_OverridesOptions() throws SQLException {
        jdbcTemplate.executeUpdate(//
                "INSERT INTO user_info (id, name, age, email, create_time) " +//
                        "VALUES (40014, 'ExplicitWins', 31, 'explwin@test.com', CURRENT_TIMESTAMP)");

        // Options 不设置 mapUnderscoreToCamelCase（或设为 false）
        Options opts = Options.of();
        LambdaTemplate optionsLambda = new LambdaTemplate(dataSource, opts);

        // CamelCaseEnabledUser 的 @Table 显式设置 mapUnderscoreToCamelCase=true
        // 注解值 true ≠ 声明默认值 false → 注解值优先
        CamelCaseEnabledUser loaded = optionsLambda.query(CamelCaseEnabledUser.class)//
                .eq(CamelCaseEnabledUser::getId, 40014)//
                .queryForObject();

        assertNotNull(loaded);
        assertNotNull("Explicit @Table(mapUnderscoreToCamelCase=true) overrides Options", loaded.getCreateTime());
    }

    // ==================== 多属性转换场景 ====================

    /**
     * 场景：多个驼峰属性同时自动转换
     * 验证：所有属性的映射都正确
     */
    @Test
    public void testCamelCase_MultipleProperties_FullRoundTrip() throws SQLException {
        Date now = new Date();
        CamelCaseEnabledUser user = new CamelCaseEnabledUser();
        user.setId(40009);
        user.setName("MultiProp");
        user.setAge(26);
        user.setEmail("multi@test.com");
        user.setCreateTime(now);

        lambdaTemplate.insert(CamelCaseEnabledUser.class)//
                .applyEntity(user)//
                .executeSumResult();

        CamelCaseEnabledUser loaded = lambdaTemplate.query(CamelCaseEnabledUser.class)//
                .eq(CamelCaseEnabledUser::getId, 40009)//
                .queryForObject();

        assertNotNull(loaded);
        assertEquals(Integer.valueOf(40009), loaded.getId());     // id → id
        assertEquals("MultiProp", loaded.getName());        // name → name
        assertEquals(Integer.valueOf(26), loaded.getAge());       // age → age
        assertEquals("multi@test.com", loaded.getEmail());  // email → email
        assertNotNull(loaded.getCreateTime());                      // createTime → create_time
    }

    // ==================== CamelCase + 其他选项组合 ====================

    /**
     * 场景：mapUnderscoreToCamelCase + useDelimited + caseInsensitive 同时开启
     * 验证：三个命名选项可以协同工作，并通过 SQL 验证生成的标识符被正确限定
     * @Table(value="user_info", mapUnderscoreToCamelCase=true, useDelimited=true, caseInsensitive=true)
     * 期望 SQL 行为：
     * - 表名和列名用 PostgreSQL 双引号包裹（useDelimited）
     * - Java 属性 createTime 映射为 create_time（mapUnderscoreToCamelCase）
     */
    @Test
    public void testCamelCase_CombinedWithDelimitedAndCaseInsensitive() throws SQLException {
        AllNamingOptionsUser user = new AllNamingOptionsUser();
        user.setId(40010);
        user.setName("AllOptions");
        user.setAge(34);
        user.setEmail("all@test.com");
        user.setCreateTime(new Date());

        // ------- 验证 INSERT SQL -------
        BoundSql insertBoundSql = lambdaTemplate.insert(AllNamingOptionsUser.class)//
                .applyEntity(user)//
                .getBoundSql();
        String insertSql = insertBoundSql.getSqlString();

        // useDelimited → 表名被引号包裹
        assertTrue("INSERT SQL should quote table name: " + insertSql, insertSql.contains("\"user_info\""));
        // mapUnderscoreToCamelCase → createTime 转为 create_time, 并被引号包裹
        assertTrue("INSERT SQL should quote create_time column: " + insertSql, insertSql.contains("\"create_time\""));
        // 其他列也应被引号包裹
        assertTrue("INSERT SQL should quote id column: " + insertSql, insertSql.contains("\"id\""));
        assertTrue("INSERT SQL should quote name column: " + insertSql, insertSql.contains("\"name\""));

        // 执行 INSERT
        int rows = lambdaTemplate.insert(AllNamingOptionsUser.class)//
                .applyEntity(user)//
                .executeSumResult();
        assertEquals(1, rows);

        // ------- 验证 SELECT SQL -------
        BoundSql queryBoundSql = lambdaTemplate.query(AllNamingOptionsUser.class)//
                .eq(AllNamingOptionsUser::getId, 40010)//
                .getBoundSql();
        String querySql = queryBoundSql.getSqlString();

        // useDelimited → SELECT 的表名用引号
        assertTrue("SELECT SQL should quote table name: " + querySql, querySql.contains("\"user_info\""));
        // WHERE 条件中 id 列也被引号包裹
        assertTrue("SELECT SQL WHERE should quote id: " + querySql, querySql.contains("\"id\""));

        // 执行查询验证数据回填
        AllNamingOptionsUser loaded = lambdaTemplate.query(AllNamingOptionsUser.class)//
                .eq(AllNamingOptionsUser::getId, 40010)//
                .queryForObject();

        assertNotNull("All options combined should work", loaded);
        assertEquals("AllOptions", loaded.getName());
        assertNotNull("createTime with camelCase+delimited should map", loaded.getCreateTime());

        // ------- 验证 UPDATE SQL -------
        BoundSql updateBoundSql = lambdaTemplate.update(AllNamingOptionsUser.class)//
                .eq(AllNamingOptionsUser::getId, 40010)//
                .updateTo(AllNamingOptionsUser::getName, "UpdatedAll")//
                .getBoundSql();
        String updateSql = updateBoundSql.getSqlString();

        assertTrue("UPDATE SQL should quote table name: " + updateSql, updateSql.contains("\"user_info\""));
        assertTrue("UPDATE SQL SET should quote name: " + updateSql, updateSql.contains("\"name\""));
        assertTrue("UPDATE SQL WHERE should quote id: " + updateSql, updateSql.contains("\"id\""));
    }

    /**
     * 场景：UPDATE 操作中的驼峰转换
     * 验证：SET create_time = ? WHERE id = ? 的列名正确解析
     */
    @Test
    public void testCamelCase_UpdateOperation() throws SQLException {
        CamelCaseEnabledUser user = new CamelCaseEnabledUser();
        user.setId(40011);
        user.setName("BeforeUpdate");
        user.setAge(22);
        user.setEmail("update@test.com");
        user.setCreateTime(new Date());

        lambdaTemplate.insert(CamelCaseEnabledUser.class).applyEntity(user).executeSumResult();

        Date newTime = new Date(System.currentTimeMillis() + 100000);
        int updated = lambdaTemplate.update(CamelCaseEnabledUser.class)//
                .eq(CamelCaseEnabledUser::getId, 40011)//
                .updateTo(CamelCaseEnabledUser::getCreateTime, newTime)//
                .doUpdate();
        assertEquals(1, updated);

        CamelCaseEnabledUser loaded = lambdaTemplate.query(CamelCaseEnabledUser.class)//
                .eq(CamelCaseEnabledUser::getId, 40011)//
                .queryForObject();
        assertNotNull("Updated createTime should be present", loaded.getCreateTime());
    }

    /**
     * 场景：DELETE 操作中的驼峰条件
     * 验证：WHERE create_time IS NOT NULL 条件中列名正确
     */
    @Test
    public void testCamelCase_DeleteWithCamelCaseCondition() throws SQLException {
        CamelCaseEnabledUser user = new CamelCaseEnabledUser();
        user.setId(40012);
        user.setName("ToDelete");
        user.setAge(29);
        user.setEmail("delete@test.com");
        user.setCreateTime(new Date());

        lambdaTemplate.insert(CamelCaseEnabledUser.class).applyEntity(user).executeSumResult();

        int deleted = lambdaTemplate.delete(CamelCaseEnabledUser.class)//
                .eq(CamelCaseEnabledUser::getId, 40012)//
                .isNotNull(CamelCaseEnabledUser::getCreateTime)//
                .doDelete();
        assertEquals(1, deleted);

        long after = lambdaTemplate.query(CamelCaseEnabledUser.class)//
                .eq(CamelCaseEnabledUser::getId, 40012)//
                .queryForCount();
        assertEquals(0, after);
    }

    /**
     * 场景：Options + Map 模式查询
     * 验证：Map 模式下直接用列名，不受驼峰转换影响
     */
    @Test
    public void testCamelCase_Options_MapMode() throws SQLException {
        jdbcTemplate.executeUpdate(//
                "INSERT INTO user_info (id, name, age, email, create_time) " +//
                        "VALUES (40013, 'MapMode', 24, 'map@test.com', CURRENT_TIMESTAMP)");

        Options opts = Options.of().mapUnderscoreToCamelCase(true);
        LambdaTemplate optionsLambda = new LambdaTemplate(dataSource, opts);

        Map<String, Object> row = optionsLambda.queryFreedom("user_info")//
                .eq("id", 40013)//
                .queryForObject();

        assertNotNull("Map mode query should work", row);
        assertEquals("MapMode", row.get("name"));
        assertNotNull("create_time column accessible by direct name", row.get("create_time"));
    }

    // ==================== Helper ====================

    private void ensurePlainUserTableExists() {
        try {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM plain_user WHERE 1=0", Integer.class);
            jdbcTemplate.executeUpdate("DELETE FROM plain_user");
        } catch (Exception e) {
            try {
                jdbcTemplate.executeUpdate("CREATE TABLE plain_user (" +//
                        "id INT PRIMARY KEY, " + //
                        "name VARCHAR(100), " +  //
                        "age INT, " +            //
                        "email VARCHAR(100), " + //
                        "create_time TIMESTAMP)");
            } catch (Exception ex) {
                throw new RuntimeException("Failed to create plain_user table", ex);
            }
        }
    }
}
