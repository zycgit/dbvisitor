package net.hasor.dbvisitor.test.suite.mapping.annotation;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.types.SpecialTypeEntity;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 验证 specialJavaType 对于 Map/List/Set 等集合类型的支持，以及 JSON/Array 映射。
 */
public class SpecialTypeTest extends AbstractOneApiTest {
    @Before
    public void init() throws SQLException, IOException {
        // execute SQL file
        jdbcTemplate.loadSplitSQL(";", "/sql/pg/special_types.sql");

        // prepare data
        SpecialTypeEntity entity = new SpecialTypeEntity();
        entity.setId(1);

        // 1. Map
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("key", "value");
        map.put("num", 123);
        entity.setJsonMap(map);

        // 2. List
        List<String> list = new LinkedList<>();
        list.add("a");
        list.add("b");
        entity.setJsonList(list);

        // 3. Set
        Set<String> set = new HashSet<>();
        set.add("x");
        set.add("y");
        entity.setJsonSet(set);

        // 4. Array
        entity.setIntArray(new Integer[] { 10, 20 });

        // Insert
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        lambda.insert(SpecialTypeEntity.class)//
                .applyEntity(entity)//
                .executeSumResult();
    }

    @Test
    public void testMap() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        SpecialTypeEntity result = lambda.query(SpecialTypeEntity.class)//
                .eq(SpecialTypeEntity::getId, 1)//
                .queryForObject();

        assertNotNull(result);
        assertNotNull(result.getJsonMap());
        assertTrue(result.getJsonMap() instanceof LinkedHashMap);
        assertEquals("value", result.getJsonMap().get("key"));
    }

    @Test
    public void testList() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        SpecialTypeEntity result = lambda.query(SpecialTypeEntity.class)//
                .eq(SpecialTypeEntity::getId, 1)//
                .queryForObject();

        assertNotNull(result);
        assertNotNull(result.getJsonList());
        assertEquals(2, result.getJsonList().size());
        assertEquals("a", result.getJsonList().get(0));
    }

    @Test
    public void testSet() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        SpecialTypeEntity result = lambda.query(SpecialTypeEntity.class)//
                .eq(SpecialTypeEntity::getId, 1)//
                .queryForObject();

        assertNotNull(result);
        assertNotNull(result.getJsonSet());
        assertTrue(result.getJsonSet() instanceof HashSet);
        assertTrue(result.getJsonSet().contains("x"));
    }

    @Test
    public void testArray() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        SpecialTypeEntity result = lambda.query(SpecialTypeEntity.class)//
                .eq(SpecialTypeEntity::getId, 1)//
                .queryForObject();

        assertNotNull(result);
        assertNotNull(result.getIntArray());
        assertEquals(2, result.getIntArray().length);
        assertEquals(Integer.valueOf(10), result.getIntArray()[0]);
    }
}