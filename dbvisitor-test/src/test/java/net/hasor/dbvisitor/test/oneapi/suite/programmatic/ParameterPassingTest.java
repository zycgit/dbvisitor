package net.hasor.dbvisitor.test.oneapi.suite.programmatic;

import java.sql.SQLException;
import java.util.*;
import net.hasor.dbvisitor.dynamic.args.ArraySqlArgSource;
import net.hasor.dbvisitor.dynamic.args.BeanSqlArgSource;
import net.hasor.dbvisitor.dynamic.args.MapSqlArgSource;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.handler.number.IntegerTypeHandler;
import net.hasor.dbvisitor.types.handler.string.StringTypeHandler;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * SQL 参数传递方式测试
 * 测试可以通过哪些方式传递参数：
 * - 位置参数（数组、SqlArg、PreparedStatementSetter）
 * - 命名参数（Map、Bean、OGNL）
 * - SQL 注入（${}）
 * - 规则参数（@{and}、@{in}、@{set}）
 * - 接口方式（ArraySqlArgSource、BeanSqlArgSource、MapSqlArgSource）
 */
public class ParameterPassingTest extends AbstractOneApiTest {

    @Override
    protected void cleanTestData() {
        super.cleanTestData();
    }

    // ========== 位置参数 - Object[] ==========

