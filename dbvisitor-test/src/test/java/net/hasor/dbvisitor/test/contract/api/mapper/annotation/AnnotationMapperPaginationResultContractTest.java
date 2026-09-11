/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@NxnContract
public abstract class AnnotationMapperPaginationResultContractTest extends AnnotationMapperResultMappingSupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_PAGE)
    public void queryResult_shouldApplyPageObjectToListResult() throws Exception {
        List<UserInfo> firstPage = queryPage(new PageObject(0, 5));
        List<UserInfo> secondPage = queryPage(new PageObject(1, 5));

        assertEquals(5, firstPage.size());
        assertEquals(5, secondPage.size());
        assertEquals("AnnoResult1", firstPage.get(0).getName());
        assertEquals("AnnoResult6", secondPage.get(0).getName());
        assertNotEquals(firstPage.get(0).getId(), secondPage.get(0).getId());
    }

    protected List<UserInfo> queryPage(PageObject page) {
        return this.mapper.selectUsersWithPagination(PATTERN, page);
    }
}
