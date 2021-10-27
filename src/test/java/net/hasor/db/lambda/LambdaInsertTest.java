/*
 * Copyright 2008-2009 the original author or authors.
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
package net.hasor.db.lambda;
import com.alibaba.druid.pool.DruidDataSource;
import net.hasor.db.dialect.BatchBoundSql;
import net.hasor.db.lambda.LambdaOperations.LambdaInsert;
import net.hasor.db.lambda.core.LambdaTemplate;
import net.hasor.test.db.AbstractDbTest;
import net.hasor.test.db.dto.TB_User;
import net.hasor.test.db.dto.TbUser;
import net.hasor.test.db.utils.DsUtils;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static net.hasor.test.db.utils.TestUtils.*;

/***
 * Lambda 方式执行 Insert 操作
 * @version : 2021-3-22
 * @author 赵永春 (zyc@hasor.net)
 */
public class LambdaInsertTest extends AbstractDbTest {
    @Test
    public void lambda_insert_1() throws Throwable {
        try (DruidDataSource dataSource = DsUtils.createDs()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(dataSource);
            lambdaTemplate.execute("delete from tb_user");
            //
            LambdaInsert<TbUser> lambdaInsert = lambdaTemplate.lambdaInsert(TbUser.class);
            lambdaInsert.applyEntity(mappingBeanForData1());
            lambdaInsert.applyMap(mapForData2());

            assert lambdaInsert.getBoundSql() instanceof BatchBoundSql;
            //
            int i = lambdaInsert.executeSumResult();
            assert i == 2;
            //
            List<TB_User> tbUsers = lambdaTemplate.lambdaQuery(TB_User.class).queryForList();
            assert tbUsers.size() == 2;
            List<String> ids = tbUsers.stream().map(TB_User::getUserUUID).collect(Collectors.toList());
            assert ids.contains(beanForData1().getUserUUID());
            assert ids.contains(beanForData2().getUserUUID());
        }
    }
}