    /**
     * 测试位置参数 - Object[] 数组
     */
    @Test
    public void testPosition_Array() throws SQLException {
        // INSERT with array
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)",//
                new Object[] { "ArrayUser", 25, "array@test.com", new Date() });

        // SELECT with array
        UserInfo user = jdbcTemplate.queryForObject("SELECT * FROM user_info WHERE name = ? AND age > ?",//
                new Object[] { "ArrayUser", 20 }, UserInfo.class);

        assertNotNull(user);
        assertEquals("ArrayUser", user.getName());
        assertEquals(Integer.valueOf(25), user.getAge());
    }

    /**
     * 测试位置参数 - NULL 值处理
     */
    @Test
    public void testPosition_NullValue() throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)",//
                new Object[] { "NullUser", null, "null@test.com", new Date() });

        Map<String, Object> result = jdbcTemplate.queryForMap("SELECT name, age FROM user_info WHERE name = ?",//
                new Object[] { "NullUser" });

        assertEquals("NullUser", result.get("name"));
        assertNull("age 应为 NULL", result.get("age"));
    }

    /**
     * 测试位置参数 - 多个参数
     */
    @Test
    public void testPosition_MultipleParams() throws SQLException {
        // 插入测试数据
        for (int i = 1; i <= 5; i++) {
            jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)",//
                    new Object[] { "MultiUser" + i, 20 + i, "multi" + i + "@test.com", new Date() });
        }

        // 使用多个位置参数查询
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE age BETWEEN ? AND ? AND name LIKE ?",//
                new Object[] { 21, 24, "MultiUser%" }, Long.class);

        assertEquals(Long.valueOf(4), count); // 21,22,23,24
    }

    // ========== 位置参数 - SqlArg ==========

    /**
     * 测试位置参数 - SqlArg 指定类型处理器
     */
    @Test
    public void testPosition_SqlArg() throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)",//
                new SqlArg[] {//
                        SqlArg.valueOf("SqlArgUser", new StringTypeHandler()),//
                        SqlArg.valueOf(30, new IntegerTypeHandler()),//
                        SqlArg.valueOf("sqlarg@test.com"),//
                        SqlArg.valueOf(new Date())//
                });

        UserInfo user = jdbcTemplate.queryForObject("SELECT * FROM user_info WHERE name = ?",//
                new Object[] { "SqlArgUser" }, UserInfo.class);

        assertNotNull(user);
        assertEquals("SqlArgUser", user.getName());
        assertEquals(Integer.valueOf(30), user.getAge());
    }

    // ========== 位置参数 - PreparedStatementSetter ==========

    /**
     * 测试位置参数 - PreparedStatementSetter Lambda
     */
    @Test
    public void testPosition_PreparedStatementSetter() throws SQLException {
        // INSERT
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)",//
                ps -> {
                    ps.setString(1, "SetterUser");
                    ps.setInt(2, 28);
                    ps.setString(3, "setter@test.com");
                    ps.setDate(4, new java.sql.Date(System.currentTimeMillis()));
                });

        // SELECT
        String email = jdbcTemplate.queryForObject("SELECT email FROM user_info WHERE name = ?", ps -> ps.setString(1, "SetterUser"), String.class);

        assertEquals("setter@test.com", email);
    }

    // ========== 命名参数 - Map (冒号语法) ==========

    /**
     * 测试命名参数 - Map 冒号语法 :name
     */
    @Test
    public void testNamed_MapColon() throws SQLException {
        // INSERT
        Map<String, Object> insertParams = new HashMap<>();
        insertParams.put("name", "ColonUser");
        insertParams.put("age", 28);
        insertParams.put("email", "colon@test.com");
        insertParams.put("createTime", new Date());

        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (:name, :age, :email, :createTime)",//
                insertParams);

        // SELECT
        Map<String, Object> selectParams = new HashMap<>();
        selectParams.put("name", "ColonUser");
        selectParams.put("minAge", 20);

        UserInfo user = jdbcTemplate.queryForObject("SELECT * FROM user_info WHERE name = :name AND age > :minAge",//
                selectParams, UserInfo.class);

        assertNotNull(user);
        assertEquals("ColonUser", user.getName());
    }

    /**
     * 测试命名参数 - Map 井号花括号语法 #{name}
     */
    @Test
    public void testNamed_MapBrace() throws SQLException {
        Map<String, Object> insertParams = new HashMap<>();
        insertParams.put("name", "BraceUser");
        insertParams.put("age", 32);
        insertParams.put("email", "brace@test.com");
        insertParams.put("createTime", new Date());

        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (#{name}, #{age}, #{email}, #{createTime})",//
                insertParams);

        Map<String, Object> selectParams = new HashMap<>();
        selectParams.put("name", "BraceUser");

        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE name = #{name}",//
                selectParams, Long.class);

        assertEquals(Long.valueOf(1), count);
    }

    // ========== 命名参数 - Bean ==========

    /**
     * 测试命名参数 - Bean 对象
     */
    @Test
    public void testNamed_Bean() throws SQLException {
        // INSERT - Bean 作为参数
        UserInfo insertUser = new UserInfo();
        insertUser.setName("BeanUser");
        insertUser.setAge(35);
        insertUser.setEmail("bean@test.com");
        insertUser.setCreateTime(new Date());

        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (:name, :age, :email, :createTime)",//
                insertUser);

        // SELECT - Bean 作为参数
        UserInfo queryUser = new UserInfo();
        queryUser.setName("BeanUser");

        String email = jdbcTemplate.queryForObject("SELECT email FROM user_info WHERE name = :name",//
                queryUser, String.class);

        assertEquals("bean@test.com", email);
    }

    // ========== 命名参数 - OGNL ==========

    /**
     * 测试命名参数 - OGNL 嵌套属性访问
     */
    @Test
    public void testNamed_OgnlNested() throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)",//
                new Object[] { "OgnlUser", 27, "ognl@test.com", new Date() });

        // 嵌套 Map 结构
        Map<String, Object> user = new HashMap<>();
        user.put("name", "OgnlUser");
        user.put("info", new HashMap<String, Object>() {{
            put("age", 27);
        }});

        Map<String, Object> params = new HashMap<>();
        params.put("user", user);

        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE name = :user.name AND age = :user.info.age",//
                params, Long.class);

        assertEquals(Long.valueOf(1), count);
    }

    /**
     * 测试命名参数 - OGNL 数组/集合访问
     */
    @Test
    public void testNamed_OgnlArray() throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)",//
                new Object[] { "ArrayOgnl", 30, "arrayognl@test.com", new Date() });

        // OGNL 访问数组元素
        Map<String, Object> params = new HashMap<>();
        params.put("ages", Arrays.asList(30, 35, 40));
        params.put("names", new String[] { "ArrayOgnl", "OtherUser" });

        UserInfo user = jdbcTemplate.queryForObject("SELECT * FROM user_info WHERE age = :ages[0] AND name = :names[0]",//
                params, UserInfo.class);

        assertNotNull(user);
        assertEquals("ArrayOgnl", user.getName());
        assertEquals(Integer.valueOf(30), user.getAge());
    }

    // ========== SQL 注入 ${} ==========

    /**
     * 测试 SQL 注入 - 动态 ORDER BY
     */
    @Test
    public void testInject_OrderBy() throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)",//
                new Object[] { "User1", 25, "u1@test.com", new Date() });
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)",//
                new Object[] { "User2", 30, "u2@test.com", new Date() });
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)",//
                new Object[] { "User3", 20, "u3@test.com", new Date() });

        Map<String, Object> params = new HashMap<>();
        params.put("minAge", 18);
        params.put("orderBy", "age DESC");

        List<UserInfo> results = jdbcTemplate.queryForList("SELECT * FROM user_info WHERE age > #{minAge} AND name LIKE 'User%' ORDER BY ${orderBy}",//
                params, UserInfo.class);

        assertTrue(results.size() >= 3);
        // 验证降序
        assertTrue(results.get(0).getAge() >= results.get(1).getAge());
        assertTrue(results.get(1).getAge() >= results.get(2).getAge());
    }

    /**
     * 测试 SQL 注入 - 动态表名
     */
    @Test
    public void testInject_TableName() throws SQLException {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", "user_info");

        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ${tableName}",//
                params, Long.class);

        assertNotNull(count);
        assertTrue(count >= 0);
    }

    /**
     * 测试 SQL 注入 - 动态列名
     */
    @Test
    public void testInject_ColumnName() throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)",//
                new Object[] { "ColumnUser", 29, "column@test.com", new Date() });

        Map<String, Object> params = new HashMap<>();
        params.put("column", "name");
        params.put("value", "ColumnUser");

        List<Map<String, Object>> results = jdbcTemplate.queryForList("SELECT ${column} FROM user_info WHERE ${column} = #{value}",//
                params);

        assertEquals(1, results.size());
        assertEquals("ColumnUser", results.get(0).get("name"));
    }

    // ========== 规则参数 @{} ==========

    /**
     * 测试规则参数 - @{and} 动态 AND 条件
     */
    @Test
    public void testRule_And() throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)",//
                new Object[] { "AndUser1", 25, "and1@test.com", new Date() });
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)",//
                new Object[] { "AndUser2", 30, "and2@test.com", new Date() });

        // 测试：WHERE 后跟 @{and} 条件（自动添加 AND）
        Map<String, Object> params = new HashMap<>();
        params.put("minAge", 20);
        params.put("name", "AndUser1");

        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE age > :minAge @{and, name = :name}",//
                params, Long.class);

        assertEquals(Long.valueOf(1), count);
    }

    /**
     * 测试规则参数 - @{in} List 展开
     */
    @Test
    public void testRule_In() throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)",//
                new Object[] { "InUser1", 25, "in1@test.com", new Date() });
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)",//
                new Object[] { "InUser2", 30, "in2@test.com", new Date() });
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)",//
                new Object[] { "InUser3", 35, "in3@test.com", new Date() });

        Map<String, Object> params = new HashMap<>();
        params.put("ages", Arrays.asList(25, 30, 35));

        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE age IN @{in, :ages}",//
                params, Long.class);

        assertEquals(Long.valueOf(3), count);
    }

    /**
     * 测试规则参数 - @{set} 动态 UPDATE
     */
    @Test
    public void testRule_Set() throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)",//
                new Object[] { 2001, "SetUser", 28, "old@test.com", new Date() });

        // age 有值，email 为 NULL - 只更新 age
        Map<String, Object> params = new HashMap<>();
        params.put("id", 2001);
        params.put("age", 29);
        params.put("email", null); // NULL 不会生成 SET 子句

        int updated = jdbcTemplate.executeUpdate("UPDATE user_info SET @{set, age = :age} , @{set, email = :email} WHERE id = :id", params);

        assertEquals(1, updated);

        // 验证 age 已更新
        Integer newAge = jdbcTemplate.queryForObject("SELECT age FROM user_info WHERE id = ?",//
                new Object[] { 2001 }, Integer.class);

        assertEquals(Integer.valueOf(29), newAge);
    }

    // ========== 接口方式 - SqlArgSource ==========

    /**
     * 测试 ArraySqlArgSource - 使用位置参数 ?
     */
    @Test
    public void testInterface_ArraySqlArgSource_PositionParam() throws SQLException {
        // INSERT with ArraySqlArgSource
        Object[] insertArray = new Object[] { "ArraySourceUser1", 26, "arraysource1@test.com", new Date() };
        ArraySqlArgSource insertSource = new ArraySqlArgSource(insertArray);

        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)", insertSource);

        // SELECT with ArraySqlArgSource
        Object[] selectArray = new Object[] { "ArraySourceUser1", 20 };
        ArraySqlArgSource selectSource = new ArraySqlArgSource(selectArray);

        UserInfo user = jdbcTemplate.queryForObject("SELECT * FROM user_info WHERE name = ? AND age > ?", selectSource, UserInfo.class);

        assertNotNull(user);
        assertEquals("ArraySourceUser1", user.getName());
        assertEquals(Integer.valueOf(26), user.getAge());
    }

    /**
     * 测试 ArraySqlArgSource - 使用名称化位置参数 :arg0, :arg1
     */
    @Test
    public void testInterface_ArraySqlArgSource_NamedPositionParam() throws SQLException {
        // INSERT with ArraySqlArgSource
        Object[] insertArray = new Object[] { "ArraySourceUser2", 33, "arraysource2@test.com", new Date() };
        ArraySqlArgSource insertSource = new ArraySqlArgSource(insertArray);

        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (:arg0, :arg1, :arg2, :arg3)", insertSource);

        // SELECT with ArraySqlArgSource
        Object[] selectArray = new Object[] { 30, "ArraySourceUser2" };
        ArraySqlArgSource selectSource = new ArraySqlArgSource(selectArray);

        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE age > :arg0 AND name = :arg1", selectSource, Long.class);

        assertEquals(Long.valueOf(1), count);
    }

    /**
     * 测试 ArraySqlArgSource - 使用 List 构造
     */
    @Test
    public void testInterface_ArraySqlArgSource_List() throws SQLException {
        // 使用 List 构造 ArraySqlArgSource
        List<Object> paramList = Arrays.asList("ArraySourceUser3", 40, "arraysource3@test.com", new Date());
        ArraySqlArgSource source = new ArraySqlArgSource(paramList);

        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)", source);

        // 验证插入成功
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE name = ?", new Object[] { "ArraySourceUser3" }, Long.class);

        assertEquals(Long.valueOf(1), count);
    }

    /**
     * 测试 BeanSqlArgSource - 使用 Bean 属性名参数 :propertyName
     */
    @Test
    public void testInterface_BeanSqlArgSource() throws SQLException {
        // INSERT with BeanSqlArgSource
        UserInfo insertUser = new UserInfo();
        insertUser.setName("BeanSourceUser1");
        insertUser.setAge(29);
        insertUser.setEmail("beansource1@test.com");
        insertUser.setCreateTime(new Date());

        BeanSqlArgSource insertSource = new BeanSqlArgSource(insertUser);

        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (:name, :age, :email, :createTime)", insertSource);

        // SELECT with BeanSqlArgSource
        UserInfo queryUser = new UserInfo();
        queryUser.setName("BeanSourceUser1");
        queryUser.setAge(25);

        BeanSqlArgSource selectSource = new BeanSqlArgSource(queryUser);

        UserInfo result = jdbcTemplate.queryForObject("SELECT * FROM user_info WHERE name = :name AND age > :age", selectSource, UserInfo.class);

        assertNotNull(result);
        assertEquals("BeanSourceUser1", result.getName());
        assertEquals(Integer.valueOf(29), result.getAge());
        assertEquals("beansource1@test.com", result.getEmail());
    }

    /**
     * 测试 BeanSqlArgSource - 使用真实 Bean 对象
     */
    @Test
    public void testInterface_BeanSqlArgSource_RealBean() throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)", new Object[] { "BeanSourceUser2", 36, "beansource2@test.com", new Date() });

        // 使用真实的 Bean 对象
        UserInfo queryBean = new UserInfo();
        queryBean.setName("BeanSourceUser2");
        queryBean.setAge(30); // 查询条件：age > 30

        BeanSqlArgSource source = new BeanSqlArgSource(queryBean);

        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE name = :name AND age > :age", source, Long.class);

        assertEquals(Long.valueOf(1), count);
    }

    /**
     * 测试 MapSqlArgSource - 使用 Map 键名参数 :key
     */
    @Test
    public void testInterface_MapSqlArgSource() throws SQLException {
        // INSERT with MapSqlArgSource
        Map<String, Object> insertMap = new HashMap<>();
        insertMap.put("name", "MapSourceUser1");
        insertMap.put("age", 31);
        insertMap.put("email", "mapsource1@test.com");
        insertMap.put("createTime", new Date());

        MapSqlArgSource insertSource = new MapSqlArgSource(insertMap);

        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (:name, :age, :email, :createTime)", insertSource);

        // SELECT with MapSqlArgSource
        Map<String, Object> selectMap = new HashMap<>();
        selectMap.put("name", "MapSourceUser1");
        selectMap.put("minAge", 30);

        MapSqlArgSource selectSource = new MapSqlArgSource(selectMap);

        UserInfo result = jdbcTemplate.queryForObject("SELECT * FROM user_info WHERE name = :name AND age > :minAge", selectSource, UserInfo.class);

        assertNotNull(result);
        assertEquals("MapSourceUser1", result.getName());
        assertEquals(Integer.valueOf(31), result.getAge());
    }

    /**
     * 测试 MapSqlArgSource - 结合 #{} 语法
     */
    @Test
    public void testInterface_MapSqlArgSource_BraceSyntax() throws SQLException {
        Map<String, Object> insertMap = new HashMap<>();
        insertMap.put("name", "MapSourceUser2");
        insertMap.put("age", 38);
        insertMap.put("email", "mapsource2@test.com");
        insertMap.put("createTime", new Date());

        MapSqlArgSource source = new MapSqlArgSource(insertMap);

        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (#{name}, #{age}, #{email}, #{createTime})", source);

        // 验证插入成功
        String email = jdbcTemplate.queryForObject("SELECT email FROM user_info WHERE name = ?", new Object[] { "MapSourceUser2" }, String.class);

        assertEquals("mapsource2@test.com", email);
    }

    /**
     * 测试 MapSqlArgSource - 结合 SQL 注入和规则参数
     */
    @Test
    public void testInterface_MapSqlArgSource_CombinedUsage() throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)", new Object[] { "MapSourceUser3", 27, "mapsource3@test.com", new Date() });
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)", new Object[] { "MapSourceUser4", 34, "mapsource4@test.com", new Date() });
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)", new Object[] { "MapSourceUser5", 42, "mapsource5@test.com", new Date() });

        Map<String, Object> selectMap = new HashMap<>();
        selectMap.put("orderBy", "age ASC");
        selectMap.put("minAge", 25);
        selectMap.put("nameList", Arrays.asList("MapSourceUser3", "MapSourceUser4", "MapSourceUser5"));

        MapSqlArgSource source = new MapSqlArgSource(selectMap);

        List<UserInfo> results = jdbcTemplate.queryForList("SELECT * FROM user_info WHERE age > :minAge AND name IN @{in, :nameList} ORDER BY ${orderBy}", source, UserInfo.class);

        assertEquals(3, results.size());
        // 验证升序排列
        assertEquals("MapSourceUser3", results.get(0).getName());
        assertEquals(Integer.valueOf(27), results.get(0).getAge());
        assertEquals("MapSourceUser4", results.get(1).getName());
        assertEquals(Integer.valueOf(34), results.get(1).getAge());
        assertEquals("MapSourceUser5", results.get(2).getName());
        assertEquals(Integer.valueOf(42), results.get(2).getAge());
    }

}
