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
import net.hasor.dbvisitor.dialect.BatchBoundSql;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dynamic.MacroRegistry;
import net.hasor.dbvisitor.dynamic.RuleRegistry;
import net.hasor.dbvisitor.jdbc.core.JdbcQueryContext;
import net.hasor.dbvisitor.lambda.dto.AnnoUserInfoDTO;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-3-22
 */
public class FreedomToCamelBuildDeleteTest {

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
        MapDelete lambda = newLambda().deleteFreedom("user_info");
        lambda.allowEmptyWhere();

        BoundSql boundSql1 = lambda.getBoundSql();
        assert boundSql1.getSqlString().equals("DELETE FROM user_info");
    }

    @Test
    public void deleteBuilder_2() throws SQLException {
        MapDelete lambda = newLambda().deleteFreedom("user_info");
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
        MapDelete lambda = newLambda().deleteFreedom("user_info");
        lambda.eq("loginName", "admin").and().eq("loginPassword", "pass");

        BoundSql boundSql1 = lambda.getBoundSql();
        assert boundSql1.getSqlString().equals("DELETE FROM user_info WHERE login_name = ? AND login_password = ?");
    }

    @Test
    public void deleteBuilder_4() throws SQLException {
        AnnoUserInfoDTO userInfo = new AnnoUserInfoDTO();
        userInfo.setName("zyc");
        userInfo.setLoginName("login");
        userInfo.setPassword("pwd");

        //case 1
        MapDelete delCase1 = newLambda().deleteFreedom("user_info");
        boolean isZyc1 = userInfo.getName().equals("zyc");
        delCase1.eq(isZyc1, "loginName", userInfo.getLoginName())//
                .eq(isZyc1, "loginPassword", userInfo.getPassword())//
                .gt("seq", 1);

        BoundSql boundSql1 = delCase1.getBoundSql();
        assert boundSql1.getSqlString().equals("DELETE FROM user_info WHERE login_name = ? AND login_password = ? AND seq > ?");
        assert boundSql1.getArgs()[0].equals("login");
        assert boundSql1.getArgs()[1].equals("pwd");
        assert boundSql1.getArgs()[2].equals(1);

        // case 2
        userInfo.setName("cyz");
        MapDelete delCase2 = newLambda().deleteFreedom("user_info");
        boolean isZyc2 = userInfo.getName().equals("zyc");
        delCase2.eq(isZyc2, "loginName", userInfo.getLoginName())//
                .eq(isZyc2, "loginPassword", userInfo.getPassword())//
                .gt("seq", 1);

        BoundSql boundSql2 = delCase2.getBoundSql();
        assert boundSql2.getSqlString().equals("DELETE FROM user_info WHERE seq > ?");
        assert boundSql2.getArgs()[0].equals(1);
    }

}
