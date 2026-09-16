/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dialect;

import java.util.List;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.page.PageResult;
import org.junit.Test;
import static org.junit.Assert.*;

public class PageResultTest {
    @Test
    public void copiedPage_shouldPreserveItsNumberAndRecordOffset() {
        for (int offset : new int[] { 0, 1, 5 }) {
            Page source = PageObject.of(offset + 2, 3, offset);
            source.setTotalCount(11);
            List<String> rows = List.of("first", "second");

            PageResult<String> result = new PageResult<>(source, rows);

            assertEquals(source.getCurrentPage(), result.getCurrentPage());
            assertEquals(source.getPageNumberOffset(), result.getPageNumberOffset());
            assertEquals(source.getPageSize(), result.getPageSize());
            assertEquals(source.getTotalCount(), result.getTotalCount());
            assertEquals(source.getTotalPage(), result.getTotalPage());
            assertEquals(6, result.getFirstRecordPosition());
            assertSame(rows, result.getData());

            result.nextPage();
            assertEquals(offset + 3, result.getCurrentPage());
            assertEquals(9, result.getFirstRecordPosition());
            assertEquals(offset + 2, source.getCurrentPage());
        }
    }

    @Test
    public void copiedEmptyPage_shouldKeepFirstPageAndEmptyData() {
        Page source = PageObject.of(1, 5, 1);
        PageResult<String> result = new PageResult<>(source);
        assertEquals(1, result.getCurrentPage());
        assertEquals(0, result.getFirstRecordPosition());
        assertTrue(result.getData().isEmpty());
        assertTrue(new PageResult<>(source, null).getData().isEmpty());
    }
}
