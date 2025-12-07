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
package net.hasor.dbvisitor.mapping;
import java.sql.JDBCType;
import java.util.Objects;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.test.AbstractDbTest;
import org.junit.Before;
import org.junit.Test;

/***
 *
 * @version 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class AutoGlobalNoTest extends AbstractDbTest {
    private MappingRegistry registry;

    @Before
    public void beforeTest() throws Exception {
        this.registry = new MappingRegistry();
        this.registry.loadMapping("/dbvisitor_coverage/basic_mapping/auto_global_no.xml");
    }

    @Test
    public void autoByAnno_1() {
        TableMapping<?> tableMapping = this.registry.findBySpace("resultMap_test", "resultMap_1");

        assert tableMapping.getPropertyByName("uid").getColumn().equals("user_uuid");
        assert tableMapping.getPropertyByName("name").getColumn().equals("user_name");
        assert tableMapping.getPropertyByName("loginName").getColumn().equals("login_name");
        assert tableMapping.getPropertyByName("password").getColumn().equals("login_password");
        assert tableMapping.getPropertyByName("email").getColumn().equals("email");
        assert tableMapping.getPropertyByName("seq").getColumn().equals("seq");
        assert tableMapping.getPropertyByName("createTime").getColumn().equals("register_time");

        assert tableMapping.getTable().equals("");
        assert tableMapping.getPropertyByName("email").getJdbcType() == null;
        assert Objects.equals(tableMapping.getPropertyByName("seq").getJdbcType(), JDBCType.INTEGER.getVendorTypeNumber());
        assert tableMapping.getPropertyByName("createTime").getJdbcType() == null;
    }

    @Test
    public void autoAndXml_1() {
        //autoMapping is invalid after result is configured
        TableMapping<?> tableMapping = this.registry.findBySpace("resultMap_test", "resultMap_2");

        assert tableMapping.getPropertyByName("userUuid").getColumn().equals("user_uuid");
        assert tableMapping.getPropertyByName("name").getColumn().equals("user_name");
        assert tableMapping.getPropertyByName("loginName").getColumn().equals("login_name");
        assert tableMapping.getPropertyByName("loginPassword").getColumn().equals("login_password");
        assert tableMapping.getPropertyByName("email").getColumn().equals("email");
        assert tableMapping.getPropertyByName("seq").getColumn().equals("seq");
        assert tableMapping.getPropertyByName("registerTime") == null; // autoMapping is invalid after result is configured

        assert tableMapping.getTable().equals("");
        assert Objects.equals(tableMapping.getPropertyByName("email").getJdbcType(), JDBCType.VARCHAR.getVendorTypeNumber());
        assert Objects.equals(tableMapping.getPropertyByName("seq").getJdbcType(), JDBCType.INTEGER.getVendorTypeNumber());
    }

    @Test
    public void auto_1() {
        TableMapping<?> tableMapping = this.registry.findBySpace("resultMap_test", "resultMap_3");

        assert tableMapping.getColumns().isEmpty();
    }
}
