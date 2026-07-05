package net.hasor.dbvisitor.test.contract.feature.type;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.test.contract.material.model.types.JsonAnnotatedBean;
import net.hasor.dbvisitor.test.contract.material.model.types.JsonTestBean;
import net.hasor.dbvisitor.test.contract.material.model.types.JsonTestBean.Address;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.types.handler.json.wrap.JsonType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class JsonTypeTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 680000;
    }

    @Test
    @Capability(CapabilityId.TYPE_JSON_WRITE_OBJECT)
    public void jsonObjectToStringCol() throws SQLException {
        requiresNxnFeature(FeatureId.JSON);
        int id = baseId() + 1;
        JsonTestBean bean = new JsonTestBean("Alice", 30, true);
        bean.setAddress(new Address("Shenzhen", "Futian Road", "518000"));

        jdbcTemplate.executeUpdate(//
                "INSERT INTO json_types_explicit_test (id, json_varchar) VALUES (#{id}, #{bean, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})", //
                CollectionUtils.asMap("id", id, "bean", bean));

        String json = jdbcTemplate.queryForObject("SELECT json_varchar FROM json_types_explicit_test WHERE id = ?", new Object[] { id }, String.class);

        assertNotNull(json);
        assertTrue(json.contains("Alice"));
        assertTrue(json.contains("30"));
        assertTrue(json.contains("Shenzhen"));
    }

    @Test
    @Capability(CapabilityId.TYPE_JSON_WRITE_SPECIAL)
    public void jsonObjectNullFieldsSpecialCharsAndEmptyObject() throws SQLException {
        requiresNxnFeature(FeatureId.JSON);
        int specialId = baseId() + 2;
        int emptyId = baseId() + 3;
        JsonTestBean special = new JsonTestBean();
        special.setName("中文名字");
        special.setAge(28);
        special.setActive(true);
        special.setTags(Arrays.asList("emoji😀", "quote\"test\"", "backslash\\path"));
        special.setAddress(new Address("上海", "南京路123号", "200000"));
        JsonTestBean empty = new JsonTestBean();

        jdbcTemplate.executeUpdate(//
                "INSERT INTO json_types_explicit_test (id, json_varchar) VALUES (#{id}, #{bean, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})", //
                CollectionUtils.asMap("id", specialId, "bean", special));
        jdbcTemplate.executeUpdate(//
                "INSERT INTO json_types_explicit_test (id, json_varchar) VALUES (#{id}, #{bean, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})", //
                CollectionUtils.asMap("id", emptyId, "bean", empty));

        String specialJson = jdbcTemplate.queryForObject("SELECT json_varchar FROM json_types_explicit_test WHERE id = ?", new Object[] { specialId }, String.class);
        String emptyJson = jdbcTemplate.queryForObject("SELECT json_varchar FROM json_types_explicit_test WHERE id = ?", new Object[] { emptyId }, String.class);

        assertNotNull(specialJson);
        assertTrue(specialJson.contains("中文名字") || specialJson.contains("\\u4e2d"));
        assertTrue(specialJson.contains("上海") || specialJson.contains("\\u4e0a"));
        assertTrue(specialJson.contains("28"));
        assertNotNull(emptyJson);
        assertTrue(emptyJson.contains("{") && emptyJson.contains("}"));
    }

    @Test
    @Capability(CapabilityId.TYPE_JSON_READ_MAP)
    public void jsonObjectAsMap() throws SQLException {
        requiresNxnFeature(FeatureId.JSON);
        int id = baseId() + 4;
        JsonTestBean bean = new JsonTestBean("Frank", 40, true);
        bean.setAddress(new Address("Shenzhen", "Futian Road", "518000"));

        jdbcTemplate.executeUpdate(//
                "INSERT INTO json_types_explicit_test (id, json_varchar) VALUES (#{id}, #{bean, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})", //
                CollectionUtils.asMap("id", id, "bean", bean));

        Map loaded = jdbcTemplate.queryForObject("SELECT json_varchar FROM json_types_explicit_test WHERE id = ?", new Object[] { id }, JsonType.jsonMap());

        assertTrue(loaded instanceof HashMap);
        assertEquals("Frank", loaded.get("name"));
        assertEquals(40, ((Number) loaded.get("age")).intValue());
        assertEquals(Boolean.TRUE, loaded.get("active"));
        assertTrue(loaded.get("address") instanceof Map);
    }

    @Test
    @Capability(CapabilityId.TYPE_JSON_READ_LIST)
    public void jsonArrayAsList() throws SQLException {
        requiresNxnFeature(FeatureId.JSON);
        int id = baseId() + 5;
        List<Map<String, Object>> list = Arrays.asList(//
                CollectionUtils.asMap("id", 1, "name", "Alice", "score", 95), //
                CollectionUtils.asMap("id", 2, "name", "Bob", "score", 88));

        jdbcTemplate.executeUpdate(//
                "INSERT INTO json_types_explicit_test (id, json_varchar) VALUES (#{id}, #{list, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})", //
                CollectionUtils.asMap("id", id, "list", list));

        List loaded = jdbcTemplate.queryForObject("SELECT json_varchar FROM json_types_explicit_test WHERE id = ?", new Object[] { id }, JsonType.jsonList());

        assertTrue(loaded instanceof ArrayList);
        assertEquals(2, loaded.size());
        assertTrue(loaded.get(0) instanceof Map);
        assertEquals("Alice", ((Map) loaded.get(0)).get("name"));
    }

    @Test
    @Capability(CapabilityId.TYPE_JSON_READ_SET)
    public void jsonArrayAsSetAndBeanLikeList() throws SQLException {
        requiresNxnFeature(FeatureId.JSON);
        int setId = baseId() + 6;
        int beanListId = baseId() + 7;
        List<Map<String, Object>> setSource = Arrays.asList(//
                CollectionUtils.asMap("id", 1, "name", "Alice", "score", 95), //
                CollectionUtils.asMap("id", 2, "name", "Bob", "score", 88), //
                CollectionUtils.asMap("id", 3, "name", "Charlie", "score", 92));
        List<JsonTestBean> beanList = Arrays.asList(//
                new JsonTestBean("George", 29, true), //
                new JsonTestBean("Helen", 31, false), //
                new JsonTestBean("Ivan", 27, true));

        jdbcTemplate.executeUpdate(//
                "INSERT INTO json_types_explicit_test (id, json_varchar) VALUES (#{id}, #{list, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})", //
                CollectionUtils.asMap("id", setId, "list", setSource));
        jdbcTemplate.executeUpdate(//
                "INSERT INTO json_types_explicit_test (id, json_varchar) VALUES (#{id}, #{list, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})", //
                CollectionUtils.asMap("id", beanListId, "list", beanList));

        Set loadedSet = jdbcTemplate.queryForObject("SELECT json_varchar FROM json_types_explicit_test WHERE id = ?", new Object[] { setId }, JsonType.jsonSet());
        List loadedList = jdbcTemplate.queryForObject("SELECT json_varchar FROM json_types_explicit_test WHERE id = ?", new Object[] { beanListId }, JsonType.jsonList());

        assertNotNull(loadedSet);
        assertEquals(3, loadedSet.size());
        assertJsonSetContainsName(loadedSet, "Alice");
        assertJsonSetContainsName(loadedSet, "Bob");
        assertTrue(loadedList instanceof ArrayList);
        assertEquals(3, loadedList.size());
        assertJsonElementName(loadedList.get(0), "George");
    }

    @Test
    @Capability(CapabilityId.TYPE_JSON_BIND_ANNOTATION)
    public void jsonAnnotatedBeanBoundTypeHandlerForPosNamedAndNullValues() throws SQLException {
        requiresNxnFeature(FeatureId.JSON);
        int positionalId = baseId() + 8;
        int namedId = baseId() + 9;
        int nullId = baseId() + 10;
        int partialId = baseId() + 11;
        JsonAnnotatedBean laptop = new JsonAnnotatedBean("Laptop", 5999.99, 10, "Electronics");
        JsonAnnotatedBean smartphone = new JsonAnnotatedBean("Smartphone", 3999.0, 20, "Mobile");
        JsonAnnotatedBean partial = new JsonAnnotatedBean("Tablet", 2999.0);

        jdbcTemplate.executeUpdate("INSERT INTO json_types_explicit_test (id, json_varchar) VALUES (?, ?)", new Object[] { positionalId, laptop });
        jdbcTemplate.executeUpdate(//
                "INSERT INTO json_types_explicit_test (id, json_varchar) VALUES (#{id}, #{product})", //
                CollectionUtils.asMap("id", namedId, "product", smartphone));
        jdbcTemplate.executeUpdate("INSERT INTO json_types_explicit_test (id, json_varchar) VALUES (?, ?)", new Object[] { nullId, null });
        jdbcTemplate.executeUpdate("INSERT INTO json_types_explicit_test (id, json_varchar) VALUES (?, ?)", new Object[] { partialId, partial });

        JsonAnnotatedBean loadedLaptop = jdbcTemplate.queryForObject("SELECT json_varchar FROM json_types_explicit_test WHERE id = ?", new Object[] { positionalId }, JsonAnnotatedBean.class);
        JsonAnnotatedBean loadedSmartphone = jdbcTemplate.queryForObject("SELECT json_varchar FROM json_types_explicit_test WHERE id = ?", new Object[] { namedId }, JsonAnnotatedBean.class);
        JsonAnnotatedBean loadedNull = jdbcTemplate.queryForObject("SELECT json_varchar FROM json_types_explicit_test WHERE id = ?", new Object[] { nullId }, JsonAnnotatedBean.class);
        JsonAnnotatedBean loadedPartial = jdbcTemplate.queryForObject("SELECT json_varchar FROM json_types_explicit_test WHERE id = ?", new Object[] { partialId }, JsonAnnotatedBean.class);

        assertJsonAnnotatedBean(loadedLaptop, "Laptop", 5999.99, 10, "Electronics");
        assertJsonAnnotatedBean(loadedSmartphone, "Smartphone", 3999.0, 20, "Mobile");
        assertNull(loadedNull);
        assertJsonAnnotatedBean(loadedPartial, "Tablet", 2999.0, null, null);
    }

    @Test
    @Capability(CapabilityId.TYPE_JSON_NULL)
    public void jsonNullRemainNull() throws SQLException {
        int id = baseId() + 12;
        jdbcTemplate.executeUpdate("INSERT INTO json_types_explicit_test (id, json_varchar, json_mysql, nested_json) VALUES (?, ?, ?, ?)", //
                new Object[] { id, null, null, null });

        assertNull(jdbcTemplate.queryForObject("SELECT json_varchar FROM json_types_explicit_test WHERE id = ?", new Object[] { id }, String.class));
        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT json_varchar, json_mysql, nested_json FROM json_types_explicit_test WHERE id = ?", new Object[] { id });
        assertNull(value(row, "json_varchar"));
        assertNull(value(row, "json_mysql"));
        assertNull(value(row, "nested_json"));
    }

    private void assertJsonSetContainsName(Set loadedSet, String expectedName) {
        for (Object element : loadedSet) {
            if (element instanceof Map && expectedName.equals(((Map) element).get("name"))) {
                return;
            }
        }
        fail("Expected JSON set to contain name: " + expectedName);
    }

    private void assertJsonElementName(Object element, String expectedName) {
        assertNotNull(element);
        if (element instanceof JsonTestBean) {
            assertEquals(expectedName, ((JsonTestBean) element).getName());
            return;
        }
        if (element instanceof Map) {
            assertEquals(expectedName, ((Map) element).get("name"));
            return;
        }
        fail("Unexpected JSON list element type: " + element.getClass());
    }

    private void assertJsonAnnotatedBean(JsonAnnotatedBean bean, String productName, Double price, Integer quantity, String category) {
        assertNotNull(bean);
        assertEquals(productName, bean.getProductName());
        assertEquals(price, bean.getPrice());
        assertEquals(quantity, bean.getQuantity());
        assertEquals(category, bean.getCategory());
    }

    private Object value(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        if (row.containsKey(key.toUpperCase())) {
            return row.get(key.toUpperCase());
        }
        return row.get(key.toLowerCase());
    }
}
