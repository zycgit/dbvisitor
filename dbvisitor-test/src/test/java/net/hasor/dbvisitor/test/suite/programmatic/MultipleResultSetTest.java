package net.hasor.dbvisitor.test.suite.programmatic;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * 多 ResultSet 处理测试 (Multiple ResultSet Tests)
 * <p>测试 JdbcTemplate.multipleExecute() 方法处理多个结果集的能力，
 * 验证 AbstractMultipleResultSetExtractor 及其子类的功能。</p>
 * <p>支持通过分号分隔的多条 SQL 语句执行，适用于 PostgreSQL、MySQL 等数据库。</p>
 * <p>使用 @{resultSet} 和 @{resultUpdate} 规则可以为结果集/更新计数指定名称。</p>
 * 参考：
 * - CallableMultipleResultSetExtractorTest (使用存储过程生成多结果集)
 * - MultipleExtractorTest (MySQL 环境下的完整测试)
 * - dbvisitor-doc/docs/guides/core/jdbc/multi.md
 * @see net.hasor.dbvisitor.jdbc.extractor.AbstractMultipleResultSetExtractor
 */
public class MultipleResultSetTest extends AbstractOneApiTest {

    @Override
    protected void cleanTestData() {
        super.cleanTestData();
    }

    /**
     * 测试多条 SELECT 语句（多结果集）
     * 验证通过分号分隔的多条 SQL 能返回多个结果集
     */
    @Test
    public void testMultipleExecute_MultipleStatements() throws SQLException {
        // 准备测试数据
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email) VALUES (?, ?, ?)",//
                new Object[] { "TestUser1", 25, "test1@example.com" });
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email) VALUES (?, ?, ?)",//
                new Object[] { "TestUser2", 30, "test2@example.com" });
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email) VALUES (?, ?, ?)",//
                new Object[] { "TestUser3", 35, "test3@example.com" });

        // 多条 SELECT 语句，分号分隔
        String sql = "SELECT * FROM user_info WHERE age < 28;\n" +//
                "SELECT * FROM user_info WHERE age >= 28;\n";

        Map<String, Object> resultMap = jdbcTemplate.multipleExecute(sql);

        // 应返回 2 个结果集
        List<Object> resultList = new ArrayList<>(resultMap.values());
        assertEquals(2, resultList.size());

        // 验证第一个结果集（age < 28）
        List<?> result1 = (List<?>) resultList.get(0);
        assertEquals(1, result1.size());

        // 验证第二个结果集（age >= 28）
        List<?> result2 = (List<?>) resultList.get(1);
        assertEquals(2, result2.size());
    }

    /**
     * 测试使用位置参数的多结果集查询
     * 验证位置参数在多条 SQL 中的正确传递
     */
    @Test
    public void testMultipleExecute_WithPositionalArgs() throws SQLException {
        // 准备测试数据
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email) VALUES (?, ?, ?)", new Object[] { "Alice", 20, "alice@test.com" });
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email) VALUES (?, ?, ?)", new Object[] { "Bob", 30, "bob@test.com" });
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email) VALUES (?, ?, ?)", new Object[] { "Charlie", 40, "charlie@test.com" });

        // 多条 SQL，每条使用位置参数
        String sql = "SELECT * FROM user_info WHERE age > ?;\n" +//
                "SELECT * FROM user_info WHERE name LIKE ?;\n";
        Map<String, Object> resultMap = jdbcTemplate.multipleExecute(sql, new Object[] { 25, "Bob%" });

        // 应返回 2 个结果集
        List<Object> resultList = new ArrayList<>(resultMap.values());
        assertEquals(2, resultList.size());

        // 验证第一个结果集（age > 25）包含数据
        List<?> result1 = (List<?>) resultList.get(0);
        assertTrue(result1.size() > 0);

        // 验证第二个结果集（name LIKE 'Bob%'）包含数据
        List<?> result2 = (List<?>) resultList.get(1);
        assertTrue(result2.size() > 0);
        Map<?, ?> row2 = (Map<?, ?>) result2.get(0);
        Object name2 = row2.get("NAME") != null ? row2.get("NAME") : row2.get("name");
        assertEquals("Bob", name2);
    }

    /**
     * 测试使用命名参数的多结果集查询
     * 验证命名参数在多条 SQL 中的正确传递
     */
    @Test
    public void testMultipleExecute_WithNamedArgs() throws SQLException {
        // 准备测试数据
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email) VALUES (?, ?, ?)", new Object[] { "David", 22, "david@test.com" });
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email) VALUES (?, ?, ?)", new Object[] { "Emma", 28, "emma@test.com" });
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email) VALUES (?, ?, ?)", new Object[] { "Frank", 32, "frank@test.com" });

        // 多条 SQL，使用命名参数
        String sql = "SELECT * FROM user_info WHERE age < :ageLimit;\n" +//
                "SELECT * FROM user_info WHERE age >= :ageLimit;\n";
        Map<String, Object> params = CollectionUtils.asMap("ageLimit", 26);

        Map<String, Object> resultMap = jdbcTemplate.multipleExecute(sql, params);

        // 应返回 2 个结果集
        List<Object> resultList = new ArrayList<>(resultMap.values());
        assertEquals(2, resultList.size());

        // 验证第一个结果集（age < 26）
        List<?> result1 = (List<?>) resultList.get(0);
        assertEquals(1, result1.size());
        Map<?, ?> row1 = (Map<?, ?>) result1.get(0);
        Object name1 = row1.get("NAME") != null ? row1.get("NAME") : row1.get("name");
        assertEquals("David", name1);

        // 验证第二个结果集（age >= 26）
        List<?> result2 = (List<?>) resultList.get(1);
        assertEquals(2, result2.size());
    }

    /**
     * 测试多结果集中包含空结果集的情况
     * 验证当某些结果集为空时，multipleExecute 仍能正确返回所有结果集
     */
    @Test
    public void testMultipleExecute_EmptyResult() throws SQLException {
        // 准备少量测试数据
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email) VALUES (?, ?, ?)", new Object[] { "Young", 18, "young@test.com" });

        // 第一条 SQL 有结果，第二条 SQL 无结果
        String sql = "SELECT * FROM user_info WHERE age < 20;\n" +//
                "SELECT * FROM user_info WHERE age > 1000;\n";

        Map<String, Object> resultMap = jdbcTemplate.multipleExecute(sql);

        // 应返回 2 个结果集
        List<Object> resultList = new ArrayList<>(resultMap.values());
        assertEquals(2, resultList.size());

        // 验证第一个结果集有数据
        List<?> result1 = (List<?>) resultList.get(0);
        assertEquals(1, result1.size());

        // 验证第二个结果集为空
        List<?> result2 = (List<?>) resultList.get(1);
        assertEquals(0, result2.size());
    }

    // ========== 多结果集测试 ==========

    /**
     * 测试无参数的多结果集查询
     * 通过分号分隔执行多条 SQL 语句
     */
    @Test
    public void testMultipleExecute_NoArgs() throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email) VALUES (?, ?, ?)", new Object[] { "User1", 25, "user1@test.com" });
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email) VALUES (?, ?, ?)", new Object[] { "User2", 30, "user2@test.com" });
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email) VALUES (?, ?, ?)", new Object[] { "User3", 35, "user3@test.com" });

        // 分号分隔的多语句
        String multipleSql = "SELECT * FROM user_info WHERE age < 30;\n" + //
                "SELECT * FROM user_info WHERE age >= 30;\n";

        Map<String, Object> resultMap = jdbcTemplate.multipleExecute(multipleSql);
        List<Object> resultList = new ArrayList<>(resultMap.values());

        // 应返回 2 个结果集
        assertEquals(2, resultList.size());

        // 验证第一个结果集
        List<?> result1 = (List<?>) resultList.get(0);
        assertEquals(1, result1.size());

        // 验证第二个结果集
        List<?> result2 = (List<?>) resultList.get(1);
        assertEquals(2, result2.size());
    }

    /**
     * 测试使用 @{resultSet} 规则指定结果集名称
     * 这是 dbVisitor 的语法功能，不依赖特定数据库
     */
    @Test
    public void testMultipleExecute_ResultSetRule() throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email) VALUES (?, ?, ?)", new Object[] { "Jack", 60, "jack@test.com" });
        jdbcTemplate.executeUpdate("INSERT INTO user_info (name, age, email) VALUES (?, ?, ?)", new Object[] { "Kate", 65, "kate@test.com" });

        // 使用 @{resultSet} 规则指定结果名称和类型
        String multipleSql = "SELECT * FROM user_info WHERE age < 63; @{resultSet,name=youngUsers,javaType=net.hasor.dbvisitor.test.model.UserInfo}\n" +//
                "SELECT * FROM user_info WHERE age >= 63; @{resultSet,name=seniorUsers,javaType=net.hasor.dbvisitor.test.model.UserInfo}\n";

        Map<String, Object> resultMap = jdbcTemplate.multipleExecute(multipleSql);

        // 验证结果按指定名称存储
        assertTrue(resultMap.containsKey("youngUsers"));
        assertTrue(resultMap.containsKey("seniorUsers"));

        List<?> youngUsers = (List<?>) resultMap.get("youngUsers");
        assertEquals(1, youngUsers.size());
        assertTrue(youngUsers.get(0) instanceof UserInfo);
        assertEquals("Jack", ((UserInfo) youngUsers.get(0)).getName());

        List<?> seniorUsers = (List<?>) resultMap.get("seniorUsers");
        assertEquals(1, seniorUsers.size());
        assertTrue(seniorUsers.get(0) instanceof UserInfo);
        assertEquals("Kate", ((UserInfo) seniorUsers.get(0)).getName());
    }
}
