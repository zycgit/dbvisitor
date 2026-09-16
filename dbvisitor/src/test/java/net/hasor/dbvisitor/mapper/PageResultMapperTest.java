/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapper;

import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageResult;
import org.junit.Test;
import static org.junit.Assert.*;

public class PageResultMapperTest {
    @Test
    public void pageResult_shouldMapItsRowTypeInsteadOfThePageWrapper() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(PagedMapper.class);

        assertEquals(Integer.class, registry.findStatement(PagedMapper.class, "scalarPage").getResultType());
        assertEquals(Map.class, registry.findStatement(PagedMapper.class, "mapPage").getResultType());
        assertEquals(Integer.class, registry.findStatement(PagedMapper.class, "list").getResultType());
        assertEquals(Integer.class, registry.findStatement(PagedMapper.class, "scalar").getResultType());
    }

    @SimpleMapper
    public interface PagedMapper {
        @Query("SELECT id FROM user_info")
        PageResult<Integer> scalarPage(Page page);

        @Query("SELECT id FROM user_info")
        PageResult<Map<String, Object>> mapPage(Page page);

        @Query("SELECT id FROM user_info")
        List<Integer> list();

        @Query("SELECT id FROM user_info")
        Integer scalar();
    }
}
