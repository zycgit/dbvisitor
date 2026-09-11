/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.jdbc.core;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import net.hasor.cobble.CollectionUtils;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.utils.DsUtils;
import net.hasor.test.utils.TestUtils;
import net.hasor.test.utils.UserInfo;
import org.junit.Test;

/***
 * execute 系列方法测试
 * @version 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class MacroTest extends AbstractDbTest {
    @Test
    public void execute_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Map<String, Object> args = CollectionUtils.asMap("seq", 1, "name", "muhammad");

            ((JdbcQueryContext) jdbcTemplate.getQueryContext()).addMacro("includeSeq", "@{and, seq = :seq}");

            Map<String, Object> mapData = jdbcTemplate.queryForMap("select * from user_info where login_name = :name and @{macro, includeSeq}", args);

            UserInfo user = TestUtils.beanForData1();
            assert mapData != null;
            assert user.getUserUuid().equals(mapData.get("user_UUID"));
        }
    }
}
