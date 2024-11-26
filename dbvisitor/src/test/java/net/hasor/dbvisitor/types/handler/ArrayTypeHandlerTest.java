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
package net.hasor.dbvisitor.types.handler;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.types.handler.array.ArrayTypeHandler;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.JDBCType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArrayTypeHandlerTest {
    @Test
    public void testArrayTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Set<String> testSet = new HashSet<>(Arrays.asList("a", "b", "c"));
            jdbcTemplate.executeUpdate("insert into tb_h2_types (a_char) values (?);", ps -> {
                new ArrayTypeHandler().setParameter(ps, 1, testSet.toArray(), JDBCType.ARRAY.getVendorTypeNumber());
            });
            List<Object> dat = jdbcTemplate.queryForList("select a_char from tb_h2_types where a_char is not null limit 1;", (rs, rowNum) -> {
                return new ArrayTypeHandler().getResult(rs, 1);
            });
            assert dat.get(0) != testSet;
            assert dat.get(0) instanceof Object[];
            assert ((Object[]) dat.get(0)).length == 3;
            assert ((Object[]) dat.get(0))[0].equals("a");
            assert ((Object[]) dat.get(0))[1].equals("b");
            assert ((Object[]) dat.get(0))[2].equals("c");
        }
    }

    @Test
    public void testArrayTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Set<String> testSet = new HashSet<>(Arrays.asList("a", "b", "c"));
            jdbcTemplate.executeUpdate("insert into tb_h2_types (a_char) values (?);", ps -> {
                new ArrayTypeHandler().setParameter(ps, 1, testSet.toArray(), JDBCType.ARRAY.getVendorTypeNumber());
            });
            List<Object> dat = jdbcTemplate.queryForList("select a_char from tb_h2_types where a_char is not null limit 1;", (rs, rowNum) -> {
                return new ArrayTypeHandler().getResult(rs, "a_char");
            });
            assert dat.get(0) != testSet;
            assert dat.get(0) instanceof Object[];
            assert ((Object[]) dat.get(0)).length == 3;
            assert ((Object[]) dat.get(0))[0].equals("a");
            assert ((Object[]) dat.get(0))[1].equals("b");
            assert ((Object[]) dat.get(0))[2].equals("c");
        }
    }
}
