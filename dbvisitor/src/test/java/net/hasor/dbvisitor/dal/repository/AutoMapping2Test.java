/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http:www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.dal.repository;
import net.hasor.dbvisitor.dal.MapperRegistry;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.test.AbstractDbTest;
import org.junit.Before;
import org.junit.Test;

import java.sql.JDBCType;
import java.util.Objects;

/***
 *
 * @version : 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class AutoMapping2Test extends AbstractDbTest {
    private MapperRegistry dalRegistry;

    @Before
    public void beforeTest() throws Exception {
        this.dalRegistry = new MapperRegistry();
        this.dalRegistry.loadMapper("/dbvisitor_coverage/dal_repository/autoMapping_2.xml");
    }

    @Test
    public void mapperTest_01() {
        TableMapping<?> tableMapping = this.dalRegistry.findBySpace("resultMap_test", "resultMap_1");

        assert tableMapping.getPropertyByName("user_uuid").getColumn().equals("user_uuid");
        assert tableMapping.getPropertyByName("user_name").getColumn().equals("user_name");
        assert tableMapping.getPropertyByName("login_name").getColumn().equals("login_name");
        assert tableMapping.getPropertyByName("login_password").getColumn().equals("login_password");
        assert tableMapping.getPropertyByName("email").getColumn().equals("email");
        assert tableMapping.getPropertyByName("seq").getColumn().equals("seq");
        assert tableMapping.getPropertyByName("register_time").getColumn().equals("register_time");

        assert tableMapping.getTable().equals("");
        assert Objects.equals(tableMapping.getPropertyByName("email").getJdbcType(), JDBCType.VARCHAR.getVendorTypeNumber());
        assert Objects.equals(tableMapping.getPropertyByName("seq").getJdbcType(), JDBCType.INTEGER.getVendorTypeNumber());
        assert Objects.equals(tableMapping.getPropertyByName("register_time").getJdbcType(), JDBCType.TIMESTAMP.getVendorTypeNumber());
    }

    @Test
    public void mapperTest_02() {
        TableMapping<?> tableMapping = this.dalRegistry.findBySpace("resultMap_test", "resultMap_2");

        assert tableMapping.getPropertyByName("uid").getColumn().equals("user_uuid");
        assert tableMapping.getPropertyByName("name").getColumn().equals("user_name");
        assert tableMapping.getPropertyByName("loginName").getColumn().equals("login_name");
        assert tableMapping.getPropertyByName("password").getColumn().equals("login_password");
        assert tableMapping.getPropertyByName("email").getColumn().equals("email");
        assert tableMapping.getPropertyByName("seq").getColumn().equals("seq");
        assert tableMapping.getPropertyByName("createTime").getColumn().equals("register_time");

        assert tableMapping.getTable().equals("");
        assert Objects.equals(tableMapping.getPropertyByName("email").getJdbcType(), JDBCType.VARCHAR.getVendorTypeNumber());
        assert Objects.equals(tableMapping.getPropertyByName("seq").getJdbcType(), JDBCType.INTEGER.getVendorTypeNumber());
        assert Objects.equals(tableMapping.getPropertyByName("createTime").getJdbcType(), JDBCType.TIMESTAMP.getVendorTypeNumber());
    }

    @Test
    public void mapperTest_03() {
        TableMapping<?> tableMapping = this.dalRegistry.findBySpace("resultMap_test", "resultMap_3");

        assert tableMapping.getPropertyByName("userUuid").getColumn().equals("user_uuid");
        assert tableMapping.getPropertyByName("name").getColumn().equals("user_name");
        assert tableMapping.getPropertyByName("loginName").getColumn().equals("login_name");
        assert tableMapping.getPropertyByName("loginPassword").getColumn().equals("login_password");
        assert tableMapping.getPropertyByName("email").getColumn().equals("email");
        assert tableMapping.getPropertyByName("seq").getColumn().equals("seq");
        assert tableMapping.getPropertyByName("registerTime").getColumn().equals("register_time");

        assert tableMapping.getTable().equals("");
        assert Objects.equals(tableMapping.getPropertyByName("email").getJdbcType(), JDBCType.VARCHAR.getVendorTypeNumber());
        assert Objects.equals(tableMapping.getPropertyByName("seq").getJdbcType(), JDBCType.INTEGER.getVendorTypeNumber());
        assert Objects.equals(tableMapping.getPropertyByName("registerTime").getJdbcType(), JDBCType.TIMESTAMP.getVendorTypeNumber());
    }

    @Test
    public void mapperTest_04() {
        TableMapping<?> tableMapping = this.dalRegistry.findBySpace("resultMap_test", "resultMap_4");

        assert tableMapping.getPropertyByName("userUuid").getColumn().equals("user_uuid");
        assert tableMapping.getPropertyByName("name").getColumn().equals("name");
        assert tableMapping.getPropertyByName("loginName").getColumn().equals("login_name");
        assert tableMapping.getPropertyByName("loginPassword").getColumn().equals("login_password");
        assert tableMapping.getPropertyByName("email").getColumn().equals("email");
        assert tableMapping.getPropertyByName("seq").getColumn().equals("seq");
        assert tableMapping.getPropertyByName("registerTime").getColumn().equals("register_time");

        assert tableMapping.getTable().equals("");
        assert Objects.equals(tableMapping.getPropertyByName("email").getJdbcType(), JDBCType.VARCHAR.getVendorTypeNumber());
        assert Objects.equals(tableMapping.getPropertyByName("seq").getJdbcType(), JDBCType.INTEGER.getVendorTypeNumber());
        assert Objects.equals(tableMapping.getPropertyByName("registerTime").getJdbcType(), JDBCType.TIMESTAMP.getVendorTypeNumber());
    }
}
