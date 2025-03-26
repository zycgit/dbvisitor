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
import net.hasor.dbvisitor.wrapper.dto.AnnoUserInfoDTO;
import org.junit.Test;

import java.sql.SQLException;

/***
 * @version 2021-3-22
 * @author 赵永春 (zyc@hasor.net)
 */
public class BuildEntQueryNestedTest {

    @Test
    public void queryBuilder_nested_or_1() throws SQLException {
        BoundSql boundSql1 = new WrapperAdapter().query(AnnoUserInfoDTO.class)//
                .eq(AnnoUserInfoDTO::getLoginName, "a").or(qc -> {
                    qc.ge(AnnoUserInfoDTO::getCreateTime, 1); // >= ?
                    qc.le(AnnoUserInfoDTO::getCreateTime, 2); // <= ?
                }).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? OR ( register_time >= ? AND register_time <= ? )");
        assert boundSql1.getArgs()[0].equals("a");
        assert boundSql1.getArgs()[1].equals(1);
        assert boundSql1.getArgs()[2].equals(2);

        BoundSql boundSql2 = new WrapperAdapter().query(AnnoUserInfoDTO.class)//
                .eq(AnnoUserInfoDTO::getLoginName, "a").or().nested(qc -> {
                    qc.ge(AnnoUserInfoDTO::getCreateTime, 1); // >= ?
                    qc.le(AnnoUserInfoDTO::getCreateTime, 2); // <= ?
                }).getBoundSql();
        assert boundSql2.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? OR ( register_time >= ? AND register_time <= ? )");
        assert boundSql2.getArgs()[0].equals("a");
        assert boundSql2.getArgs()[1].equals(1);
        assert boundSql2.getArgs()[2].equals(2);
    }

    @Test
    public void queryBuilder_nested_or_1_2map() throws SQLException {
        BoundSql boundSql1 = new WrapperAdapter().query(AnnoUserInfoDTO.class).asMap()//
                .eq("loginName", "a").or(qc -> {
                    qc.ge("createTime", 1); // >= ?
                    qc.le("createTime", 2); // <= ?
                }).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? OR ( register_time >= ? AND register_time <= ? )");
        assert boundSql1.getArgs()[0].equals("a");
        assert boundSql1.getArgs()[1].equals(1);
        assert boundSql1.getArgs()[2].equals(2);

        BoundSql boundSql2 = new WrapperAdapter().query(AnnoUserInfoDTO.class).asMap()//
                .eq("loginName", "a").or().nested(qc -> {
                    qc.ge("createTime", 1); // >= ?
                    qc.le("createTime", 2); // <= ?
                }).getBoundSql();
        assert boundSql2.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? OR ( register_time >= ? AND register_time <= ? )");
        assert boundSql2.getArgs()[0].equals("a");
        assert boundSql2.getArgs()[1].equals(1);
        assert boundSql2.getArgs()[2].equals(2);
    }

    @Test
    public void queryBuilder_nested_or_2() throws SQLException {
        BoundSql boundSql1 = new WrapperAdapter().query(AnnoUserInfoDTO.class)//
                .nested(qc -> {
                    qc.eq(AnnoUserInfoDTO::getName, "user-1").eq(AnnoUserInfoDTO::getSeq, 1);
                }).or(qc -> {
                    qc.eq(AnnoUserInfoDTO::getName, "user-2").eq(AnnoUserInfoDTO::getSeq, 2);
                }).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE ( user_name = ? AND seq = ? ) OR ( user_name = ? AND seq = ? )");
        assert boundSql1.getArgs()[0].equals("user-1");
        assert boundSql1.getArgs()[1].equals(1);
        assert boundSql1.getArgs()[2].equals("user-2");
        assert boundSql1.getArgs()[3].equals(2);
    }

