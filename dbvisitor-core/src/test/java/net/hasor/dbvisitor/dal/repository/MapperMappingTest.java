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
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.test.db.AbstractDbTest;
import org.junit.Before;
import org.junit.Test;

import java.sql.JDBCType;
import java.util.Objects;

/***
 *
 * @version : 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class MapperMappingTest extends AbstractDbTest {
    private DalRegistry dalRegistry;

    @Before
    public void beforeTest() throws Exception {
        this.dalRegistry = new DalRegistry();
        this.dalRegistry.loadMapper("/net_hasor_db/dal_repository/mapper_1.xml");
    }

    @Test
    public void mapperTest_01() {
        TableMapping<?> tableMapping = this.dalRegistry.findTableMapping("resultMap_test", "resultMap_1");

        assert tableMapping.getPropertyByName("uid").getColumn().equals("userUUID");
        assert tableMapping.getPropertyByName("name").getColumn().equals("name");
        assert tableMapping.getPropertyByName("account").getColumn().equals("loginName");
        assert tableMapping.getPropertyByName("password").getColumn().equals("loginPassword");
        assert tableMapping.getPropertyByName("mail").getColumn().equals("email");
        assert tableMapping.getPropertyByName("index").getColumn().equals("index");
        assert tableMapping.getPropertyByName("createTime").getColumn().equals("registerTime");

        assert tableMapping.getTable().equals("tb_user");
        assert Objects.equals(tableMapping.getPropertyByName("mail").getJdbcType(), JDBCType.VARCHAR.getVendorTypeNumber());
        assert Objects.equals(tableMapping.getPropertyByName("index").getJdbcType(), JDBCType.INTEGER.getVendorTypeNumber());
        assert Objects.equals(tableMapping.getPropertyByName("createTime").getJdbcType(), JDBCType.TIMESTAMP.getVendorTypeNumber());
    }

    @Test
    public void mapperTest_02() {
        TableMapping<?> tableMapping = this.dalRegistry.findTableMapping("resultMap_test", "resultMap_2");

        assert tableMapping.getPropertyByName("uid").getColumn().equals("uid");
        assert tableMapping.getPropertyByName("name").getColumn().equals("name");
        assert tableMapping.getPropertyByName("account").getColumn().equals("account");
        assert tableMapping.getPropertyByName("password").getColumn().equals("password");
        assert tableMapping.getPropertyByName("mail").getColumn().equals("mail");
        assert tableMapping.getPropertyByName("index").getColumn().equals("index");
        assert tableMapping.getPropertyByName("createTime").getColumn().equals("createTime");

        assert tableMapping.getTable().equals("TbUser2");
        assert Objects.equals(tableMapping.getPropertyByName("mail").getJdbcType(), JDBCType.VARCHAR.getVendorTypeNumber());
        assert Objects.equals(tableMapping.getPropertyByName("index").getJdbcType(), JDBCType.INTEGER.getVendorTypeNumber());
        assert Objects.equals(tableMapping.getPropertyByName("createTime").getJdbcType(), JDBCType.TIMESTAMP.getVendorTypeNumber());
    }

    @Test
    public void mapperTest_03() {
        TableMapping<?> tableMapping = this.dalRegistry.findTableMapping("resultMap_test", "resultMap_3");

        assert tableMapping.getPropertyByName("uid").getColumn().equals("user_uuid");
        assert tableMapping.getPropertyByName("name").getColumn().equals("name");
        assert tableMapping.getPropertyByName("account").getColumn().equals("login_name");
        assert tableMapping.getPropertyByName("password").getColumn().equals("login_password");
        assert tableMapping.getPropertyByName("mail").getColumn().equals("email");
        assert tableMapping.getPropertyByName("index").getColumn().equals("index");
        assert tableMapping.getPropertyByName("createTime").getColumn().equals("register_time");

        assert tableMapping.getTable().equals("TbUser2");
        assert Objects.equals(tableMapping.getPropertyByName("mail").getJdbcType(), JDBCType.VARCHAR.getVendorTypeNumber());
        assert Objects.equals(tableMapping.getPropertyByName("index").getJdbcType(), JDBCType.INTEGER.getVendorTypeNumber());
        assert Objects.equals(tableMapping.getPropertyByName("createTime").getJdbcType(), JDBCType.TIMESTAMP.getVendorTypeNumber());
    }

    @Test
    public void mapperTest_04() {
        TableMapping<?> tableMapping = this.dalRegistry.findTableMapping("resultMap_test", "resultMap_4");

        assert tableMapping.getPropertyByName("uid").getColumn().equals("uid");
        assert tableMapping.getPropertyByName("name").getColumn().equals("name");
        assert tableMapping.getPropertyByName("account").getColumn().equals("account");
        assert tableMapping.getPropertyByName("password").getColumn().equals("password");
        assert tableMapping.getPropertyByName("mail").getColumn().equals("mail");
        assert tableMapping.getPropertyByName("index").getColumn().equals("index");
        assert tableMapping.getPropertyByName("createTime").getColumn().equals("create_time");

        assert tableMapping.getTable().equals("tb_user2");
        assert Objects.equals(tableMapping.getPropertyByName("mail").getJdbcType(), JDBCType.VARCHAR.getVendorTypeNumber());
        assert Objects.equals(tableMapping.getPropertyByName("index").getJdbcType(), JDBCType.INTEGER.getVendorTypeNumber());
        assert Objects.equals(tableMapping.getPropertyByName("createTime").getJdbcType(), JDBCType.TIMESTAMP.getVendorTypeNumber());
    }
}
