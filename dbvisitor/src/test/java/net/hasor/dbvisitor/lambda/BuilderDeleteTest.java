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
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.provider.MySqlDialect;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.UserInfo;
import net.hasor.test.dto.keywords_table;
import net.hasor.test.dto.user_info;
import org.junit.Test;

/***
 * @version : 2021-3-22
 * @author 赵永春 (zyc@hasor.net)
 */
public class BuilderDeleteTest extends AbstractDbTest {
    @Test
    public void deleteBuilder_1() {
        try {
            EntityDeleteOperation<UserInfo> lambdaDelete = new LambdaTemplate().lambdaDelete(UserInfo.class);
            SqlDialect dialect = new MySqlDialect();
            lambdaDelete.getBoundSql(dialect);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("The dangerous DELETE operation,");
        }
    }

    @Test
    public void deleteBuilder_2() {
        EntityDeleteOperation<user_info> lambdaDelete = new LambdaTemplate().lambdaDelete(user_info.class);
        lambdaDelete.allowEmptyWhere();

        SqlDialect dialect = new MySqlDialect();
        BoundSql boundSql1 = lambdaDelete.getBoundSql(dialect);
        assert boundSql1.getSqlString().equals("DELETE FROM user_info");

        BoundSql boundSql2 = lambdaDelete.useQualifier().getBoundSql(dialect);
        assert boundSql2.getSqlString().equals("DELETE FROM `user_info`");
    }

    @Test
    public void deleteBuilder_3() {
        EntityDeleteOperation<UserInfo> lambdaDelete = new LambdaTemplate().lambdaDelete(UserInfo.class);
        lambdaDelete.allowEmptyWhere();

        SqlDialect dialect = new MySqlDialect();
        BoundSql boundSql1 = lambdaDelete.getBoundSql(dialect);
        assert boundSql1.getSqlString().equals("DELETE FROM UserInfo");

        BoundSql boundSql2 = lambdaDelete.useQualifier().getBoundSql(dialect);
        assert boundSql2.getSqlString().equals("DELETE FROM `UserInfo`");
    }

    @Test
    public void deleteBuilder_4() {
        EntityDeleteOperation<keywords_table> lambdaDelete = new LambdaTemplate().lambdaDelete(keywords_table.class);
        lambdaDelete.and(queryBuilder -> {
            queryBuilder.eq(keywords_table::getIndex, 123);
        });

        SqlDialect dialect = new MySqlDialect();
        BoundSql boundSql1 = lambdaDelete.getBoundSql(dialect);
        assert !(boundSql1 instanceof BatchBoundSql);
        assert boundSql1.getSqlString().equals("DELETE FROM keywords_table WHERE ( `index` = ? )");

        BoundSql boundSql2 = lambdaDelete.useQualifier().getBoundSql(dialect);
        assert !(boundSql2 instanceof BatchBoundSql);
        assert boundSql2.getSqlString().equals("DELETE FROM `keywords_table` WHERE ( `index` = ? )");
    }

    @Test
    public void deleteBuilder_5() {
        EntityDeleteOperation<UserInfo> lambdaDelete = new LambdaTemplate().lambdaDelete(UserInfo.class);
        lambdaDelete.eq(UserInfo::getLoginName, "admin").and().eq(UserInfo::getLoginPassword, "pass");

        SqlDialect dialect = new MySqlDialect();
        BoundSql boundSql1 = lambdaDelete.getBoundSql(dialect);
        assert boundSql1.getSqlString().equals("DELETE FROM UserInfo WHERE loginName = ? AND loginPassword = ?");

        BoundSql boundSql2 = lambdaDelete.useQualifier().getBoundSql(dialect);
        assert boundSql2.getSqlString().equals("DELETE FROM `UserInfo` WHERE `loginName` = ? AND `loginPassword` = ?");
    }
}
