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
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import net.hasor.cobble.time.DateTimeFormat;
import net.hasor.dbvisitor.jdbc.core.JdbcQueryContext;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.types.custom.MyStringTypeHandler1;
import net.hasor.dbvisitor.types.custom.MyTypeHandler;
import net.hasor.dbvisitor.types.custom.UserFutures2;
import net.hasor.dbvisitor.types.custom.UserTable;
import net.hasor.dbvisitor.types.handler.json.JsonUseForFastjson2TypeHandler;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

public class AnnosTest {
    @Test
    public void testArrayTypeHandler_1() {
        TypeHandlerRegistry registry = new TypeHandlerRegistry();
        registry.registerHandler(MyTypeHandler.class, new MyTypeHandler());
        registry.register(Types.VARCHAR, new MyTypeHandler());
        registry.register(StringBuilder.class, new MyTypeHandler());
        registry.register(Types.BIGINT, InputStream.class, new MyTypeHandler());

        assert registry.hasTypeHandler(StringBuilder.class);
        assert registry.getTypeHandler(StringBuilder.class) instanceof MyTypeHandler;

        assert registry.getTypeHandler(Types.VARCHAR) instanceof MyTypeHandler;

        assert registry.getTypeHandler(String.class, Types.DATALINK) instanceof MyTypeHandler;
        assert registry.getTypeHandler(StringBuffer.class, Types.VARCHAR) instanceof MyTypeHandler;
        assert registry.getTypeHandler(InputStream.class, Types.BIGINT) instanceof MyTypeHandler;
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
            assert DateTimeFormat.WKF_DATE_TIME24.format(list.get(0).getCreateTime2()).equals("2021-05-11 00:00:00");
        }
    }

    @Test
    public void customTypeHandlerTest_2() throws SQLException {
        TypeHandlerRegistry registry = new TypeHandlerRegistry();
        MappingRegistry context = new MappingRegistry(null, registry, null);
        JdbcQueryContext jqc = new JdbcQueryContext();
        jqc.setTypeRegistry(registry);

        MyStringTypeHandler1 handler1 = new MyStringTypeHandler1();
        registry.registerHandler(MyStringTypeHandler1.class, handler1);
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbc = new JdbcTemplate(c, context, jqc);

            jdbc.execute("insert into user_table values (10, 'Verdi', 66, '2021-05-11 00:00:00');");

            String arg = "Verdi";
            assert !handler1.isReadMark();
            List<UserTable> list = jdbc.queryForList("select * from user_table where name = ?", arg, UserTable.class);

            assert list.size() == 1;
            assert list.get(0).getName().equals("Verdi");

            assert handler1.isReadMark();
        }
    }

    @Test
    public void testBindTypeHandler_2() {
        assert TypeHandlerRegistry.DEFAULT.hasTypeHandler(UserFutures2.class);
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(UserFutures2.class) instanceof JsonUseForFastjson2TypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(UserFutures2.class) == TypeHandlerRegistry.DEFAULT.getTypeHandler(UserFutures2.class);
    }

}
