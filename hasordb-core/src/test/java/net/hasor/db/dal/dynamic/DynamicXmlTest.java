/*
 * Copyright 2002-2005 the original author or authors.
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
package net.hasor.db.dal.dynamic;
import net.hasor.cobble.ResourcesUtils;
import net.hasor.cobble.io.IOUtils;
import net.hasor.db.dal.repository.config.InsertSqlConfig;
import net.hasor.db.dialect.SqlBuilder;
import net.hasor.db.types.UnknownTypeHandler;
import net.hasor.db.types.handler.StringTypeHandler;
import net.hasor.test.db.dal.dynamic.TextBuilderContext;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DynamicXmlTest {
    private final DynamicParser xmlParser = new DynamicParser();

    private String loadString(String queryConfig) throws IOException {
        return IOUtils.readToString(ResourcesUtils.getResourceAsStream(queryConfig), "UTF-8");
    }

    @Test
    public void ifTest_01() throws Throwable {
        String queryConfig = loadString("/net_hasor_db/dal_dynamic/fragment/if_01.xml");
        DynamicSql parseXml = xmlParser.parseDynamicSql(queryConfig);
        //
        String querySql1 = loadString("/net_hasor_db/dal_dynamic/fragment/if_01.xml.sql_1");
        Map<String, Object> data1 = new HashMap<>();
        data1.put("ownerID", "123");
        data1.put("ownerType", "SYSTEM");
        SqlBuilder builder1 = parseXml.buildQuery(data1, new TextBuilderContext());
        assert builder1.getSqlString().trim().equals(querySql1.trim());
        assert ((SqlArg) builder1.getArgs()[0]).getValue().equals("123");
        assert ((SqlArg) builder1.getArgs()[1]).getValue().equals("SYSTEM");
        //
        String querySql2 = loadString("/net_hasor_db/dal_dynamic/fragment/if_01.xml.sql_2");
        Map<String, Object> data2 = new HashMap<>();
        data1.put("ownerID", "123");
        data1.put("ownerType", null);
        SqlBuilder builder2 = parseXml.buildQuery(data2, new TextBuilderContext());
        assert builder2.getSqlString().trim().equals(querySql2.trim());
        assert builder2.getArgs().length == 0;
    }

    @Test
    public void includeTest_01() throws Throwable {
        String queryConfig = loadString("/net_hasor_db/dal_dynamic/fragment/include_01.xml");
        DynamicSql parseXml = xmlParser.parseDynamicSql(queryConfig);
        //
        String querySql1 = loadString("/net_hasor_db/dal_dynamic/fragment/include_01.xml.sql_1");
        Map<String, Object> data1 = new HashMap<>();
        data1.put("eventType", "123");
        SqlBuilder builder1 = parseXml.buildQuery(data1, new TextBuilderContext());
        assert builder1.getSqlString().trim().equals(querySql1.trim());
        assert ((SqlArg) builder1.getArgs()[0]).getValue().equals("123");
    }

    @Test
    public void foreachTest_01() throws Throwable {
        String queryConfig = loadString("/net_hasor_db/dal_dynamic/fragment/foreach_03.xml");
        DynamicSql parseXml = xmlParser.parseDynamicSql(queryConfig);
        //
        String querySql1 = loadString("/net_hasor_db/dal_dynamic/fragment/foreach_03.xml.sql_1");
        Map<String, Object> data1 = new HashMap<>();
        data1.put("eventTypes", Arrays.asList("a", "b", "c", "d", "e"));
        SqlBuilder builder1 = parseXml.buildQuery(data1, new TextBuilderContext());
        assert builder1.getSqlString().trim().equals(querySql1.trim());
        assert ((SqlArg) builder1.getArgs()[0]).getValue().equals("a");
        assert ((SqlArg) builder1.getArgs()[1]).getValue().equals("b");
        assert ((SqlArg) builder1.getArgs()[2]).getValue().equals("c");
        assert ((SqlArg) builder1.getArgs()[3]).getValue().equals("d");
        assert ((SqlArg) builder1.getArgs()[4]).getValue().equals("e");
    }

    @Test
    public void setTest_01() throws Throwable {
        String queryConfig = loadString("/net_hasor_db/dal_dynamic/fragment/set_04.xml");
        DynamicSql parseXml = xmlParser.parseDynamicSql(queryConfig);
        //
        String querySql1 = loadString("/net_hasor_db/dal_dynamic/fragment/set_04.xml.sql_1");
        Map<String, Object> data1 = new HashMap<>();
        data1.put("phone", "1234");
        data1.put("email", "zyc@zyc");
        data1.put("expression", "ddd");
        data1.put("id", "~~~");
        data1.put("uid", "1111");
        SqlBuilder builder1 = parseXml.buildQuery(data1, new TextBuilderContext());
        assert builder1.getSqlString().trim().equals(querySql1.trim());
        assert ((SqlArg) builder1.getArgs()[0]).getValue().equals("1234");
        assert ((SqlArg) builder1.getArgs()[1]).getValue().equals("zyc@zyc");
        assert ((SqlArg) builder1.getArgs()[2]).getValue().equals("ddd");
        assert ((SqlArg) builder1.getArgs()[3]).getValue().equals("~~~");
        assert ((SqlArg) builder1.getArgs()[4]).getValue().equals("1111");
        //
        String querySql2 = loadString("/net_hasor_db/dal_dynamic/fragment/set_04.xml.sql_2");
        Map<String, Object> data2 = new HashMap<>();
        data2.put("id", "~~~");
        data2.put("uid", "1111");
        SqlBuilder builder2 = parseXml.buildQuery(data2, new TextBuilderContext());
        assert builder2.getSqlString().trim().equals(querySql2.trim());
        assert ((SqlArg) builder2.getArgs()[0]).getValue().equals("~~~");
        assert ((SqlArg) builder2.getArgs()[1]).getValue().equals("1111");
    }

    @Test
    public void bindTest_01() throws Throwable {
        String queryConfig = loadString("/net_hasor_db/dal_dynamic/fragment/bind_01.xml");
        DynamicSql parseXml = xmlParser.parseDynamicSql(queryConfig);
        //
        String querySql1 = loadString("/net_hasor_db/dal_dynamic/fragment/bind_01.xml.sql_1");
        Map<String, Object> data1 = new HashMap<>();
        data1.put("sellerId", "123");
        SqlBuilder builder1 = parseXml.buildQuery(data1, new TextBuilderContext());
        assert builder1.getSqlString().trim().equals(querySql1.trim());
        assert ((SqlArg) builder1.getArgs()[0]).getValue().equals("123abc");
    }

    @Test
    public void whereTest_01() throws Throwable {
        String queryConfig = loadString("/net_hasor_db/dal_dynamic/fragment/where_01.xml");
        DynamicSql parseXml = xmlParser.parseDynamicSql(queryConfig);
        //
        String querySql1 = loadString("/net_hasor_db/dal_dynamic/fragment/where_01.xml.sql_1");
        Map<String, Object> data1 = new HashMap<>();
        data1.put("sellerId", "123");
        SqlBuilder builder1 = parseXml.buildQuery(data1, new TextBuilderContext());
        assert builder1.getSqlString().trim().equals(querySql1.trim());
        //
        String querySql2 = loadString("/net_hasor_db/dal_dynamic/fragment/where_01.xml.sql_2");
        Map<String, Object> data2 = new HashMap<>();
        data2.put("state", "123");
        data2.put("title", "aaa");
        SqlBuilder builder2 = parseXml.buildQuery(data2, new TextBuilderContext());
        assert builder2.getSqlString().trim().equals(querySql2.trim());
        assert ((SqlArg) builder2.getArgs()[0]).getValue().equals("123");
        assert ((SqlArg) builder2.getArgs()[1]).getValue().equals("aaa");
    }

    @Test
    public void chooseTest_01() throws Throwable {
        String queryConfig = loadString("/net_hasor_db/dal_dynamic/fragment/choose_01.xml");
        DynamicSql parseXml = xmlParser.parseDynamicSql(queryConfig);
        //
        String querySql1 = loadString("/net_hasor_db/dal_dynamic/fragment/choose_01.xml.sql_1");
        Map<String, Object> data1 = new HashMap<>();
        data1.put("title", "123");
        data1.put("content", "aaa");
        SqlBuilder builder1 = parseXml.buildQuery(data1, new TextBuilderContext());
        assert builder1.getSqlString().trim().equals(querySql1.trim());
        assert ((SqlArg) builder1.getArgs()[0]).getValue().equals("123");
    }

    @Test
    public void chooseTest_02() throws Throwable {
        String queryConfig = loadString("/net_hasor_db/dal_dynamic/fragment/choose_01.xml");
        DynamicSql parseXml = xmlParser.parseDynamicSql(queryConfig);
        //
        String querySql1 = loadString("/net_hasor_db/dal_dynamic/fragment/choose_01.xml.sql_2");
        Map<String, Object> data1 = new HashMap<>();
        SqlBuilder builder1 = parseXml.buildQuery(data1, new TextBuilderContext());
        assert builder1.getSqlString().trim().equals(querySql1.trim());
    }

    @Test
    public void tokenTest_01() throws Throwable {
        String queryConfig = loadString("/net_hasor_db/dal_dynamic/fragment/token_01.xml");
        DynamicSql parseXml = xmlParser.parseDynamicSql(queryConfig);
        //
        String querySql1 = loadString("/net_hasor_db/dal_dynamic/fragment/token_01.xml.sql_1");
        Map<String, Object> data1 = new HashMap<>();
        data1.put("abc", "123");
        data1.put("futures", "11");
        data1.put("orderBy", "user_name asc");
        data1.put("info", new HashMap<String, Object>() {{
            put("status", true);
        }});
        SqlBuilder builder1 = parseXml.buildQuery(data1, new TextBuilderContext());
        assert builder1.getSqlString().trim().equals(querySql1.trim());
        assert ((SqlArg) builder1.getArgs()[0]).getValue().equals("123");
        assert ((SqlArg) builder1.getArgs()[1]).getValue() == null;// mode = out not eval value.
        assert ((SqlArg) builder1.getArgs()[0]).getJavaType() == String.class;
        assert ((SqlArg) builder1.getArgs()[1]).getJavaType() == net.hasor.test.db.dto.TB_User.class;
        assert ((SqlArg) builder1.getArgs()[0]).getTypeHandler() instanceof StringTypeHandler;
        assert ((SqlArg) builder1.getArgs()[1]).getTypeHandler() instanceof UnknownTypeHandler;
        assert ((SqlArg) builder1.getArgs()[0]).getSqlMode() == SqlMode.In;
        assert ((SqlArg) builder1.getArgs()[1]).getSqlMode() == SqlMode.Out;
    }

    @Test
    public void selectKeyTest_01() throws Throwable {
        String queryConfig = loadString("/net_hasor_db/dal_dynamic/fragment/selectkey_01.xml");
        DynamicSql parseXml = xmlParser.parseDynamicSql(queryConfig);

        String querySql1 = loadString("/net_hasor_db/dal_dynamic/fragment/selectkey_01.xml.sql_1");
        Map<String, Object> data1 = new HashMap<>();
        data1.put("uid", "zyc_uid");
        data1.put("name", "zyc_name");
        SqlBuilder builder1 = parseXml.buildQuery(data1, new TextBuilderContext());
        assert builder1.getSqlString().trim().equals(querySql1.trim());

        InsertSqlConfig insertSqlConfig = new InsertSqlConfig(parseXml);
        assert insertSqlConfig.getSelectKey() != null;

        String querySql2 = loadString("/net_hasor_db/dal_dynamic/fragment/selectkey_01.xml.sql_2");
        SqlBuilder builder2 = insertSqlConfig.getSelectKey().buildQuery(data1, new TextBuilderContext());
        assert builder2.getSqlString().trim().equals(querySql2.trim());
    }
}
