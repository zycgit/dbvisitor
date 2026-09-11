/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.jdbc.mapper;
import java.sql.ResultSet;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.junit.Test;

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
