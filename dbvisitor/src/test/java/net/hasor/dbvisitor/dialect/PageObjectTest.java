/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.dialect;
import org.junit.Test;

import java.util.Map;

/**
 * 分页器的功能测试。
 */
public class PageObjectTest {
    @Test
    public void pageTest_1() {
        Page page = new PageObject();
        assert page.getPageSize() == 0;
        assert page.getCurrentPage() == 0;
        assert page.getPageNumberOffset() == 0;
        assert page.getFirstRecordPosition() == 0;
        assert page.getTotalPage() == 0;
        assert page.getTotalCount() == 0;

        Page page2 = new PageObject();
        page2.setPageSize(-1000);
        assert page2.getPageSize() == 0;

        Page page3 = new PageObject();
        page3.setCurrentPage(-1000);
        assert page3.getCurrentPage() == 0;
        page3.setPageNumberOffset(2);
        assert page3.getCurrentPage() == 2;
        page3.setPageNumberOffset(-1000);
        assert page3.getCurrentPage() == 0;
    }

    @Test
    public void pageTest_2() {
        Page page = new PageObject();
        page.setPageSize(10);
        assert page.getPageSize() == 10;
        assert page.getCurrentPage() == 0;
        assert page.getPageNumberOffset() == 0;
        assert page.getFirstRecordPosition() == 0;
        assert page.getTotalPage() == 0;
        assert page.getTotalCount() == 0;
    }

    @Test
    public void pageTest_3() {
        Page page1 = new PageObject(0, 10, 10L);
        assert page1.getPageSize() == 10;
        assert page1.getCurrentPage() == 0;
        assert page1.getPageNumberOffset() == 0;
        assert page1.getFirstRecordPosition() == 0;
        assert page1.getTotalPage() == 1;
        assert page1.getTotalCount() == 10;

        Page page2 = new PageObject(0, 10, 7L);
        assert page2.getPageSize() == 10;
        assert page2.getCurrentPage() == 0;
        assert page2.getPageNumberOffset() == 0;
        assert page2.getFirstRecordPosition() == 0;
        assert page2.getTotalPage() == 1;
        assert page2.getTotalCount() == 7;

        Page page3 = new PageObject(0, 0, 7L);
        assert page3.getPageSize() == 0;
        assert page3.getCurrentPage() == 0;
        assert page3.getPageNumberOffset() == 0;
        assert page3.getFirstRecordPosition() == 0;
        assert page3.getTotalPage() == 1;
        assert page3.getTotalCount() == 7;
    }

    @Test
    public void pageTest_4() {
        Page page1 = new PageObject(0, 10, 10L);
        page1.setPageNumberOffset(2);
        assert page1.getPageSize() == 10;
        assert page1.getCurrentPage() == 2;     // offset +2
        assert page1.getPageNumberOffset() == 2;// offset
        assert page1.getFirstRecordPosition() == 0;
        assert page1.getTotalPage() == 3;       // offset +2
        assert page1.getTotalCount() == 10;
    }

    @Test
    public void pageTest_5() {
        Page page1 = new PageObject(0, 4, 15L);
        assert page1.getTotalPage() == 4;

        Page page2 = new PageObject(0, 4, 16L);
        assert page2.getTotalPage() == 4;

        Page page3 = new PageObject(0, 4, 17L);
        assert page3.getTotalPage() == 5;
    }

    @Test
    public void pageTest_6() {
        Page page1 = new PageObject(0, 4, 15L);
        page1.setCurrentPage(1);
        assert page1.getFirstRecordPosition() == 4;
        page1.setCurrentPage(4);
        assert page1.getFirstRecordPosition() == 16;

        Page page2 = new PageObject(0, 4, 16L);
        page2.setCurrentPage(1);
        assert page2.getFirstRecordPosition() == 4;
        page2.setCurrentPage(4);
        assert page2.getFirstRecordPosition() == 16;

        Page page3 = new PageObject(0, 4, 17L);
        page3.setCurrentPage(1);
        assert page3.getFirstRecordPosition() == 4;
        page3.setCurrentPage(4);
        assert page3.getFirstRecordPosition() == 16;
    }