    @Test
    public void queryBuilder_nested_or_2_2map() throws SQLException {
        BoundSql boundSql1 = new WrapperAdapter().query(AnnoUserInfoDTO.class).asMap()//
                .nested(qc -> {
                    qc.eq("name", "user-1").eq("seq", 1);
                }).or(qc -> {
                    qc.eq("name", "user-2").eq("seq", 2);
                }).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE ( user_name = ? AND seq = ? ) OR ( user_name = ? AND seq = ? )");
        assert boundSql1.getArgs()[0].equals("user-1");
        assert boundSql1.getArgs()[1].equals(1);
        assert boundSql1.getArgs()[2].equals("user-2");
        assert boundSql1.getArgs()[3].equals(2);
    }

    @Test
    public void queryBuilder_nested_and_1() throws SQLException {
        BoundSql boundSql1 = new WrapperAdapter().query(AnnoUserInfoDTO.class)//
                .eq(AnnoUserInfoDTO::getLoginName, "a").and(qc -> {
                    qc.ge(AnnoUserInfoDTO::getCreateTime, 1); // >= ?
                    qc.le(AnnoUserInfoDTO::getCreateTime, 2); // <= ?
                }).eq(AnnoUserInfoDTO::getLoginName, 123).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? AND ( register_time >= ? AND register_time <= ? ) AND login_name = ?");
        assert boundSql1.getArgs()[0].equals("a");
        assert boundSql1.getArgs()[1].equals(1);
        assert boundSql1.getArgs()[2].equals(2);
        assert boundSql1.getArgs()[3].equals(123);

        BoundSql boundSql2 = new WrapperAdapter().query(AnnoUserInfoDTO.class)//
                .eq(AnnoUserInfoDTO::getLoginName, "a").and().nested(qc -> {
                    qc.ge(AnnoUserInfoDTO::getCreateTime, 1); // >= ?
                    qc.le(AnnoUserInfoDTO::getCreateTime, 2); // <= ?
                }).eq(AnnoUserInfoDTO::getLoginName, 123).getBoundSql();
        assert boundSql2.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? AND ( register_time >= ? AND register_time <= ? ) AND login_name = ?");
        assert boundSql2.getArgs()[0].equals("a");
        assert boundSql2.getArgs()[1].equals(1);
        assert boundSql2.getArgs()[2].equals(2);
        assert boundSql2.getArgs()[3].equals(123);
    }

    @Test
    public void queryBuilder_nested_and_1_2map() throws SQLException {
        BoundSql boundSql1 = new WrapperAdapter().query(AnnoUserInfoDTO.class).asMap()//
                .eq("loginName", "a").and(qc -> {
                    qc.ge("createTime", 1); // >= ?
                    qc.le("createTime", 2); // <= ?
                }).eq("loginName", 123).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? AND ( register_time >= ? AND register_time <= ? ) AND login_name = ?");
        assert boundSql1.getArgs()[0].equals("a");
        assert boundSql1.getArgs()[1].equals(1);
        assert boundSql1.getArgs()[2].equals(2);
        assert boundSql1.getArgs()[3].equals(123);

        BoundSql boundSql2 = new WrapperAdapter().query(AnnoUserInfoDTO.class)//
                .eq(AnnoUserInfoDTO::getLoginName, "a").and().nested(qc -> {
                    qc.ge(AnnoUserInfoDTO::getCreateTime, 1); // >= ?
                    qc.le(AnnoUserInfoDTO::getCreateTime, 2); // <= ?
                }).eq(AnnoUserInfoDTO::getLoginName, 123).getBoundSql();
        assert boundSql2.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? AND ( register_time >= ? AND register_time <= ? ) AND login_name = ?");
        assert boundSql2.getArgs()[0].equals("a");
        assert boundSql2.getArgs()[1].equals(1);
        assert boundSql2.getArgs()[2].equals(2);
        assert boundSql2.getArgs()[3].equals(123);
    }

