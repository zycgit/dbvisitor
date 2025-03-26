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
package net.hasor.dbvisitor.wrapper;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.provider.MySqlDialect;
import net.hasor.dbvisitor.dynamic.MacroRegistry;
import net.hasor.dbvisitor.dynamic.RuleRegistry;
import net.hasor.dbvisitor.jdbc.core.JdbcQueryContext;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import net.hasor.dbvisitor.wrapper.dto.AnnoUserInfoDTO;
import net.hasor.test.dto.UserInfo2;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static net.hasor.test.utils.TestUtils.INSERT_ARRAY;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-3-22
 */
public class DoEntPageTest {

    private WrapperAdapter newLambda() throws SQLException {
        Options opt = Options.of().dialect(new MySqlDialect());
        JdbcQueryContext context = new JdbcQueryContext();
        context.setTypeRegistry(new TypeHandlerRegistry());
        context.setMacroRegistry(new MacroRegistry());
        context.setRuleRegistry(new RuleRegistry());
        MappingRegistry registry = new MappingRegistry(null, context.getTypeRegistry(), opt);

        return new WrapperAdapter((DataSource) null, registry, context);
    }

    private WrapperAdapter newLambda(Connection c) throws SQLException {
        Options opt = Options.of().dialect(new MySqlDialect());
        JdbcQueryContext context = new JdbcQueryContext();
        context.setTypeRegistry(new TypeHandlerRegistry());
        context.setMacroRegistry(new MacroRegistry());
        context.setRuleRegistry(new RuleRegistry());
        MappingRegistry registry = new MappingRegistry(null, context.getTypeRegistry(), opt);

        return new WrapperAdapter(c, registry, context);
    }

    @Test
    public void buildPageTest_1() throws SQLException {
        BoundSql boundSql = newLambda().query(UserInfo2.class).select(UserInfo2::getLoginName)//
                .initPage(10, 2)//
                .getBoundSql();
        assert boundSql.getSqlString().equals("SELECT login_name FROM user_info LIMIT ?, ?");
        assert boundSql.getArgs()[0].equals(20L);
        assert boundSql.getArgs()[1].equals(10L);
    }

    @Test
    public void buildPageTest_2() throws SQLException {
        BoundSql boundSql = newLambda().query(UserInfo2.class).select(UserInfo2::getLoginName)//
                .eq(UserInfo2::getSeq, 1)//
                .rangeBetween(UserInfo2::getLoginName, 2, 3)//
                .initPage(10, 2)//
                .getBoundSql();
        assert boundSql.getSqlString().equals("SELECT login_name FROM user_info WHERE seq = ? AND login_name BETWEEN ? AND ? LIMIT ?, ?");
        assert boundSql.getArgs()[0].equals(1);
        assert boundSql.getArgs()[1].equals(2);
        assert boundSql.getArgs()[2].equals(3);
        assert boundSql.getArgs()[3].equals(20L);
        assert boundSql.getArgs()[4].equals(10L);
    }

    @Test
    public void buildPageTest_3() throws SQLException {
        BoundSql boundSql1 = newLambda().query(UserInfo2.class).select(UserInfo2::getLoginName)//
                .orderBy(UserInfo2::getUid).initPage(5, 0).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT login_name FROM user_info ORDER BY user_uuid LIMIT ?");
        assert boundSql1.getArgs()[0].equals(5L);

        BoundSql boundSql2 = newLambda().query(UserInfo2.class).select(UserInfo2::getLoginName)//
                .orderBy(UserInfo2::getUid).initPage(5, 1).getBoundSql();
        assert boundSql2.getSqlString().equals("SELECT login_name FROM user_info ORDER BY user_uuid LIMIT ?, ?");
        assert boundSql2.getArgs()[0].equals(5L);
        assert boundSql2.getArgs()[1].equals(5L);
    }

    private static int initData(Connection c) throws SQLException {
        // init data
        JdbcTemplate jdbc = new JdbcTemplate(c);
        jdbc.execute("delete from user_info");

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
        jdbc.executeBatch(INSERT_ARRAY, batchValues);//批量执行执行插入语句
        assert jdbc.queryForInt("select count(1) from user_info") == 13;
        return count;
    }

    @Test
    public void queryPage_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            int count = initData(c);

