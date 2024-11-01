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
package net.hasor.dbvisitor.lambda;
import net.hasor.dbvisitor.lambda.dto.AnnoUserInfoDTO;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

import static net.hasor.test.utils.TestUtils.*;

/***
 * Lambda 方式执行 Delete 操作
 * @version : 2021-3-22
 * @author 赵永春 (zyc@hasor.net)
 */
public class DoEntDeleteTest {
    @Test
    public void delete_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            int delete = new LambdaTemplate(c).deleteByEntity(AnnoUserInfoDTO.class)//
                    .allowEmptyWhere().doDelete();
            assert delete == 3;
        }
    }

    @Test
    public void delete_2() {
        try (Connection c = DsUtils.h2Conn()) {
            new LambdaTemplate(c).deleteByEntity(AnnoUserInfoDTO.class)//
                    .doDelete();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("The dangerous DELETE operation, You must call `allowEmptyWhere()` to enable DELETE ALL.");
        }
    }

    @Test
    public void delete_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            int delete = new LambdaTemplate(c).deleteByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, beanForData1().getLoginName())//
                    .doDelete();
            assert delete == 1;

            List<AnnoUserInfoDTO> tbUsers = new LambdaTemplate(c).queryByEntity(AnnoUserInfoDTO.class)//
                    .queryForList();

            assert tbUsers.size() == 2;
            List<String> collect = tbUsers.stream().map(AnnoUserInfoDTO::getUid).collect(Collectors.toList());
            assert collect.contains(beanForData2().getUserUuid());
            assert collect.contains(beanForData3().getUserUuid());
        }
    }
}
