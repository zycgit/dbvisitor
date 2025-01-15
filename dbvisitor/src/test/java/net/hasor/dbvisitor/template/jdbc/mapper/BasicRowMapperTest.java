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
package net.hasor.dbvisitor.template.jdbc.mapper;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.junit.Test;

import java.sql.ResultSet;

public class BasicRowMapperTest {
    @Test
    public void testColumnMapRowMapper_1() {
        AbstractRowMapper rowMapper = new AbstractRowMapper() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum) {
                return null;
            }
        };
        assert rowMapper.getHandlerRegistry() == TypeHandlerRegistry.DEFAULT;
    }

    @Test
    public void testColumnMapRowMapper_2() {
        assert !new ColumnMapRowMapper(false, TypeHandlerRegistry.DEFAULT).isCaseInsensitive();
        assert new ColumnMapRowMapper(true, TypeHandlerRegistry.DEFAULT).isCaseInsensitive();
    }
}
