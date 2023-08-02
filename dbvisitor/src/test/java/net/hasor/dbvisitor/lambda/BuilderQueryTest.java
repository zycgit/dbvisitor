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
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.UserInfo2;
import net.hasor.test.dto.user_info;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/***
 * @version : 2021-3-22
 * @author 赵永春 (zyc@hasor.net)
 */
public class BuilderQueryTest extends AbstractDbTest {
    @Test
    public void queryBuilder1() {
        LambdaTemplate lambdaTemplate = new LambdaTemplate();

        BoundSql boundSql1 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getLoginName, "abc").getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ?");
        assert boundSql1.getArgs()[0].equals("abc");

        BoundSql boundSql2 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).eq(UserInfo2::getLoginName, "abc").getBoundSql();
        assert boundSql2.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? AND login_name = ?");
        assert boundSql2.getArgs()[0].equals(1);
        assert boundSql2.getArgs()[1].equals("abc");
        BoundSql boundSql3 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).or().eq(UserInfo2::getLoginName, "abc").getBoundSql();
        assert boundSql3.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? OR login_name = ?");
        assert boundSql3.getArgs()[0].equals(1);
        assert boundSql3.getArgs()[1].equals("abc");

        BoundSql boundSql4 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).ne(UserInfo2::getLoginName, "abc").getBoundSql();
        assert boundSql4.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? AND login_name <> ?");
        assert boundSql4.getArgs()[0].equals(1);
        assert boundSql4.getArgs()[1].equals("abc");
        BoundSql boundSql5 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).or().ne(UserInfo2::getLoginName, "abc").getBoundSql();
        assert boundSql5.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? OR login_name <> ?");
        assert boundSql5.getArgs()[0].equals(1);
        assert boundSql5.getArgs()[1].equals("abc");

        BoundSql boundSql6 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).gt(UserInfo2::getLoginName, "abc").getBoundSql();
        assert boundSql6.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? AND login_name > ?");
        assert boundSql6.getArgs()[0].equals(1);
        assert boundSql6.getArgs()[1].equals("abc");
        BoundSql boundSql7 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).or().gt(UserInfo2::getLoginName, "abc").getBoundSql();
        assert boundSql7.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? OR login_name > ?");
        assert boundSql7.getArgs()[0].equals(1);
        assert boundSql7.getArgs()[1].equals("abc");

        BoundSql boundSql8 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).ge(UserInfo2::getLoginName, "abc").getBoundSql();
        assert boundSql8.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? AND login_name >= ?");
        assert boundSql8.getArgs()[0].equals(1);
        assert boundSql8.getArgs()[1].equals("abc");
        BoundSql boundSql9 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).or().ge(UserInfo2::getLoginName, "abc").getBoundSql();
        assert boundSql9.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? OR login_name >= ?");
        assert boundSql9.getArgs()[0].equals(1);
        assert boundSql9.getArgs()[1].equals("abc");

        BoundSql boundSql10 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).lt(UserInfo2::getLoginName, "abc").getBoundSql();
        assert boundSql10.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? AND login_name < ?");
        assert boundSql10.getArgs()[0].equals(1);
        assert boundSql10.getArgs()[1].equals("abc");
        BoundSql boundSql11 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).or().lt(UserInfo2::getLoginName, "abc").getBoundSql();
        assert boundSql11.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? OR login_name < ?");
        assert boundSql11.getArgs()[0].equals(1);
        assert boundSql11.getArgs()[1].equals("abc");

        BoundSql boundSql12 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).le(UserInfo2::getLoginName, "abc").getBoundSql();
        assert boundSql12.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? AND login_name <= ?");
        assert boundSql12.getArgs()[0].equals(1);
        assert boundSql12.getArgs()[1].equals("abc");
        BoundSql boundSql13 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).or().le(UserInfo2::getLoginName, "abc").getBoundSql();
        assert boundSql13.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? OR login_name <= ?");
        assert boundSql13.getArgs()[0].equals(1);
        assert boundSql13.getArgs()[1].equals("abc");

        BoundSql boundSql14 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).isNull(UserInfo2::getLoginName).getBoundSql();
        assert boundSql14.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? AND login_name IS NULL");
        assert boundSql14.getArgs()[0].equals(1);
        BoundSql boundSql15 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).or().isNull(UserInfo2::getLoginName).getBoundSql();
        assert boundSql15.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? OR login_name IS NULL");
        assert boundSql15.getArgs()[0].equals(1);

        BoundSql boundSql16 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).isNotNull(UserInfo2::getLoginName).getBoundSql();
        assert boundSql16.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? AND login_name IS NOT NULL");
        assert boundSql16.getArgs()[0].equals(1);
        BoundSql boundSql17 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).or().isNotNull(UserInfo2::getLoginName).getBoundSql();
        assert boundSql17.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? OR login_name IS NOT NULL");
        assert boundSql17.getArgs()[0].equals(1);

        List<String> inData = Arrays.asList("a", "b", "c");
        BoundSql boundSql18 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).in(UserInfo2::getLoginName, inData).getBoundSql();
        assert boundSql18.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? AND login_name IN ( ? , ? , ? )");
        assert boundSql18.getArgs()[0].equals(1);
        assert boundSql18.getArgs()[1].equals("a");
        assert boundSql18.getArgs()[2].equals("b");
        assert boundSql18.getArgs()[3].equals("c");
        BoundSql boundSql19 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).or().in(UserInfo2::getLoginName, inData).getBoundSql();
        assert boundSql19.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? OR login_name IN ( ? , ? , ? )");
        assert boundSql19.getArgs()[0].equals(1);
        assert boundSql19.getArgs()[1].equals("a");
        assert boundSql19.getArgs()[2].equals("b");
        assert boundSql19.getArgs()[3].equals("c");

        List<String> notInData = Arrays.asList("a", "b", "c");
        BoundSql boundSql20 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).notIn(UserInfo2::getLoginName, notInData).getBoundSql();
        assert boundSql20.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? AND login_name NOT IN ( ? , ? , ? )");
        assert boundSql20.getArgs()[0].equals(1);
        assert boundSql20.getArgs()[1].equals("a");
        assert boundSql20.getArgs()[2].equals("b");
        assert boundSql20.getArgs()[3].equals("c");
        BoundSql boundSql21 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).or().notIn(UserInfo2::getLoginName, notInData).getBoundSql();
        assert boundSql21.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? OR login_name NOT IN ( ? , ? , ? )");
        assert boundSql21.getArgs()[0].equals(1);
        assert boundSql21.getArgs()[1].equals("a");
        assert boundSql21.getArgs()[2].equals("b");
        assert boundSql21.getArgs()[3].equals("c");

        BoundSql boundSql22 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).between(UserInfo2::getLoginName, 2, 3).getBoundSql();
        assert boundSql22.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? AND login_name BETWEEN ? AND ?");
        assert boundSql22.getArgs()[0].equals(1);
        assert boundSql22.getArgs()[1].equals(2);
        assert boundSql22.getArgs()[2].equals(3);
        BoundSql boundSql23 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).or().between(UserInfo2::getLoginName, 2, 3).getBoundSql();
        assert boundSql23.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? OR login_name BETWEEN ? AND ?");
        assert boundSql23.getArgs()[0].equals(1);
        assert boundSql23.getArgs()[1].equals(2);
        assert boundSql23.getArgs()[2].equals(3);

        BoundSql boundSql24 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).notBetween(UserInfo2::getLoginName, 2, 3).getBoundSql();
        assert boundSql24.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? AND login_name NOT BETWEEN ? AND ?");
        assert boundSql24.getArgs()[0].equals(1);
        assert boundSql24.getArgs()[1].equals(2);
        assert boundSql24.getArgs()[2].equals(3);
        BoundSql boundSql25 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).or().notBetween(UserInfo2::getLoginName, 2, 3).getBoundSql();
        assert boundSql25.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? OR login_name NOT BETWEEN ? AND ?");
        assert boundSql25.getArgs()[0].equals(1);
        assert boundSql25.getArgs()[1].equals(2);
        assert boundSql25.getArgs()[2].equals(3);

        BoundSql boundSql26 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).like(UserInfo2::getLoginName, "abc").getBoundSql();
        assert boundSql26.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? AND login_name LIKE CONCAT('%', ? ,'%')");
        assert boundSql26.getArgs()[0].equals(1);
        assert boundSql26.getArgs()[1].equals("abc");
        BoundSql boundSql27 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).or().notLike(UserInfo2::getLoginName, "abc").getBoundSql();
        assert boundSql27.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? OR login_name NOT LIKE CONCAT('%', ? ,'%')");
        assert boundSql27.getArgs()[0].equals(1);
        assert boundSql27.getArgs()[1].equals("abc");

        BoundSql boundSql28 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).likeRight(UserInfo2::getLoginName, "abc").getBoundSql();
        assert boundSql28.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? AND login_name LIKE CONCAT( ? ,'%')");
        assert boundSql28.getArgs()[0].equals(1);
        assert boundSql28.getArgs()[1].equals("abc");
        BoundSql boundSql29 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).or().notLikeRight(UserInfo2::getLoginName, "abc").getBoundSql();
        assert boundSql29.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? OR login_name NOT LIKE CONCAT( ? ,'%')");
        assert boundSql29.getArgs()[0].equals(1);
        assert boundSql29.getArgs()[1].equals("abc");

        BoundSql boundSql30 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).likeLeft(UserInfo2::getLoginName, "abc").getBoundSql();
        assert boundSql30.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? AND login_name LIKE CONCAT('%', ? )");
        assert boundSql30.getArgs()[0].equals(1);
        assert boundSql30.getArgs()[1].equals("abc");
        BoundSql boundSql31 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getSeq, 1).or().notLikeLeft(UserInfo2::getLoginName, "abc").getBoundSql();
        assert boundSql31.getSqlString().equals("SELECT * FROM user_info WHERE seq = ? OR login_name NOT LIKE CONCAT('%', ? )");
        assert boundSql31.getArgs()[0].equals(1);
        assert boundSql31.getArgs()[1].equals("abc");

        BoundSql boundSql32 = lambdaTemplate.lambdaQuery(UserInfo2.class)//
                .eq(UserInfo2::getLoginName, "a").eq(UserInfo2::getLoginName, "b").getBoundSql();
        assert boundSql32.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? AND login_name = ?");
        assert boundSql32.getArgs()[0].equals("a");
        assert boundSql32.getArgs()[1].equals("b");

        BoundSql boundSql33 = lambdaTemplate.lambdaQuery(UserInfo2.class)//
                .eq(UserInfo2::getLoginName, "a").eq(UserInfo2::getLoginName, "b").apply("limit ?", 123).getBoundSql();
        assert boundSql33.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? AND login_name = ? limit ?");
        assert boundSql33.getArgs()[0].equals("a");
        assert boundSql33.getArgs()[1].equals("b");
        assert boundSql33.getArgs()[2].equals(123);

        BoundSql boundSql34 = lambdaTemplate.lambdaQuery(UserInfo2.class).eqBySample(new UserInfo2()).getBoundSql();
        assert boundSql34.getSqlString().equals("SELECT * FROM user_info WHERE ( seq = ? )");
        assert boundSql34.getArgs()[0].equals(0);

        BoundSql boundSql35 = lambdaTemplate.lambdaQuery(user_info.class).eqBySample(new user_info()).getBoundSql();
        assert boundSql35.getSqlString().equals("SELECT * FROM user_info");
    }

    @Test
    public void queryBuilder2() {
        LambdaTemplate lambdaTemplate = new LambdaTemplate();

        BoundSql boundSql1 = lambdaTemplate.lambdaQuery(UserInfo2.class)//
                .eq(UserInfo2::getLoginName, "a").eq(UserInfo2::getLoginName, "b")//
                .groupBy(UserInfo2::getSeq).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT seq FROM user_info WHERE login_name = ? AND login_name = ? GROUP BY seq");
        assert boundSql1.getArgs()[0].equals("a");
        assert boundSql1.getArgs()[1].equals("b");

        BoundSql boundSql2 = lambdaTemplate.lambdaQuery(UserInfo2.class)//
                .eq(UserInfo2::getLoginName, "a")//
                .eq(UserInfo2::getLoginName, "b")//
                .apply("limit 1")//
                .groupBy(UserInfo2::getSeq)//
                .apply("limit 1").getBoundSql();
        assert boundSql2.getSqlString().equals("SELECT seq FROM user_info WHERE login_name = ? AND login_name = ? limit 1 GROUP BY seq limit 1");
    }

    @Test
    public void queryBuilder3() {

        try {
            LambdaTemplate lambdaTemplate = new LambdaTemplate();
            lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getLoginName, "a").eq(UserInfo2::getLoginName, "b").apply("limit 1")//
                    .groupBy(UserInfo2::getSeq).eq(UserInfo2::getLoginName, "b"); // after groupBy is Error.
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("must before (group by/order by) invoke it.");
        }

        try {
            LambdaTemplate lambdaTemplate = new LambdaTemplate();
            lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getLoginName, "a").eq(UserInfo2::getLoginName, "b").apply("limit 1")//
                    .orderBy(UserInfo2::getSeq).eq(UserInfo2::getLoginName, "b"); // << --- after orderBy is Error.
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("must before (group by/order by) invoke it.");
        }

        try {
            LambdaTemplate lambdaTemplate = new LambdaTemplate();
            lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getLoginName, "a").eq(UserInfo2::getLoginName, "b").apply("limit 1")//
                    .groupBy(UserInfo2::getSeq)  //
                    .orderBy(UserInfo2::getSeq)  //
                    .groupBy(UserInfo2::getSeq); // << --- after orderBy is Error.
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("must before order by invoke it.");
        }
    }

    @Test
    public void queryBuilder_or_1() {
        LambdaTemplate lambdaTemplate = new LambdaTemplate();

        BoundSql boundSql3 = lambdaTemplate.lambdaQuery(UserInfo2.class)//
                .eq(UserInfo2::getLoginName, "a").or(nestedQuery -> {
                    nestedQuery.ge(UserInfo2::getCreateTime, 1); // >= ?
                    nestedQuery.le(UserInfo2::getCreateTime, 2); // <= ?
                }).getBoundSql();
        BoundSql boundSql4 = lambdaTemplate.lambdaQuery(UserInfo2.class)//
                .eq(UserInfo2::getLoginName, "a").or().nested(nestedQuery -> {
                    nestedQuery.ge(UserInfo2::getCreateTime, 1); // >= ?
                    nestedQuery.le(UserInfo2::getCreateTime, 2); // <= ?
                }).getBoundSql();

        assert boundSql3.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? OR ( register_time >= ? AND register_time <= ? )");
        assert boundSql3.getArgs()[0].equals("a");
        assert boundSql3.getArgs()[1].equals(1);
        assert boundSql3.getArgs()[2].equals(2);
        assert boundSql4.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? OR ( register_time >= ? AND register_time <= ? )");
        assert boundSql4.getArgs()[0].equals("a");
        assert boundSql4.getArgs()[1].equals(1);
        assert boundSql4.getArgs()[2].equals(2);
    }

    @Test
    public void queryBuilder_and_1() {
        LambdaTemplate lambdaTemplate = new LambdaTemplate();

        BoundSql boundSql3 = lambdaTemplate.lambdaQuery(UserInfo2.class)//
                .eq(UserInfo2::getLoginName, "a").and(nestedQuery -> {
                    nestedQuery.ge(UserInfo2::getCreateTime, 1); // >= ?
                    nestedQuery.le(UserInfo2::getCreateTime, 2); // <= ?
                }).eq(UserInfo2::getLoginName, 123).getBoundSql();
        BoundSql boundSql4 = lambdaTemplate.lambdaQuery(UserInfo2.class)//
                .eq(UserInfo2::getLoginName, "a").and().nested(nestedQuery -> {
                    nestedQuery.ge(UserInfo2::getCreateTime, 1); // >= ?
                    nestedQuery.le(UserInfo2::getCreateTime, 2); // <= ?
                }).eq(UserInfo2::getLoginName, 123).getBoundSql();

        assert boundSql3.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? AND ( register_time >= ? AND register_time <= ? ) AND login_name = ?");
        assert boundSql3.getArgs()[0].equals("a");
        assert boundSql3.getArgs()[1].equals(1);
        assert boundSql3.getArgs()[2].equals(2);
        assert boundSql3.getArgs()[3].equals(123);
        assert boundSql4.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? AND ( register_time >= ? AND register_time <= ? ) AND login_name = ?");
        assert boundSql4.getArgs()[0].equals("a");
        assert boundSql4.getArgs()[1].equals(1);
        assert boundSql4.getArgs()[2].equals(2);
        assert boundSql4.getArgs()[3].equals(123);
    }

    @Test
    public void queryBuilder_nested_1() {
        LambdaTemplate lambdaTemplate = new LambdaTemplate();

        BoundSql boundSql3 = lambdaTemplate.lambdaQuery(UserInfo2.class)//
                .eq(UserInfo2::getLoginName, "a").nested(nestedQuery -> {
                    nestedQuery.ge(UserInfo2::getCreateTime, 1); // >= ?
                    nestedQuery.le(UserInfo2::getCreateTime, 2); // <= ?
                }).eq(UserInfo2::getLoginName, 123).getBoundSql();
        assert boundSql3.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? AND ( register_time >= ? AND register_time <= ? ) AND login_name = ?");
        assert boundSql3.getArgs()[0].equals("a");
        assert boundSql3.getArgs()[1].equals(1);
        assert boundSql3.getArgs()[2].equals(2);
        assert boundSql3.getArgs()[3].equals(123);

        BoundSql boundSql4 = lambdaTemplate.lambdaQuery(UserInfo2.class)//
                .nested(nq0 -> {
                    nq0.nested(nq1 -> {
                        nq1.ge(UserInfo2::getCreateTime, 1); // >= ?
                        nq1.le(UserInfo2::getCreateTime, 2); // <= ?
                    }).nested(nq2 -> {
                        nq2.eq(UserInfo2::getSeq, 1);
                    });
                }).eq(UserInfo2::getLoginName, 123).getBoundSql();
        assert boundSql4.getSqlString().equals("SELECT * FROM user_info WHERE ( ( register_time >= ? AND register_time <= ? ) AND ( seq = ? ) ) AND login_name = ?");
        assert boundSql4.getArgs()[0].equals(1);
        assert boundSql4.getArgs()[1].equals(2);
        assert boundSql4.getArgs()[2].equals(1);
        assert boundSql4.getArgs()[3].equals(123);

        BoundSql boundSql5 = lambdaTemplate.lambdaQuery(UserInfo2.class)//
                .nested(nq0 -> {
                    nq0.nested(nq1 -> {
                        nq1.ge(UserInfo2::getCreateTime, 1); // >= ?
                        nq1.le(UserInfo2::getCreateTime, 2); // <= ?
                    }).nested(nq2 -> {
                        nq2.eq(UserInfo2::getSeq, 1);
                    });
                }).getBoundSql();
        assert boundSql5.getSqlString().equals("SELECT * FROM user_info WHERE ( ( register_time >= ? AND register_time <= ? ) AND ( seq = ? ) )");
        assert boundSql5.getArgs()[0].equals(1);
        assert boundSql5.getArgs()[1].equals(2);
        assert boundSql5.getArgs()[2].equals(1);
    }

    @Test
    public void queryBuilder4() {
        try {
            LambdaTemplate lambdaTemplate = new LambdaTemplate();
            lambdaTemplate.lambdaQuery(UserInfo2.class)//
                    .eq(UserInfo2::getLoginName, "muhammad").apply("limit 1")//
                    .queryForMap();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().contains("Connection unavailable, any of (Connection/DynamicConnection/DataSource) is required.");
        }
    }

    @Test
    public void queryBuilder5() {
        LambdaTemplate lambdaTemplate = new LambdaTemplate();

        BoundSql boundSql1 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getLoginName, "a").asc(UserInfo2::getLoginName).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? ORDER BY login_name ASC");
        assert boundSql1.getArgs()[0].equals("a");

        BoundSql boundSql2 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getLoginName, "a").asc(UserInfo2::getLoginName, UserInfo2::getSeq).getBoundSql();
        assert boundSql2.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? ORDER BY login_name ASC , seq ASC");
        assert boundSql2.getArgs()[0].equals("a");

        BoundSql boundSql3 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getLoginName, "a").desc(UserInfo2::getLoginName).getBoundSql();
        assert boundSql3.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? ORDER BY login_name DESC");
        assert boundSql3.getArgs()[0].equals("a");

        BoundSql boundSql4 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getLoginName, "a").asc(UserInfo2::getSeq).desc(UserInfo2::getLoginName).getBoundSql();
        assert boundSql4.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? ORDER BY seq ASC , login_name DESC");
        assert boundSql4.getArgs()[0].equals("a");

        BoundSql boundSql5 = lambdaTemplate.lambdaQuery(UserInfo2.class).eq(UserInfo2::getLoginName, "a").orderBy(UserInfo2::getSeq).getBoundSql();
        assert boundSql5.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? ORDER BY seq");
        assert boundSql5.getArgs()[0].equals("a");
    }

    @Test
    public void queryBuilder6() {
        LambdaTemplate lambdaTemplate = new LambdaTemplate();

        BoundSql boundSql1 = lambdaTemplate.lambdaQuery(UserInfo2.class).applySelect("a, b, c, d")//
                .eq(UserInfo2::getSeq, 1).or().between(UserInfo2::getLoginName, 2, 3).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT a, b, c, d FROM user_info WHERE seq = ? OR login_name BETWEEN ? AND ?");
        assert boundSql1.getArgs()[0].equals(1);
        assert boundSql1.getArgs()[1].equals(2);
        assert boundSql1.getArgs()[2].equals(3);
    }

    @Test
    public void queryBuilder7() {
        LambdaTemplate lambdaTemplate = new LambdaTemplate();

        BoundSql boundSql1 = lambdaTemplate.lambdaQuery(UserInfo2.class).select(UserInfo2::getLoginName, UserInfo2::getSeq)//
                .eq(UserInfo2::getSeq, 1).or().between(UserInfo2::getLoginName, 2, 3).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT login_name , seq FROM user_info WHERE seq = ? OR login_name BETWEEN ? AND ?");
        assert boundSql1.getArgs()[0].equals(1);
        assert boundSql1.getArgs()[1].equals(2);
        assert boundSql1.getArgs()[2].equals(3);
    }

    @Test
    public void queryBuilder8() {
        LambdaTemplate lambdaTemplate = new LambdaTemplate();

        BoundSql boundSql1 = lambdaTemplate.lambdaQuery(UserInfo2.class).select(UserInfo2::getLoginName)//
                .eq(UserInfo2::getSeq, 1).or().between(UserInfo2::getLoginName, 2, 3).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT login_name FROM user_info WHERE seq = ? OR login_name BETWEEN ? AND ?");
        assert boundSql1.getArgs()[0].equals(1);
        assert boundSql1.getArgs()[1].equals(2);
        assert boundSql1.getArgs()[2].equals(3);
    }
}