    @Test
    public void pageTest_7() {
        Page page = new PageObject(0, 4, 15L);
        assert page.getFirstRecordPosition() == 0;

        page.nextPage();
        assert page.getFirstRecordPosition() == 4;
        page.nextPage();
        assert page.getFirstRecordPosition() == 8;
        page.nextPage();
        assert page.getFirstRecordPosition() == 12;
        page.nextPage();
        assert page.getFirstRecordPosition() == 16;
        page.nextPage();
        assert page.getFirstRecordPosition() == 20;
    }

    @Test
    public void pageTest_8() {
        Page page = new PageObject(0, 4, 15L);
        page.setCurrentPage(6);
        assert page.getFirstRecordPosition() == 24;

        page.previousPage();
        assert page.getFirstRecordPosition() == 20;
        page.previousPage();
        assert page.getFirstRecordPosition() == 16;
        page.previousPage();
        assert page.getFirstRecordPosition() == 12;
        page.previousPage();
        assert page.getFirstRecordPosition() == 8;
        page.previousPage();
        assert page.getFirstRecordPosition() == 4;
        page.previousPage();
        assert page.getFirstRecordPosition() == 0;
        page.previousPage();
        assert page.getFirstRecordPosition() == 0;
    }

    @Test
    public void pageTest_9() {
        Page page = new PageObject(0, 4, 15L);
        page.setCurrentPage(6);
        assert page.getFirstRecordPosition() == 24;

        page.firstPage();
        assert page.getFirstRecordPosition() == 0;
        page.lastPage();
        assert page.getFirstRecordPosition() == 12;
    }

    @Test
    public void pageTest_10() {
        Page page = new PageObject(0, 4, 15L);
        page.setCurrentPage(6);
        Map<String, Object> pageInfo = page.toPageInfo();

        assert pageInfo.get("enable").equals(true);
        assert pageInfo.get("pageSize").equals(4L);
        assert pageInfo.get("totalCount").equals(15L);
        assert pageInfo.get("totalPage").equals(4L);
        assert pageInfo.get("currentPage").equals(6L);
        assert pageInfo.get("recordPosition").equals(24L);

        page.setPageNumberOffset(2);
        pageInfo = page.toPageInfo();
        assert pageInfo.get("enable").equals(true);
        assert pageInfo.get("pageSize").equals(4L);
        assert pageInfo.get("totalCount").equals(15L);
        assert pageInfo.get("totalPage").equals(6L);// 4+2
        assert pageInfo.get("currentPage").equals(8L);// 6(CurrentPage) +2
        assert pageInfo.get("recordPosition").equals(24L);
    }

    @Test
    public void pageTest_11() {
        Page page = new PageObject(0, 0, 15L);
        page.setCurrentPage(6);
        Map<String, Object> pageInfo = page.toPageInfo();

        assert pageInfo.get("enable").equals(false);
        assert pageInfo.get("pageSize").equals(0L);
        assert pageInfo.get("totalCount").equals(15L);
        assert pageInfo.get("totalPage").equals(1L);
        assert pageInfo.get("currentPage").equals(0L);
        assert pageInfo.get("recordPosition").equals(0L);

        page.setPageNumberOffset(2);
        pageInfo = page.toPageInfo();
        assert pageInfo.get("enable").equals(false);
        assert pageInfo.get("pageSize").equals(0L);
        assert pageInfo.get("totalCount").equals(15L);
        assert pageInfo.get("totalPage").equals(3L);
        assert pageInfo.get("currentPage").equals(2L);
        assert pageInfo.get("recordPosition").equals(0L);
    }

    @Test
    public void pageTest_12() {
        Page page = new PageObject();
        assert page.getPageSize() == 0;
        assert page.getCurrentPage() == 0;
        assert page.getPageNumberOffset() == 0;
        assert page.getFirstRecordPosition() == 0;
        assert page.getTotalPage() == 0;
        assert page.getTotalCount() == 0;

        Page page2 = new PageObject();
        page2.setPageSize(-1000);
        assert page2.getPageSize() == 0;

        Page page3 = new PageObject();
        page3.setCurrentPage(-1000);
        assert page3.getCurrentPage() == 0;
        page3.setPageNumberOffset(2);
        assert page3.getCurrentPage() == 2;
        page3.setPageNumberOffset(-1000);
        assert page3.getCurrentPage() == 0;
    }
}
