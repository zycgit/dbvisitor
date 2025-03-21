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
import net.hasor.dbvisitor.dialect.provider.MySqlDialect;
import net.hasor.dbvisitor.dynamic.MacroRegistry;
import net.hasor.dbvisitor.dynamic.RuleRegistry;
import net.hasor.dbvisitor.jdbc.core.JdbcQueryContext;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import net.hasor.dbvisitor.wrapper.dto.UserInfo;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static net.hasor.test.utils.TestUtils.newID;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-3-22
 */
public class BuildPojoInsertTest {
    private WrapperAdapter newLambda() throws SQLException {
        Options opt = Options.of().dialect(new MySqlDialect());
        JdbcQueryContext context = new JdbcQueryContext();
        context.setTypeRegistry(new TypeHandlerRegistry());
        context.setMacroRegistry(new MacroRegistry());
        context.setRuleRegistry(new RuleRegistry());
        MappingRegistry registry = new MappingRegistry(null, context.getTypeRegistry(), opt);

        return new WrapperAdapter((DataSource) null, registry, context);
    }

    private static Map<String, Object> mapForData1() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("uid", newID());
        map.put("name", "Carmen");
        map.put("loginName", "carmen");
        map.put("loginPassword", "123");
        map.put("email", "carmen@cc.com");
        map.put("seq", 1);
        map.put("createTime", new Date());

        map.put("abc", "abc");
        return map;
    }

    @Test
    public void insert_1() throws SQLException {
        InsertWrapper<UserInfo> lambdaInsert = newLambda().insert(UserInfo.class);
        lambdaInsert.applyMap(mapForData1());

        BoundSql boundSql1 = lambdaInsert.getBoundSql();
        assert boundSql1 instanceof BatchBoundSql;
        assert boundSql1.getSqlString().equals("INSERT INTO UserInfo (uid, name, loginName, email, seq, createTime) VALUES (?, ?, ?, ?, ?, ?)");
    }

    @Test
    public void insert_1_2map() throws SQLException {
        Map<String, Object> data = mapForData1();

        InsertWrapper<Map<String, Object>> lambdaInsert = newLambda().insert(UserInfo.class).asMap();
        lambdaInsert.applyMap(data);

        BoundSql boundSql1 = lambdaInsert.getBoundSql();
        assert boundSql1 instanceof BatchBoundSql;
        assert boundSql1.getSqlString().equals("INSERT INTO UserInfo (uid, name, loginName, email, seq, createTime) VALUES (?, ?, ?, ?, ?, ?)");
    }

    @Test
    public void insert_duplicateKeyBlock_1() throws SQLException {
        InsertWrapper<UserInfo> lambdaInsert = newLambda().insert(UserInfo.class);
        lambdaInsert.applyMap(mapForData1());
        lambdaInsert.onDuplicateStrategy(DuplicateKeyStrategy.Into);

        BoundSql boundSql1 = lambdaInsert.getBoundSql();
        assert boundSql1 instanceof BatchBoundSql;
        assert boundSql1.getSqlString().equals("INSERT INTO UserInfo (uid, name, loginName, email, seq, createTime) VALUES (?, ?, ?, ?, ?, ?)");
    }

    @Test
    public void insert_duplicateKeyBlock_1_2map() throws SQLException {
        InsertWrapper<Map<String, Object>> lambdaInsert = newLambda().insert(UserInfo.class).asMap();
        lambdaInsert.applyMap(mapForData1());
        lambdaInsert.onDuplicateStrategy(DuplicateKeyStrategy.Into);

        BoundSql boundSql1 = lambdaInsert.getBoundSql();
        assert boundSql1 instanceof BatchBoundSql;
        assert boundSql1.getSqlString().equals("INSERT INTO UserInfo (uid, name, loginName, email, seq, createTime) VALUES (?, ?, ?, ?, ?, ?)");
    }

    @Test
    public void insert_duplicateKeyUpdate_1() throws SQLException {
        InsertWrapper<UserInfo> lambdaInsert = newLambda().insert(UserInfo.class);
        lambdaInsert.applyMap(mapForData1());
        lambdaInsert.onDuplicateStrategy(DuplicateKeyStrategy.Update);

        BoundSql boundSql1 = lambdaInsert.getBoundSql();
        assert boundSql1 instanceof BatchBoundSql;
        assert boundSql1.getSqlString().equals("INSERT INTO UserInfo (uid, name, loginName, email, seq, createTime) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE uid=VALUES(uid), name=VALUES(name), loginName=VALUES(loginName), email=VALUES(email), seq=VALUES(seq), createTime=VALUES(createTime)");
    }

    @Test
    public void insert_duplicateKeyUpdate_1_2map() throws SQLException {
        InsertWrapper<Map<String, Object>> lambdaInsert = newLambda().insert(UserInfo.class).asMap();
        lambdaInsert.applyMap(mapForData1());
        lambdaInsert.onDuplicateStrategy(DuplicateKeyStrategy.Update);

        BoundSql boundSql1 = lambdaInsert.getBoundSql();
        assert boundSql1 instanceof BatchBoundSql;
        assert boundSql1.getSqlString().equals("INSERT INTO UserInfo (uid, name, loginName, email, seq, createTime) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE uid=VALUES(uid), name=VALUES(name), loginName=VALUES(loginName), email=VALUES(email), seq=VALUES(seq), createTime=VALUES(createTime)");
    }

    @Test
    public void insert_duplicateKeyIgnore_1() throws SQLException {
        InsertWrapper<UserInfo> lambdaInsert = newLambda().insert(UserInfo.class);
        lambdaInsert.applyMap(mapForData1());
        lambdaInsert.onDuplicateStrategy(DuplicateKeyStrategy.Ignore);

        BoundSql boundSql1 = lambdaInsert.getBoundSql();
        assert boundSql1 instanceof BatchBoundSql;
        assert boundSql1.getSqlString().equals("INSERT IGNORE UserInfo (uid, name, loginName, email, seq, createTime) VALUES (?, ?, ?, ?, ?, ?)");
    }

    @Test
    public void insert_duplicateKeyIgnore_1_2map() throws SQLException {
        InsertWrapper<Map<String, Object>> lambdaInsert = newLambda().insert(UserInfo.class).asMap();
        lambdaInsert.applyMap(mapForData1());
        lambdaInsert.onDuplicateStrategy(DuplicateKeyStrategy.Ignore);

        BoundSql boundSql1 = lambdaInsert.getBoundSql();
        assert boundSql1 instanceof BatchBoundSql;
        assert boundSql1.getSqlString().equals("INSERT IGNORE UserInfo (uid, name, loginName, email, seq, createTime) VALUES (?, ?, ?, ?, ?, ?)");
    }
}
