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
package net.hasor.scene.page;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.UserInfo2;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-3-22
 */
public class PageTestCase extends AbstractDbTest {
    @Test
    public void lambdaQuery_stream_page_0() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            //
            List<String> userIds = new ArrayList<>();
            Iterator<UserInfo2> userIterator = lambdaTemplate.queryByEntity(UserInfo2.class).queryForIterator(-1, 1);
            while (userIterator.hasNext()) {
                userIds.add(userIterator.next().getUid());
            }

            assert lambdaTemplate.queryByEntity(UserInfo2.class).queryForCount() == userIds.size();
        }
    }

    @Test
    public void lambdaQuery_stream_page_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            List<String> userIds = new ArrayList<>();
            Iterator<UserInfo2> userIterator = lambdaTemplate.queryByEntity(UserInfo2.class).queryForIterator(2, 1);
            while (userIterator.hasNext()) {
                userIds.add(userIterator.next().getUid());
            }

            assert userIds.size() == 2;
        }
    }

    @Test
    public void lambdaQuery_stream_page_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            Page userIterator = lambdaTemplate.queryByEntity(UserInfo2.class).pageInfo();
            assert userIterator.getTotalCount() == 3;
        }
    }

    @Test
    public void lambdaQuery_stream_page_3() {
        LambdaTemplate lambdaTemplate = new LambdaTemplate();
        Page userIterator = lambdaTemplate.queryByEntity(UserInfo2.class).pageInfo();
        userIterator.setTotalCount(123);
        assert userIterator.getTotalCount() == 123;
    }

    @Test
    public void lambdaQuery_stream_page_4() {
        Page pageInfo = new PageObject(2, 10);
        LambdaTemplate lambdaTemplate = new LambdaTemplate();
        Page userIterator = lambdaTemplate.queryByEntity(UserInfo2.class).usePage(pageInfo).pageInfo();
        assert userIterator.getTotalCount() == 10;
    }
}
