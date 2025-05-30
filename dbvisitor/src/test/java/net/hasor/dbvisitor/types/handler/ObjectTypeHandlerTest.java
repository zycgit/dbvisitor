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
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ObjectTypeHandlerTest {
    @Test
    public void testObjectTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Set<String> testSet = new HashSet<>(Arrays.asList("a", "b", "c"));
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_object) values (?);", new Object[] { testSet });
            List<Object> dat = jdbcTemplate.queryForList("select c_object from tb_h2_types where c_object is not null limit 1;", (rs, rowNum) -> {
                return new ObjectTypeHandler().getResult(rs, 1);
            });
            assert dat.get(0) != testSet;
            assert dat.get(0) instanceof Set;
            assert ((Set<?>) dat.get(0)).size() == 3;
            assert ((Set<?>) dat.get(0)).contains("a");
            assert ((Set<?>) dat.get(0)).contains("b");
            assert ((Set<?>) dat.get(0)).contains("c");
        }
    }

    @Test
    public void testObjectTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Set<String> testSet = new HashSet<>(Arrays.asList("a", "b", "c"));
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_object) values (?);", new Object[] { testSet });
            List<Object> dat = jdbcTemplate.queryForList("select c_object from tb_h2_types where c_object is not null limit 1;", (rs, rowNum) -> {
                return new ObjectTypeHandler().getResult(rs, "c_object");
            });
            assert dat.get(0) != testSet;
            assert dat.get(0) instanceof Set;
            assert ((Set<?>) dat.get(0)).size() == 3;
            assert ((Set<?>) dat.get(0)).contains("a");
            assert ((Set<?>) dat.get(0)).contains("b");
            assert ((Set<?>) dat.get(0)).contains("c");
        }
    }

    @Test
    public void testObjectTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Set<String> testSet = new HashSet<>(Arrays.asList("a", "b", "c"));
            List<Object> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new ObjectTypeHandler().setParameter(ps, 1, testSet, JDBCType.OTHER.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new ObjectTypeHandler().getNullableResult(rs, 1);
            });

            assert dat.get(0) != testSet;
            assert dat.get(0) instanceof Set;
            assert ((Set<?>) dat.get(0)).size() == 3;
            assert ((Set<?>) dat.get(0)).contains("a");
            assert ((Set<?>) dat.get(0)).contains("b");
            assert ((Set<?>) dat.get(0)).contains("c");
        }
    }

    @Test
    public void testObjectTypeHandler_4() throws SQLException {
        //        try (Connection conn = DsUtils.localMySQL()) {
        //            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
        //            jdbcTemplate.execute("drop procedure if exists proc_double;");
        //            jdbcTemplate.execute("create procedure proc_double(out p_out double) begin set p_out=123.123; end;");
        //
        //            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_double(?)}",//
        //                    Collections.singletonList(CallableSqlParameter.withOutput("out", JDBCType.DOUBLE, new DoubleTypeHandler())));
        //
        //            assert objectMap.size() == 2;
        //            assert objectMap.get("out") instanceof Double;
        //            assert objectMap.get("out").equals(123.123d);
        //            assert objectMap.get("#update-count-1").equals(0);
        //        }
    }
}
