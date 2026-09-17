/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.session;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import org.junit.Test;
import static org.junit.Assert.*;

public class MapperMapGeneratedKeysTest {
    @SimpleMapper
    public interface KeysMapper {
        @Insert(value = "INSERT INTO items(name) VALUES(#{name})", useGeneratedKeys = true, keyProperty = "id")
        int insert(Map<String, Object> values) throws SQLException;

        @Insert(value = "INSERT INTO items(name) VALUES(#{name})", useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
        int namedColumn(Map<String, Object> values) throws SQLException;

        @Insert("INSERT INTO items(name) VALUES(#{name})")
        int withoutKeys(Map<String, Object> values) throws SQLException;

        @Insert(value = "INSERT INTO items(name) VALUES(#{info.name})", useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
        int namedBean(@Param("info") Item info) throws SQLException;

        @Insert(value = "INSERT INTO items(name) VALUES(#{info.name})", useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
        int namedMap(@Param("info") Map<String, Object> info) throws SQLException;
    }

    public static class Item {
        private Long   id;
        private String name;

        public Long getId() {
            return this.id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void namedSingleArgumentUsesPlainKeyProperty() throws Exception {
        try (Session session = openSession()) {
            KeysMapper mapper = session.createMapper(KeysMapper.class);
            Item item = new Item();
            item.setName("named bean");
            assertEquals(1, mapper.namedBean(item));
            assertNotNull(item.getId());
            assertEquals(item.getName(), session.jdbc().queryForString("SELECT name FROM items WHERE id=?", item.getId()));
            Map<String, Object> values = values("named map");
            assertEquals(1, mapper.namedMap(values));
            assertTrue(values.get("id") instanceof Long);
            assertEquals("named map", session.jdbc().queryForString("SELECT name FROM items WHERE id=?", values.get("id")));
        }
    }

    @Test
    public void generatedKeyShouldReachOriginalMapWhetherKeyIsMissingOrNull() throws Exception {
        try (Session session = openSession()) {
            KeysMapper mapper = session.createMapper(KeysMapper.class);
            Map<String, Object> missing = values("missing");
            assertEquals(1, mapper.insert(missing));
            assertTrue(missing.get("id") instanceof Long);
            Map<String, Object> existing = values("existing");
            existing.put("id", null);
            assertEquals(1, mapper.namedColumn(existing));
            assertTrue(existing.get("id") instanceof Long);
            assertNotEquals(missing.get("id"), existing.get("id"));
            assertEquals("missing", session.jdbc().queryForString("SELECT name FROM items WHERE id=?", missing.get("id")));
            assertEquals("existing", session.jdbc().queryForString("SELECT name FROM items WHERE id=?", existing.get("id")));
        }
    }

    @Test
    public void failedInsertShouldNotAddGeneratedKeyToOriginalMap() throws Exception {
        try (Session session = openSession()) {
            KeysMapper mapper = session.createMapper(KeysMapper.class);
            Map<String, Object> values = values(null);
            assertThrows(SQLException.class, () -> mapper.insert(values));
            assertFalse(values.containsKey("id"));
            assertEquals(1, values.size());
        }
    }

    @Test
    public void disabledGeneratedKeysShouldNotMutateOriginalMap() throws Exception {
        try (Session session = openSession()) {
            Map<String, Object> values = values("no keys");
            assertEquals(1, session.createMapper(KeysMapper.class).withoutKeys(values));
            assertEquals(Map.of("name", "no keys"), values);
        }
    }

    private Session openSession() throws Exception {
        Connection connection = DriverManager.getConnection("jdbc:h2:mem:keys_" + UUID.randomUUID());
        Session session = new Configuration().newSession(connection);
        session.jdbc().execute("CREATE TABLE items(id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, name VARCHAR(32) NOT NULL)");
        return session;
    }

    private Map<String, Object> values(String name) {
        Map<String, Object> values = new HashMap<>();
        values.put("name", name);
        return values;
    }
}
