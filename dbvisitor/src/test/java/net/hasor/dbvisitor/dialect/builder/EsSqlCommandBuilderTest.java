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
package net.hasor.dbvisitor.dialect.builder;
import java.sql.SQLException;
import java.util.Collections;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlCommandBuilder;
import net.hasor.dbvisitor.dialect.SqlCommandBuilder.ConditionLogic;
import net.hasor.dbvisitor.dialect.SqlCommandBuilder.ConditionType;
import net.hasor.dbvisitor.dialect.provider.Elastic6Dialect;
import net.hasor.dbvisitor.dialect.provider.Elastic7Dialect;
import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;
import net.hasor.dbvisitor.lambda.core.OrderType;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EsSqlCommandBuilderTest {
    @Test
    public void testSelectEs6() throws SQLException {
        SqlCommandBuilder builder = new Elastic6Dialect().newBuilder();
        builder.setTable("my_index", "my_type", null);
        builder.addCondition(ConditionLogic.AND, "age", null, ConditionType.GT, 18, null, null);

        BoundSql boundSql = builder.buildSelect(false);
        String sql = boundSql.getSqlString();

        assertTrue(sql.startsWith("POST /my_index/my_type/_search"));
        assertTrue(sql.contains("\"query\": { \"bool\": { \"must\": [{ \"range\": { \"age\": { \"gt\": ? } } }] } }"));
        assertEquals(1, boundSql.getArgs().length);
        assertEquals(18, boundSql.getArgs()[0]);
    }

    @Test
    public void testSelectEs7() throws SQLException {
        SqlCommandBuilder builder = new Elastic7Dialect().newBuilder();
        builder.setTable("my_index", "my_type", null); // type should be ignored or handled differently
        builder.addCondition(ConditionLogic.AND, "age", null, ConditionType.GT, 18, null, null);

        BoundSql boundSql = builder.buildSelect(false);
        String sql = boundSql.getSqlString();

        assertTrue(sql.startsWith("POST /my_index/_search"));
        assertTrue(sql.contains("\"query\": { \"bool\": { \"must\": [{ \"range\": { \"age\": { \"gt\": ? } } }] } }"));
        assertEquals(1, boundSql.getArgs().length);
        assertEquals(18, boundSql.getArgs()[0]);
    }

    @Test
    public void testSelectProjection() throws SQLException {
        SqlCommandBuilder builder = new Elastic6Dialect().newBuilder();
        builder.setTable("my_index", "my_type", null);
        builder.addSelect("name", null);
        builder.addSelect("age", null);
        builder.addCondition(ConditionLogic.AND, "active", null, ConditionType.EQ, true, null, null);

        BoundSql boundSql = builder.buildSelect(false);
        String sql = boundSql.getSqlString();

        assertTrue(sql.contains("\"_source\": [\"name\", \"age\"]"));
        assertTrue(sql.contains("\"match\": { \"active\": ? }"));
    }

    @Test
    public void testSelectSort() throws SQLException {
        SqlCommandBuilder builder = new Elastic6Dialect().newBuilder();
        builder.setTable("my_index", "my_type", null);
        builder.addOrderBy("create_time", null, OrderType.DESC, null);
        builder.addOrderBy("name", null, OrderType.ASC, null);

        BoundSql boundSql = builder.buildSelect(false);
        String sql = boundSql.getSqlString();

        assertTrue(sql.contains("\"sort\": [{ \"create_time\": { \"order\": \"desc\" } }, { \"name\": { \"order\": \"asc\" } }]"));
    }

    @Test
    public void testInsertEs6() throws SQLException {
        SqlCommandBuilder builder = new Elastic6Dialect().newBuilder();
        builder.setTable("my_index", "my_type", null);
        builder.addInsert("name", "John", null);
        builder.addInsert("age", 25, null);

        BoundSql boundSql = builder.buildInsert(false, Collections.emptyList(), DuplicateKeyStrategy.Into);
        String sql = boundSql.getSqlString();

        assertTrue(sql.startsWith("POST /my_index/my_type"));
        assertTrue(sql.contains("\"name\": ?"));
        assertTrue(sql.contains("\"age\": ?"));
        assertEquals(2, boundSql.getArgs().length);
    }

    @Test
    public void testInsertEs7() throws SQLException {
        SqlCommandBuilder builder = new Elastic7Dialect().newBuilder();
        builder.setTable("my_index", "my_type", null);
        builder.addInsert("name", "John", null);
        builder.addInsert("age", 25, null);

        BoundSql boundSql = builder.buildInsert(false, Collections.emptyList(), DuplicateKeyStrategy.Into);
        String sql = boundSql.getSqlString();

        assertTrue(sql.startsWith("POST /my_index/_doc"));
        assertTrue(sql.contains("\"name\": ?"));
        assertTrue(sql.contains("\"age\": ?"));
    }

    @Test
    public void testUpdateEs6() throws SQLException {
        SqlCommandBuilder builder = new Elastic6Dialect().newBuilder();
        builder.setTable("my_index", "my_type", null);
        builder.addUpdateSet("name", "Doe", null);
        builder.addCondition(ConditionLogic.AND, "id", null, ConditionType.EQ, 1, null, null);

        BoundSql boundSql = builder.buildUpdate(false, false);
        String sql = boundSql.getSqlString();

        assertTrue(sql.startsWith("POST /my_index/my_type/_update_by_query"));
        assertTrue(sql.contains("\"script\": { \"source\": \"ctx._source.putAll(params.data)\", \"lang\": \"painless\", \"params\": { \"data\": {"));
        assertTrue(sql.contains("\"name\": ?"));
        assertTrue(sql.contains("\"match\": { \"id\": ? }"));
    }

    @Test
    public void testUpdateEs7() throws SQLException {
        SqlCommandBuilder builder = new Elastic7Dialect().newBuilder();
        builder.setTable("my_index", "my_type", null);
        builder.addUpdateSet("name", "Doe", null);
        builder.addCondition(ConditionLogic.AND, "id", null, ConditionType.EQ, 1, null, null);

        BoundSql boundSql = builder.buildUpdate(false, false);
        String sql = boundSql.getSqlString();

        assertTrue(sql.startsWith("POST /my_index/_update_by_query"));
        assertTrue(sql.contains("\"script\": { \"source\": \"ctx._source.putAll(params.data)\", \"lang\": \"painless\", \"params\": { \"data\": {"));
        assertTrue(sql.contains("\"name\": ?"));
    }

    @Test
    public void testDeleteEs6() throws SQLException {
        SqlCommandBuilder builder = new Elastic6Dialect().newBuilder();
        builder.setTable("my_index", "my_type", null);
        builder.addCondition(ConditionLogic.AND, "status", null, ConditionType.EQ, "inactive", null, null);

        BoundSql boundSql = builder.buildDelete(false, false);
        String sql = boundSql.getSqlString();

        assertTrue(sql.startsWith("POST /my_index/my_type/_delete_by_query"));
        assertTrue(sql.contains("\"match\": { \"status\": ? }"));
    }

    @Test
    public void testDeleteEs7() throws SQLException {
        SqlCommandBuilder builder = new Elastic7Dialect().newBuilder();
        builder.setTable("my_index", "my_type", null);
        builder.addCondition(ConditionLogic.AND, "status", null, ConditionType.EQ, "inactive", null, null);

        BoundSql boundSql = builder.buildDelete(false, false);
        String sql = boundSql.getSqlString();

        assertTrue(sql.startsWith("POST /my_index/_delete_by_query"));
        assertTrue(sql.contains("\"match\": { \"status\": ? }"));
    }
}
