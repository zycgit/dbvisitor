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

import net.hasor.dbvisitor.mapper.dto.Error1Mapper;
import net.hasor.dbvisitor.mapper.dto.Error2Mapper;
import org.junit.Test;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-12-10
 */
public class ErrorMapperTest {
    @Test
    public void error_1() {
        try {
            MapperRegistry registry = new MapperRegistry();
            registry.loadMapper(Error1Mapper.class);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("the wrapper type 'java.lang.Short' is returned as the primitive type 'short'");
        }
    }

    @Test
    public void error_2() {
        try {
            MapperRegistry registry = new MapperRegistry();
            registry.loadMapper(Error2Mapper.class);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("the type 'java.util.Date' cannot be as 'java.sql.Time'");
        }
    }

}
