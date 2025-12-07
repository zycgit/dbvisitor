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
package net.hasor.dbvisitor.dynamic.logic;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.hasor.dbvisitor.dynamic.DynamicParsed;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.dynamic.TestQueryContext;
import net.hasor.dbvisitor.dynamic.dto.UserFutures;
import net.hasor.dbvisitor.dynamic.dto.UsersDTO;
import net.hasor.dbvisitor.dynamic.segment.PlanDynamicSql;
import org.junit.Test;

/**
 * 多个 SQL 节点组合成一个 SqlNode
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-05-24
 */
public class LogicTest {
    private TestQueryContext registryCtx() {
        return new TestQueryContext();
    }

    @Test
    public void bindDynamicSql() throws SQLException {
        UsersDTO dto = new UsersDTO();
        dto.setFutures(new UserFutures());

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("users", dto);
        ctx.put("ctxNumber", 123);

        assert dto.getFutures().getExt2() == null;

        BindDynamicSql bindSql = new BindDynamicSql("userName", "users.futures.ext2 = ctxNumber");
        SqlBuilder sqlBuilder = bindSql.buildQuery(ctx, registryCtx());

        assert !bindSql.isHaveInjection();
        assert sqlBuilder.getSqlString().equals("");
        assert sqlBuilder.getArgs().length == 0;
        assert dto.getFutures().getExt2() == 123;
    }

    @Test
    public void planDynamicSql_1() throws SQLException {
        PlanDynamicSql textSql = new PlanDynamicSql("text body");
        SqlBuilder sqlBuilder = textSql.buildQuery(Collections.emptyMap(), registryCtx());

        assert !textSql.isHaveInjection();
        assert sqlBuilder.getSqlString().equals("text body");
        assert sqlBuilder.getArgs().length == 0;
    }

    @Test
    public void planDynamicSql_2_1() throws SQLException {
        PlanDynamicSql textSql = new PlanDynamicSql();
        textSql.appendString(null);
        textSql.appendString("text body");
        SqlBuilder sqlBuilder = textSql.buildQuery(Collections.emptyMap(), registryCtx());

        assert !textSql.isHaveInjection();
        assert sqlBuilder.getSqlString().equals("text body");
        assert sqlBuilder.getArgs().length == 0;
    }

    @Test
    public void planDynamicSql_2() throws SQLException {
        PlanDynamicSql textSql = new PlanDynamicSql("");
        textSql.appendString(null);
        textSql.appendString("text body");
        SqlBuilder sqlBuilder = textSql.buildQuery(Collections.emptyMap(), registryCtx());

        assert !textSql.isHaveInjection();
        assert sqlBuilder.getSqlString().equals("text body");
        assert sqlBuilder.getArgs().length == 0;
    }

    @Test
    public void planDynamicSql_3() throws SQLException {
        Map<String, Object> ctx = Collections.singletonMap("ctxNumber", 456);

        PlanDynamicSql textSql = new PlanDynamicSql("");
        textSql.appendString(null);
        textSql.parsedAppend("text body ${ctxNumber}");
        SqlBuilder sqlBuilder = textSql.buildQuery(ctx, registryCtx());

        assert textSql.isHaveInjection();
        assert sqlBuilder.getSqlString().equals("text body 456");
        assert sqlBuilder.getArgs().length == 0;
    }

    @Test
    public void ifDynamicSql_1() throws SQLException {
        IfDynamicSql ifSql = new IfDynamicSql("ctxNumber == 123");
        ifSql.addChildNode(new PlanDynamicSql("age = "));
        ifSql.addChildNode(DynamicParsed.getParsedSql("#{ctxNumber}"));

        //
        SqlBuilder build1 = ifSql.buildQuery(Collections.singletonMap("ctxNumber", 123), registryCtx());
        assert !ifSql.isHaveInjection();
        assert build1.getSqlString().equals("age = ?");
        assert build1.getArgs().length == 1 && build1.getArgs()[0].equals(123);

        //
        SqlBuilder build2 = ifSql.buildQuery(Collections.singletonMap("ctxNumber", 456), registryCtx());
        assert !ifSql.isHaveInjection();
        assert build2.getSqlString().equals("");
        assert build2.getArgs().length == 0;
    }

