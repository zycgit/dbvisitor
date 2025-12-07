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
package net.hasor.dbvisitor.jdbc.mapper;
import java.sql.Connection;
import java.util.List;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.UserInfo2;
import net.hasor.test.utils.DsUtils;
import net.hasor.test.utils.TestUtils;
import org.junit.Test;

/***
 *
 * @version 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class MappingRowMapperTest extends AbstractDbTest {
    @Test
    public void testBeanRowMapper_0() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<UserInfo2> users = jdbcTemplate.queryForList("select * from user_info", new BeanMappingRowMapper<>(UserInfo2.class));
            assert users.size() == 3;
            assert TestUtils.beanForData1().getUserUuid().equals(users.get(0).getUid());
            assert TestUtils.beanForData2().getUserUuid().equals(users.get(1).getUid());
            assert TestUtils.beanForData3().getUserUuid().equals(users.get(2).getUid());
        }
    }
}
