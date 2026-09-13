/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.xml;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.junit.Before;
import org.junit.Test;

import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class XmlMapperResultMapCase extends AbstractNxnContractTest {
    protected Session session;

    @Before
    public void createXmlMapperSession() throws Exception {
        Configuration config = newConfiguration();
        config.loadMapper(mapperResource());
        this.session = openSession(config);
    }

    protected Session openSession(Configuration configuration) throws Exception {
        return configuration.newSession(dataSource);
    }

    protected String mapperResource() {
        return "/mapper/XmlResultMapMapper.xml";
    }

    @Override
    protected void initData() throws SQLException {
        for (int i = 1; i <= 3; i++) {
            jdbcTemplate.executeUpdate(//
                    "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                    new Object[] { baseId() + i, "RmCfg" + i, 25 + i, "rmcfg" + i + "@nxn.test", new Date() });
        }
    }

    protected int baseId() {
        return 952000;
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULTMAP_PARTIAL)
    public void resultMap_shouldApplyExplicitPartialColumnMapping() throws Exception {
        List<?> list = this.session.queryStatement("xmltest.ResultMapMapper.selectByIdBase", mapOf("id", baseId() + 1));

        assertEquals(1, list.size());
        assertProperties(list.get(0), expectedPartial(1));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULTMAP_FULL)
    public void resultMap_shouldApplyFullColumnMapping() throws Exception {
        List<?> list = this.session.queryStatement("xmltest.ResultMapMapper.selectByIdExtended", mapOf("id", baseId() + 1));

        assertEquals(1, list.size());
        assertProperties(list.get(0), expectedFull(1));
        for (String property : nonNullFullProperties()) {
            assertNotNull(property, propertyValue(list.get(0), property));
        }
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULTMAP_JAVA_TYPE)
    public void resultMap_shouldHonorJavaTypeAttributes() throws Exception {
        List<?> list = this.session.queryStatement("xmltest.ResultMapMapper.selectByIdTyped", mapOf("id", baseId() + 2));

        assertEquals(1, list.size());
        assertProperties(list.get(0), expectedFull(2));
        for (Map.Entry<String, Class<?>> entry : expectedPropertyTypes().entrySet()) {
            assertTrue(entry.getKey(), entry.getValue().isInstance(propertyValue(list.get(0), entry.getKey())));
        }
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULTMAP_AUTO_MAPPING)
    public void resultMap_shouldSupportAutoMappingFlag() throws Exception {
        List<?> list = this.session.queryStatement("xmltest.ResultMapMapper.selectByIdAutoMapping", mapOf("id", baseId() + 1));

        assertEquals(1, list.size());
        assertProperties(list.get(0), expectedFull(1));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULTMAP_CASE_INSENSITIVE)
    public void resultMap_shouldSupportCaseInsensitiveColumnNames() throws Exception {
        List<?> list = this.session.queryStatement("xmltest.ResultMapMapper.selectByIdCaseInsensitive", mapOf("id", baseId() + 2));

        assertEquals(1, list.size());
        assertProperties(list.get(0), expectedFull(2));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULTMAP_CAMELCASE)
    public void resultMap_shouldSupportUnderscoreToCamelCaseMapping() throws Exception {
        List<UserInfo> list = this.session.queryStatement("xmltest.ResultMapMapper.selectByIdCamelCase", mapOf("id", baseId() + 1));

        assertEquals(1, list.size());
        assertEquals(Integer.valueOf(baseId() + 1), list.get(0).getId());
        assertNotNull(list.get(0).getCreateTime());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULTMAP_RENAMED_MAP)
    public void resultTypeMap_shouldUseResultColumnLabelsAsKeys() throws Exception {
        List<Map<String, Object>> list = queryColumnLabelRows();

        assertEquals(1, list.size());
        Map<String, Object> row = list.get(0);
        for (Map.Entry<String, Object> entry : expectedColumnLabels().entrySet()) {
            Object actual = value(row, entry.getKey());
            if (entry.getValue() instanceof Number) {
                assertTrue(actual instanceof Number);
                assertEquals(((Number) entry.getValue()).longValue(), ((Number) actual).longValue());
            } else {
                assertEquals(entry.getValue(), actual);
            }
        }
    }

    /** The SQL material supplies column labels; the contract checks how resultType=map exposes them. */
    protected List<Map<String, Object>> queryColumnLabelRows() throws Exception {
        return this.session.queryStatement("xmltest.ResultMapMapper.selectByIdAsRenamedMap", mapOf("id", baseId() + 3));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULTMAP_PARTIAL_LIST)
    public void resultMap_shouldApplyPartialMappingToLists() throws Exception {
        Map<String, Object> range = mapOf("firstId", baseId() + 1);
        range.put("lastId", baseId() + 3);
        List<?> list = this.session.queryStatement("xmltest.ResultMapMapper.selectAllBase", range);

        assertEquals(3, list.size());
        for (Object row : list) {
            for (String property : nonNullPartialProperties()) {
                assertNotNull(property, propertyValue(row, property));
            }
            for (String property : nullPartialProperties()) {
                assertNull(property, propertyValue(row, property));
            }
        }
    }

    protected Map<String, Object> expectedFull(int offset) {
        return Map.of("id", baseId() + offset, "name", "RmCfg" + offset,
                "age", 25 + offset, "email", "rmcfg" + offset + "@nxn.test");
    }

    protected Map<String, Object> expectedPartial(int offset) {
        Map<String, Object> expected = new LinkedHashMap<>(expectedFull(offset));
        expected.put("email", null);
        expected.put("createTime", null);
        return expected;
    }

    protected Map<String, Class<?>> expectedPropertyTypes() {
        return Map.of("id", Integer.class, "name", String.class, "age", Integer.class);
    }

    protected List<String> nonNullFullProperties() {
        return List.of("createTime");
    }

    protected List<String> nonNullPartialProperties() {
        return List.of("id", "name", "age");
    }

    protected List<String> nullPartialProperties() {
        return List.of("email");
    }

    protected Map<String, Object> expectedColumnLabels() {
        return Map.of("user_id", baseId() + 3, "user_name", "RmCfg3", "user_age", 28);
    }

    private void assertProperties(Object bean, Map<String, Object> expected) throws Exception {
        assertTrue("The material must define mapped properties", !expected.isEmpty());
        for (Map.Entry<String, Object> entry : expected.entrySet()) {
            assertEquals(entry.getKey(), entry.getValue(), propertyValue(bean, entry.getKey()));
        }
    }

    private Object propertyValue(Object bean, String name) throws Exception {
        for (PropertyDescriptor property : Introspector.getBeanInfo(bean.getClass()).getPropertyDescriptors()) {
            if (property.getName().equals(name) && property.getReadMethod() != null) {
                return property.getReadMethod().invoke(bean);
            }
        }
        throw new AssertionError("Missing readable property: " + name);
    }

    private Map<String, Object> mapOf(String key, Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put(key, value);
        return params;
    }

    private Object value(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        String lower = key.toLowerCase();
        if (row.containsKey(lower)) {
            return row.get(lower);
        }
        return row.get(key.toUpperCase());
    }

}
