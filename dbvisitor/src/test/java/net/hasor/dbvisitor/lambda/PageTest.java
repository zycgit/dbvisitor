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
import net.hasor.dbvisitor.JdbcUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlDialectRegister;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.UserInfo2;
import net.hasor.test.dto.user_info;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static net.hasor.test.utils.TestUtils.INSERT_ARRAY;

/**
 * @version : 2021-3-22
 * @author 赵永春 (zyc@hasor.net)
 */
public class PageTest extends AbstractDbTest {
    @Test
    public void pageTest_1() {
        BoundSql boundSql = new LambdaTemplate().lambdaQuery(UserInfo2.class).select(UserInfo2::getLoginName)//
                .initPage(10, 2)//
                .getBoundSql(SqlDialectRegister.findOrCreate(JdbcUtils.MYSQL));
        assert boundSql.getSqlString().equals("SELECT login_name FROM user_info LIMIT ?, ?");
        assert boundSql.getArgs()[0].equals(20L);
        assert boundSql.getArgs()[1].equals(10L);
    }

    @Test
    public void pageTest_2() {
        BoundSql boundSql = new LambdaTemplate().lambdaQuery(UserInfo2.class).select(UserInfo2::getLoginName)//
                .eq(UserInfo2::getSeq, 1)//
                .between(UserInfo2::getLoginName, 2, 3)//
                .initPage(10, 2)//
                .getBoundSql(SqlDialectRegister.findOrCreate(JdbcUtils.MYSQL));
        assert boundSql.getSqlString().equals("SELECT login_name FROM user_info WHERE seq = ? AND login_name BETWEEN ? AND ? LIMIT ?, ?");
        assert boundSql.getArgs()[0].equals(1);
        assert boundSql.getArgs()[1].equals(2);
        assert boundSql.getArgs()[2].equals(3);
        assert boundSql.getArgs()[3].equals(20L);
        assert boundSql.getArgs()[4].equals(10L);
    }

    @Test
    public void pageTest_3() {
        BoundSql boundSql1 = new LambdaTemplate().lambdaQuery(UserInfo2.class).select(UserInfo2::getLoginName)//
                .orderBy(UserInfo2::getUid).initPage(5, 0).getBoundSql(SqlDialectRegister.findOrCreate(JdbcUtils.MYSQL));
        assert boundSql1.getSqlString().equals("SELECT login_name FROM user_info ORDER BY user_uuid LIMIT ?");
        assert boundSql1.getArgs()[0].equals(5L);

        BoundSql boundSql2 = new LambdaTemplate().lambdaQuery(UserInfo2.class).select(UserInfo2::getLoginName)//
                .orderBy(UserInfo2::getUid).initPage(5, 1).getBoundSql(SqlDialectRegister.findOrCreate(JdbcUtils.MYSQL));
        assert boundSql2.getSqlString().equals("SELECT login_name FROM user_info ORDER BY user_uuid LIMIT ?, ?");
        assert boundSql2.getArgs()[0].equals(5L);
        assert boundSql2.getArgs()[1].equals(5L);
    }

    @Test
    public void pageTest_4() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            lambdaTemplate.execute("delete from user_info");

            int count = 13;
            Object[][] batchValues = new Object[count][];
            for (int i = 0; i < count; i++) {
                batchValues[i] = new Object[7];
                batchValues[i][0] = "id_" + i;
                batchValues[i][1] = String.format("默认用户_%s", i);
                batchValues[i][2] = String.format("acc_%s", i);
                batchValues[i][3] = String.format("pwd_%s", i);
                batchValues[i][4] = String.format("autoUser_%s@hasor.net", i);
                batchValues[i][5] = i;
                batchValues[i][6] = new Date();
            }
            lambdaTemplate.executeBatch(INSERT_ARRAY, batchValues);//批量执行执行插入语句
            assert lambdaTemplate.queryForInt("select count(1) from user_info") == 13;

