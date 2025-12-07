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
import net.hasor.dbvisitor.dialect.ConditionSqlDialect.SqlLike;
import net.hasor.dbvisitor.dialect.provider.MongoDialect;
import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;
import net.hasor.dbvisitor.lambda.core.OrderType;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class MongoCommandBuilderTest {
    @Test
    public void testSelect() throws SQLException {
        MongoCommandBuilder builder = new MongoCommandBuilder();
        builder.setTable("my_db", null, "user_collection");
        builder.addCondition(ConditionLogic.AND, "age", null, ConditionType.GT, 18, null, null);

        BoundSql boundSql = builder.buildSelect(new MongoDialect(), false);
        assertEquals("my_db.user_collection.find({age: { $gt: ? }})", boundSql.getSqlString());
        assertEquals(1, boundSql.getArgs().length);
        assertEquals(18, boundSql.getArgs()[0]);
    }

    @Test
    public void testSelectProjection() throws SQLException {
        MongoCommandBuilder builder = new MongoCommandBuilder();
        builder.setTable(null, null, "user_collection");
        builder.addSelect("name", null);
        builder.addSelect("age", null);
        builder.addCondition(ConditionLogic.AND, "active", null, ConditionType.EQ, true, null, null);

        BoundSql boundSql = builder.buildSelect(new MongoDialect(), false);
        assertEquals("db.user_collection.find({active: ?}, {name: 1, age: 1})", boundSql.getSqlString());
        assertEquals(1, boundSql.getArgs().length);
        assertEquals(true, boundSql.getArgs()[0]);
    }

    @Test
    public void testSelectSort() throws SQLException {
        MongoCommandBuilder builder = new MongoCommandBuilder();
        builder.setTable(null, null, "user_collection");
        builder.addOrderBy("create_time", null, OrderType.DESC, null);
        builder.addOrderBy("name", null, OrderType.ASC, null);

        BoundSql boundSql = builder.buildSelect(new MongoDialect(), false);
        assertEquals("db.user_collection.find({}).sort({create_time: -1, name: 1})", boundSql.getSqlString());
    }

    @Test
    public void testInsert() throws SQLException {
        MongoCommandBuilder builder = new MongoCommandBuilder();
        builder.setTable(null, null, "user_collection");
        builder.addInsert("name", "John", null);
        builder.addInsert("age", 25, null);

        BoundSql boundSql = builder.buildInsert(new MongoDialect(), false, Collections.emptyList(), DuplicateKeyStrategy.Into);
        assertEquals("db.user_collection.insertMany([{name: ?, age: ?}])", boundSql.getSqlString());
        assertEquals(2, boundSql.getArgs().length);
        assertEquals("John", boundSql.getArgs()[0]);
        assertEquals(25, boundSql.getArgs()[1]);
    }

    @Test
    public void testUpdate() throws SQLException {
        MongoCommandBuilder builder = new MongoCommandBuilder();
        builder.setTable(null, null, "user_collection");
        builder.addUpdateSet("name", null, "Doe", null);
        builder.addCondition(ConditionLogic.AND, "id", null, ConditionType.EQ, 1, null, null);

        BoundSql boundSql = builder.buildUpdate(new MongoDialect(), false, false);
        assertEquals("db.user_collection.updateMany({id: ?}, { $set: {name: ?} })", boundSql.getSqlString());
        assertEquals(2, boundSql.getArgs().length);
        assertEquals(1, boundSql.getArgs()[0]);
        assertEquals("Doe", boundSql.getArgs()[1]);
    }

    @Test
    public void testDelete() throws SQLException {
        MongoCommandBuilder builder = new MongoCommandBuilder();
        builder.setTable(null, null, "user_collection");
        builder.addCondition(ConditionLogic.AND, "status", null, ConditionType.EQ, "inactive", null, null);

        BoundSql boundSql = builder.buildDelete(new MongoDialect(), false, false);
        assertEquals("db.user_collection.deleteMany({status: ?})", boundSql.getSqlString());
        assertEquals(1, boundSql.getArgs().length);
        assertEquals("inactive", boundSql.getArgs()[0]);
    }

    @Test
    public void testConditions() throws SQLException {
        MongoCommandBuilder builder = new MongoCommandBuilder();
        builder.setTable(null, null, "test");

        builder.addCondition(ConditionLogic.AND, "c1", null, ConditionType.EQ, 1, null, null);
        builder.addCondition(ConditionLogic.AND, "c2", null, ConditionType.NE, 2, null, null);
        builder.addCondition(ConditionLogic.AND, "c3", null, ConditionType.GT, 3, null, null);
        builder.addCondition(ConditionLogic.AND, "c4", null, ConditionType.GE, 4, null, null);
        builder.addCondition(ConditionLogic.AND, "c5", null, ConditionType.LT, 5, null, null);
        builder.addCondition(ConditionLogic.AND, "c6", null, ConditionType.LE, 6, null, null);
        builder.addCondition(ConditionLogic.AND, "c7", null, ConditionType.IS_NULL, null, null, null);
        builder.addCondition(ConditionLogic.AND, "c8", null, ConditionType.IS_NOT_NULL, null, null, null);
        builder.addCondition(ConditionLogic.AND, "c9", null, ConditionType.LIKE, "abc", null, SqlLike.DEFAULT);

        BoundSql boundSql = builder.buildSelect(new MongoDialect(), false);
        String expected = "db.test.find({" + "c1: ?, " + "c2: { $ne: ? }, " + "c3: { $gt: ? }, " + "c4: { $gte: ? }, " + "c5: { $lt: ? }, " + "c6: { $lte: ? }, " + "c7: null, " + "c8: { $ne: null }, " + "c9: { $regex: abc }" + "})";

        assertEquals(expected, boundSql.getSqlString());
        assertEquals(6, boundSql.getArgs().length);
    }

    @Test
    public void testBetween() throws SQLException {
        MongoCommandBuilder builder = new MongoCommandBuilder();
        builder.setTable(null, null, "test");
        builder.addConditionForBetween(ConditionLogic.AND, "age", null, ConditionType.BETWEEN, 10, null, 20, null);

        BoundSql boundSql = builder.buildSelect(new MongoDialect(), false);
        assertEquals("db.test.find({age: { $gte: ?, $lte: ? }})", boundSql.getSqlString());
        assertEquals(2, boundSql.getArgs().length);
        assertEquals(10, boundSql.getArgs()[0]);
        assertEquals(20, boundSql.getArgs()[1]);
    }

    @Test
    public void testIn() throws SQLException {
        MongoCommandBuilder builder = new MongoCommandBuilder();
        builder.setTable(null, null, "test");
        builder.addConditionForIn(ConditionLogic.AND, "status", null, ConditionType.IN, new Object[] { "A", "B" }, null);

        BoundSql boundSql = builder.buildSelect(new MongoDialect(), false);
        assertEquals("db.test.find({status: { $in: [?, ?] }})", boundSql.getSqlString());
        assertEquals(2, boundSql.getArgs().length);
        assertEquals("A", boundSql.getArgs()[0]);
        assertEquals("B", boundSql.getArgs()[1]);
    }
}
