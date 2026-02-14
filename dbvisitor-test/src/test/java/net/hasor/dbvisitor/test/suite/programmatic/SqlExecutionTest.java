package net.hasor.dbvisitor.test.suite.programmatic;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * SQL 执行方式测试
 * 充分测试可以通过哪些方式执行 SQL：
 * - executeUpdate: INSERT/UPDATE/DELETE/DDL
 * - execute: 通用执行
 * - queryForXxx: SELECT 查询
 * - executeBatch: 批量操作
 * - 事务控制
 */
public class SqlExecutionTest extends AbstractOneApiTest {

    @Override
    protected void cleanTestData() {
        super.cleanTestData();
    }

    /**
     * 测试 CRUD - executeUpdate 执行方式
     * INSERT, UPDATE, DELETE 都使用 executeUpdate
     */
    @Test
    public void testCRUD_ExecuteUpdate() throws SQLException {
        // 1. INSERT - 使用 executeUpdate
        String insertSql = "INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)";
        int inserted = jdbcTemplate.executeUpdate(insertSql, new Object[] { "Alice", 25, "alice@test.com", new Date() });
        assertEquals(1, inserted);

        // 2. SELECT - 使用 queryForObject
        String selectSql = "SELECT * FROM user_info WHERE name = ?";
        UserInfo loaded = jdbcTemplate.queryForObject(selectSql, new Object[] { "Alice" }, UserInfo.class);
        assertNotNull(loaded);
        assertEquals("Alice", loaded.getName());
        Integer userId = loaded.getId();

        // 3. UPDATE - 使用 executeUpdate
        String updateSql = "UPDATE user_info SET age = ? WHERE id = ?";
        int updated = jdbcTemplate.executeUpdate(updateSql, new Object[] { 26, userId });
        assertEquals(1, updated);

        // 验证更新结果
        UserInfo afterUpdate = jdbcTemplate.queryForObject("SELECT * FROM user_info WHERE id = ?", new Object[] { userId }, UserInfo.class);
        assertEquals(Integer.valueOf(26), afterUpdate.getAge());

        // 4. DELETE - 使用 executeUpdate
        String deleteSql = "DELETE FROM user_info WHERE id = ?";
        int deleted = jdbcTemplate.executeUpdate(deleteSql, new Object[] { userId });
        assertEquals(1, deleted);

        // 验证删除结果
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE id = ?", new Object[] { userId }, Long.class);
        assertEquals(Long.valueOf(0), count);
    }

    /**
     * 测试 CRUD - 命名参数执行方式
     * 使用 Map 传递命名参数
     */
    @Test
    public void testCRUD_NamedParameters() throws SQLException {
        // INSERT - 命名参数
        String insertSql = "INSERT INTO user_info (name, age, email, create_time) VALUES (:name, :age, :email, :createTime)";
        Map<String, Object> insertParams = new HashMap<>();
        insertParams.put("name", "Bob");
        insertParams.put("age", 30);
        insertParams.put("email", "bob@test.com");
        insertParams.put("createTime", new Date());
        int result = jdbcTemplate.executeUpdate(insertSql, insertParams);
        assertEquals(1, result);

        // SELECT - 命名参数
        String selectSql = "SELECT * FROM user_info WHERE name = :name AND age > :age";
        Map<String, Object> selectParams = new HashMap<>();
        selectParams.put("name", "Bob");
        selectParams.put("age", 20);
        List<UserInfo> list = jdbcTemplate.queryForList(selectSql, selectParams, UserInfo.class);
        assertEquals(1, list.size());
        Integer userId = list.get(0).getId();

        // UPDATE - 命名参数
        String updateSql = "UPDATE user_info SET email = :email WHERE id = :id";
        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("email", "bob_updated@test.com");
        updateParams.put("id", userId);
        jdbcTemplate.executeUpdate(updateSql, updateParams);

        // DELETE - 命名参数
        String deleteSql = "DELETE FROM user_info WHERE id = :id";
        Map<String, Object> deleteParams = new HashMap<>();
        deleteParams.put("id", userId);
        jdbcTemplate.executeUpdate(deleteSql, deleteParams);
    }

