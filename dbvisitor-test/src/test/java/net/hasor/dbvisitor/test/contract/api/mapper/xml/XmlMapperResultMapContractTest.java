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
public abstract class XmlMapperResultMapContractTest extends AbstractNxnContractTest {
    private Session session;

    @Before
    public void createXmlMapperSession() throws Exception {
        Configuration config = newConfiguration();
        config.loadMapper("/mapper/XmlResultMapMapper.xml");
        this.session = openSession(config);
    }

    protected Session openSession(Configuration configuration) throws Exception {
        return configuration.newSession(dataSource);
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
        List<UserInfo> list = this.session.queryStatement("xmltest.ResultMapMapper.selectByIdBase", mapOf("id", baseId() + 1));

        assertEquals(1, list.size());
        UserInfo user = list.get(0);
        assertEquals(Integer.valueOf(baseId() + 1), user.getId());
        assertEquals("RmCfg1", user.getName());
        assertEquals(Integer.valueOf(26), user.getAge());
        assertNull(user.getEmail());
        assertNull(user.getCreateTime());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULTMAP_FULL)
    public void resultMap_shouldApplyFullColumnMapping() throws Exception {
        List<UserInfo> list = this.session.queryStatement("xmltest.ResultMapMapper.selectByIdExtended", mapOf("id", baseId() + 1));

        assertEquals(1, list.size());
        UserInfo user = list.get(0);
        assertEquals(Integer.valueOf(baseId() + 1), user.getId());
        assertEquals("RmCfg1", user.getName());
        assertEquals(Integer.valueOf(26), user.getAge());
        assertEquals("rmcfg1@nxn.test", user.getEmail());
        assertNotNull(user.getCreateTime());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULTMAP_JAVA_TYPE)
    public void resultMap_shouldHonorJavaTypeAttributes() throws Exception {
        List<UserInfo> list = this.session.queryStatement("xmltest.ResultMapMapper.selectByIdTyped", mapOf("id", baseId() + 2));

        assertEquals(1, list.size());
        UserInfo user = list.get(0);
        assertTrue(user.getId() instanceof Integer);
        assertTrue(user.getName() instanceof String);
        assertTrue(user.getAge() instanceof Integer);
        assertEquals("rmcfg2@nxn.test", user.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULTMAP_AUTO_MAPPING)
    public void resultMap_shouldSupportAutoMappingFlag() throws Exception {
        List<UserInfo> list = this.session.queryStatement("xmltest.ResultMapMapper.selectByIdAutoMapping", mapOf("id", baseId() + 1));

        assertEquals(1, list.size());
        UserInfo user = list.get(0);
        assertEquals(Integer.valueOf(baseId() + 1), user.getId());
        assertEquals("RmCfg1", user.getName());
        assertEquals(Integer.valueOf(26), user.getAge());
        assertEquals("rmcfg1@nxn.test", user.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULTMAP_CASE_INSENSITIVE)
    public void resultMap_shouldSupportCaseInsensitiveColumnNames() throws Exception {
        List<UserInfo> list = this.session.queryStatement("xmltest.ResultMapMapper.selectByIdCaseInsensitive", mapOf("id", baseId() + 2));

        assertEquals(1, list.size());
        UserInfo user = list.get(0);
        assertEquals(Integer.valueOf(baseId() + 2), user.getId());
        assertEquals("RmCfg2", user.getName());
        assertEquals(Integer.valueOf(27), user.getAge());
        assertEquals("rmcfg2@nxn.test", user.getEmail());
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
        assertEquals(baseId() + 3, number(row, "user_id").intValue());
        assertEquals("RmCfg3", value(row, "user_name"));
        assertEquals(28, number(row, "user_age").intValue());
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
        List<UserInfo> list = this.session.queryStatement("xmltest.ResultMapMapper.selectAllBase", range);

        assertEquals(3, list.size());
        for (UserInfo user : list) {
            assertNotNull(user.getId());
            assertNotNull(user.getName());
            assertNotNull(user.getAge());
            assertNull(user.getEmail());
        }
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

    private Number number(Map<String, Object> row, String key) {
        return (Number) value(row, key);
    }
}