            List<UserInfo2> page0 = lambdaTemplate.lambdaQuery(UserInfo2.class).orderBy(UserInfo2::getSeq).initPage(5, 0).queryForList();
            List<UserInfo2> page1 = lambdaTemplate.lambdaQuery(UserInfo2.class).orderBy(UserInfo2::getSeq).initPage(5, 1).queryForList();
            List<UserInfo2> page2 = lambdaTemplate.lambdaQuery(UserInfo2.class).orderBy(UserInfo2::getSeq).initPage(5, 2).queryForList();
            List<UserInfo2> page3 = lambdaTemplate.lambdaQuery(UserInfo2.class).orderBy(UserInfo2::getSeq).initPage(5, 3).queryForList();
            List<UserInfo2> page4 = lambdaTemplate.lambdaQuery(UserInfo2.class).orderBy(UserInfo2::getSeq).initPage(5, 4).queryForList();

            assert page0.size() == 5;
            assert page1.size() == 5;
            assert page2.size() == 3;
            assert page3.size() == 0;
            assert page4.size() == 0;

            List<UserInfo2> pageAll = new ArrayList<>();
            pageAll.addAll(page0);
            pageAll.addAll(page1);
            pageAll.addAll(page2);
            pageAll.addAll(page3);
            pageAll.addAll(page4);
            for (int i = 0; i < count; i++) {
                assert pageAll.get(i).getUid().equals("id_" + i);
            }
        }
    }

    @Test
    public void pageTest_5() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            lambdaTemplate.execute("delete from user_info");

            int count = 13;
            Object[][] batchValues = new Object[count][];
            for (int i = 0; i < count; i++) {
                batchValues[i] = new Object[7];
                batchValues[i][0] = "id_" + i;
                batchValues[i][1] = String.format("默认用户_%s", i);
                batchValues[i][2] = String.format("acc_%s", i);
                batchValues[i][3] = String.format("pwd_%s", i);
                batchValues[i][4] = String.format("autoUser_%s@hasor.net", i);
                batchValues[i][5] = i;
                batchValues[i][6] = new Date();
            }
            lambdaTemplate.executeBatch(INSERT_ARRAY, batchValues);//批量执行执行插入语句
            assert lambdaTemplate.queryForInt("select count(1) from user_info") == 13;

            List<user_info> page0 = lambdaTemplate.lambdaQuery(user_info.class).orderBy(user_info::getSeq).initPage(5, 0).queryForList();
            List<user_info> page1 = lambdaTemplate.lambdaQuery(user_info.class).orderBy(user_info::getSeq).initPage(5, 1).queryForList();
            List<user_info> page2 = lambdaTemplate.lambdaQuery(user_info.class).orderBy(user_info::getSeq).initPage(5, 2).queryForList();
            List<user_info> page3 = lambdaTemplate.lambdaQuery(user_info.class).orderBy(user_info::getSeq).initPage(5, 3).queryForList();
            List<user_info> page4 = lambdaTemplate.lambdaQuery(user_info.class).orderBy(user_info::getSeq).initPage(5, 4).queryForList();

            assert page0.size() == 5;
            assert page1.size() == 5;
            assert page2.size() == 3;
            assert page3.size() == 0;
            assert page4.size() == 0;

            List<user_info> pageAll = new ArrayList<>();
            pageAll.addAll(page0);
            pageAll.addAll(page1);
            pageAll.addAll(page2);
            pageAll.addAll(page3);
            pageAll.addAll(page4);
            for (int i = 0; i < count; i++) {
                assert pageAll.get(i).getUser_uuid().equals("id_" + i);
            }
        }
    }

    @Test
    public void lambdaQuery_stream_page_0() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            List<String> userIds = new ArrayList<>();
            Iterator<UserInfo2> userIterator = lambdaTemplate.lambdaQuery(UserInfo2.class).queryForIterator(-1, 1);
            while (userIterator.hasNext()) {
                userIds.add(userIterator.next().getUid());
            }

            assert lambdaTemplate.lambdaQuery(UserInfo2.class).queryForCount() == userIds.size();
        }
    }

    @Test
    public void lambdaQuery_stream_page_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            List<String> userIds = new ArrayList<>();
            Iterator<UserInfo2> userIterator = lambdaTemplate.lambdaQuery(UserInfo2.class).queryForIterator(2, 1);
            while (userIterator.hasNext()) {
                userIds.add(userIterator.next().getUid());
            }

            assert userIds.size() == 2;
        }
    }
}