            // pageQuery
            WrapperAdapter lambda = newLambda(c);
            List<AnnoUserInfoDTO> page0 = lambda.query(AnnoUserInfoDTO.class).orderBy(AnnoUserInfoDTO::getSeq).initPage(5, 0).queryForList();
            List<AnnoUserInfoDTO> page1 = lambda.query(AnnoUserInfoDTO.class).orderBy(AnnoUserInfoDTO::getSeq).initPage(5, 1).queryForList();
            List<AnnoUserInfoDTO> page2 = lambda.query(AnnoUserInfoDTO.class).orderBy(AnnoUserInfoDTO::getSeq).initPage(5, 2).queryForList();
            List<AnnoUserInfoDTO> page3 = lambda.query(AnnoUserInfoDTO.class).orderBy(AnnoUserInfoDTO::getSeq).initPage(5, 3).queryForList();
            List<AnnoUserInfoDTO> page4 = lambda.query(AnnoUserInfoDTO.class).orderBy(AnnoUserInfoDTO::getSeq).initPage(5, 4).queryForList();

            assert page0.size() == 5;
            assert page1.size() == 5;
            assert page2.size() == 3;
            assert page3.size() == 0;
            assert page4.size() == 0;

            // check
            List<AnnoUserInfoDTO> pageAll = new ArrayList<>();
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

    //    @Test
    //    public void iteratorForLimit_1() throws Throwable {
    //        try (Connection c = DsUtils.h2Conn()) {
    //            int count = initData(c);
    //
    //            // iterator Query
    //            List<AnnoUserInfoDTO> pageAll = new ArrayList<>();
    //            Iterator<AnnoUserInfoDTO> iterator = newLambda(c).queryByEntity(AnnoUserInfoDTO.class)//
    //                    .iteratorForLimit(-1, 1);
    //            while (iterator.hasNext()) {
    //                pageAll.add(iterator.next());
    //            }
    //
    //            // check
    //            for (int i = 0; i < count; i++) {
    //                assert pageAll.get(i).getUid().equals("id_" + i);
    //            }
    //        }
    //    }
    //
    //    @Test
    //    public void iteratorForLimit_2() throws Throwable {
    //        try (Connection c = DsUtils.h2Conn()) {
    //            int count = initData(c);
    //            assert count == 13;
    //
    //            // iterator Query
    //            List<AnnoUserInfoDTO> pageAll = new ArrayList<>();
    //            Iterator<AnnoUserInfoDTO> iterator = newLambda(c).queryByEntity(AnnoUserInfoDTO.class)//
    //                    .iteratorForLimit(2, 1);
    //            while (iterator.hasNext()) {
    //                pageAll.add(iterator.next());
    //            }
    //
    //            // check
    //            assert pageAll.size() == 2;
    //            assert pageAll.get(0).getUid().equals("id_0");
    //            assert pageAll.get(1).getUid().equals("id_1");
    //        }
    //    }
    //
    //    @Test
    //    public void iteratorForLimit_3() throws Throwable {
    //        try (Connection c = DsUtils.h2Conn()) {
    //            int count = initData(c);
    //            assert count == 13;
    //
    //            // iterator Query
    //            List<AnnoUserInfoDTO> pageAll = new ArrayList<>();
    //            Iterator<AnnoUserInfoDTO> iterator = newLambda(c).queryByEntity(AnnoUserInfoDTO.class)//
    //                    .iteratorForLimit(2);
    //            while (iterator.hasNext()) {
    //                pageAll.add(iterator.next());
    //            }
    //
    //            // check
    //            assert pageAll.size() == 2;
    //            assert pageAll.get(0).getUid().equals("id_0");
    //            assert pageAll.get(1).getUid().equals("id_1");
    //        }
    //    }
    //
    //    @Test
    //    public void iteratorForLimit_4() throws Throwable {
    //        try (Connection c = DsUtils.h2Conn()) {
    //            int count = initData(c);
    //            assert count == 13;
    //
    //            // iterator Query
    //            List<String> pageAll = new ArrayList<>();
    //            Iterator<String> iterator = newLambda(c).queryByEntity(AnnoUserInfoDTO.class)//
    //                    .iteratorForLimit(2, 1, AnnoUserInfoDTO::getUid);
    //            while (iterator.hasNext()) {
    //                pageAll.add(iterator.next());
    //            }
    //
    //            // check
    //            assert pageAll.size() == 2;
    //            assert pageAll.get(0).equals("id_0");
    //            assert pageAll.get(1).equals("id_1");
    //        }
    //    }
    //
    //    @Test
    //    public void iteratorByBatch_1() throws Throwable {
    //        try (Connection c = DsUtils.h2Conn()) {
    //            int count = initData(c);
    //
    //            // iterator Query
    //            List<AnnoUserInfoDTO> pageAll = new ArrayList<>();
    //            Iterator<AnnoUserInfoDTO> iterator = newLambda(c).queryByEntity(AnnoUserInfoDTO.class)//
    //                    .iteratorByBatch(-1);
    //            while (iterator.hasNext()) {
    //                pageAll.add(iterator.next());
    //            }
    //
    //            // check
    //            for (int i = 0; i < count; i++) {
    //                assert pageAll.get(i).getUid().equals("id_" + i);
    //            }
    //        }
    //    }
}
