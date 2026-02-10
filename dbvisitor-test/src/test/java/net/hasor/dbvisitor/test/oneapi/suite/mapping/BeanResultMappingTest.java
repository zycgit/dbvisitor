package net.hasor.dbvisitor.test.oneapi.suite.mapping;

import java.sql.SQLException;
import java.util.List;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.jdbc.mapper.BeanMappingRowMapper;
import net.hasor.dbvisitor.jdbc.mapper.SingleColumnRowMapper;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserBasicDTO;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import net.hasor.dbvisitor.test.oneapi.model.annotation.IgnoredEmailUser;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Bean 结果集映射测试
 * 验证 JDBC 结果集到 Java Bean 及简单类型的映射
 */
public class BeanResultMappingTest extends AbstractOneApiTest {

    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() throws Exception {
        super.setup();
        jdbcTemplate = new JdbcTemplate(dataSource);

        // 插入测试数据
        jdbcTemplate.executeUpdate(//
                "INSERT INTO user_info (id, name, age, email) VALUES (?, ?, ?, ?)",//
                new Object[] { 97001, "BeanMap1", 21, "bean1@test.com" });
        jdbcTemplate.executeUpdate(//
                "INSERT INTO user_info (id, name, age, email) VALUES (?, ?, ?, ?)",//
                new Object[] { 97002, "BeanMap2", 22, "bean2@test.com" });
        jdbcTemplate.executeUpdate(//
                "INSERT INTO user_info (id, name, age, email) VALUES (?, ?, ?, ?)",//
                new Object[] { 97003, "BeanMap3", null, null }); // 测试 NULL 值
    }

    /**
     * 测试 queryForObject -> Bean
     * 自动使用 BeanPropertyRowMapper
     */
    @Test
    public void testQueryForObject_Bean() throws SQLException {
        String sql = "SELECT * FROM user_info WHERE id = ?";
        UserInfo user = jdbcTemplate.queryForObject(sql, new Object[] { 97001 }, UserInfo.class);

        assertNotNull(user);
        assertEquals(Integer.valueOf(97001), user.getId());
        assertEquals("BeanMap1", user.getName());
        assertEquals(Integer.valueOf(21), user.getAge());
        assertEquals("bean1@test.com", user.getEmail());
    }

    /**
     * 测试 queryForList -> Bean List
     */
    @Test
    public void testQueryForList_Bean() throws SQLException {
        String sql = "SELECT * FROM user_info WHERE name LIKE 'BeanMap%' ORDER BY id ASC";
        List<UserInfo> users = jdbcTemplate.queryForList(sql, UserInfo.class);

        assertNotNull(users);
        assertTrue(users.size() >= 3);

        UserInfo u1 = users.stream().filter(u -> u.getId().equals(97001)).findFirst().orElse(null);
        assertNotNull(u1);
        assertEquals("BeanMap1", u1.getName());

        UserInfo u3 = users.stream().filter(u -> u.getId().equals(97003)).findFirst().orElse(null);
        assertNotNull(u3);
        assertNull(u3.getAge()); // 验证 Null 映射
        assertNull(u3.getEmail());
    }

    /**
     * 测试 queryForList -> DTO (部分字段映射)
     */
    @Test
    public void testQueryForList_DTO() throws SQLException {
        String sql = "SELECT name, age FROM user_info WHERE id = ?";
        List<UserBasicDTO> users = jdbcTemplate.queryForList(sql, new Object[] { 97001 }, UserBasicDTO.class);

        assertNotNull(users);
        assertEquals(1, users.size());

        UserBasicDTO dto = users.get(0);
        assertEquals("BeanMap1", dto.getName());
        assertEquals(Integer.valueOf(21), dto.getAge());
    }

    /**
     * 测试显示使用 BeanMappingRowMapper
     */
    @Test
    public void testBeanMappingRowMapper_Explicit() throws SQLException {
        String sql = "SELECT * FROM user_info WHERE id = ?";
        BeanMappingRowMapper<UserInfo> rowMapper = new BeanMappingRowMapper<>(UserInfo.class);

        UserInfo user = jdbcTemplate.queryForObject(sql, new Object[] { 97002 }, rowMapper);

        assertNotNull(user);
        assertEquals("BeanMap2", user.getName());
    }

    /**
     * 测试单列映射 -> String
     */
    @Test
    public void testSingleColumn_String() throws SQLException {
        String sql = "SELECT name FROM user_info WHERE id = ?";
        String name = jdbcTemplate.queryForObject(sql, new Object[] { 97001 }, String.class);

        assertEquals("BeanMap1", name);
    }

    /**
     * 测试单列映射 -> Integer
     */
    @Test
    public void testSingleColumn_Integer() throws SQLException {
        String sql = "SELECT age FROM user_info WHERE id = ?";
        Integer age = jdbcTemplate.queryForObject(sql, new Object[] { 97001 }, Integer.class);

        assertEquals(Integer.valueOf(21), age);
    }

    /**
     * 测试单列映射 -> Count (Long)
     */
    @Test
    public void testSingleColumn_Count() throws SQLException {
        String sql = "SELECT count(1) FROM user_info WHERE name LIKE 'BeanMap%'";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);

        assertTrue(count >= 3);
    }

    /**
     * 测试 SingleColumnRowMapper 显式使用
     */
    @Test
    public void testSingleColumnRowMapper_Explicit() throws SQLException {
        String sql = "SELECT name FROM user_info WHERE name LIKE 'BeanMap%' ORDER BY id";
        SingleColumnRowMapper<String> mapper = new SingleColumnRowMapper<>(String.class);

        List<String> names = jdbcTemplate.queryForList(sql, mapper);

        assertNotNull(names);
        assertTrue(names.contains("BeanMap1"));
        assertTrue(names.contains("BeanMap2"));
    }

    /**
     * 测试 @Ignore 注解在结果集映射中的表现。
     * 只有当使用 BeanMappingRowMapper 且字段标记为 @Ignore 时，该字段应被忽略不进行映射（保持为 null）。
     */
    @Test
    public void testIgnoreAnnotation_ResultMapping() throws SQLException {
        // 1. 插入一条完整数据（包含 email）
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email) VALUES (?, ?, ?, ?)", new Object[] { 97004, "IgnoreMap", 30, "should_be_ignored@test.com" });

        // 2. 使用 IgnoredEmailUser (email 字段有 @Ignore) 进行查询
        String sql = "SELECT * FROM user_info WHERE id = ?";
        IgnoredEmailUser user = jdbcTemplate.queryForObject(sql, new Object[] { 97004 }, IgnoredEmailUser.class);

        assertNotNull(user);
        assertEquals("IgnoreMap", user.getName());
        assertEquals(Integer.valueOf(30), user.getAge());

        // 3. 验证 email 字段是否被忽略（应为 null）
        assertNull("Email field marked with @Ignore should not be mapped from ResultSet", user.getEmail());
    }
}
