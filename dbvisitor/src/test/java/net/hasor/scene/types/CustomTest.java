package net.hasor.scene.types;
import net.hasor.cobble.WellKnowFormat;
import net.hasor.dbvisitor.dynamic.DynamicContext;
import net.hasor.dbvisitor.dynamic.rule.RuleRegistry;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import net.hasor.scene.types.custom.MyStringTypeHandler1;
import net.hasor.scene.types.custom.UserTable;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class CustomTest {
    @Test
    public void typeHandlerTest_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbc = new JdbcTemplate(c);

            jdbc.execute("insert into user_table values (10, 'Verdi', 66, '2021-05-11 00:00:00');");

            String arg = "2021-05-11";
            List<UserTable> list = jdbc.queryForList("select * from user_table where create_time = #{arg0, typeHandler=net.hasor.scene.types.custom.MyDateTypeHandler}", arg, UserTable.class);

            assert list.size() == 1;
            assert list.get(0).getName().equals("Verdi");
            assert list.get(0).getCreateTime1().equals("2021-05-11");
            assert WellKnowFormat.WKF_DATE_TIME24.format(list.get(0).getCreateTime2()).equals("2021-05-11 00:00:00");
        }
    }

    @Test
    public void typeHandlerTest_2() throws SQLException {
        TypeHandlerRegistry registry = new TypeHandlerRegistry();
        DynamicContext context = new DynamicContext(registry, RuleRegistry.DEFAULT);



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