    /**
     * 测试批量 INSERT - executeBatch
     */
    @Test
    public void testBatch_Insert() throws SQLException {
        String insertSql = "INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)";
        Object[][] batchInsert = new Object[10][];
        for (int i = 0; i < 10; i++) {
            batchInsert[i] = new Object[] { "BatchUser" + i, 20 + i, "batch" + i + "@test.com", new Date() };
        }

        int[] insertResults = jdbcTemplate.executeBatch(insertSql, batchInsert);
        assertEquals(10, insertResults.length);

        // 验证插入结果
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE name LIKE 'BatchUser%'", Long.class);
        assertEquals(Long.valueOf(10), count);
    }

    /**
     * 测试批量 UPDATE - executeBatch
     */
    @Test
    public void testBatch_Update() throws SQLException {
        // 准备测试数据
        String insertSql = "INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)";
        for (int i = 0; i < 5; i++) {
            jdbcTemplate.executeUpdate(insertSql, new Object[] { "UpdateUser" + i, 20 + i, "update" + i + "@test.com", new Date() });
        }

        // 批量 UPDATE
        List<UserInfo> users = jdbcTemplate.queryForList("SELECT * FROM user_info WHERE name LIKE 'UpdateUser%'", UserInfo.class);
        String updateSql = "UPDATE user_info SET age = ? WHERE id = ?";
        Object[][] batchUpdate = new Object[users.size()][];
        for (int i = 0; i < users.size(); i++) {
            batchUpdate[i] = new Object[] { 100 + i, users.get(i).getId() };
        }

        int[] updateResults = jdbcTemplate.executeBatch(updateSql, batchUpdate);
        assertEquals(users.size(), updateResults.length);

        // 验证更新结果
        UserInfo updated = jdbcTemplate.queryForObject("SELECT * FROM user_info WHERE id = ?", new Object[] { users.get(0).getId() }, UserInfo.class);
        assertEquals(Integer.valueOf(100), updated.getAge());
    }

    /**
     * 测试批量 DELETE - executeBatch
     */
    @Test
    public void testBatch_Delete() throws SQLException {
        // 准备测试数据
        String insertSql = "INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)";
        for (int i = 0; i < 5; i++) {
            jdbcTemplate.executeUpdate(insertSql, new Object[] { "DeleteUser" + i, 20 + i, "delete" + i + "@test.com", new Date() });
        }

        // 批量 DELETE
        List<UserInfo> users = jdbcTemplate.queryForList("SELECT * FROM user_info WHERE name LIKE 'DeleteUser%'", UserInfo.class);
        String deleteSql = "DELETE FROM user_info WHERE id = ?";
        Object[][] batchDelete = new Object[users.size()][];
        for (int i = 0; i < users.size(); i++) {
            batchDelete[i] = new Object[] { users.get(i).getId() };
        }

        int[] deleteResults = jdbcTemplate.executeBatch(deleteSql, batchDelete);
        assertEquals(users.size(), deleteResults.length);

        // 验证删除结果
        Long afterDelete = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE name LIKE 'DeleteUser%'", Long.class);
        assertEquals(Long.valueOf(0), afterDelete);
    }

    /**
     * 测试 UPSERT - 更新已存在记录（PostgreSQL: INSERT ... ON CONFLICT）
     */
    @Test
    public void testMerge_UpdateExisting() throws SQLException {
        // 先插入一条记录
        String insertSql = "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 9001, "OriginalUser", 25, "original@test.com", new Date() });

        // UPSERT 更新已存在的记录（PostgreSQL 语法）
        String upsertSql = "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?) " + "ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, age = EXCLUDED.age, email = EXCLUDED.email";
        int upsertResult = jdbcTemplate.executeUpdate(upsertSql, new Object[] { 9001, "UpdatedUser", 26, "updated@test.com", new Date() });
        assertTrue(upsertResult > 0);

        // 验证更新结果
        UserInfo updated = jdbcTemplate.queryForObject("SELECT * FROM user_info WHERE id = ?", new Object[] { 9001 }, UserInfo.class);
        assertEquals("UpdatedUser", updated.getName());
        assertEquals(Integer.valueOf(26), updated.getAge());
    }

