/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.basemapper;

import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class BaseMapperPaginationContractTest extends BaseMapperCrudSupport {
    @Test
    @Capability(CapabilityId.BASEMAPPER_PAGE_BY_SAMPLE)
    public void baseMapperPageBySample_shouldReturnPagedRows() {
        for (int i = 1; i <= 8; i++) {
            this.mapper.insert(user(baseId() + 60 + i, "BasePage" + i, 71, null));
        }
        UserInfo sample = new UserInfo();
        sample.setAge(71);

        Page page = this.mapper.pageInitBySample(sample, 1, 3);
        List<UserInfo> loaded = this.mapper.pageBySample(sample, page).getData();

        assertEquals(8, page.getTotalCount());
        assertEquals(3, page.getTotalPage());
        assertEquals(3, loaded.size());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_PAGE_BOUNDARY)
    public void baseMapperPageBySample_shouldHandleZeroLargeSizeAndEmptySamplePages() {
        for (int i = 1; i <= 10; i++) {
            this.mapper.insert(user(baseId() + 420 + i, "BasePageBoundary" + i, 40, null));
        }

        UserInfo sample = new UserInfo();
        sample.setAge(40);

        PageObject first = new PageObject();
        first.setPageSize(5);
        first.setCurrentPage(0);
        assertEquals(5, this.mapper.pageBySample(sample, first).getData().size());

        PageObject large = new PageObject();
        large.setPageSize(10);
        large.setCurrentPage(100);
        assertEquals(0, this.mapper.pageBySample(sample, large).getData().size());

        PageObject zeroSize = new PageObject();
        zeroSize.setPageSize(0);
        zeroSize.setCurrentPage(0);
        assertEquals(10, this.mapper.pageBySample(sample, zeroSize).getData().size());

        PageObject emptySamplePage = new PageObject();
        emptySamplePage.setPageSize(5);
        emptySamplePage.setCurrentPage(0);
        assertTrue(this.mapper.pageBySample(new UserInfo(), emptySamplePage).getData().size() >= 5);
    }
}
