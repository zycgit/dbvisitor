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
import net.hasor.dbvisitor.dialect.BatchBoundSql;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.wrapper.dto.AnnoUserInfoDTO;
import org.junit.Test;

/***
 * @version : 2021-3-22
 * @author 赵永春 (zyc@hasor.net)
 */
public class BuildEntDeleteTest {
    @Test
    public void deleteBuilder_1() {
        EntityDeleteWrapper<AnnoUserInfoDTO> lambda = new WrapperAdapter().deleteByEntity(AnnoUserInfoDTO.class);
        lambda.allowEmptyWhere();

        BoundSql boundSql1 = lambda.getBoundSql();
        assert boundSql1.getSqlString().equals("DELETE FROM user_info");
    }

    @Test
    public void deleteBuilder_1_2map() {
        MapDeleteWrapper lambda = new WrapperAdapter().deleteByEntity(AnnoUserInfoDTO.class).asMap();
        lambda.allowEmptyWhere();

        BoundSql boundSql1 = lambda.getBoundSql();
        assert boundSql1.getSqlString().equals("DELETE FROM user_info");
    }

    @Test
    public void deleteBuilder_2() {
        EntityDeleteWrapper<AnnoUserInfoDTO> lambda = new WrapperAdapter().deleteByEntity(AnnoUserInfoDTO.class);
        lambda.and(queryBuilder -> {
            queryBuilder.eq(AnnoUserInfoDTO::getSeq, 123);
        });

        BoundSql boundSql1 = lambda.getBoundSql();
        assert !(boundSql1 instanceof BatchBoundSql);
        assert boundSql1.getSqlString().equals("DELETE FROM user_info WHERE ( seq = ? )");
        assert boundSql1.getArgs()[0].equals(123);
    }

    @Test
    public void deleteBuilder_2_2map() {
        MapDeleteWrapper lambda = new WrapperAdapter().deleteByEntity(AnnoUserInfoDTO.class).asMap();
        lambda.and(queryBuilder -> {
            queryBuilder.eq("seq", 123);
        });

        BoundSql boundSql1 = lambda.getBoundSql();
        assert !(boundSql1 instanceof BatchBoundSql);
        assert boundSql1.getSqlString().equals("DELETE FROM user_info WHERE ( seq = ? )");
        assert boundSql1.getArgs()[0].equals(123);
    }

    @Test
    public void deleteBuilder_3() {
        EntityDeleteWrapper<AnnoUserInfoDTO> lambda = new WrapperAdapter().deleteByEntity(AnnoUserInfoDTO.class);
        lambda.eq(AnnoUserInfoDTO::getLoginName, "admin").and().eq(AnnoUserInfoDTO::getPassword, "pass");

        BoundSql boundSql1 = lambda.getBoundSql();
        assert boundSql1.getSqlString().equals("DELETE FROM user_info WHERE login_name = ? AND login_password = ?");
    }

    @Test
    public void deleteBuilder_3_2map() {
        MapDeleteWrapper lambda = new WrapperAdapter().deleteByEntity(AnnoUserInfoDTO.class).asMap();
        lambda.eq("loginName", "admin").and().eq("password", "pass");

        BoundSql boundSql1 = lambda.getBoundSql();
        assert boundSql1.getSqlString().equals("DELETE FROM user_info WHERE login_name = ? AND login_password = ?");
    }