    /**
     * 测试 UPSERT - 插入新记录（PostgreSQL: INSERT ... ON CONFLICT）
     */
    @Test
    public void testMerge_InsertNew() throws SQLException {
        // UPSERT 插入新记录（PostgreSQL 语法）
        String upsertSql = "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?) " + "ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, age = EXCLUDED.age, email = EXCLUDED.email";
        int upsertResult = jdbcTemplate.executeUpdate(upsertSql, new Object[] { 9002, "NewUser", 30, "new@test.com", new Date() });
        assertTrue(upsertResult > 0);

        // 验证插入结果
        UserInfo inserted = jdbcTemplate.queryForObject("SELECT * FROM user_info WHERE id = ?", new Object[] { 9002 }, UserInfo.class);
        assertNotNull(inserted);
        assertEquals("NewUser", inserted.getName());
        assertEquals(Integer.valueOf(30), inserted.getAge());
    }

    /**
     * 测试 UPSERT - 批量操作（PostgreSQL: INSERT ... ON CONFLICT）
     */
    @Test
    public void testMerge_Batch() throws SQLException {
        // 先插入部分记录
        String insertSql = "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 9001, "User1", 21, "user1@test.com", new Date() });
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 9002, "User2", 22, "user2@test.com", new Date() });

        // 批量 UPSERT (更新已存在的 9001-9002，插入新的 9003-9005) - PostgreSQL 语法
        String upsertSql = "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?) " + "ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, age = EXCLUDED.age, email = EXCLUDED.email";
        Object[][] batchUpsert = new Object[5][];
        for (int i = 9001; i <= 9005; i++) {
            batchUpsert[i - 9001] = new Object[] { i, "MergeUser" + i, 20 + i, "merge" + i + "@test.com", new Date() };
        }

        int[] upsertResults = jdbcTemplate.executeBatch(upsertSql, batchUpsert);
        assertEquals(5, upsertResults.length);

        // 验证结果：应该有 5 条记录
        Long upsertCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE id BETWEEN 9001 AND 9005", Long.class);
        assertEquals(Long.valueOf(5), upsertCount);

        // 验证更新的记录
        UserInfo updated = jdbcTemplate.queryForObject("SELECT * FROM user_info WHERE id = ?", new Object[] { 9001 }, UserInfo.class);
        assertEquals("MergeUser9001", updated.getName());
    }

    /**
     * 测试查询 - queryForObject 查询单个对象
     */
    @Test
    public void testQuery_Object() throws SQLException {
        // 准备测试数据
        String insertSql = "INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { "QueryUser1", 21, "query1@test.com", new Date() });

        // queryForObject - 查询单个对象
        UserInfo user = jdbcTemplate.queryForObject("SELECT * FROM user_info WHERE name = ?", new Object[] { "QueryUser1" }, UserInfo.class);
        assertNotNull(user);
        assertEquals("QueryUser1", user.getName());
        assertEquals(Integer.valueOf(21), user.getAge());
    }

    /**
     * 测试查询 - queryForList 查询列表
     */
    @Test
    public void testQuery_List() throws SQLException {
        // 准备测试数据
        String insertSql = "INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)";
        for (int i = 1; i <= 5; i++) {
            jdbcTemplate.executeUpdate(insertSql, new Object[] { "ListUser" + i, 20 + i, "list" + i + "@test.com", new Date() });
        }

        // queryForList - 查询列表
        List<UserInfo> list = jdbcTemplate.queryForList("SELECT * FROM user_info WHERE name LIKE ?", new Object[] { "ListUser%" }, UserInfo.class);
        assertEquals(5, list.size());
        assertEquals("ListUser1", list.get(0).getName());
    }

    /**
     * 测试查询 - queryForMap 查询为 Map
     */
    @Test
    public void testQuery_Map() throws SQLException {
        // 准备测试数据
        String insertSql = "INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { "MapUser", 25, "map@test.com", new Date() });

        // queryForMap - 查询为 Map
        Map<String, Object> map = jdbcTemplate.queryForMap("SELECT * FROM user_info WHERE name = ?", new Object[] { "MapUser" });
        assertNotNull(map);
        assertEquals("MapUser", map.get("name"));
        assertEquals(25, ((Number) map.get("age")).intValue());
    }

    /**
     * 测试查询 - 聚合函数（COUNT, MAX, MIN）
     */
    @Test
    public void testQuery_Aggregate() throws SQLException {
        // 准备测试数据
        String insertSql = "INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)";
        for (int i = 1; i <= 5; i++) {
            jdbcTemplate.executeUpdate(insertSql, new Object[] { "AggUser" + i, 20 + i, "agg" + i + "@test.com", new Date() });
        }

        // COUNT
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE name LIKE ?", new Object[] { "AggUser%" }, Long.class);
        assertEquals(Long.valueOf(5), count);

        // MAX
        Integer maxAge = jdbcTemplate.queryForObject("SELECT MAX(age) FROM user_info WHERE name LIKE ?", new Object[] { "AggUser%" }, Integer.class);
        assertEquals(Integer.valueOf(25), maxAge);

        // MIN
        Integer minAge = jdbcTemplate.queryForObject("SELECT MIN(age) FROM user_info WHERE name LIKE ?", new Object[] { "AggUser%" }, Integer.class);
        assertEquals(Integer.valueOf(21), minAge);
    }

    /**
     * 测试查询 - queryForList 查询基本类型列表
     */
    @Test
    public void testQuery_PrimitiveList() throws SQLException {
        // 准备测试数据
        String insertSql = "INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)";
        for (int i = 1; i <= 3; i++) {
            jdbcTemplate.executeUpdate(insertSql, new Object[] { "PrimUser" + i, 30 + i, "prim" + i + "@test.com", new Date() });
        }

        // 查询基本类型列表 - 字符串列表
        List<String> names = jdbcTemplate.queryForList("SELECT name FROM user_info WHERE name LIKE ? ORDER BY name", new Object[] { "PrimUser%" }, String.class);
        assertEquals(3, names.size());
        assertEquals("PrimUser1", names.get(0));
        assertEquals("PrimUser2", names.get(1));
        assertEquals("PrimUser3", names.get(2));

        // 查询基本类型列表 - 整数列表
        List<Integer> ages = jdbcTemplate.queryForList("SELECT age FROM user_info WHERE name LIKE ? ORDER BY age", new Object[] { "PrimUser%" }, Integer.class);
        assertEquals(3, ages.size());
        assertEquals(Integer.valueOf(31), ages.get(0));
    }

    /**
     * 测试查询 - queryForPairs 查询键值对
     * 将查询结果转换为 Map，第一列作为 key，第二列作为 value
     */
    @Test
    public void testQuery_Pairs() throws SQLException {
        // 准备测试数据
        String insertSql = "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 1001, "PairUser1", 21, "pair1@test.com", new Date() });
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 1002, "PairUser2", 22, "pair2@test.com", new Date() });
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 1003, "PairUser3", 23, "pair3@test.com", new Date() });

        // 1. 无参数 - id -> name
        Map<Integer, String> idToName = jdbcTemplate.queryForPairs("SELECT id, name FROM user_info WHERE id BETWEEN 1001 AND 1003 ORDER BY id", Integer.class, String.class);
        assertEquals(3, idToName.size());
        assertEquals("PairUser1", idToName.get(1001));
        assertEquals("PairUser2", idToName.get(1002));
        assertEquals("PairUser3", idToName.get(1003));

        // 2. 位置参数 - name -> age
        Map<String, Integer> nameToAge = jdbcTemplate.queryForPairs("SELECT name, age FROM user_info WHERE id >= ? AND id <= ?", String.class, Integer.class, new Object[] { 1001, 1003 });
        assertEquals(3, nameToAge.size());
        assertEquals(Integer.valueOf(21), nameToAge.get("PairUser1"));
        assertEquals(Integer.valueOf(22), nameToAge.get("PairUser2"));
        assertEquals(Integer.valueOf(23), nameToAge.get("PairUser3"));

        // 3. 命名参数 - id -> email
        Map<String, Object> params = new HashMap<>();
        params.put("minId", 1001);
        params.put("maxId", 1003);
        Map<Integer, String> idToEmail = jdbcTemplate.queryForPairs("SELECT id, email FROM user_info WHERE id >= :minId AND id <= :maxId ORDER BY id", Integer.class, String.class, params);
        assertEquals(3, idToEmail.size());
        assertEquals("pair1@test.com", idToEmail.get(1001));
        assertEquals("pair2@test.com", idToEmail.get(1002));
        assertEquals("pair3@test.com", idToEmail.get(1003));

        // 4. 复杂类型 - Long -> Date
        Map<Long, Date> idToDate = jdbcTemplate.queryForPairs("SELECT CAST(id AS BIGINT), create_time FROM user_info WHERE id BETWEEN 1001 AND 1002", Long.class, Date.class);
        assertEquals(2, idToDate.size());
        assertNotNull(idToDate.get(1001L));
        assertNotNull(idToDate.get(1002L));
    }

    /**
     * 测试查询 - queryForLong 查询 Long 值
     */
    @Test
    public void testQuery_Long() throws SQLException {
        // 准备测试数据
        String insertSql = "INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)";
        for (int i = 1; i <= 5; i++) {
            jdbcTemplate.executeUpdate(insertSql, new Object[] { "LongUser" + i, 20 + i, "long" + i + "@test.com", new Date() });
        }

        // 1. 无参数 - COUNT
        Long count = jdbcTemplate.queryForLong("SELECT COUNT(*) FROM user_info WHERE name LIKE 'LongUser%'");
        assertEquals(Long.valueOf(5), count);

        // 2. 位置参数 - MAX(age)
        Long maxAge = jdbcTemplate.queryForLong("SELECT MAX(age) FROM user_info WHERE name LIKE ?", new Object[] { "LongUser%" });
        assertEquals(Long.valueOf(25), maxAge);

        // 3. 命名参数 - SUM(age)
        Map<String, Object> params = new HashMap<>();
        params.put("pattern", "LongUser%");
        Long sumAge = jdbcTemplate.queryForLong("SELECT SUM(age) FROM user_info WHERE name LIKE :pattern", params);
        assertEquals(Long.valueOf(115), sumAge); // 21+22+23+24+25 = 115
    }

    /**
     * 测试查询 - queryForInt 查询 Integer 值
     */
    @Test
    public void testQuery_Int() throws SQLException {
        // 准备测试数据
        String insertSql = "INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)";
        for (int i = 1; i <= 3; i++) {
            jdbcTemplate.executeUpdate(insertSql, new Object[] { "IntUser" + i, 30 + i, "int" + i + "@test.com", new Date() });
        }

        // 1. 无参数 - COUNT
        Integer count = jdbcTemplate.queryForInt("SELECT COUNT(*) FROM user_info WHERE name LIKE 'IntUser%'");
        assertEquals(Integer.valueOf(3), count);

        // 2. 位置参数 - MIN(age)
        Integer minAge = jdbcTemplate.queryForInt("SELECT MIN(age) FROM user_info WHERE name LIKE ?", new Object[] { "IntUser%" });
        assertEquals(Integer.valueOf(31), minAge);

        // 3. 命名参数 - MAX(age)
        Map<String, Object> params = new HashMap<>();
        params.put("pattern", "IntUser%");
        Integer maxAge = jdbcTemplate.queryForInt("SELECT MAX(age) FROM user_info WHERE name LIKE :pattern", params);
        assertEquals(Integer.valueOf(33), maxAge);

        // 4. 查询单个 age 值
        Integer age = jdbcTemplate.queryForInt("SELECT age FROM user_info WHERE name = ?", new Object[] { "IntUser2" });
        assertEquals(Integer.valueOf(32), age);
    }

    /**
     * 测试查询 - queryForString 查询 String 值
     */
    @Test
    public void testQuery_String() throws SQLException {
        // 准备测试数据
        String insertSql = "INSERT INTO user_info (name, age, email, create_time) VALUES (?, ?, ?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { "StringUser1", 25, "string1@test.com", new Date() });
        jdbcTemplate.executeUpdate(insertSql, new Object[] { "StringUser2", 26, "string2@test.com", new Date() });
        jdbcTemplate.executeUpdate(insertSql, new Object[] { "StringUser3", 27, "string3@test.com", new Date() });

        // 1. 无参数 - 查询单个 name
        String name = jdbcTemplate.queryForString("SELECT name FROM user_info WHERE name = 'StringUser1'");
        assertEquals("StringUser1", name);

        // 2. 位置参数 - 查询 email
        String email = jdbcTemplate.queryForString("SELECT email FROM user_info WHERE name = ?", new Object[] { "StringUser2" });
        assertEquals("string2@test.com", email);

        // 3. 命名参数 - 查询 name
        Map<String, Object> params = new HashMap<>();
        params.put("name", "StringUser3");
        String result = jdbcTemplate.queryForString("SELECT name FROM user_info WHERE name = :name", params);
        assertEquals("StringUser3", result);

        // 4. 查询 MAX(name) - 字符串最大值
        String maxName = jdbcTemplate.queryForString("SELECT MAX(name) FROM user_info WHERE name LIKE ?", new Object[] { "StringUser%" });
        assertEquals("StringUser3", maxName);
    }

    /**
     * 测试 DDL - 表操作
     * CREATE TABLE, ALTER TABLE, DROP TABLE
     */
    @Test
    public void testDDL_TableOperations() throws SQLException {
        // 1. CREATE TABLE
        String createTableSql = "CREATE TABLE test_temp_table (" + "id INT PRIMARY KEY, " + "name VARCHAR(100), " + "age INT, " + "create_time TIMESTAMP" + ")";
        jdbcTemplate.executeUpdate(createTableSql);

        // 验证表已创建 - 尝试插入数据
        String insertSql = "INSERT INTO test_temp_table (id, name, age, create_time) VALUES (?, ?, ?, ?)";
        int inserted = jdbcTemplate.executeUpdate(insertSql, new Object[] { 1, "TestUser", 25, new Date() });
        assertEquals(1, inserted);

        // 2. ALTER TABLE - 添加列
        String alterAddColumnSql = "ALTER TABLE test_temp_table ADD COLUMN email VARCHAR(100)";
        jdbcTemplate.executeUpdate(alterAddColumnSql);

        // 验证新列可用
        String updateEmailSql = "UPDATE test_temp_table SET email = ? WHERE id = ?";
        jdbcTemplate.executeUpdate(updateEmailSql, new Object[] { "test@test.com", 1 });

        Map<String, Object> result = jdbcTemplate.queryForMap("SELECT * FROM test_temp_table WHERE id = ?", new Object[] { 1 });
        assertEquals("test@test.com", result.get("email"));

        // 3. DROP TABLE
        String dropTableSql = "DROP TABLE test_temp_table";
        jdbcTemplate.executeUpdate(dropTableSql);

        // 验证表已删除 - 查询应失败
        try {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_temp_table", Long.class);
            fail("Table should be dropped");
        } catch (SQLException e) {
            // Expected
        }
    }
}
