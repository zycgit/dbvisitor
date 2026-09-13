/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dialect.builder;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Arrays;
import java.util.regex.Pattern;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlCommandBuilder;
import net.hasor.dbvisitor.dialect.SqlCommandBuilder.ConditionLogic;
import net.hasor.dbvisitor.dialect.SqlCommandBuilder.ConditionType;
import net.hasor.dbvisitor.dialect.SqlDialect.SqlLike;
import net.hasor.dbvisitor.dialect.provider.MongoDialect;
import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;
import net.hasor.dbvisitor.lambda.GeneratedKeyStrategy;
import net.hasor.dbvisitor.lambda.core.OrderType;
import net.hasor.dbvisitor.types.SqlArg;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class MongoSqlCommandBuilderTest {
    @Test
    public void testConflictHintsPreserveDocumentParameterOrder() throws SQLException {
        MongoDialect builder = new MongoDialect();
        builder.setTable(null, null, "test");
        builder.addInsert("name", "x'}, {$out:'other'}", null);
        builder.addInsert("tenant", 3, null);
        builder.addInsert("age", 20, null);
        builder.addInsert("id", 7, null);
        BoundSql sql = builder.buildInsert(false, Arrays.asList("tenant", "id"), 1, Collections.emptyList(), DuplicateKeyStrategy.Update, GeneratedKeyStrategy.OneByOne);
        assertEquals("/*+ mongo_duplicate_strategy='update', mongo_primary_keys='dGVuYW50.aWQ' */db.test.insertMany([{name: ?, tenant: ?, age: ?, id: ?}])", sql.getSqlString());
        assertArrayEquals(new Object[] { "x'}, {$out:'other'}", 3, 20, 7 }, sql.getArgs());
        assertArrayEquals(sql.getArgs(), builder.buildInsert(false, Arrays.asList("tenant", "id"), 1, Collections.emptyList(), DuplicateKeyStrategy.Update, GeneratedKeyStrategy.OneByOne).getArgs());
        assertTrue(builder.buildInsert(false, Arrays.asList("tenant", "id"), 1, Collections.emptyList(), DuplicateKeyStrategy.Ignore, GeneratedKeyStrategy.OneByOne).getSqlString().contains("mongo_duplicate_strategy='ignore'"));
    }

    @Test
    public void testConflictStrategyCapabilities() {
        MongoDialect dialect = new MongoDialect();
        for (DuplicateKeyStrategy strategy : DuplicateKeyStrategy.values()) {
            assertEquals(GeneratedKeyStrategy.OneByOne, dialect.generatedKeyStrategy(Collections.singletonList("id"), Collections.singletonList("id"), Collections.emptyList(), strategy));
            assertEquals(GeneratedKeyStrategy.OneByOne, dialect.generatedKeyStrategy(Collections.singletonList("id"), Collections.singletonList("id"), Collections.singletonList("id"), strategy));
            assertTrue(dialect.supportDuplicateStrategy(Collections.singletonList("id"), Collections.singletonList("id"), Collections.emptyList(), strategy));
        }
        assertFalse(dialect.supportDuplicateStrategy(Collections.emptyList(), Collections.singletonList("name"), Collections.emptyList(), DuplicateKeyStrategy.Ignore));
        assertFalse(dialect.supportDuplicateStrategy(Arrays.asList("tenant", "id"), Collections.singletonList("id"), Collections.emptyList(), DuplicateKeyStrategy.Update));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testConflictRequiresCompletePrimaryKey() throws SQLException {
        MongoDialect builder = new MongoDialect();
        builder.setTable(null, null, "test");
        builder.addInsert("id", 1, null);
        builder.buildInsert(false, Arrays.asList("tenant", "id"), 1, Collections.emptyList(), DuplicateKeyStrategy.Update, GeneratedKeyStrategy.OneByOne);
    }

    @Test
    public void testNativeAggregatePipelineKeepsFilterParametersSeparate() throws SQLException {
        MongoDialect builder = new MongoDialect();
        builder.setTable(null, null, "test");
        builder.addSelectCustom("[{$group: {_id: null, value: {$sum: '$age'}}}, {$project: {_id: 0, value: 1}}]", null);
        String value = "x'}, {$out: 'other'}";
        builder.addCondition(ConditionLogic.AND, "name", null, ConditionType.EQ, value, null, null);
        BoundSql sql = builder.buildSelect(false);
        assertEquals("db.test.aggregate([{$match: {name: ?}}, {$group: {_id: null, value: {$sum: '$age'}}}, {$project: {_id: 0, value: 1}}])", sql.getSqlString());
        assertArrayEquals(new Object[] { value }, sql.getArgs());
        assertArrayEquals(sql.getArgs(), builder.buildSelect(false).getArgs());
        assertTrue(builder.pageSql(sql, 2, 3).getSqlString().endsWith("{$skip: 2}, {$limit: 3}])"));
        assertTrue(builder.countSql(sql).getSqlString().contains("{$facet: {rows: [{$count: 'value'}]}}"));
        assertArrayEquals(sql.getArgs(), builder.countSql(sql).getArgs());
    }

    @Test
    public void testStructuredGroupWithNativeAccumulator() throws SQLException {
        MongoDialect builder = new MongoDialect();
        builder.setTable(null, null, "test");
        builder.addSelectCustom("{cnt: {$sum: 1}}", null);
        builder.addGroupBy("age", null);
        builder.addOrderBy("age", null, OrderType.ASC, null);
        assertEquals("db.test.aggregate([{$group: {_id: {age: '$age'}, cnt: {$sum: 1}}}, {$replaceRoot: {newRoot: {$mergeObjects: ['$_id', '$$ROOT']}}}, {$project: {_id: 0}}, {$sort: {age: 1}}])", builder.buildSelect(false).getSqlString());
        builder.clearAll();
        builder.setTable(null, null, "test");
        assertEquals("db.test.find({})", builder.buildSelect(false).getSqlString());
    }

    @Test
    public void testNativeCalculatedProjection() throws SQLException {
        MongoDialect builder = new MongoDialect();
        builder.setTable(null, null, "test");
        builder.addSelectCustom("{_id: 0, doubled_age: {$multiply: ['$age', 2]}}", null);
        assertEquals("db.test.aggregate([{$project: {_id: 0, doubled_age: {$multiply: ['$age', 2]}}}])", builder.buildSelect(false).getSqlString());
        builder.clearSelect();
        assertEquals("db.test.find({})", builder.buildSelect(false).getSqlString());
    }

    @Test
    public void testLikeWildcardsAndEscapedLiterals() {
        MongoDialect dialect = new MongoDialect();
        String regex = dialect.like(SqlLike.DEFAULT, "prefix%_end", null);
        assertTrue(Pattern.compile(regex).matcher("prefix\nXend").find());
        assertFalse(Pattern.compile(regex).matcher("prefixend").find());
        assertTrue(Pattern.compile(dialect.like(SqlLike.RIGHT, "a\\%\\_.*", null)).matcher("a%_.*suffix").find());
        assertFalse(Pattern.compile(dialect.like(SqlLike.RIGHT, "a\\%\\_.*", null)).matcher("aVALUE").find());
        assertTrue(Pattern.compile(dialect.like(SqlLike.LEFT, "a%", null)).matcher("prefixabc").find());
    }

    @Test
    public void testRepeatedSortKeepsFirstDirection() throws SQLException {
        SqlCommandBuilder builder = new MongoDialect().newBuilder();
        builder.setTable(null, null, "test");
        builder.addOrderBy("age", null, OrderType.ASC, null);
        builder.addOrderBy("age", null, OrderType.DESC, null);
        builder.addOrderBy("name", null, OrderType.DESC, null);
        assertEquals("db.test.find({}).sort({age: 1, name: -1})", builder.buildSelect(false).getSqlString());
        builder.clearAll();
        builder.setTable(null, null, "test");
        builder.addOrderBy("age", null, OrderType.DESC, null);
        assertEquals("db.test.find({}).sort({age: -1})", builder.buildSelect(false).getSqlString());
    }

    @Test
    public void testSelect() throws SQLException {
        SqlCommandBuilder builder = new MongoDialect().newBuilder();
        builder.setTable("my_db", null, "user_collection");
        builder.addCondition(ConditionLogic.AND, "age", null, ConditionType.GT, 18, null, null);

        BoundSql boundSql = builder.buildSelect(false);
        assertEquals("my_db.user_collection.find({age: { $gt: ? }})", boundSql.getSqlString());
        assertEquals(1, boundSql.getArgs().length);
        assertEquals(18, boundSql.getArgs()[0]);
    }

    @Test
    public void testSelectProjection() throws SQLException {
        SqlCommandBuilder builder = new MongoDialect().newBuilder();
        builder.setTable(null, null, "user_collection");
        builder.addSelect("name", null);
        builder.addSelect("age", null);
        builder.addCondition(ConditionLogic.AND, "active", null, ConditionType.EQ, true, null, null);

        BoundSql boundSql = builder.buildSelect(false);
        assertEquals("db.user_collection.find({active: ?}, {name: 1, age: 1, _id: 0})", boundSql.getSqlString());
        assertEquals(1, boundSql.getArgs().length);
        assertEquals(true, boundSql.getArgs()[0]);
    }

    @Test
    public void testSelectSort() throws SQLException {
        SqlCommandBuilder builder = new MongoDialect().newBuilder();
        builder.setTable(null, null, "user_collection");
        builder.addOrderBy("create_time", null, OrderType.DESC, null);
        builder.addOrderBy("name", null, OrderType.ASC, null);

        BoundSql boundSql = builder.buildSelect(false);
        assertEquals("db.user_collection.find({}).sort({create_time: -1, name: 1})", boundSql.getSqlString());
    }

    @Test
    public void testInsert() throws SQLException {
        SqlCommandBuilder builder = new MongoDialect().newBuilder();
        builder.setTable(null, null, "user_collection");
        builder.addInsert("name", "John", null);
        builder.addInsert("age", 25, null);

        BoundSql boundSql = builder.buildInsert(false, Collections.emptyList(), 1, Collections.emptyList(), DuplicateKeyStrategy.Into, GeneratedKeyStrategy.OneByOne);
        assertEquals("db.user_collection.insertMany([{name: ?, age: ?}])", boundSql.getSqlString());
        assertEquals(2, boundSql.getArgs().length);
        assertEquals("John", boundSql.getArgs()[0]);
        assertEquals(25, boundSql.getArgs()[1]);
    }

    @Test
    public void testUpdate() throws SQLException {
        SqlCommandBuilder builder = new MongoDialect().newBuilder();
        builder.setTable(null, null, "user_collection");
        builder.addUpdateSet("name", "Doe", null);
        builder.addCondition(ConditionLogic.AND, "id", null, ConditionType.EQ, 1, null, null);

        BoundSql boundSql = builder.buildUpdate(false, false);
        assertEquals("db.user_collection.updateMany({id: ?}, { $set: {name: ?} })", boundSql.getSqlString());
        assertEquals(2, boundSql.getArgs().length);
        assertEquals(1, boundSql.getArgs()[0]);
        assertEquals("Doe", boundSql.getArgs()[1]);
    }

    @Test
    public void testDelete() throws SQLException {
        SqlCommandBuilder builder = new MongoDialect().newBuilder();
        builder.setTable(null, null, "user_collection");
        builder.addCondition(ConditionLogic.AND, "status", null, ConditionType.EQ, "inactive", null, null);

        BoundSql boundSql = builder.buildDelete(false, false);
        assertEquals("db.user_collection.deleteMany({status: ?})", boundSql.getSqlString());
        assertEquals(1, boundSql.getArgs().length);
        assertEquals("inactive", boundSql.getArgs()[0]);
    }

    @Test
    public void testConditions() throws SQLException {
        SqlCommandBuilder builder = new MongoDialect().newBuilder();
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

        BoundSql boundSql = builder.buildSelect(false);
        String expected = "db.test.find({$and: [{c1: ?}, {c2: { $ne: ? }}, {c3: { $gt: ? }}, {c4: { $gte: ? }}, {c5: { $lt: ? }}, {c6: { $lte: ? }}, {c7: null}, {c8: { $ne: null }}, {c9: { $regex: ? }}]})";

        assertEquals(expected, boundSql.getSqlString());
        assertEquals(7, boundSql.getArgs().length);
        assertEquals("\\Qabc\\E", boundSql.getArgs()[6]);
    }

    @Test
    public void testBetween() throws SQLException {
        SqlCommandBuilder builder = new MongoDialect().newBuilder();
        builder.setTable(null, null, "test");
        builder.addConditionForBetween(ConditionLogic.AND, "age", null, ConditionType.BETWEEN, 10, null, 20, null);

        BoundSql boundSql = builder.buildSelect(false);
        assertEquals("db.test.find({age: { $gte: ?, $lte: ? }})", boundSql.getSqlString());
        assertEquals(2, boundSql.getArgs().length);
        assertEquals(10, boundSql.getArgs()[0]);
        assertEquals(20, boundSql.getArgs()[1]);
    }

    @Test
    public void testIn() throws SQLException {
        SqlCommandBuilder builder = new MongoDialect().newBuilder();
        builder.setTable(null, null, "test");
        builder.addConditionForIn(ConditionLogic.AND, "status", null, ConditionType.IN, new Object[] { "A", "B" }, null);

        BoundSql boundSql = builder.buildSelect(false);
        assertEquals("db.test.find({status: { $in: [?, ?] }})", boundSql.getSqlString());
        assertEquals(2, boundSql.getArgs().length);
        assertEquals("A", boundSql.getArgs()[0]);
        assertEquals("B", boundSql.getArgs()[1]);
    }

    @Test
    public void testRepeatedFieldAndOrPrecedence() throws SQLException {
        SqlCommandBuilder builder = new MongoDialect().newBuilder();
        builder.setTable(null, null, "test");
        builder.addCondition(ConditionLogic.AND, "age", null, ConditionType.GT, 10, null, null);
        builder.addCondition(ConditionLogic.AND, "age", null, ConditionType.LT, 20, null, null);
        builder.addCondition(ConditionLogic.OR, "name", null, ConditionType.EQ, "other", null, null);
        BoundSql sql = builder.buildSelect(false);
        assertEquals("db.test.find({$or: [{$and: [{age: { $gt: ? }}, {age: { $lt: ? }}]}, {name: ?}]})", sql.getSqlString());
        assertArrayEquals(new Object[] { 10, 20, "other" }, sql.getArgs());
        assertArrayEquals(sql.getArgs(), builder.buildSelect(false).getArgs());
    }

    @Test
    public void testNestedRawAndNegation() throws SQLException {
        SqlCommandBuilder builder = new MongoDialect().newBuilder();
        builder.setTable(null, null, "test");
        builder.addConditionGroup(ConditionLogic.AND_NOT, group -> {
            group.addRawCondition(ConditionLogic.AND, new BoundSql.BoundSqlObj("{name: ?}", new Object[] { "x\"}, $where: 'bad'" }));
            group.addCondition(ConditionLogic.OR, "age", null, ConditionType.EQ, 10, null, null);
        });
        builder.addUpdateSet("age", 30, null);
        BoundSql sql = builder.buildUpdate(false, false);
        assertEquals("db.test.updateMany({$nor: [{$or: [{name: ?}, {age: ?}]}]}, { $set: {age: ?} })", sql.getSqlString());
        assertArrayEquals(new Object[] { "x\"}, $where: 'bad'", 10, 30 }, sql.getArgs());
        assertArrayEquals(sql.getArgs(), builder.buildUpdate(false, false).getArgs());
        assertArrayEquals(new Object[] { "x\"}, $where: 'bad'", 10 }, builder.buildDelete(false, false).getArgs());
    }

    @Test
    public void testLikeLiteralRemainsBound() throws SQLException {
        SqlCommandBuilder builder = new MongoDialect().newBuilder();
        builder.setTable(null, null, "test");
        builder.addCondition(ConditionLogic.AND, "name", null, ConditionType.LIKE, "a.*\"", null, SqlLike.LEFT);
        BoundSql sql = builder.buildSelect(false);
        assertEquals("db.test.find({name: { $regex: ? }})", sql.getSqlString());
        assertArrayEquals(new Object[] { "\\Qa.*\"\\E$" }, sql.getArgs());
    }

    @Test
    public void testLikeWrappedParameter() throws SQLException {
        SqlCommandBuilder builder = new MongoDialect().newBuilder();
        builder.setTable(null, null, "test");
        SqlArg value = SqlArg.valueOf("alice");
        builder.addCondition(ConditionLogic.AND, "name", null, ConditionType.LIKE, value, null, SqlLike.RIGHT);
        BoundSql sql = builder.buildSelect(false);
        assertEquals("^\\Qalice\\E", ((SqlArg) sql.getArgs()[0]).getValue());
        assertEquals("alice", value.getValue());
        assertEquals("^\\Qalice\\E", ((SqlArg) builder.buildSelect(false).getArgs()[0]).getValue());
    }

    @Test
    public void testProjectionSelectionAndReset() throws SQLException {
        SqlCommandBuilder builder = new MongoDialect().newBuilder();
        builder.setTable(null, null, "test");
        builder.addSelect("_id", null);
        assertTrue(builder.hasSelect("_id"));
        assertEquals("db.test.find({}, {_id: 1})", builder.buildSelect(false).getSqlString());
        builder.clearSelect();
        assertFalse(builder.hasSelect("_id"));
        builder.addSelect("name", null);
        builder.addSelectAll();
        assertEquals("db.test.find({})", builder.buildSelect(false).getSqlString());
        builder.clearAll();
        builder.setTable(null, null, "next");
        assertEquals("db.next.find({})", builder.buildSelect(false).getSqlString());
        assertEquals(0, builder.buildSelect(false).getArgs().length);
    }

    @Test(expected = IllegalStateException.class)
    public void testEmptyDeleteRequiresOptIn() throws SQLException {
        SqlCommandBuilder builder = new MongoDialect().newBuilder();
        builder.setTable(null, null, "test");
        builder.addConditionGroup(ConditionLogic.AND, group -> group.addRawCondition(ConditionLogic.AND, new BoundSql.BoundSqlObj("{}")));
        builder.buildDelete(false, false);
    }

    @Test(expected = IllegalStateException.class)
    public void testEmptyUpdateRequiresOptIn() throws SQLException {
        SqlCommandBuilder builder = new MongoDialect().newBuilder();
        builder.setTable(null, null, "test");
        builder.addUpdateSet("age", 20, null);
        builder.buildUpdate(false, false);
    }

    @Test
    public void testEmptyMutationOptIn() throws SQLException {
        SqlCommandBuilder builder = new MongoDialect().newBuilder();
        builder.setTable(null, null, "test");
        builder.addUpdateSet("age", 20, null);
        assertEquals("db.test.updateMany({}, { $set: {age: ?} })", builder.buildUpdate(false, true).getSqlString());
        assertEquals("db.test.deleteMany({})", builder.buildDelete(false, true).getSqlString());
    }
}
