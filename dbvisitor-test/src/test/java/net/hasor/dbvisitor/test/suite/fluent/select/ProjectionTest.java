package net.hasor.dbvisitor.test.suite.fluent.select;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserBasicDTO;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProjectionTest extends AbstractOneApiTest {

    @Test
    public void testSelectSingleColumn() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        UserInfo u = new UserInfo();
        u.setName("Alice");
        u.setAge(25);
        lambda.insert(UserInfo.class)//
                .applyEntity(u)//
                .executeSumResult();

        // Select Name only
        List<String> names = lambda.query(UserInfo.class)//
                .select(UserInfo::getName)//
                .queryForList(String.class);

        assertEquals(1, names.size());
        assertEquals("Alice", names.get(0));
    }

    @Test
    public void testSelectMultipleColumnsToMap() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        UserInfo u = new UserInfo();
        u.setName("Bob");
        u.setAge(30);
        u.setEmail("bob@example.com");
        lambda.insert(UserInfo.class)//
                .applyEntity(u)//
                .executeSumResult();

        // Select Name + Age
        List<Map<String, Object>> list = lambda.query(UserInfo.class)//
                .select(UserInfo::getName, UserInfo::getAge)//
                .queryForMapList();

        assertEquals(1, list.size());
        Map<String, Object> map = list.get(0);
        assertTrue(containsKey(map, "name"));
        assertTrue(containsKey(map, "age"));
        assertFalse(containsKey(map, "email")); // Email not selected

        assertEquals("Bob", getVal(map, "name"));
    }

    @Test
    public void testSelectToDto() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        UserInfo u = new UserInfo();
        u.setName("Charlie");
        u.setAge(35);
        u.setEmail("charlie@example.com");
        lambda.insert(UserInfo.class)//
                .applyEntity(u)//
                .executeSumResult();

        // Select Name + Age -> DTO
        List<UserBasicDTO> dtos = lambda.query(UserInfo.class)//
                .select(UserInfo::getName, UserInfo::getAge)//
                .queryForList(UserBasicDTO.class);

        assertEquals(1, dtos.size());
        UserBasicDTO dto = dtos.get(0);
        assertEquals("Charlie", dto.getName());
        assertEquals(35, dto.getAge()//
                .intValue());
    }

    private boolean containsKey(Map<String, Object> map, String key) {
        return map.containsKey(key) || map.containsKey(key.toUpperCase()) || map.containsKey(key.toLowerCase());
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
