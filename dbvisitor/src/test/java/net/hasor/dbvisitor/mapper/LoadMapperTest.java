/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapper;

import org.junit.Test;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
 */
public class LoadMapperTest {
    @Test
    public void load_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");
    }

    @Test
    public void load_2() {
        try {
            MapperRegistry registry = new MapperRegistry();
            registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_4.xml");
            registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_5.xml");
            assert false;
        } catch (Exception e) {
            assert e.getMessage().contains("the resultMap 'pojo_bean1' already exists.");
        }
    }
}
