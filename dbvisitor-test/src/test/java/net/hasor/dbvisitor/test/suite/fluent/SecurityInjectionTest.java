package net.hasor.dbvisitor.test.suite.fluent;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * SQL Injection Security Test (安全注入攻击测试)
 * 从黑客视角审视 dbvisitor lambda API 的安全性。
 * 测试分类:
 * <ul>
 *   <li>A - 值参数注入(Value Injection): eq/like/in 等条件的值参数化验证</li>
 *   <li>B - apply() 参数化占位符安全性验证</li>
 *   <li>C - Freedom模式列名注入: fmtName 引号逃逸攻击</li>
 *   <li>D - ORDER BY / GROUP BY 注入</li>
 *   <li>E - eqBySampleMap key注入</li>
 *   <li>F - UPDATE SET 列名注入</li>
 *   <li>G - INSERT 列名注入</li>
 *   <li>H - 表名注入</li>
 *   <li>I - Lambda方法引用安全性验证</li>
 *   <li>J - 二次注入场景</li>
 *   <li>K - SELECT列名注入</li>
 *   <li>L - allowEmptyWhere 联合攻击</li>
 * </ul>
 */
public class SecurityInjectionTest extends AbstractOneApiTest {

    private LambdaTemplate lambda;

    @Before
    public void setUp() throws Exception {
        super.setup();
        this.lambda = new LambdaTemplate(dataSource);
    }

    // ==================== A: 值参数注入（应全部安全 - 参数化查询） ====================

    /**
     * A1: 经典 SQL 注入攻击 — ' OR '1'='1 绕过 WHERE
     * 攻击目标: eq() 的 value 参数
     * 预期: 参数化防护，注入无效，返回空
     */
    @Test
    public void testValueInjection_ClassicOrBypass() throws SQLException {
        lambda.insert(UserInfo.class).applyEntity(createUser(80001, "Alice", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(80002, "Bob", 30)).executeSumResult();

        String payload = "Alice' OR '1'='1";
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .eq(UserInfo::getName, payload)//
                .queryForList();

        assertEquals("Classic OR injection should return 0 (parameterized)", 0, result.size());
    }

    /**
     * A2: UNION SELECT 注入 — 尝试通过值窃取数据
     * 攻击目标: eq() 的 value 参数
     * 预期: 参数化防护
     */
    @Test
    public void testValueInjection_UnionSelect() throws SQLException {
        lambda.insert(UserInfo.class).applyEntity(createUser(80011, "Target", 25)).executeSumResult();

        String payload = "' UNION SELECT id, name, age, email, create_time FROM user_info --";
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .eq(UserInfo::getName, payload)//
                .queryForList();

        assertEquals("UNION SELECT via value should return 0", 0, result.size());
    }

    /**
     * A3: 堆叠查询注入 — 通过分号执行附加语句
     * 攻击目标: eq() 的 value 参数
     * 预期: 参数化防护
     */
    @Test
    public void testValueInjection_StackedQuery() throws SQLException {
        lambda.insert(UserInfo.class).applyEntity(createUser(80021, "Victim", 25)).executeSumResult();

        String payload = "Victim'; DELETE FROM user_info; --";
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .eq(UserInfo::getName, payload)//
                .queryForList();

        assertEquals("Stacked query via value should return 0", 0, result.size());

        // 验证数据未被删除
        UserInfo victim = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 80021)//
                .queryForObject();
        assertNotNull("Victim record should still exist (stacked query blocked)", victim);
    }

    /**
     * A4: LIKE 条件注入 — 通过 like 值尝试绕过
     * 攻击目标: like() 的 value 参数
     * 预期: 参数化防护
     */
    @Test
    public void testValueInjection_LikeBypass() throws SQLException {
        lambda.insert(UserInfo.class).applyEntity(createUser(80031, "LikeTarget", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(80032, "Other", 30)).executeSumResult();

        String payload = "%' OR '1'='1' --";
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, Arrays.asList(80031, 80032))//
                .like(UserInfo::getName, payload)//
                .queryForList();

        assertEquals("LIKE injection via value should return 0", 0, result.size());
    }

