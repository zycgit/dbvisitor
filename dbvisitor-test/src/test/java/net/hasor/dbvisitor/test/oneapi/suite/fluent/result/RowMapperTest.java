package net.hasor.dbvisitor.test.oneapi.suite.fluent.result;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * RowMapper Test
 * 验证自定义映射器和类型转换能力
 */
public class RowMapperTest extends AbstractOneApiTest {

    /**
     * 测试自定义 RowMapper
     * 场景: 完全自定义映射逻辑
     */
    @Test
    public void testCustomRowMapper() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(25101, "Custom1", 25))//
                .executeSumResult();

        // 自定义 RowMapper
        RowMapper<UserInfo> customMapper = (rs, rowNum) -> {
            UserInfo u = new UserInfo();
            u.setId(rs.getInt("id"));
            u.setName(rs.getString("name")//
                    .toUpperCase()); // 转大写
            u.setAge(rs.getInt("age") * 2); // 年龄翻倍
            u.setEmail(rs.getString("email"));
            return u;
        };

        UserInfo result = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 25101)//
                .queryForObject(customMapper);

        assertNotNull(result);
        assertEquals("CUSTOM1", result.getName()); // 大写
        assertEquals(Integer.valueOf(50), result.getAge()); // 25 * 2
    }

    /**
     * 测试 Lambda 简化 RowMapper
     * 使用 Lambda 表达式简化映射代码
     */
    @Test
    public void testLambdaRowMapper() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(25201, "Lambda1", 30))//
                .executeSumResult();

        // Lambda 风格的 RowMapper
        RowMapper<String> nameMapper = (rs, rowNum) -> rs.getString("name");

        String name = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 25201)//
                .queryForObject(nameMapper);

        assertEquals("Lambda1", name);
    }

    /**
     * 测试 queryForList 与自定义 RowMapper
     * 批量映射多条记录
     */
    @Test
    public void testQueryForListWithCustomMapper() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(25301, "List1", 20))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(25302, "List2", 25))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(25303, "List3", 30))//
                .executeSumResult();

        // 自定义映射器：只提取 name
        RowMapper<String> nameMapper = (rs, rowNum) -> rs.getString("name");

        List<String> names = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(25301, 25302, 25303))//
                .orderBy("id")//
                .queryForList(nameMapper);

        assertNotNull(names);
        assertEquals(3, names.size());
        assertEquals("List1", names.get(0));
        assertEquals("List2", names.get(1));
        assertEquals("List3", names.get(2));
    }

    /**
     * 测试 queryForObject(Class<?>) 类型转换
     * 场景: 查询单列值为不同类型
     */
    @Test
    public void testQueryForObjectTypeConversion() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(25401, "Type1", 888))//
                .executeSumResult();

        // 查询为 Integer
        Integer ageInt = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 25401)//
                .applySelect("age")//
                .queryForObject(Integer.class);

        assertEquals(Integer.valueOf(888), ageInt);

        // 查询为 Long
        Long ageLong = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 25401)//
                .applySelect("age")//
                .queryForObject(Long.class);

        assertEquals(Long.valueOf(888), ageLong);

        // 查询为 String
        String ageStr = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 25401)//
                .applySelect("age")//
                .queryForObject(String.class);

        assertEquals("888", ageStr);

        // 查询 name 为 String
        String name = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 25401)//
                .applySelect("name")//
                .queryForObject(String.class);

        assertEquals("Type1", name);
    }

    /**
     * 测试 queryForList(Class<?>) 类型转换
     * 批量查询单列值
     */
    @Test
    public void testQueryForListTypeConversion() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(25501, "ListType1", 10))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(25502, "ListType2", 20))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(25503, "ListType3", 30))//
                .executeSumResult();

        // 查询所有 age 为 Integer 列表
        List<Integer> ages = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(25501, 25502, 25503))//
                .applySelect("age")//
                .orderBy("age")//
                .queryForList(Integer.class);

        assertNotNull(ages);
        assertEquals(3, ages.size());
        assertEquals(Integer.valueOf(10), ages.get(0));
        assertEquals(Integer.valueOf(20), ages.get(1));
        assertEquals(Integer.valueOf(30), ages.get(2));

        // 查询所有 name 为 String 列表
        List<String> names = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(25501, 25502, 25503))//
                .applySelect("name")//
                .orderBy("id")//
                .queryForList(String.class);

        assertEquals(3, names.size());
        assertEquals("ListType1", names.get(0));
    }

    /**
     * 测试 Map 结果映射
     * queryForMapList 返回 Map 列表
     */
    @Test
    public void testQueryForMapList() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(25601, "Map1", 25))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(25602, "Map2", 30))//
                .executeSumResult();

        List<Map<String, Object>> result = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(25601, 25602))//
                .orderBy("id")//
                .queryForMapList();

        assertNotNull(result);
        assertEquals(2, result.size());

        Map<String, Object> first = result.get(0);
        assertNotNull(first);
        assertEquals(25601, getVal(first, "id"));
        assertEquals("Map1", getVal(first, "name"));
        assertEquals(25, ((Number) getVal(first, "age")).intValue());
    }

    /**
     * 测试部分字段映射
     * 只查询部分列
     */
    @Test
    public void testPartialFieldMapping() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(25701, "Partial1", 25))//
                .executeSumResult();

        // 只查询 id 和 name
        RowMapper<UserInfo> partialMapper = (rs, rowNum) -> {
            UserInfo u = new UserInfo();
            u.setId(rs.getInt("id"));
            u.setName(rs.getString("name"));
            // age 和 email 不设置
            return u;
        };

        UserInfo result = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 25701)//
                .applySelect("id, name")//
                .queryForObject(partialMapper);

        assertNotNull(result);
        assertEquals(Integer.valueOf(25701), result.getId());
        assertEquals("Partial1", result.getName());
        assertNull("Age should not be mapped", result.getAge());
    }

    /**
     * 测试嵌套对象映射
     * 场景: 映射为包含其他对象的复合对象
     */
    @Test
    public void testNestedObjectMapping() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(25801, "Nested1", 30))//
                .executeSumResult();

        // 自定义 DTO 类
        class UserDTO {
            Integer userId;
            String  displayName;

            public UserDTO(Integer userId, String displayName) {
                this.userId = userId;
                this.displayName = displayName;
            }
        }

        RowMapper<UserDTO> dtoMapper = (rs, rowNum) -> {
            return new UserDTO(rs.getInt("id"), rs.getString("name") + " (" + rs.getInt("age") + ")");
        };

        UserDTO dto = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 25801)//
                .queryForObject(dtoMapper);

        assertNotNull(dto);
        assertEquals(Integer.valueOf(25801), dto.userId);
        assertEquals("Nested1 (30)", dto.displayName);
    }

    /**
     * 测试字段名映射策略
     * 场景: 驼峰命名 vs 下划线命名
     */
    @Test
    public void testFieldNameMappingStrategy() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(25901, "CamelCase", 25))//
                .executeSumResult();

        // 测试默认映射（假设表字段是 create_time，对象是 createTime）
        UserInfo result = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 25901)//
                .queryForObject();

        assertNotNull(result);
        assertNotNull("createTime should be mapped from create_time", result.getCreateTime());
    }

    /**
     * 测试 NULL 值映射
     * RowMapper 应正确处理 NULL 值
     */
    @Test
    public void testNullValueMapping() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        UserInfo u = new UserInfo();
        u.setId(26001);
        u.setName("NullMap");
        u.setAge(null); // NULL age
        u.setEmail(null); // NULL email
        lambda.insert(UserInfo.class)//
                .applyEntity(u)//
                .executeSumResult();

        RowMapper<UserInfo> nullSafeMapper = (rs, rowNum) -> {
            UserInfo user = new UserInfo();
            user.setId(rs.getInt("id"));
            user.setName(rs.getString("name"));

            // 安全处理 NULL
            int age = rs.getInt("age");
            user.setAge(rs.wasNull() ? null : age);

            user.setEmail(rs.getString("email"));
            return user;
        };

        UserInfo result = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 26001)//
                .queryForObject(nullSafeMapper);

        assertNotNull(result);
        assertEquals("NullMap", result.getName());
        assertNull("Age should be null", result.getAge());
        assertNull("Email should be null", result.getEmail());
    }

    /**
     * 测试计算字段映射
     * 查询计算列并映射
     */
    @Test
    public void testCalculatedFieldMapping() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(26101, "Calc1", 30))//
                .executeSumResult();

        // 查询计算字段：age * 2
        RowMapper<Integer> calcMapper = (rs, rowNum) -> rs.getInt("doubled_age");

        Integer doubled = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 26101)//
                .applySelect("age * 2 as doubled_age")//
                .queryForObject(calcMapper);

        assertEquals(Integer.valueOf(60), doubled);
    }

    /**
     * 测试聚合结果映射
     * 映射 GROUP BY 聚合结果
     */
    @Test
    public void testAggregationResultMapping() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(26201, "Agg1", 20))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(26202, "Agg2", 20))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(26203, "Agg3", 30))//
                .executeSumResult();

        // 映射 GROUP BY 结果
        class AgeGroup {
            Integer age;
            Long    count;

            public AgeGroup(Integer age, Long count) {
                this.age = age;
                this.count = count;
            }
        }

        RowMapper<AgeGroup> groupMapper = (rs, rowNum) -> {
            return new AgeGroup(rs.getInt("age"), rs.getLong("cnt"));
        };

        List<AgeGroup> groups = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(26201, 26202, 26203))//
                .applySelect("age, count(*) as cnt")//
                .groupBy("age")//
                .orderBy("age")//
                .queryForList(groupMapper);

        assertNotNull(groups);
        assertEquals(2, groups.size());
        assertEquals(Integer.valueOf(20), groups.get(0).age);
        assertEquals(Long.valueOf(2), groups.get(0).count);
    }

    private Object getVal(Map<String, Object> map, String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        if (map.containsKey(key.toUpperCase())) {
            return map.get(key.toUpperCase());
        }
        if (map.containsKey(key.toLowerCase())) {
            return map.get(key.toLowerCase());
        }
        return null;
    }

    /**
     * Helper: 创建 UserInfo
     */
    private UserInfo createUser(int id, String name, Integer age) {
        UserInfo u = new UserInfo();
        u.setId(id);
        u.setName(name);
        u.setAge(age);
        u.setEmail(name.toLowerCase() + "@mapper.com");
        u.setCreateTime(new Date());
        return u;
    }
}