    @Test
    public void ifDynamicSql_2() throws SQLException {
        IfDynamicSql ifSql = new IfDynamicSql("ctxNumber == 123");
        ifSql.addChildNode(new PlanDynamicSql("age = ${ctxNumber}"));

        //
        SqlBuilder build1 = ifSql.buildQuery(Collections.singletonMap("ctxNumber", 123), registryCtx());
        assert ifSql.isHaveInjection();
        assert build1.getSqlString().equals("age = 123");
        assert build1.getArgs().length == 0;
    }

    @Test
    public void chooseDynamicSql_1() throws SQLException {
        ChooseDynamicSql chooseSql = new ChooseDynamicSql();
        chooseSql.addThen("ctxNumber < 123", new PlanDynamicSql("age = #{ctxNumber}"));
        chooseSql.addThen("ctxNumber < 500", new PlanDynamicSql("age = ${ctxNumber}"));
        chooseSql.setDefaultNode(new PlanDynamicSql("1 = 1"));
        assert chooseSql.isHaveInjection();
        assert chooseSql.subNodes.size() == 2;

        //
        SqlBuilder build1 = chooseSql.buildQuery(Collections.singletonMap("ctxNumber", 100), registryCtx());
        assert build1.getSqlString().equals("age = ?");
        assert build1.getArgs().length == 1 && build1.getArgs()[0].equals(100);

        //
        SqlBuilder build2 = chooseSql.buildQuery(Collections.singletonMap("ctxNumber", 400), registryCtx());
        assert chooseSql.isHaveInjection();
        assert build2.getSqlString().equals("age = 400");
        assert build2.getArgs().length == 0;

        //
        SqlBuilder build3 = chooseSql.buildQuery(Collections.singletonMap("ctxNumber", 500), registryCtx());
        assert chooseSql.isHaveInjection();
        assert build3.getSqlString().equals("1 = 1");
        assert build3.getArgs().length == 0;
    }

    @Test
    public void chooseDynamicSql_2() {
        try {
            ChooseDynamicSql chooseSql = new ChooseDynamicSql();
            chooseSql.addChildNode(new PlanDynamicSql(" abc "));
            assert false;
        } catch (Exception e) {
            assert e.getMessage().contains("only supports IfDynamicSql nodes.");
        }
    }

    @Test
    public void setDynamicSql_1() throws SQLException {
        SetDynamicSql setSql = new SetDynamicSql();
        SqlBuilder sqlBuilder = setSql.buildQuery(Collections.emptyMap(), registryCtx());

        assert !setSql.isHaveInjection();
        assert sqlBuilder.getSqlString().equals("");
        assert sqlBuilder.getArgs().length == 0;
    }

    @Test
    public void setDynamicSql_2() throws SQLException {
        Map<String, Object> ctx = Collections.singletonMap("ctxNumber", 456);

        SetDynamicSql setSql = new SetDynamicSql();
        setSql.addChildNode(new PlanDynamicSql("abc = #{ctxNumber}"));
        SqlBuilder sqlBuilder = setSql.buildQuery(ctx, registryCtx());

        assert !setSql.isHaveInjection();
        assert sqlBuilder.getSqlString().equals("set abc = ? ");
        assert sqlBuilder.getArgs().length == 1 && sqlBuilder.getArgs()[0].equals(456);
    }

