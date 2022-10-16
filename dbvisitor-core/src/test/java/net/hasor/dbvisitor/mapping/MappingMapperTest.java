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
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.jdbc.mapper.MappingResultSetExtractor;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.mapping.resolve.ClassTableMappingResolve;
import net.hasor.dbvisitor.mapping.resolve.TableMappingResolve;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import net.hasor.test.dto.TbUser;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public class MappingMapperTest {
    @Test
    public void testColumnMapRowMapper_1() throws ClassNotFoundException {
        TableMappingResolve resolve = new ClassTableMappingResolve();

        TableMapping<TbUser> tableMapping = resolve.resolveTableMapping(//
                TbUser.class, Thread.currentThread().getContextClassLoader(), TypeHandlerRegistry.DEFAULT, null);

        assert tableMapping.getSchema().equals("");
        assert tableMapping.getTable().equals("tb_user");
        assert tableMapping.getPropertyByColumn("loginPassword").getProperty().equals("password");
        assert tableMapping.getPropertyByName("password").getColumn().equals("loginPassword");

    }

    @Test
    public void testColumnMapRowMapper_2() throws Throwable {
        try (Connection c = DsUtils.createConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
            TableMappingResolve resolve = new ClassTableMappingResolve();

            TableMapping<TbUser> tableMapping = resolve.resolveTableMapping(//
                    TbUser.class, Thread.currentThread().getContextClassLoader(), TypeHandlerRegistry.DEFAULT, null);

            MappingResultSetExtractor<TbUser> extractor = new MappingResultSetExtractor<>(tableMapping.toReader());

            List<TbUser> userList = jdbcTemplate.query("select * from tb_user order by loginName", extractor);

            assert userList.get(0).getAccount().equals("belon");
            assert userList.get(1).getAccount().equals("feiyan");
            assert userList.get(2).getAccount().equals("muhammad");
        }
    }

    @Test
    public void testColumnMapRowMapper_3() throws Throwable {
        try (Connection c = DsUtils.createConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
            TableMappingResolve resolve = new ClassTableMappingResolve();

            TableMapping<TbUser> tableMapping = resolve.resolveTableMapping(//
                    TbUser.class, Thread.currentThread().getContextClassLoader(), TypeHandlerRegistry.DEFAULT, null);

            MappingResultSetExtractor<Map<String, Object>> extractor = new MappingResultSetExtractor<>(tableMapping.toMapReader());

            List<Map<String, Object>> userList = jdbcTemplate.query("select * from tb_user order by loginName", extractor);

            assert userList.get(0).get("account").equals("belon");
            assert userList.get(1).get("account").equals("feiyan");
            assert userList.get(2).get("account").equals("muhammad");
        }
    }
}