    @Test
    public void deleteBuilder_4() {
        AnnoUserInfoDTO userInfo = new AnnoUserInfoDTO();
        userInfo.setName("zyc");
        userInfo.setLoginName("login");
        userInfo.setPassword("pwd");

        //case 1
        EntityDeleteWrapper<AnnoUserInfoDTO> delCase1 = new WrapperAdapter().deleteByEntity(AnnoUserInfoDTO.class);
        boolean isZyc1 = userInfo.getName().equals("zyc");
        delCase1.eq(isZyc1, AnnoUserInfoDTO::getLoginName, userInfo.getLoginName())//
                .eq(isZyc1, AnnoUserInfoDTO::getPassword, userInfo.getPassword())//
                .gt(AnnoUserInfoDTO::getSeq, 1);

        BoundSql boundSql1 = delCase1.getBoundSql();
        assert boundSql1.getSqlString().equals("DELETE FROM user_info WHERE login_name = ? AND login_password = ? AND seq > ?");
        assert boundSql1.getArgs()[0].equals("login");
        assert boundSql1.getArgs()[1].equals("pwd");
        assert boundSql1.getArgs()[2].equals(1);

        // case 2
        userInfo.setName("cyz");
        EntityDeleteWrapper<AnnoUserInfoDTO> delCase2 = new WrapperAdapter().deleteByEntity(AnnoUserInfoDTO.class);
        boolean isZyc2 = userInfo.getName().equals("zyc");
        delCase2.eq(isZyc2, AnnoUserInfoDTO::getLoginName, userInfo.getLoginName())//
                .eq(isZyc2, AnnoUserInfoDTO::getPassword, userInfo.getPassword())//
                .gt(AnnoUserInfoDTO::getSeq, 1);

        BoundSql boundSql2 = delCase2.getBoundSql();
        assert boundSql2.getSqlString().equals("DELETE FROM user_info WHERE seq > ?");
        assert boundSql2.getArgs()[0].equals(1);
    }

    @Test
    public void deleteBuilder_4_2map() {
        AnnoUserInfoDTO userInfo = new AnnoUserInfoDTO();
        userInfo.setName("zyc");
        userInfo.setLoginName("login");
        userInfo.setPassword("pwd");

        //case 1
        MapDeleteWrapper delCase1 = new WrapperAdapter().deleteByEntity(AnnoUserInfoDTO.class).asMap();
        boolean isZyc1 = userInfo.getName().equals("zyc");
        delCase1.eq(isZyc1, "loginName", userInfo.getLoginName())//
                .eq(isZyc1, "password", userInfo.getPassword())//
                .gt("seq", 1);

        BoundSql boundSql1 = delCase1.getBoundSql();
        assert boundSql1.getSqlString().equals("DELETE FROM user_info WHERE login_name = ? AND login_password = ? AND seq > ?");
        assert boundSql1.getArgs()[0].equals("login");
        assert boundSql1.getArgs()[1].equals("pwd");
        assert boundSql1.getArgs()[2].equals(1);

        // case 2
        userInfo.setName("cyz");
        EntityDeleteWrapper<AnnoUserInfoDTO> delCase2 = new WrapperAdapter().deleteByEntity(AnnoUserInfoDTO.class);
        boolean isZyc2 = userInfo.getName().equals("zyc");
        delCase2.eq(isZyc2, AnnoUserInfoDTO::getLoginName, userInfo.getLoginName())//
                .eq(isZyc2, AnnoUserInfoDTO::getPassword, userInfo.getPassword())//
                .gt(AnnoUserInfoDTO::getSeq, 1);

        BoundSql boundSql2 = delCase2.getBoundSql();
        assert boundSql2.getSqlString().equals("DELETE FROM user_info WHERE seq > ?");
        assert boundSql2.getArgs()[0].equals(1);
    }

    @Test
    public void bad_1() {
        try {
            EntityDeleteWrapper<AnnoUserInfoDTO> lambdaDelete = new WrapperAdapter().deleteByEntity(AnnoUserInfoDTO.class);
            lambdaDelete.getBoundSql();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("The dangerous DELETE operation,");
        }
    }

    @Test
    public void bad_2() {
        try {
            EntityDeleteWrapper<AnnoUserInfoDTO> lambdaDelete = new WrapperAdapter().deleteByEntity(AnnoUserInfoDTO.class);
            lambdaDelete.getBoundSql();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("The dangerous DELETE operation,");
        }
    }
}