    @Test
    public void setDynamicSql_3() throws SQLException {
        Map<String, Object> ctx = Collections.singletonMap("ctxNumber", 456);

        SetDynamicSql setSql = new SetDynamicSql();
        setSql.addChildNode(new PlanDynamicSql("abc = #{ctxNumber},"));
        setSql.addChildNode(new PlanDynamicSql("abc = #{ctxNumber},"));

        SqlBuilder sqlBuilder = setSql.buildQuery(ctx, registryCtx());

        assert !setSql.isHaveInjection();
        assert sqlBuilder.getSqlString().equals("set abc = ?,abc = ? ");
        assert sqlBuilder.getArgs().length == 2 && sqlBuilder.getArgs()[0].equals(456) && sqlBuilder.getArgs()[1].equals(456);
    }

    @Test
    public void whereDynamicSql_1() throws SQLException {
        WhereDynamicSql setSql = new WhereDynamicSql();
        SqlBuilder sqlBuilder = setSql.buildQuery(Collections.emptyMap(), registryCtx());

        assert !setSql.isHaveInjection();
        assert sqlBuilder.getSqlString().equals("");
        assert sqlBuilder.getArgs().length == 0;
    }

    @Test
    public void whereDynamicSql_2() throws SQLException {
        Map<String, Object> ctx = Collections.singletonMap("ctxNumber", 456);

        WhereDynamicSql setSql = new WhereDynamicSql();
        setSql.addChildNode(new PlanDynamicSql("abc = #{ctxNumber}"));
        SqlBuilder sqlBuilder = setSql.buildQuery(ctx, registryCtx());

        assert !setSql.isHaveInjection();
        assert sqlBuilder.getSqlString().equals("where abc = ? ");
        assert sqlBuilder.getArgs().length == 1 && sqlBuilder.getArgs()[0].equals(456);
    }

    @Test
    public void whereDynamicSql_3() throws SQLException {
        Map<String, Object> ctx = Collections.singletonMap("ctxNumber", 456);

        WhereDynamicSql setSql = new WhereDynamicSql();
        setSql.addChildNode(new PlanDynamicSql(" and abc = #{ctxNumber}"));
        setSql.addChildNode(new PlanDynamicSql(" and abc = #{ctxNumber}"));

        SqlBuilder sqlBuilder = setSql.buildQuery(ctx, registryCtx());

        assert !setSql.isHaveInjection();
        assert sqlBuilder.getSqlString().equals("where  abc = ? and abc = ? ");
        assert sqlBuilder.getArgs().length == 2 && sqlBuilder.getArgs()[0].equals(456) && sqlBuilder.getArgs()[1].equals(456);
    }

    @Test
    public void macroDynamicSql_1() throws SQLException {
        TestQueryContext manager = new TestQueryContext();
        manager.addMacro("abc", "aacc");

        Map<String, Object> ctx = Collections.singletonMap("ctxNumber", 456);
        MacroDynamicSql macroSql = new MacroDynamicSql("abc");
        SqlBuilder sqlBuilder = macroSql.buildQuery(ctx, manager);

        assert macroSql.isHaveInjection();
        assert sqlBuilder.getSqlString().equals("aacc");
        assert sqlBuilder.getArgs().length == 0;
    }

    @Test
    public void foreachDynamicSql_1() throws SQLException {
        TestQueryContext manager = new TestQueryContext();
        manager.addMacro("abc", "aacc");

        ForeachDynamicSql forSql = new ForeachDynamicSql("array", "item", "(", ")", ",");
        forSql.addChildNode(new PlanDynamicSql("#{item}"));

        Map<String, Object> ctx = Collections.singletonMap("array", Arrays.asList("a", "b", "c", "d", "e"));
        SqlBuilder sqlBuilder = forSql.buildQuery(ctx, manager);

        assert !forSql.isHaveInjection();
        assert sqlBuilder.getSqlString().equals("(?,?,?,?,?)");
        assert sqlBuilder.getArgs().length == 5 && //
                sqlBuilder.getArgs()[0].equals("a") && sqlBuilder.getArgs()[1].equals("b") && sqlBuilder.getArgs()[2].equals("c") &&//
                sqlBuilder.getArgs()[3].equals("d") && sqlBuilder.getArgs()[4].equals("e");
    }
}