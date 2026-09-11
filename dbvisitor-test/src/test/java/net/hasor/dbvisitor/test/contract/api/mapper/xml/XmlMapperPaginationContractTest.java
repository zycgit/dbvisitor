/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.xml;

import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.page.PageResult;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class XmlMapperPaginationContractTest extends XmlMapperCrudSupport {
    @Test
    @Capability(CapabilityId.MAPPER_XML_QUERY_PAGE)
    public void xmlMapperQueryStatement_shouldApplyPageObjectBoundaries() throws Exception {
        List<UserInfo> first = this.session.queryStatement("xmltest.CrudMapper.selectForPage", null, new PageObject(0, 3));
        List<UserInfo> second = this.session.queryStatement("xmltest.CrudMapper.selectForPage", null, new PageObject(1, 2));
        List<UserInfo> lastPartial = this.session.queryStatement("xmltest.CrudMapper.selectForPage", null, new PageObject(2, 2));
        List<UserInfo> beyond = this.session.queryStatement("xmltest.CrudMapper.selectForPage", null, new PageObject(5, 2));
        List<UserInfo> largerThanTotal = this.session.queryStatement("xmltest.CrudMapper.selectForPage", null, new PageObject(0, 100));
        List<UserInfo> firstSingle = this.session.queryStatement("xmltest.CrudMapper.selectForPage", null, new PageObject(0, 1));
        List<UserInfo> lastSingle = this.session.queryStatement("xmltest.CrudMapper.selectForPage", null, new PageObject(4, 1));

        assertEquals(3, first.size());
        assertEquals("XmlCrud1", first.get(0).getName());
        assertEquals("XmlCrud3", first.get(2).getName());
        assertEquals(2, second.size());
        assertEquals("XmlCrud3", second.get(0).getName());
        assertEquals("XmlCrud4", second.get(1).getName());
        assertEquals(1, lastPartial.size());
        assertEquals("XmlCrud5", lastPartial.get(0).getName());
        assertTrue(beyond.isEmpty());
        assertEquals(5, largerThanTotal.size());
        assertEquals("XmlCrud1", firstSingle.get(0).getName());
        assertEquals("XmlCrud5", lastSingle.get(0).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_PAGE_STATEMENT)
    public void xmlMapperPageStatement_shouldReturnPageResult() throws Exception {
        Page page = new PageObject(1, 2);

        PageResult<UserInfo> result = this.session.pageStatement("xmltest.CrudMapper.selectForPage", null, page);

        assertNotNull(result);
        assertFalse(result.getData().isEmpty());
        assertEquals(2, result.getData().size());
        assertEquals("XmlCrud3", result.getData().get(0).getName());
        assertEquals("XmlCrud4", result.getData().get(1).getName());
        assertEquals(5, result.getTotalCount());
        assertEquals(3, result.getTotalPage());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_PAGE_STATEMENT_BOUNDARY)
    public void xmlMapperPageStatement_shouldReturnTotalsForBoundaryPagesAndOffset() throws Exception {
        PageResult<UserInfo> first = this.session.pageStatement("xmltest.CrudMapper.selectForPage", null, new PageObject(0, 2));
        PageResult<UserInfo> lastPartial = this.session.pageStatement("xmltest.CrudMapper.selectForPage", null, new PageObject(2, 2));
        PageResult<UserInfo> beyond = this.session.pageStatement("xmltest.CrudMapper.selectForPage", null, new PageObject(10, 2));
        PageResult<UserInfo> exactOnePage = this.session.pageStatement("xmltest.CrudMapper.selectForPage", null, new PageObject(0, 5));

        PageObject oneBased = new PageObject();
        oneBased.setPageNumberOffset(1);
        oneBased.setPageSize(2);
        oneBased.setCurrentPage(1);
        PageResult<UserInfo> offsetFirst = this.session.pageStatement("xmltest.CrudMapper.selectForPage", null, oneBased);
        oneBased.setCurrentPage(2);
        PageResult<UserInfo> offsetSecond = this.session.pageStatement("xmltest.CrudMapper.selectForPage", null, oneBased);

        assertEquals(2, first.getData().size());
        assertEquals("XmlCrud1", first.getData().get(0).getName());
        assertEquals(5, first.getTotalCount());
        assertEquals(3, first.getTotalPage());
        assertEquals(1, lastPartial.getData().size());
        assertEquals("XmlCrud5", lastPartial.getData().get(0).getName());
        assertTrue(beyond.getData().isEmpty());
        assertEquals(5, beyond.getTotalCount());
        assertEquals(5, exactOnePage.getData().size());
        assertEquals(1, exactOnePage.getTotalPage());
        assertEquals("XmlCrud1", offsetFirst.getData().get(0).getName());
        assertEquals("XmlCrud3", offsetSecond.getData().get(0).getName());
    }
}
