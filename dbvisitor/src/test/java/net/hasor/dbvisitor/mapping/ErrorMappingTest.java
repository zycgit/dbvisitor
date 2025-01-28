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
package net.hasor.dbvisitor.mapping;
import net.hasor.dbvisitor.mapping.dto.UserInfoUsingMap;
import org.junit.Test;

public class ErrorMappingTest {

    @Test
    public void alreadyExists() {
        try {
            MappingRegistry registry = new MappingRegistry();
            registry.loadMapper("/dbvisitor_coverage/basic_mapping/error_1.xml");
            registry.loadResultMapToSpace(UserInfoUsingMap.class);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().contains("the resultMap 'net.hasor.dbvisitor.mapping.dto.userInfo' already exists.");
        }
    }
}

