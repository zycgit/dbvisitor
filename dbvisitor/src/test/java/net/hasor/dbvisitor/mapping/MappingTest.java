/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapping;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import net.hasor.cobble.time.DateTimeFormat;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.mapping.dto.MultipleMappingBean1;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

public class MappingTest {

    @Test
    public void multipleMappingTest_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbc = new JdbcTemplate(c);

            jdbc.execute("insert into user_table values (10, 'Verdi', 66, '2021-05-11 00:00:00');");

            String arg = "2021-05-11";
            List<MultipleMappingBean1> list = jdbc.queryForList("select * from user_table where id = 10", arg, MultipleMappingBean1.class);

            assert list.size() == 1;
            assert list.get(0).getName().equals("Verdi");
            assert list.get(0).getCreateTime1() != list.get(0).getCreateTime2();
            assert list.get(0).getCreateTime1().equals(list.get(0).getCreateTime2());
            assert DateTimeFormat.WKF_DATE_TIME24.format(list.get(0).getCreateTime1()).equals("2021-05-11 00:00:00");
            assert DateTimeFormat.WKF_DATE_TIME24.format(list.get(0).getCreateTime2()).equals("2021-05-11 00:00:00");
        }
    }

}
