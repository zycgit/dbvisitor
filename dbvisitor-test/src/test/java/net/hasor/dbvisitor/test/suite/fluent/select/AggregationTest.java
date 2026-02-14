package net.hasor.dbvisitor.test.suite.fluent.select;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AggregationTest extends AbstractOneApiTest {

    @Test
    public void testCount() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // Init data
        lambda.insert(UserInfo.class).applyEntity(createUser("u1", 10)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser("u2", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser("u3", 20)).executeSumResult();

        // Count all
        int total = lambda.query(UserInfo.class)//
                .queryForCount();
        assertEquals(3, total);

        // Count with condition
        int age20 = lambda.query(UserInfo.class)//
                .eq(UserInfo::getAge, 20)//
                .queryForCount();
        assertEquals(2, age20);
    }

    @Test
    public void testGroupBy() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser("u1", 10)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser("u2", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser("u3", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser("u4", 30)).executeSumResult();

        // Group By Age
        // Note: use applySelect to customize select columns for group by
        List<Map<String, Object>> result = lambda.query(UserInfo.class)//
                .applySelect("age, count(*) as cnt")//
                .groupBy("age")//
                .orderBy("age")//
                .queryForMapList();

        assertNotNull(result);
        assertEquals(3, result.size()); // 10, 20, 30

        // Check 20 has 2
        // Map keys are usually lower case or upper case depending on DB, but usually column name.
        // H2 usually UPPERCASE by default, but let's check flexibly.

        Map<String, Object> group20 = result.stream()//
                .filter(m -> Integer.valueOf(20).equals(getVal(m, "age")))//
                .findFirst()//
                .orElse(null);

        assertNotNull(group20);
        assertEquals(2L, ((Number) getVal(group20, "cnt")).longValue());
    }

    @Test
    public void testSumMax() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser("u1", 10))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser("u2", 20))//
                .executeSumResult();

        // Sum
        // Expected: 30
        Long sumAge = lambda.query(UserInfo.class)//
                .applySelect("sum(age)")//
                .queryForObject(Long.class);
        assertEquals(30L, sumAge.longValue());

        // Max
        // Expected: 20
        Integer maxAge = lambda.query(UserInfo.class)//
                .applySelect("max(age)")//
                .queryForObject(Integer.class);
        assertEquals(20, maxAge.intValue());
    }

    private UserInfo createUser(String name, int age) {
        UserInfo u = new UserInfo();
        u.setName(name);
        u.setAge(age);
        u.setEmail(name + "@example.com");
        u.setCreateTime(new java.util.Date());
        return u;
    }

    private Object getVal(Map<String, Object> map, String key) {
        if (map.containsKey(key))
            return map.get(key);
        if (map.containsKey(key.toUpperCase()))
            return map.get(key.toUpperCase());
        if (map.containsKey(key.toLowerCase()))
            return map.get(key.toLowerCase());
        return null;
    }
}
