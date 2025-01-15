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
package net.hasor.dbvisitor.types;
import net.hasor.cobble.WellKnowFormat;
import net.hasor.dbvisitor.dynamic.MacroRegistry;
import net.hasor.dbvisitor.dynamic.RegistryManager;
import net.hasor.dbvisitor.dynamic.rule.RuleRegistry;
import net.hasor.dbvisitor.template.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.types.custom.MyStringTypeHandler1;
import net.hasor.dbvisitor.types.custom.UserTable;
import net.hasor.test.types.MyTypeHandler;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

public class AnnosTest {
    @Test
    public void testArrayTypeHandler_1() {
        TypeHandlerRegistry.DEFAULT.registerHandler(MyTypeHandler.class, new MyTypeHandler());
        TypeHandlerRegistry.DEFAULT.register(Types.VARCHAR, new MyTypeHandler());
        TypeHandlerRegistry.DEFAULT.register(StringBuilder.class, new MyTypeHandler());
        TypeHandlerRegistry.DEFAULT.register(Types.BIGINT, InputStream.class, new MyTypeHandler());

        assert TypeHandlerRegistry.DEFAULT.hasTypeHandler(StringBuilder.class);
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(StringBuilder.class) instanceof MyTypeHandler;

        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(Types.VARCHAR) instanceof MyTypeHandler;

        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(String.class, Types.DATALINK) instanceof MyTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(StringBuffer.class, Types.VARCHAR) instanceof MyTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(InputStream.class, Types.BIGINT) instanceof MyTypeHandler;
    }

    @Test
    public void customTypeHandlerTest_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbc = new JdbcTemplate(c);

            jdbc.execute("insert into user_table values (10, 'Verdi', 66, '2021-05-11 00:00:00');");

            String arg = "2021-05-11";
            List<UserTable> list = jdbc.queryForList("select * from user_table where create_time = #{arg0, typeHandler=net.hasor.dbvisitor.types.custom.MyDateTypeHandler}", arg, UserTable.class);

            assert list.size() == 1;
            assert list.get(0).getName().equals("Verdi");
            assert list.get(0).getCreateTime1().equals("2021-05-11");
            assert WellKnowFormat.WKF_DATE_TIME24.format(list.get(0).getCreateTime2()).equals("2021-05-11 00:00:00");
        }
    }

    @Test
    public void customTypeHandlerTest_2() throws SQLException {
        TypeHandlerRegistry registry = new TypeHandlerRegistry();
        RegistryManager context = new RegistryManager(registry, RuleRegistry.DEFAULT, MacroRegistry.DEFAULT);

        MyStringTypeHandler1 handler1 = new MyStringTypeHandler1();
        registry.registerHandler(MyStringTypeHandler1.class, handler1);

        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbc = new JdbcTemplate(c, context);

            jdbc.execute("insert into user_table values (10, 'Verdi', 66, '2021-05-11 00:00:00');");

            String arg = "Verdi";
            assert !handler1.isReadMark();
            List<UserTable> list = jdbc.queryForList("select * from user_table where name = ?", arg, UserTable.class);

            assert list.size() == 1;
            assert list.get(0).getName().equals("Verdi");

            assert handler1.isReadMark();
        }
    }

}
