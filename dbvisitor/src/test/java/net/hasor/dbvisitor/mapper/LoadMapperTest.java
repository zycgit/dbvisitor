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
package net.hasor.dbvisitor.mapper;

import org.junit.Test;

import java.io.IOException;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-12-10
 */
public class LoadMapperTest {
    @Test
    public void load_1() throws IOException {
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
