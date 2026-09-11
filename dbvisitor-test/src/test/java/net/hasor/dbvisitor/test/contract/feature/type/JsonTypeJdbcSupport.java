/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.type;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import net.hasor.dbvisitor.test.contract.material.model.types.JsonAnnotatedBean;
import net.hasor.dbvisitor.test.contract.material.model.types.JsonTestBean;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public abstract class JsonTypeJdbcSupport extends AbstractNxnContractTest {
    protected int baseId() {
        return 680000;
    }

    protected Object fixtureKey(int id) {
        return id;
    }

    protected String insertCommand(String columns, String... parameters) {
        return "INSERT INTO json_types_explicit_test (" + columns + ") VALUES (" + String.join(", ", parameters) + ")";
    }

    protected String selectCommand(String columns) throws SQLException {
        return "SELECT " + columns + " FROM json_types_explicit_test WHERE id = ?";
    }

    // Serialization assertions inspect the stored text; scalar result conversion is tested separately.
    protected String storedJson(Object id) throws SQLException {
        Map<String, Object> row = jdbcTemplate.queryForMap(selectCommand("json_varchar"), new Object[] { id });
        return (String) value(row, "json_varchar");
    }

    protected void assertJsonSetContainsName(Set loadedSet, String expectedName) {
        for (Object element : loadedSet) {
            if (element instanceof Map && expectedName.equals(((Map) element).get("name"))) {
                return;
            }
        }
        fail("Expected JSON set to contain name: " + expectedName);
    }

    protected void assertJsonElementName(Object element, String expectedName) {
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

    protected void assertJsonAnnotatedBean(JsonAnnotatedBean bean, String productName, Double price, Integer quantity, String category) {
        assertNotNull(bean);
        assertEquals(productName, bean.getProductName());
        assertEquals(price, bean.getPrice());
        assertEquals(quantity, bean.getQuantity());
        assertEquals(category, bean.getCategory());
    }

    protected Object value(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        if (row.containsKey(key.toUpperCase())) {
            return row.get(key.toUpperCase());
        }
        return row.get(key.toLowerCase());
    }
}