    /**
     * A5: IN 条件注入 — 传入恶意值列表
     * 攻击目标: in() 的 values 参数
     * 预期: 所有值独立参数化
     */
    @Test
    public void testValueInjection_InClause() throws SQLException {
        lambda.insert(UserInfo.class).applyEntity(createUser(80041, "InTarget", 25)).executeSumResult();

        // 使用 String 类型字段（name）避免类型转换错误
        List<Object> payload = Arrays.<Object>asList("InTarget' OR '1'='1", "x'; DROP TABLE users; --");
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 80041)//
                .in(UserInfo::getName, payload)//
                .queryForList();

        assertEquals("IN clause injection should return 0", 0, result.size());
    }

    /**
     * A6: BETWEEN 注入 — 通过边界值注入
     * 攻击目标: between() 的 value 参数
     * 预期: 参数化防护
     */
    @Test
    public void testValueInjection_Between() throws SQLException {
        lambda.insert(UserInfo.class).applyEntity(createUser(80051, "BetweenTarget", 25)).executeSumResult();

        // between 的值传递 String 而非 Integer — 应被参数化处理（类型不匹配会报错或返回空）
        try {
            List<UserInfo> result = lambda.query(UserInfo.class)//
                    .eq(UserInfo::getId, 80051)//
                    .between("age", "0 OR 1=1 --", "100")//
                    .queryForList();
            // 如果没抛异常，结果不应因注入而返回额外行
            assertTrue("Between injection should not bypass", result.size() <= 1);
        } catch (Exception e) {
            // 类型转换错误也是一种防护
            assertNotNull(e.getMessage());
        }
    }

    // ==================== B: apply() 参数化安全性验证 ====================

    /**
     * B1: apply() — 使用参数化占位符（安全用法）
     * 攻击向量: 证明 apply 的 ? 占位符是参数化的
     */
    @Test
    public void testApply_ParameterizedUsage() throws SQLException {
        lambda.insert(UserInfo.class).applyEntity(createUser(80111, "ApplyParam", 25)).executeSumResult();

        // 安全用法: 使用 ? 参数化
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .apply("name = ?", "ApplyParam")//
                .queryForList();
        assertEquals("apply with ? should work parameterized", 1, result.size());

        // 验证 ? 中的值不会被当做SQL
        result = lambda.query(UserInfo.class)//
                .apply("name = ?", "ApplyParam' OR '1'='1")//
                .queryForList();
        assertEquals("apply with ? prevents value injection", 0, result.size());
    }

    // ==================== C: Freedom模式列名注入（通过 fmtName 逃逸） ====================

    /**
     * C1: Freedom模式 eq() 列名注入 — 引号逃逸
     * 攻击向量: 属性名中嵌入双引号(PG)逃逸 fmtName 包裹
     * 例: eq("id\" = 80301 OR 1=1 --", "x")
     */
    @Test
    public void testFreedomColumnInjection_QuoteEscape() throws SQLException {
        lambda.insert(UserInfo.class).applyEntity(createUser(80301, "FreedomVictim1", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(80302, "FreedomVictim2", 30)).executeSumResult();

        // 攻击: 使用双引号逃逸列名（PostgreSQL 使用双引号作列限定符）
        String maliciousColumn = "id\" = 80301 OR 1=1 --";
        try {
            List<Map<String, Object>> result = lambda.queryFreedom("user_info")//
                    .eq(maliciousColumn, "irrelevant")//
                    .queryForList();

            if (result.size() >= 2) {
                fail("Column quote escape injection returned " + result.size() + " rows");
            }
        } catch (SQLException e) {
            // SQL 错误 = 注入被阻止（列不存在）
        }
    }

    /**
     * C2: Freedom模式 eq() 列名注入 — OR 条件绕过
     * 攻击向量: 列名中嵌入 OR 子句实现全表扫描
     */
    @Test
    public void testFreedomColumnInjection_OrBypass() throws SQLException {
        lambda.insert(UserInfo.class).applyEntity(createUser(80311, "FreedomOr1", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(80312, "FreedomOr2", 30)).executeSumResult();

        // 攻击: 列名注入 OR 条件
        String maliciousColumn = "name\" = 'FreedomOr1' OR \"name";
        try {
            List<Map<String, Object>> result = lambda.queryFreedom("user_info")//
                    .like(maliciousColumn, "Freedom%")//
                    .queryForList();

            if (result.size() >= 2) {
                fail("Freedom column OR bypass returned " + result.size() + " rows");
            }
        } catch (SQLException e) {
            // SQL 错误 = 注入被阻止
        }
    }

    /**
     * C3: Freedom模式 — 利用列名注入执行子查询
     * 攻击向量: 属性名中嵌入子查询
     */
    @Test
    public void testFreedomColumnInjection_Subquery() throws SQLException {
        lambda.insert(UserInfo.class).applyEntity(createUser(80321, "FreedomSub1", 25)).executeSumResult();

        // 攻击: 列名中注入子查询
        String maliciousColumn = "(SELECT name FROM user_info LIMIT 1)";
        try {
            List<Map<String, Object>> result = lambda.queryFreedom("user_info")//
                    .eq(maliciousColumn, "FreedomSub1")//
                    .queryForList();
            fail("Subquery in column name was not blocked, returned " + result.size() + " rows");
        } catch (SQLException e) {
            assertNotNull("Subquery in column name blocked", e.getMessage());
        }
    }

    // ==================== D: ORDER BY / GROUP BY 注入 ====================

    /**
     * D1: ORDER BY 注入 — 通过 asc() 列名注入
     * 攻击向量: 排序列名中插入恶意SQL
     */
    @Test
    public void testOrderByInjection_AscColumn() throws SQLException {
        lambda.insert(UserInfo.class).applyEntity(createUser(80401, "OrderInj1", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(80402, "OrderInj2", 30)).executeSumResult();

        // 攻击: 在 asc() 的列名中注入
        String maliciousColumn = "id\"; DROP TABLE user_info; --";
        try {
            List<Map<String, Object>> result = lambda.queryFreedom("user_info")//
                    .like("name", "OrderInj%")//
                    .asc(maliciousColumn)//
                    .queryForList();
        } catch (SQLException e) {
            assertNotNull("ORDER BY injection blocked", e.getMessage());
        }

        // 验证表仍然存在
        long count = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 80401)//
                .queryForCount();
        assertTrue("Table should survive ORDER BY injection", count >= 1);
    }

    /**
     * D2: ORDER BY 注入 — CASE WHEN 布尔盲注
     * 攻击向量: 通过 ORDER BY 中的 CASE WHEN 判断布尔条件推断数据
     */
    @Test
    public void testOrderByInjection_BlindBoolean() throws SQLException {
        lambda.insert(UserInfo.class).applyEntity(createUser(80411, "BlindOrd1", 10)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(80412, "BlindOrd2", 20)).executeSumResult();

        // 攻击: CASE WHEN 盲注 — 根据排序顺序推断布尔值
        String maliciousColumn = "CASE WHEN (SELECT count(*) FROM user_info) > 0 THEN id ELSE name END";
        try {
            List<Map<String, Object>> result = lambda.queryFreedom("user_info")//
                    .like("name", "BlindOrd%")//
                    .asc(maliciousColumn)//
                    .queryForList();

            // 如果返回结果且不报错，说明 CASE WHEN 在 ORDER BY 中被执行
            fail("Blind boolean ORDER BY injection was not blocked, returned " + result.size() + " rows");
        } catch (SQLException e) {
            assertNotNull("Blind boolean ORDER BY blocked", e.getMessage());
        }
    }

    /**
     * D3: GROUP BY 注入 — 同 ORDER BY 的攻击面
     */
    @Test
    public void testGroupByInjection() throws SQLException {
        lambda.insert(UserInfo.class).applyEntity(createUser(80421, "GroupInj1", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(80422, "GroupInj2", 25)).executeSumResult();

        // 攻击: GROUP BY 注入
        String maliciousColumn = "age\"; DROP TABLE user_info; --";
        try {
            lambda.queryFreedom("user_info")//
                    .like("name", "GroupInj%")//
                    .groupBy(maliciousColumn)//
                    .queryForList();
        } catch (SQLException e) {
            assertNotNull("GROUP BY injection blocked", e.getMessage());
        }

        // 验证表仍然存在
        long count = lambda.query(UserInfo.class).eq(UserInfo::getId, 80421).queryForCount();
        assertTrue("Table should survive GROUP BY injection", count >= 1);
    }

    // ==================== E: eqBySampleMap key 注入 ====================

    /**
     * E1: eqBySampleMap — Map key 作为列名注入
     * 攻击向量: Freedom模式下 Map 的 key 直接成为列名
     */
    @Test
    public void testEqBySampleMapInjection() throws SQLException {
        lambda.insert(UserInfo.class).applyEntity(createUser(80501, "SampleMapVictim1", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(80502, "SampleMapVictim2", 30)).executeSumResult();

        // 攻击: Map key 中注入 SQL
        Map<String, Object> maliciousSample = new LinkedHashMap<String, Object>();
        maliciousSample.put("id\" = 80501 OR \"id", 80501); // 尝试逃逸引号

        try {
            List<Map<String, Object>> result = lambda.queryFreedom("user_info")//
                    .eqBySampleMap(maliciousSample)//
                    .queryForList();

            if (result.size() > 1) {
                fail("eqBySampleMap key injection succeeded! Returned " + result.size() + " rows");
            }
        } catch (SQLException e) {
            assertNotNull("eqBySampleMap injection blocked", e.getMessage());
        }
    }

    /**
     * E2: eqBySampleMap — 布尔条件注入
     * 攻击向量: key 中加入永真条件
     */
    @Test
    public void testEqBySampleMapInjection_AlwaysTrue() throws SQLException {
        lambda.insert(UserInfo.class).applyEntity(createUser(80511, "SampleAlways1", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(80512, "SampleAlways2", 30)).executeSumResult();

        // 攻击: 注入 1=1 永真条件
        Map<String, Object> maliciousSample = new LinkedHashMap<String, Object>();
        maliciousSample.put("1\" = 1 OR \"1", 1);

        try {
            List<Map<String, Object>> result = lambda.queryFreedom("user_info")//
                    .eqBySampleMap(maliciousSample)//
                    .queryForList();

            fail("Always-true injection was not blocked, returned " + result.size() + " rows");
        } catch (SQLException e) {
            assertNotNull("Always-true injection blocked", e.getMessage());
        }
    }

    // ==================== F: UPDATE SET 列名注入 ====================

    /**
     * F1: Freedom模式 updateTo — 列名注入篡改额外字段
     * 攻击向量: property 中注入额外的 SET 子句
     * 如: SET "name" = 'HACKED', "age" = 999 --" = ?
     */
    @Test
    public void testUpdateColumnInjection() throws SQLException {
        lambda.insert(UserInfo.class).applyEntity(createUser(80601, "UpdateInj", 25)).executeSumResult();

        // 攻击: 通过列名注入篡改额外字段
        String maliciousColumn = "name\" = 'HACKED', \"age\" = 999 --";
        try {
            lambda.updateFreedom("user_info")//
                    .eq("id", 80601)//
                    .updateTo(maliciousColumn, "irrelevant")//
                    .doUpdate();

            UserInfo loaded = lambda.query(UserInfo.class).eq(UserInfo::getId, 80601).queryForObject();
            assertNotNull(loaded);
            if ("HACKED".equals(loaded.getName()) || Integer.valueOf(999).equals(loaded.getAge())) {
                fail("UPDATE column injection changed data: name=" + loaded.getName() + ", age=" + loaded.getAge());
            }
        } catch (SQLException e) {
            // SQL 错误 = 注入被阻止（列不存在）
        }
    }

    /**
     * F2: Freedom模式 updateToSampleMap — Map key 注入
     * 攻击向量: Map 的 key 注入额外 SET 子句
     */
    @Test
    public void testUpdateSampleMapInjection() throws SQLException {
        lambda.insert(UserInfo.class).applyEntity(createUser(80611, "UpdMapInj", 25)).executeSumResult();

        // 攻击: Map key 中注入破坏性 SET
        Map<String, Object> maliciousMap = new LinkedHashMap<String, Object>();
        maliciousMap.put("name\" = 'HACKED' WHERE 1=1 --", "irrelevant");

        try {
            lambda.updateFreedom("user_info")//
                    .eq("id", 80611)//
                    .updateToSampleMap(maliciousMap)//
                    .doUpdate();

            UserInfo loaded = lambda.query(UserInfo.class).eq(UserInfo::getId, 80611).queryForObject();
            if (loaded != null && "HACKED".equals(loaded.getName())) {
                fail("updateToSampleMap key injection changed name to HACKED");
            }
        } catch (SQLException e) {
            // SQL 错误 = 注入被阻止
        }
    }

    // ==================== G: INSERT 列名注入 ====================

    /**
     * G1: Freedom模式 INSERT — Map key 作为列名注入
     * 攻击向量: applyMap 的 key 注入额外列
     */
    @Test
    public void testInsertColumnInjection() throws SQLException {
        // 攻击: 列名中注入闭合并添加恶意值
        Map<String, Object> maliciousMap = new LinkedHashMap<String, Object>();
        maliciousMap.put("id", 80701);
        maliciousMap.put("name\", age) VALUES (80701, 'HACKED', 999); --", "irrelevant");

        try {
            lambda.insertFreedom("user_info")//
                    .applyMap(maliciousMap)//
                    .executeSumResult();

            // 检查是否超出预期的插入
            UserInfo loaded = lambda.query(UserInfo.class).eq(UserInfo::getId, 80701).queryForObject();
            if (loaded != null && "HACKED".equals(loaded.getName())) {
                fail("INSERT column injection succeeded!");
            }
        } catch (Exception e) {
            // 注入导致 SQL 语法错误 = 安全
            assertNotNull("INSERT column injection blocked", e.getMessage());
        }
    }

    // ==================== H: 表名注入 ====================

    /**
     * H1: Freedom模式表名注入 — 通过表名参数注入
     * 攻击向量: queryFreedom 的表名参数试图逃逸
     */
    @Test
    public void testTableNameInjection() throws SQLException {
        lambda.insert(UserInfo.class).applyEntity(createUser(80801, "TableInj", 25)).executeSumResult();

        // 攻击: 表名中注入 UNION
        String maliciousTable = "user_info\" UNION SELECT 1,2,3,4,5 FROM \"user_info";
        try {
            List<Map<String, Object>> result = lambda.queryFreedom(maliciousTable)//
                    .queryForList();
        } catch (Exception e) {
            // 预期 SQL 错误
            assertNotNull("Table name injection blocked", e.getMessage());
        }

        // 验证原表数据安全
        UserInfo safe = lambda.query(UserInfo.class).eq(UserInfo::getId, 80801).queryForObject();
        assertNotNull("Original table should be safe", safe);
    }

    // ==================== I: Lambda方法引用安全性验证 ====================

    /**
     * I1: 验证 Lambda 方法引用不可注入
     * Lambda 引用的属性名在编译期确定，用户无法运行时篡改
     */
    @Test
    public void testLambdaMethodRefSafe() throws SQLException {
        lambda.insert(UserInfo.class).applyEntity(createUser(80901, "LambdaSafe", 25)).executeSumResult();

        // Lambda 方法引用 — 属性名编译期确定，无法注入
        UserInfo result = lambda.query(UserInfo.class)//
                .eq(UserInfo::getName, "LambdaSafe")//
                .eq(UserInfo::getAge, 25)//
                .queryForObject();
        assertNotNull(result);

        // Lambda 引用的 like — 值参数化
        List<UserInfo> list = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "' OR 1=1 --")//
                .queryForList();
        assertEquals("Lambda like with injection payload should return 0", 0, list.size());
    }

    /**
     * I2: 验证 eqBySample (Entity) 不可注入
     * 属性名来自编译期 ColumnMapping，值参数化
     */
    @Test
    public void testEqBySampleEntitySafe() throws SQLException {
        lambda.insert(UserInfo.class).applyEntity(createUser(80911, "SampleSafe", 25)).executeSumResult();

        // eqBySample(Entity) — 属性名来自已注册映射  
        UserInfo sample = new UserInfo();
        sample.setName("' OR 1=1 --");
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .eqBySample(sample)//
                .queryForList();
        assertEquals("eqBySample with injection payload should return 0", 0, result.size());
    }

    // ==================== J: 二次注入场景 ====================

    /**
     * J1: 二次注入 — 存储恶意值后查询时是否安全
     * 攻击方式: 先存入恶意字符串，再通过该值做关联查询
     */
    @Test
    public void testSecondOrderInjection() throws SQLException {
        // 第一步: 存入恶意 name（值参数化允许存入任意字符串）
        String maliciousName = "' OR '1'='1";
        lambda.insert(UserInfo.class).applyEntity(createUser(81001, maliciousName, 25)).executeSumResult();

        // 第二步: 取出恶意值
        UserInfo loaded = lambda.query(UserInfo.class).eq(UserInfo::getId, 81001).queryForObject();
        assertNotNull(loaded);
        assertEquals(maliciousName, loaded.getName());

        // 第三步: 用取出的恶意值再做查询 — 应仍然安全（参数化）
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .eq(UserInfo::getName, loaded.getName())//
                .queryForList();
        assertEquals("Second-order injection should find exactly the stored record", 1, result.size());
        assertEquals(Integer.valueOf(81001), result.get(0).getId());
    }

    /**
     * J2: 二次注入 — Freedom模式下存储恶意值
     * 攻击方式: 通过 Freedom Insert 存入，再用 Entity Query 检索
     */
    @Test
    public void testSecondOrderInjection_FreedomInsert() throws SQLException {
        String maliciousName = "admin'--";
        Map<String, Object> row = new LinkedHashMap<String, Object>();
        row.put("id", 81011);
        row.put("name", maliciousName);
        row.put("age", 30);

        lambda.insertFreedom("user_info").applyMap(row).executeSumResult();

        // 用 Entity API 查询 — 值应被安全处理
        UserInfo result = lambda.query(UserInfo.class).eq(UserInfo::getId, 81011).queryForObject();
        assertNotNull(result);
        assertEquals(maliciousName, result.getName());

        // 用恶意值做查询
        List<UserInfo> list = lambda.query(UserInfo.class)//
                .eq(UserInfo::getName, result.getName())//
                .queryForList();
        assertEquals(1, list.size());
    }

    // ==================== K: SELECT 列名注入（Freedom String API） ====================

    /**
     * K1: Freedom 模式 select() — 列名注入子查询
     * 攻击向量: select("id\", (SELECT ...) as leak --") 注入子查询到 SELECT
     */
    @Test
    public void testSelectColumnInjection() throws SQLException {
        lambda.insert(UserInfo.class).applyEntity(createUser(81101, "SelColInj", 25)).executeSumResult();

        // 攻击: 通过 select 参数的列名注入子查询
        String maliciousColumn = "id\", (SELECT count(*) FROM user_info) as leaked_count --";
        try {
            List<Map<String, Object>> result = lambda.queryFreedom("user_info")//
                    .select(maliciousColumn)//
                    .eq("id", 81101)//
                    .queryForList();

            // 如果 leaked_count 出现在结果中，说明注入成功
            if (!result.isEmpty() && result.get(0).containsKey("leaked_count")) {
                fail("SELECT column injection succeeded! Leaked count: " + result.get(0).get("leaked_count"));
            }
        } catch (SQLException e) {
            assertNotNull("SELECT column injection blocked", e.getMessage());
        }
    }

    // ==================== L: allowEmptyWhere + 注入联合攻击 ====================

    /**
     * L1: allowEmptyWhere + apply("1=1") — 全表操作
     * 攻击向量: 结合 allowEmptyWhere 和 apply 实现全表 DELETE
     */
    @Test
    public void testAllowEmptyWhereWithApply() throws SQLException {
        lambda.insert(UserInfo.class).applyEntity(createUser(81201, "EmptyWhere1", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(81202, "EmptyWhere2", 30)).executeSumResult();

        // 正常场景: 无 allowEmptyWhere 应阻止无条件操作
        try {
            lambda.delete(UserInfo.class).doDelete();
            fail("Should throw exception without conditions");
        } catch (Exception e) {
            assertTrue("Should mention allowEmptyWhere", e.getMessage().contains("allowEmptyWhere"));
        }

        // 攻击场景: apply("1=1") + allowEmptyWhere 组合
        // 注意: apply 添加了条件，所以不需要 allowEmptyWhere
        int deleted = lambda.delete(UserInfo.class)//
                .in(UserInfo::getId, Arrays.asList(81201, 81202)) // 限制范围
                .apply("1=1") // 永真但不扩大范围（AND 关系）
                .doDelete();

        assertEquals("apply(1=1) in AND context should only delete scoped rows", 2, deleted);
    }

    /**
     * Helper: 创建 UserInfo
     */
    private UserInfo createUser(int id, String name, Integer age) {
        UserInfo u = new UserInfo();
        u.setId(id);
        u.setName(name);
        u.setAge(age);
        u.setEmail(id + "@security.com");
        u.setCreateTime(new java.util.Date());
        return u;
    }
}
