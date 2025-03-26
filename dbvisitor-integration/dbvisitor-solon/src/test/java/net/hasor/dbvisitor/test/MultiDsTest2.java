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
package net.hasor.dbvisitor.test;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.solon.Db;
import net.hasor.dbvisitor.test.dao.role.RoleMapper;
import net.hasor.dbvisitor.test.dao.user.UserMapper;
import net.hasor.dbvisitor.test.dto.UserDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonJUnit4ClassRunner;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Import(profiles = "classpath:multi-ds.yml")
@RunWith(SolonJUnit4ClassRunner.class)
public class MultiDsTest2 {
    @Inject
    private RoleMapper   roleMapper;
    @Inject
    private UserMapper   userMapper;
    @Db("one") // in multi-ds this is ambiguous.
    private JdbcTemplate jdbc1;
    @Db("two") // in multi-ds this is ambiguous.
    private JdbcTemplate jdbc2;

    @Test
    public void getListTest() throws SQLException, IOException {
        this.jdbc1.loadSQL("CreateDB.sql");

        assert userMapper != null;
        assert roleMapper != null;

        List<UserDTO> users = userMapper.queryAll();

        assert users.size() == 5;
        assert users.get(0).getId() == 1L;
        assert users.get(0).getName().equals("mali");
        assert users.get(0).getGender().equals("F");
        assert users.get(0).getEmail().equals("mali@hasor.net");
        assert users.get(0).getRoleId() == 1L;

        List<String> collect = users.stream().map(UserDTO::getName).collect(Collectors.toList());
        assert collect.contains("mali");
        assert collect.contains("dative");
        assert collect.contains("jon wes");
        assert collect.contains("mary");
        assert collect.contains("matt");
    }
}
