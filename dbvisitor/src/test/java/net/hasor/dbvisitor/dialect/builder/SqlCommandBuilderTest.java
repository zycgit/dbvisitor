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
import net.hasor.dbvisitor.dialect.provider.MySqlDialect;
import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;
import net.hasor.dbvisitor.lambda.core.OrderNullsStrategy;
import net.hasor.dbvisitor.lambda.core.OrderType;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class SqlCommandBuilderTest {
    @Test
    public void testSelect() throws SQLException {
        SqlCommandBuilder builder = new MySqlDialect().newBuilder();
        builder.setTable(null, null, "user_table");
        builder.addSelect("id", null);
        builder.addSelect("name", null);
        builder.addCondition(ConditionLogic.AND, "age", null, ConditionType.GT, 18, null, null);
        builder.addOrderBy("create_time", null, OrderType.DESC, null);

        BoundSql boundSql = builder.buildSelect(false);
        assertEquals("SELECT id, name FROM user_table WHERE age > ? ORDER BY create_time DESC", boundSql.getSqlString());
        assertEquals(1, boundSql.getArgs().length);
        assertEquals(18, boundSql.getArgs()[0]);
    }

    @Test
    public void testSelectAll() throws SQLException {
        SqlCommandBuilder builder = new MySqlDialect().newBuilder();
        builder.setTable(null, null, "user_table");

        BoundSql boundSql = builder.buildSelect(false);
        assertEquals("SELECT * FROM user_table", boundSql.getSqlString());
    }

    @Test
    public void testGroupBy() throws SQLException {
        SqlCommandBuilder builder = new MySqlDialect().newBuilder();
        builder.setTable(null, null, "user_table");
        builder.addSelect("role", null);
        builder.addSelect("cnt", "count(*)");
        builder.addGroupBy("role", null);

        BoundSql boundSql = builder.buildSelect(false);
        assertEquals("SELECT role, count(*) cnt FROM user_table GROUP BY role", boundSql.getSqlString());
    }

    @Test
    public void testInsert() throws SQLException {
        SqlCommandBuilder builder = new MySqlDialect().newBuilder();
        builder.setTable(null, null, "user_table");
        builder.addInsert("name", "John", null);
        builder.addInsert("age", 25, null);

        BoundSql boundSql = builder.buildInsert(false, Collections.emptyList(), DuplicateKeyStrategy.Into);
        assertEquals("INSERT INTO user_table (name, age) VALUES (?, ?)", boundSql.getSqlString());
        assertEquals(2, boundSql.getArgs().length);
        assertEquals("John", boundSql.getArgs()[0]);
        assertEquals(25, boundSql.getArgs()[1]);
    }

    @Test
    public void testUpdate() throws SQLException {
        SqlCommandBuilder builder = new MySqlDialect().newBuilder();
        builder.setTable(null, null, "user_table");
        builder.addUpdateSet("name", "Doe", null);
        builder.addCondition(ConditionLogic.AND, "id", null, ConditionType.EQ, 1, null, null);

        BoundSql boundSql = builder.buildUpdate(false, false);
        assertEquals("UPDATE user_table SET name = ? WHERE id = ?", boundSql.getSqlString());
        assertEquals(2, boundSql.getArgs().length);
        assertEquals("Doe", boundSql.getArgs()[0]);
        assertEquals(1, boundSql.getArgs()[1]);
    }

    @Test
    public void testDelete() throws SQLException {
        SqlCommandBuilder builder = new MySqlDialect().newBuilder();
        builder.setTable(null, null, "user_table");
        builder.addCondition(ConditionLogic.AND, "id", null, ConditionType.EQ, 1, null, null);

        BoundSql boundSql = builder.buildDelete(false, false);
        assertEquals("DELETE FROM user_table WHERE id = ?", boundSql.getSqlString());
        assertEquals(1, boundSql.getArgs().length);
        assertEquals(1, boundSql.getArgs()[0]);
    }

    @Test
    public void testConditionLogic() throws SQLException {
        SqlCommandBuilder builder = new MySqlDialect().newBuilder();
        builder.setTable(null, null, "user_table");
        builder.addCondition(ConditionLogic.AND, "a", null, ConditionType.EQ, 1, null, null);
        builder.addCondition(ConditionLogic.OR, "b", null, ConditionType.EQ, 2, null, null);
        builder.addCondition(ConditionLogic.AND_NOT, "c", null, ConditionType.EQ, 3, null, null);

        BoundSql boundSql = builder.buildSelect(false);
        assertEquals("SELECT * FROM user_table WHERE a = ? OR b = ? AND NOT c = ?", boundSql.getSqlString());
    }

    @Test
    public void testSpecialTerm() throws SQLException {
        SqlCommandBuilder builder = new MySqlDialect().newBuilder();
        builder.setTable(null, null, "gis_table");
        builder.addSelect("location", "ST_AsText(location)");
        builder.addCondition(ConditionLogic.AND, "location", null, ConditionType.EQ, "POINT(1 1)", "ST_GeomFromText(?)", null);

        BoundSql boundSql = builder.buildSelect(false);
        assertEquals("SELECT ST_AsText(location) location FROM gis_table WHERE location = ST_GeomFromText(?)", boundSql.getSqlString());
        assertEquals(1, boundSql.getArgs().length);
        assertEquals("POINT(1 1)", boundSql.getArgs()[0]);
    }

    @Test
    public void testSelectWithTerm() throws SQLException {
        SqlCommandBuilder builder = new MySqlDialect().newBuilder();
        builder.setTable(null, null, "user_table");
        builder.addSelect("name", "upper(name)");

        BoundSql boundSql = builder.buildSelect(false);
        assertEquals("SELECT upper(name) name FROM user_table", boundSql.getSqlString());
    }

    @Test
    public void testConditionWithTerm() throws SQLException {
        SqlCommandBuilder builder = new MySqlDialect().newBuilder();
        builder.setTable(null, null, "user_table");
        builder.addCondition(ConditionLogic.AND, "age", "abs(age)", ConditionType.GT, 18, "abs(?)", null);

        BoundSql boundSql = builder.buildSelect(false);
        assertEquals("SELECT * FROM user_table WHERE abs(age) > abs(?)", boundSql.getSqlString());
        assertEquals(1, boundSql.getArgs().length);
        assertEquals(18, boundSql.getArgs()[0]);
    }

    @Test
    public void testUpdateWithTerm() throws SQLException {
        SqlCommandBuilder builder = new MySqlDialect().newBuilder();
        builder.setTable(null, null, "user_table");
        builder.addUpdateSet("name", "Doe", "upper(?)");
        builder.addCondition(ConditionLogic.AND, "id", null, ConditionType.EQ, 1, null, null);

        BoundSql boundSql = builder.buildUpdate(false, false);
        assertEquals("UPDATE user_table SET name = upper(?) WHERE id = ?", boundSql.getSqlString());
        assertEquals(2, boundSql.getArgs().length);
        assertEquals("Doe", boundSql.getArgs()[0]);
        assertEquals(1, boundSql.getArgs()[1]);
    }

    @Test
    public void testInsertWithTerm() throws SQLException {
        SqlCommandBuilder builder = new MySqlDialect().newBuilder();
        builder.setTable(null, null, "user_table");
        builder.addInsert("name", "John", "upper(?)");
        builder.addInsert("age", 25, null);

        BoundSql boundSql = builder.buildInsert(false, Collections.emptyList(), DuplicateKeyStrategy.Into);
        assertEquals("INSERT INTO user_table (name, age) VALUES (upper(?), ?)", boundSql.getSqlString());
        assertEquals(2, boundSql.getArgs().length);
        assertEquals("John", boundSql.getArgs()[0]);
        assertEquals(25, boundSql.getArgs()[1]);
    }

    @Test
    public void testOrderByWithTerm() throws SQLException {
        SqlCommandBuilder builder = new MySqlDialect().newBuilder();
        builder.setTable(null, null, "user_table");
        builder.addOrderBy("create_time", "year(create_time)", OrderType.DESC, null);

        BoundSql boundSql = builder.buildSelect(false);
        assertEquals("SELECT * FROM user_table ORDER BY year(create_time) DESC", boundSql.getSqlString());
    }

    @Test
    public void testUpdateWithNull() throws SQLException {
        SqlCommandBuilder builder = new MySqlDialect().newBuilder();
        builder.setTable(null, null, "user_table");
        builder.addUpdateSet("name", null, null);
        builder.addCondition(ConditionLogic.AND, "id", null, ConditionType.EQ, 1, null, null);

        BoundSql boundSql = builder.buildUpdate(false, false);
        assertEquals("UPDATE user_table SET name = NULL WHERE id = ?", boundSql.getSqlString());
        assertEquals(1, boundSql.getArgs().length);
        assertEquals(1, boundSql.getArgs()[0]);
    }

    @Test
    public void testInsertWithNull() throws SQLException {
        SqlCommandBuilder builder = new MySqlDialect().newBuilder();
        builder.setTable(null, null, "user_table");
        builder.addInsert("name", null, null);
        builder.addInsert("age", 25, null);

        BoundSql boundSql = builder.buildInsert(false, Collections.emptyList(), DuplicateKeyStrategy.Into);
        assertEquals("INSERT INTO user_table (name, age) VALUES (?, ?)", boundSql.getSqlString());
        assertEquals(2, boundSql.getArgs().length);
        assertEquals(null, boundSql.getArgs()[0]);
        assertEquals(25, boundSql.getArgs()[1]);
    }

    @Test
    public void testConditionWithNull() throws SQLException {
        SqlCommandBuilder builder = new MySqlDialect().newBuilder();
        builder.setTable(null, null, "user_table");
        builder.addCondition(ConditionLogic.AND, "name", null, ConditionType.IS_NULL, null, null, null);
        builder.addCondition(ConditionLogic.AND, "age", null, ConditionType.IS_NOT_NULL, null, null, null);

        BoundSql boundSql = builder.buildSelect(false);
        assertEquals("SELECT * FROM user_table WHERE name IS NULL AND age IS NOT NULL", boundSql.getSqlString());
        assertEquals(0, boundSql.getArgs().length);
    }

    @Test
    public void testOrderByNulls() throws SQLException {
        SqlCommandBuilder builder = new MySqlDialect().newBuilder();
        builder.setTable(null, null, "user_table");
        builder.addOrderBy("name", null, OrderType.ASC, OrderNullsStrategy.FIRST);

        BoundSql boundSql = builder.buildSelect(false);
        assertEquals("SELECT * FROM user_table ORDER BY name IS NULL DESC, name ASC", boundSql.getSqlString());
    }
}
