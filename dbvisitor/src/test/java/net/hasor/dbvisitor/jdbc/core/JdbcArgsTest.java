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
import java.util.*;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

/***
 * executeBatch 系列方法测试
 * @version 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class JdbcArgsTest extends AbstractDbTest {

    @Test
    public void badTest_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbc = new JdbcTemplate(c);

            List<?> args = Arrays.asList("安妮.贝隆", "belon");
            List<Map<String, Object>> users = jdbc.queryForList("select * from user_info where user_name = ? and login_name = ?", args);
            assert users.size() == 1;
        }
    }

    @Test
    public void badTest_2() {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbc = new JdbcTemplate(c);

            Set<?> args = new HashSet<>(Arrays.asList("安妮.贝隆", "belon"));
            List<Map<String, Object>> users = jdbc.queryForList("select * from user_info where user_name = ? and login_name = ?", args);
            assert users.size() == 1;
            assert false;
        } catch (Throwable e) {
            assert e instanceof UnsupportedOperationException;
        }
    }
}