    @Test
    public void queryBuilder_nested_and_2() throws SQLException {
        BoundSql boundSql1 = new WrapperAdapter().query(AnnoUserInfoDTO.class)//
                .nested(qc -> {
                    qc.eq(AnnoUserInfoDTO::getSeq, 1).or().eq(AnnoUserInfoDTO::getSeq, 2);
                }).and(qc -> {
                    qc.eq(AnnoUserInfoDTO::getName, "user-1").or().eq(AnnoUserInfoDTO::getName, "user-2");
                }).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE ( seq = ? OR seq = ? ) AND ( user_name = ? OR user_name = ? )");
        assert boundSql1.getArgs()[0].equals(1);
        assert boundSql1.getArgs()[1].equals(2);
        assert boundSql1.getArgs()[2].equals("user-1");
        assert boundSql1.getArgs()[3].equals("user-2");
    }

    @Test
    public void queryBuilder_nested_and_2_2map() throws SQLException {
        BoundSql boundSql1 = new WrapperAdapter().query(AnnoUserInfoDTO.class).asMap()//
                .nested(qc -> {
                    qc.eq("seq", 1).or().eq("seq", 2);
                }).and(qc -> {
                    qc.eq("name", "user-1").or().eq("name", "user-2");
                }).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE ( seq = ? OR seq = ? ) AND ( user_name = ? OR user_name = ? )");
        assert boundSql1.getArgs()[0].equals(1);
        assert boundSql1.getArgs()[1].equals(2);
        assert boundSql1.getArgs()[2].equals("user-1");
        assert boundSql1.getArgs()[3].equals("user-2");
    }

    @Test
    public void queryBuilder_nested_1() throws SQLException {
        BoundSql boundSql1 = new WrapperAdapter().query(AnnoUserInfoDTO.class)//
                .eq(AnnoUserInfoDTO::getLoginName, "a").nested(qc -> {
                    qc.ge(AnnoUserInfoDTO::getCreateTime, 1); // >= ?
                    qc.le(AnnoUserInfoDTO::getCreateTime, 2); // <= ?
                }).eq(AnnoUserInfoDTO::getLoginName, 123).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? AND ( register_time >= ? AND register_time <= ? ) AND login_name = ?");
        assert boundSql1.getArgs()[0].equals("a");
        assert boundSql1.getArgs()[1].equals(1);
        assert boundSql1.getArgs()[2].equals(2);
        assert boundSql1.getArgs()[3].equals(123);

        BoundSql boundSql2 = new WrapperAdapter().query(AnnoUserInfoDTO.class)//
                .nested(nq0 -> {
                    nq0.nested(nq1 -> {
                        nq1.ge(AnnoUserInfoDTO::getCreateTime, 1); // >= ?
                        nq1.le(AnnoUserInfoDTO::getCreateTime, 2); // <= ?
                    }).nested(nq2 -> {
                        nq2.eq(AnnoUserInfoDTO::getSeq, 1);
                    });
                }).eq(AnnoUserInfoDTO::getLoginName, 123).getBoundSql();
        assert boundSql2.getSqlString().equals("SELECT * FROM user_info WHERE ( ( register_time >= ? AND register_time <= ? ) AND ( seq = ? ) ) AND login_name = ?");
        assert boundSql2.getArgs()[0].equals(1);
        assert boundSql2.getArgs()[1].equals(2);
        assert boundSql2.getArgs()[2].equals(1);
        assert boundSql2.getArgs()[3].equals(123);

        BoundSql boundSql3 = new WrapperAdapter().query(AnnoUserInfoDTO.class)//
                .nested(nq0 -> {
                    nq0.nested(nq1 -> {
                        nq1.ge(AnnoUserInfoDTO::getCreateTime, 1); // >= ?
                        nq1.le(AnnoUserInfoDTO::getCreateTime, 2); // <= ?
                    }).nested(nq2 -> {
                        nq2.eq(AnnoUserInfoDTO::getSeq, 1);
                    });
                }).getBoundSql();
        assert boundSql3.getSqlString().equals("SELECT * FROM user_info WHERE ( ( register_time >= ? AND register_time <= ? ) AND ( seq = ? ) )");
        assert boundSql3.getArgs()[0].equals(1);
        assert boundSql3.getArgs()[1].equals(2);
        assert boundSql3.getArgs()[2].equals(1);
    }

