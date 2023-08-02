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
import org.junit.Test;

import java.sql.SQLException;

import static net.hasor.test.utils.TestUtils.beanForData1;
import static net.hasor.test.utils.TestUtils.mapForData2;

/***
 * @version : 2021-3-22
 * @author 赵永春 (zyc@hasor.net)
 */
public class BuilderInsertTest extends AbstractDbTest {
    @Test
    public void insert_1() throws SQLException {
        InsertOperation<UserInfo> lambdaInsert = new LambdaTemplate().lambdaInsert(UserInfo.class);
        lambdaInsert.applyEntity(beanForData1());
        lambdaInsert.applyMap(mapForData2());

        SqlDialect dialect = new MySqlDialect();
        BoundSql boundSql1 = lambdaInsert.getBoundSql(dialect);
        assert boundSql1 instanceof BatchBoundSql;
        assert boundSql1.getSqlString().equals("INSERT INTO UserInfo (userUuid, name, loginName, loginPassword, email, seq, registerTime) VALUES (?, ?, ?, ?, ?, ?, ?)");

        BoundSql boundSql2 = lambdaInsert.useQualifier().getBoundSql(dialect);
        assert boundSql2 instanceof BatchBoundSql;
        assert boundSql2.getSqlString().equals("INSERT INTO `UserInfo` (`userUuid`, `name`, `loginName`, `loginPassword`, `email`, `seq`, `registerTime`) VALUES (?, ?, ?, ?, ?, ?, ?)");
    }

    @Test
    public void insertDuplicateKeyBlock_1() throws SQLException {
        InsertOperation<UserInfo> lambdaInsert = new LambdaTemplate().lambdaInsert(UserInfo.class);
        lambdaInsert.applyEntity(beanForData1());
        lambdaInsert.applyMap(mapForData2());
        lambdaInsert.onDuplicateStrategy(DuplicateKeyStrategy.Into);

        SqlDialect dialect = new MySqlDialect();
        BoundSql boundSql1 = lambdaInsert.getBoundSql(dialect);
        assert boundSql1 instanceof BatchBoundSql;
        assert boundSql1.getSqlString().equals("INSERT INTO UserInfo (userUuid, name, loginName, loginPassword, email, seq, registerTime) VALUES (?, ?, ?, ?, ?, ?, ?)");

        BoundSql boundSql2 = lambdaInsert.useQualifier().getBoundSql(dialect);
        assert boundSql2 instanceof BatchBoundSql;
        assert boundSql2.getSqlString().equals("INSERT INTO `UserInfo` (`userUuid`, `name`, `loginName`, `loginPassword`, `email`, `seq`, `registerTime`) VALUES (?, ?, ?, ?, ?, ?, ?)");
    }

    @Test
    public void insertDuplicateKeyUpdate_1() throws SQLException {
        InsertOperation<UserInfo> lambdaInsert = new LambdaTemplate().lambdaInsert(UserInfo.class);
        lambdaInsert.applyEntity(beanForData1());
        lambdaInsert.applyMap(mapForData2());
        lambdaInsert.onDuplicateStrategy(DuplicateKeyStrategy.Update);

        SqlDialect dialect = new MySqlDialect();
        BoundSql boundSql1 = lambdaInsert.getBoundSql(dialect);
        assert boundSql1 instanceof BatchBoundSql;
        assert boundSql1.getSqlString().equals("INSERT INTO UserInfo (userUuid, name, loginName, loginPassword, email, seq, registerTime) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE userUuid=VALUES(userUuid), name=VALUES(name), loginName=VALUES(loginName), loginPassword=VALUES(loginPassword), email=VALUES(email), seq=VALUES(seq), registerTime=VALUES(registerTime)");

        BoundSql boundSql2 = lambdaInsert.useQualifier().getBoundSql(dialect);
        assert boundSql2 instanceof BatchBoundSql;
        assert boundSql2.getSqlString().equals("INSERT INTO `UserInfo` (`userUuid`, `name`, `loginName`, `loginPassword`, `email`, `seq`, `registerTime`) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `userUuid`=VALUES(`userUuid`), `name`=VALUES(`name`), `loginName`=VALUES(`loginName`), `loginPassword`=VALUES(`loginPassword`), `email`=VALUES(`email`), `seq`=VALUES(`seq`), `registerTime`=VALUES(`registerTime`)");
    }

    @Test
    public void insertDuplicateKeyIgnore_1() throws SQLException {
        InsertOperation<UserInfo> lambdaInsert = new LambdaTemplate().lambdaInsert(UserInfo.class);
        lambdaInsert.applyEntity(beanForData1());
        lambdaInsert.applyMap(mapForData2());
        lambdaInsert.onDuplicateStrategy(DuplicateKeyStrategy.Ignore);

        SqlDialect dialect = new MySqlDialect();
        BoundSql boundSql1 = lambdaInsert.getBoundSql(dialect);
        assert boundSql1 instanceof BatchBoundSql;
        assert boundSql1.getSqlString().equals("INSERT IGNORE UserInfo (userUuid, name, loginName, loginPassword, email, seq, registerTime) VALUES (?, ?, ?, ?, ?, ?, ?)");

        BoundSql boundSql2 = lambdaInsert.useQualifier().getBoundSql(dialect);
        assert boundSql2 instanceof BatchBoundSql;
        assert boundSql2.getSqlString().equals("INSERT IGNORE `UserInfo` (`userUuid`, `name`, `loginName`, `loginPassword`, `email`, `seq`, `registerTime`) VALUES (?, ?, ?, ?, ?, ?, ?)");
    }
}
