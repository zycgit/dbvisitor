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
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class XmlMapperSqlFragmentCase extends AbstractNxnContractTest {
    protected Session session;

    @Before
    public void createXmlMapperSession() throws Exception {
        Configuration config = newConfiguration();
        config.loadMapper(mapperResource());
        this.session = config.newSession(dataSource);
    }

    protected String mapperResource() {
        return "/mapper/XmlSqlFragmentMapper.xml";
    }

    @Override
    protected void initData() throws SQLException {
        for (int i = 1; i <= 5; i++) {
            jdbcTemplate.executeUpdate(//
                    "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                    new Object[] { baseId() + i, "SqlFrag" + i, 20 + i * 5, "frag" + i + "@nxn.test", new Date(timestamp()) });
        }
    }

    protected int baseId() {
        return 953000;
    }

    protected long timestamp() {
        return 1700000000000L;
    }

    // 能力归属：Mapper 文件 / sql 标签。
    @Test
    @Capability(value = CapabilityId.MAPPER_XML_FRAGMENT_COLUMNS, column = "mapper-files/statements/fragments")
    public void sqlFragment_shouldIncludeReusableColumnListInSelect() throws Exception {
        List<UserInfo> list = this.session.queryStatement("xmltest.SqlFragmentMapper.selectWithColumnFragment", mapOf("id", baseId() + 1));

        assertEquals(1, list.size());
        UserInfo user = list.get(0);
        assertEquals(Integer.valueOf(baseId() + 1), user.getId());
        assertEquals("SqlFrag1", user.getName());
        assertEquals(Integer.valueOf(25), user.getAge());
        assertEquals("frag1@nxn.test", user.getEmail());
        assertNotNull(user.getCreateTime());
        assertEquals(timestamp(), user.getCreateTime().getTime());
    }

    // 能力归属：Mapper 文件 / sql 标签。
    @Test
    @Capability(value = CapabilityId.MAPPER_XML_FRAGMENT_DYNAMIC_CONDITION, column = "mapper-files/statements/fragments")
    public void sqlFragment_shouldIncludeDynamicConditionFragment() throws Exception {
        List<UserInfo> allRows = this.session.queryStatement("xmltest.SqlFragmentMapper.selectWithConditionFragment", new HashMap<String, Object>());
        List<UserInfo> byName = this.session.queryStatement("xmltest.SqlFragmentMapper.selectWithConditionFragment", mapOf("name", "SqlFrag1"));
        Map<String, Object> ageRange = mapOf("minAge", 30);
        ageRange.put("maxAge", 40);
        List<UserInfo> byAgeRange = this.session.queryStatement("xmltest.SqlFragmentMapper.selectWithConditionFragment", ageRange);
        Map<String, Object> allConditions = mapOf("name", allNamesParameter());
        allConditions.put("minAge", 25);
        allConditions.put("maxAge", 35);
        List<UserInfo> combined = this.session.queryStatement("xmltest.SqlFragmentMapper.selectWithConditionFragment", allConditions);

        assertEquals(5, allRows.size());
        assertEquals(1, byName.size());
        assertEquals("SqlFrag1", byName.get(0).getName());
        assertEquals(3, byAgeRange.size());
        for (UserInfo user : byAgeRange) {
            assertTrue(user.getAge() >= 30 && user.getAge() <= 40);
        }
        assertEquals(3, combined.size());
        allConditions.put("name", "SqlFrag3");
        List<UserInfo> narrowed = this.session.queryStatement("xmltest.SqlFragmentMapper.selectWithConditionFragment", allConditions);
        assertEquals(1, narrowed.size());
        assertEquals("SqlFrag3", narrowed.get(0).getName());
        assertEquals(Integer.valueOf(30), byAgeRange.get(0).getAge());
        assertEquals(Integer.valueOf(40), byAgeRange.get(2).getAge());
    }

    protected String allNamesParameter() {
        return "SqlFrag%";
    }

    // 能力归属：Mapper 文件 / sql 标签。
    @Test
    @Capability(value = CapabilityId.MAPPER_XML_FRAGMENT_MULTIPLE, column = "mapper-files/statements/fragments")
    public void sqlFragment_shouldCombineColumnAndOrderFragments() throws Exception {
        List<UserInfo> list = this.session.queryStatement("xmltest.SqlFragmentMapper.selectWithMultipleFragments", null);

        assertEquals(5, list.size());
        assertAscendingById(list);
        assertEquals("SqlFrag1", list.get(0).getName());
        assertNotNull(list.get(0).getCreateTime());
        for (int i = 1; i <= list.size(); i++) {
            assertEquals(Integer.valueOf(baseId() + i), list.get(i - 1).getId());
            assertEquals("SqlFrag" + i, list.get(i - 1).getName());
            assertEquals(timestamp(), list.get(i - 1).getCreateTime().getTime());
        }
    }

    // 能力归属：Mapper 文件 / sql 标签。
    @Test
    @Capability(value = CapabilityId.MAPPER_XML_FRAGMENT_ORDER, column = "mapper-files/statements/fragments")
    public void sqlFragment_shouldIncludeOrderFragmentOnly() throws Exception {
        List<UserInfo> list = this.session.queryStatement("xmltest.SqlFragmentMapper.selectAllOrdered", null);

        assertEquals(5, list.size());
        assertAscendingById(list);
        for (int i = 1; i <= list.size(); i++) {
            assertEquals("SqlFrag" + i, list.get(i - 1).getName());
        }
    }

    private void assertAscendingById(List<UserInfo> list) {
        for (int i = 1; i < list.size(); i++) {
            assertTrue(list.get(i - 1).getId() < list.get(i).getId());
        }
    }

    private Map<String, Object> mapOf(String key, Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put(key, value);
        return params;
    }
}
