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
import java.sql.SQLException;
import javax.sql.DataSource;
import net.hasor.dbvisitor.dialect.BatchBoundSql;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dynamic.MacroRegistry;
import net.hasor.dbvisitor.dynamic.RuleRegistry;
import net.hasor.dbvisitor.jdbc.core.JdbcQueryContext;
import net.hasor.dbvisitor.lambda.dto.UserInfo;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.junit.Test;

/***
 * @version 2021-3-22
 * @author 赵永春 (zyc@hasor.net)
 */
public class BuildToCamelPojoDeleteTest {
    private LambdaTemplate newLambda() throws SQLException {
        Options opt = Options.of().mapUnderscoreToCamelCase(true);
        JdbcQueryContext context = new JdbcQueryContext();
        context.setTypeRegistry(new TypeHandlerRegistry());
        context.setMacroRegistry(new MacroRegistry());
        context.setRuleRegistry(new RuleRegistry());
        MappingRegistry registry = new MappingRegistry(null, context.getTypeRegistry(), opt);

        return new LambdaTemplate((DataSource) null, registry, context);
    }

    @Test
    public void deleteBuilder_1() throws SQLException {
        EntityDelete<UserInfo> lambda = newLambda().delete(UserInfo.class);
        lambda.allowEmptyWhere();

        BoundSql boundSql1 = lambda.getBoundSql();
        assert boundSql1.getSqlString().equals("DELETE FROM user_info");
    }

    @Test
    public void deleteBuilder_1_2map() throws SQLException {
        MapDelete lambda = newLambda().delete(UserInfo.class).asMap();
        lambda.allowEmptyWhere();

        BoundSql boundSql1 = lambda.getBoundSql();
        assert boundSql1.getSqlString().equals("DELETE FROM user_info");
    }

    @Test
    public void deleteBuilder_2() throws SQLException {
        EntityDelete<UserInfo> lambda = newLambda().delete(UserInfo.class);
        lambda.and(queryBuilder -> {
            queryBuilder.eq(UserInfo::getSeq, 123);
        });

        BoundSql boundSql1 = lambda.getBoundSql();
        assert !(boundSql1 instanceof BatchBoundSql);
        assert boundSql1.getSqlString().equals("DELETE FROM user_info WHERE ( seq = ? )");
        assert boundSql1.getArgs()[0].equals(123);
    }

    @Test
    public void deleteBuilder_2_2map() throws SQLException {
        MapDelete lambda = newLambda().delete(UserInfo.class).asMap();
        lambda.and(queryBuilder -> {
            queryBuilder.eq("seq", 123);
        });

        BoundSql boundSql1 = lambda.getBoundSql();
        assert !(boundSql1 instanceof BatchBoundSql);
        assert boundSql1.getSqlString().equals("DELETE FROM user_info WHERE ( seq = ? )");
        assert boundSql1.getArgs()[0].equals(123);
    }

    @Test
    public void deleteBuilder_3() throws SQLException {
        EntityDelete<UserInfo> lambda = newLambda().delete(UserInfo.class);
        lambda.eq(UserInfo::getLoginName, "admin").and().eq(UserInfo::getPassword, "pass");

        BoundSql boundSql1 = lambda.getBoundSql();
        assert boundSql1.getSqlString().equals("DELETE FROM user_info WHERE login_name = ? AND password = ?");
    }

    @Test
    public void deleteBuilder_3_2map() throws SQLException {
        MapDelete lambda = newLambda().delete(UserInfo.class).asMap();
        lambda.eq("loginName", "admin").and().eq("password", "pass");

        BoundSql boundSql1 = lambda.getBoundSql();
        assert boundSql1.getSqlString().equals("DELETE FROM user_info WHERE login_name = ? AND password = ?");
    }

    @Test
    public void deleteBuilder_4() throws SQLException {
        UserInfo userInfo = new UserInfo();
        userInfo.setName("zyc");
        userInfo.setLoginName("login");
        userInfo.setPassword("pwd");

        //case 1
        EntityDelete<UserInfo> delCase1 = newLambda().delete(UserInfo.class);
        boolean isZyc1 = userInfo.getName().equals("zyc");
        delCase1.eq(isZyc1, UserInfo::getLoginName, userInfo.getLoginName())//
                .eq(isZyc1, UserInfo::getPassword, userInfo.getPassword())//
                .gt(UserInfo::getSeq, 1);

        BoundSql boundSql1 = delCase1.getBoundSql();
        assert boundSql1.getSqlString().equals("DELETE FROM user_info WHERE login_name = ? AND password = ? AND seq > ?");
        assert boundSql1.getArgs()[0].equals("login");
        assert boundSql1.getArgs()[1].equals("pwd");
        assert boundSql1.getArgs()[2].equals(1);

        // case 2
        userInfo.setName("cyz");
        EntityDelete<UserInfo> delCase2 = newLambda().delete(UserInfo.class);
        boolean isZyc2 = userInfo.getName().equals("zyc");
        delCase2.eq(isZyc2, UserInfo::getLoginName, userInfo.getLoginName())//
                .eq(isZyc2, UserInfo::getPassword, userInfo.getPassword())//
                .gt(UserInfo::getSeq, 1);

        BoundSql boundSql2 = delCase2.getBoundSql();
        assert boundSql2.getSqlString().equals("DELETE FROM user_info WHERE seq > ?");
        assert boundSql2.getArgs()[0].equals(1);
    }

    @Test
    public void deleteBuilder_4_2map() throws SQLException {
        UserInfo userInfo = new UserInfo();
        userInfo.setName("zyc");
        userInfo.setLoginName("login");
        userInfo.setPassword("pwd");

        //case 1
        MapDelete delCase1 = newLambda().delete(UserInfo.class).asMap();
        boolean isZyc1 = userInfo.getName().equals("zyc");
        delCase1.eq(isZyc1, "loginName", userInfo.getLoginName())//
                .eq(isZyc1, "password", userInfo.getPassword())//
                .gt("seq", 1);

        BoundSql boundSql1 = delCase1.getBoundSql();
        assert boundSql1.getSqlString().equals("DELETE FROM user_info WHERE login_name = ? AND password = ? AND seq > ?");
        assert boundSql1.getArgs()[0].equals("login");
        assert boundSql1.getArgs()[1].equals("pwd");
        assert boundSql1.getArgs()[2].equals(1);

        // case 2
        userInfo.setName("cyz");
        EntityDelete<UserInfo> delCase2 = newLambda().delete(UserInfo.class);
        boolean isZyc2 = userInfo.getName().equals("zyc");
        delCase2.eq(isZyc2, UserInfo::getLoginName, userInfo.getLoginName())//
                .eq(isZyc2, UserInfo::getPassword, userInfo.getPassword())//
                .gt(UserInfo::getSeq, 1);

        BoundSql boundSql2 = delCase2.getBoundSql();
        assert boundSql2.getSqlString().equals("DELETE FROM user_info WHERE seq > ?");
        assert boundSql2.getArgs()[0].equals(1);
    }

    @Test
    public void bad_1() {
        try {
            EntityDelete<UserInfo> lambdaDelete = newLambda().delete(UserInfo.class);
            lambdaDelete.getBoundSql();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("The dangerous DELETE operation,");
        }
    }

    @Test
    public void bad_2() {
        try {
            EntityDelete<UserInfo> lambdaDelete = newLambda().delete(UserInfo.class);
            lambdaDelete.getBoundSql();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("The dangerous DELETE operation,");
        }
    }
}
