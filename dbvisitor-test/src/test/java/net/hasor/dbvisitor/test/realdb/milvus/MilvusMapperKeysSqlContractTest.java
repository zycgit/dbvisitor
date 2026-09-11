/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.util.HashMap;
import java.util.Map;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusMapperKeysSqlContractTest extends MilvusSqlContractSupport {
    @SimpleMapper
    public interface KeysMapper {
        @Insert(value = "INSERT INTO ${collection} (name, v) VALUES (#{name}, #{vector})",
                useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
        int namedColumn(Map<String, Object> values) throws SQLException;

        @Insert(value = "INSERT INTO ${collection} (name, v) VALUES (#{name}, #{vector})",
                useGeneratedKeys = true, keyProperty = "id")
        int defaultColumn(Map<String, Object> values);
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_MAPPER_ANNOTATION_KEYS)
    public void annotationShouldPopulateAutoIdAndReportUnsupportedColumnSelection() throws Exception {
        try (Session session = prepareSession()) {
            KeysMapper mapper = session.createMapper(KeysMapper.class);
            Map<String, Object> first = values("annotation named");
            Map<String, Object> second = values("annotation default");
            assertEquals(1, mapper.defaultColumn(second));
            assertStored(second);
            SQLFeatureNotSupportedException error = assertThrows(SQLFeatureNotSupportedException.class, () -> mapper.namedColumn(first));
            assertEquals("columnNames not supported", error.getMessage());
            assertFalse(first.containsKey("id"));
            assertEquals(1, countRows(""));
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_MAPPER_XML_KEYS)
    public void xmlShouldPopulateAutoIdAndReportUnsupportedColumnSelection() throws Exception {
        try (Session session = prepareSession()) {
            Map<String, Object> first = values("xml named");
            Map<String, Object> second = values("xml default");
            assertEquals(1, session.executeStatement("milvus.GeneratedKeys.defaultColumn", second));
            assertStored(second);
            SQLFeatureNotSupportedException error = assertThrows(SQLFeatureNotSupportedException.class,
                    () -> session.executeStatement("milvus.GeneratedKeys.namedColumn", first));
            assertEquals("columnNames not supported", error.getMessage());
            assertFalse(first.containsKey("id"));
            assertEquals(1, countRows(""));
        }
    }

    private Session prepareSession() throws Exception {
        createCollection("id INT64 PRIMARY KEY AUTO_ID, name VARCHAR(32), v FLOAT_VECTOR(2)");
        createIndex("v", "FLAT", "L2");
        loadCollection();
        Configuration configuration = new Configuration();
        configuration.loadMapper("/realdb/milvus/mapper/GeneratedKeysMapper.xml");
        return configuration.newSession(newAdapterConnection());
    }

    private Map<String, Object> values(String name) {
        Map<String, Object> values = new HashMap<>();
        values.put("collection", this.collection);
        values.put("name", name);
        values.put("vector", new float[] { 1, 0 });
        return values;
    }

    private void assertStored(Map<String, Object> values) throws Exception {
        assertTrue(values.get("id") instanceof Long);
        long id = (Long) values.get("id");
        assertTrue(id > 0);
        Map<String, Object> stored = this.jdbcTemplate.queryForMap("SELECT id, name FROM " + this.collection + " WHERE id = ?", new Object[] { id });
        assertEquals(id, stored.get("id"));
        assertEquals(values.get("name"), stored.get("name"));
    }
}
