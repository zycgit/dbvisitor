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
package net.hasor.dbvisitor.jdbc.core;
import net.hasor.cobble.CollectionUtils;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.UserInfo;
import net.hasor.test.utils.DsUtils;
import net.hasor.test.utils.TestUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/***
 * execute 系列方法测试
 * @version : 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class MacroTest extends AbstractDbTest {
    @Test
    public void execute_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Map<String, Object> args = CollectionUtils.asMap("seq", 1, "name", "muhammad");

            jdbcTemplate.getRegistry().addMacro("includeSeq", "@{and, seq = :seq}");

            Map<String, Object> mapData = jdbcTemplate.queryForMap("select * from user_info where login_name = :name and @{macro, includeSeq}", args);

            UserInfo user = TestUtils.beanForData1();
            assert mapData != null;
            assert user.getUserUuid().equals(mapData.get("user_UUID"));
        }
    }
}
