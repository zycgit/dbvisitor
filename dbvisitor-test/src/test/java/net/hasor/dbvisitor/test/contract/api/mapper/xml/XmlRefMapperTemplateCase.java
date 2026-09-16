/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.xml;

import java.util.Arrays;
import java.util.List;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.page.PageResult;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.AbstractOneApiTest;
import net.hasor.dbvisitor.test.contract.material.dao.XmlRefMapperDao;
import net.hasor.dbvisitor.test.contract.material.dao.XmlRefPageMapper;
import net.hasor.dbvisitor.test.contract.material.dao.XmlRefPageResultMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class XmlRefMapperTemplateCase extends XmlRefMapperSupport {
    // 能力归属：Mapper 文件 / 调用文件 Mapper。
    @Test
    @Capability(value = CapabilityId.MAPPER_XML_REF_DYNAMIC, column = "mapper-files/external-mapper-references/calls")
    public void refMapper_shouldRunXmlDynamicWhereStatements() throws Exception {
        XmlRefMapperDao conditionalMapper = (XmlRefMapperDao) this.dao;
        List<UserInfo> all = conditionalMapper.selectByCondition(null, null);
        List<UserInfo> byName = conditionalMapper.selectByCondition("RefMapA", null);
        List<UserInfo> byNameAndAge = conditionalMapper.selectByCondition("RefMap%", 30);

        assertEquals(4, all.size());
        assertEquals(1, byName.size());
        assertEquals("RefMapA", byName.get(0).getName());
        assertEquals(1, byNameAndAge.size());
        assertEquals("RefMapC", byNameAndAge.get(0).getName());
    }

    // 能力归属：Mapper 文件 / 调用文件 Mapper。
    @Test
    @Capability(value = CapabilityId.MAPPER_XML_REF_FOREACH, column = "mapper-files/external-mapper-references/calls")
    public void refMapper_shouldExpandForeachInClause() throws Exception {
        List<UserInfo> list = this.dao.selectByIds(Arrays.asList(baseId() + 1, baseId() + 3));

        assertEquals(2, list.size());
        assertEquals("RefMapA", list.get(0).getName());
        assertEquals("RefMapC", list.get(1).getName());
    }

    // 能力归属：Mapper 文件 / 调用文件 Mapper。
    @Test
    @Capability(value = CapabilityId.MAPPER_XML_REF_TEXT_AND_MAP, column = "mapper-files/external-mapper-references/calls")
    public void refMapper_shouldSupportTextReplacement() throws Exception {
        List<UserInfo> byId = this.dao.selectWithOrderBy(orderExpression("id"));
        List<UserInfo> byAge = this.dao.selectWithOrderBy(orderExpression("age"));

        assertEquals(4, byId.size());
        assertAscendingById(byId);

        assertEquals(4, byAge.size());
        for (int i = 1; i < byAge.size(); i++) {
            assertTrue(byAge.get(i - 1).getAge() <= byAge.get(i).getAge());
        }
    }

    // 能力归属：Mapper 文件 / 分页查询。
    @Test
    @Capability(value = CapabilityId.MAPPER_XML_REF_QUERY_PAGE, column = "mapper-files/pagination/file-pagination")
    public void refMapper_shouldApplyPageArgumentToListResults() throws Exception {
        XmlRefPageMapper mapper = createPagingMapper(XmlRefPageMapper.class);

        assertNames(mapper.selectPage(0, new PageObject(0, 3)), "RefMapA", "RefMapB", "RefMapC");
        assertNames(mapper.selectPage(0, new PageObject(1, 3)), "RefMapD");
        assertTrue(mapper.selectPage(0, new PageObject(2, 3)).isEmpty());
        assertNames(mapper.selectPage(0, PageObject.of(2, 2, 1)), "RefMapC", "RefMapD");
        assertNames(mapper.selectPage(28, new PageObject(0, 2)), "RefMapB", "RefMapC");
        assertTrue(mapper.selectPage(999, new PageObject(0, 2)).isEmpty());
    }

    // 能力归属：Mapper 文件 / 分页查询。
    @Test
    @Capability(value = CapabilityId.MAPPER_XML_REF_PAGE_RESULT, column = "mapper-files/pagination/file-pagination")
    public void refMapper_shouldReturnPageResultWithFilteredTotalsAndOriginalPage() throws Exception {
        XmlRefPageResultMapper mapper = createPagingMapper(XmlRefPageResultMapper.class);

        PageResult<UserInfo> filtered = mapper.selectPage(28, new PageObject(0, 2));
        assertNames(filtered.getData(), "RefMapB", "RefMapC");
        assertEquals(3, filtered.getTotalCount());
        assertEquals(2, filtered.getTotalPage());
        assertEquals(0, filtered.getCurrentPage());
        assertEquals(2, filtered.getPageSize());

        PageResult<UserInfo> last = mapper.selectPage(28, new PageObject(1, 2));
        assertNames(last.getData(), "RefMapD");
        assertEquals(3, last.getTotalCount());
        assertEquals(1, last.getCurrentPage());

        PageResult<UserInfo> beyond = mapper.selectPage(28, new PageObject(4, 2));
        assertTrue(beyond.getData().isEmpty());
        assertEquals(3, beyond.getTotalCount());
        PageResult<UserInfo> empty = mapper.selectPage(999, new PageObject(0, 2));
        assertTrue(empty.getData().isEmpty());
        assertEquals(0, empty.getTotalCount());

        Page oneBased = PageObject.of(2, 2, 1);
        PageResult<UserInfo> offset = mapper.selectPage(0, oneBased);
        assertNames(offset.getData(), "RefMapC", "RefMapD");
        assertEquals(4, offset.getTotalCount());
        assertEquals(oneBased.getCurrentPage(), offset.getCurrentPage());
        assertEquals(oneBased.getPageNumberOffset(), offset.getPageNumberOffset());
        assertEquals(oneBased.getPageSize(), offset.getPageSize());
        assertEquals(oneBased.getFirstRecordPosition(), offset.getFirstRecordPosition());
    }

    protected String pageQueryCommand() {
        return "SELECT id, name, age, email FROM user_info WHERE age >= #{minAge} ORDER BY " + orderExpression("id");
    }

    private <T> T createPagingMapper(Class<T> mapperType) throws Exception {
        Configuration configuration = newConfiguration();
        configuration.addMacro("xmlRefPageQuery", pageQueryCommand());
        Session pagingSession;
        if (this.jdbcTemplate.getConnection() != null) {
            pagingSession = configuration.newSession(this.jdbcTemplate.getConnection());
        } else {
            pagingSession = configuration.newSession(dataSource);
        }
        // Keep the two return-type contracts independent when mapper initialization fails.
        return pagingSession.createMapper(mapperType);
    }

    private void assertNames(List<UserInfo> rows, String... expected) {
        assertEquals(Arrays.asList(expected), rows.stream().map(UserInfo::getName).toList());
    }
}