    @Test
    public void queryBuilder_nested_1_2map() throws SQLException {
        BoundSql boundSql1 = new WrapperAdapter().query(AnnoUserInfoDTO.class).asMap()//
                .eq("loginName", "a").nested(nestedQuery -> {
                    nestedQuery.ge("createTime", 1); // >= ?
                    nestedQuery.le("createTime", 2); // <= ?
                }).eq("loginName", 123).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE login_name = ? AND ( register_time >= ? AND register_time <= ? ) AND login_name = ?");
        assert boundSql1.getArgs()[0].equals("a");
        assert boundSql1.getArgs()[1].equals(1);
        assert boundSql1.getArgs()[2].equals(2);
        assert boundSql1.getArgs()[3].equals(123);

        BoundSql boundSql2 = new WrapperAdapter().query(AnnoUserInfoDTO.class)//
                .nested(nq0 -> {
                    nq0.nested(nq1 -> {
                        nq1.ge(AnnoUserInfoDTO::getCreateTime, 1); // >= ?
                        nq1.le(AnnoUserInfoDTO::getCreateTime, 2); // <= ?
                    }).nested(nq2 -> {
                        nq2.eq(AnnoUserInfoDTO::getSeq, 1);
                    });
                }).eq(AnnoUserInfoDTO::getLoginName, 123).getBoundSql();
        assert boundSql2.getSqlString().equals("SELECT * FROM user_info WHERE ( ( register_time >= ? AND register_time <= ? ) AND ( seq = ? ) ) AND login_name = ?");
        assert boundSql2.getArgs()[0].equals(1);
        assert boundSql2.getArgs()[1].equals(2);
        assert boundSql2.getArgs()[2].equals(1);
        assert boundSql2.getArgs()[3].equals(123);

        BoundSql boundSql3 = new WrapperAdapter().query(AnnoUserInfoDTO.class)//
                .nested(nq0 -> {
                    nq0.nested(nq1 -> {
                        nq1.ge(AnnoUserInfoDTO::getCreateTime, 1); // >= ?
                        nq1.le(AnnoUserInfoDTO::getCreateTime, 2); // <= ?
                    }).nested(nq2 -> {
                        nq2.eq(AnnoUserInfoDTO::getSeq, 1);
                    });
                }).getBoundSql();
        assert boundSql3.getSqlString().equals("SELECT * FROM user_info WHERE ( ( register_time >= ? AND register_time <= ? ) AND ( seq = ? ) )");
        assert boundSql3.getArgs()[0].equals(1);
        assert boundSql3.getArgs()[1].equals(2);
        assert boundSql3.getArgs()[2].equals(1);
    }

    @Test
    public void queryBuilder_nested_not_1() throws SQLException {
        BoundSql boundSql1 = new WrapperAdapter().query(AnnoUserInfoDTO.class)//
                .not(qc -> {
                    qc.eq(AnnoUserInfoDTO::getSeq, 1).or().eq(AnnoUserInfoDTO::getLoginName, "a");
                }).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE NOT ( seq = ? OR login_name = ? )");
        assert boundSql1.getArgs()[0].equals(1);
        assert boundSql1.getArgs()[1].equals("a");
    }

    @Test
    public void queryBuilder_nested_not_1_2map() throws SQLException {
        BoundSql boundSql1 = new WrapperAdapter().query(AnnoUserInfoDTO.class).asMap()//
                .not(qc -> {
                    qc.eq("seq", 1).or().eq("loginName", "a");
                }).getBoundSql();
        assert boundSql1.getSqlString().equals("SELECT * FROM user_info WHERE NOT ( seq = ? OR login_name = ? )");
        assert boundSql1.getArgs()[0].equals(1);
        assert boundSql1.getArgs()[1].equals("a");
    }
}
