/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import java.util.List;
import java.util.Map;

import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.api.adapter.AdapterContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;

public class MongoJdbcDslContractTest extends AdapterContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MONGO_JDBC_DSL_CRUD)
    public void mongoJdbcDsl_shouldExecuteCollectionCrud() throws Exception {
        try (Connection c = newAdapterConnection()) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            dropUserInfo(jdbc);

            jdbc.execute("test.user_info.insert({name: 'mali', age: 26})");
            jdbc.execute("test.user_info.insert({name: 'dative', age: 32})");
            jdbc.execute("test.user_info.insert({name: 'jon wes', age: 41})");

            List<Map<String, Object>> list = jdbc.queryForList("test.user_info.find()");
            assertEquals(3, list.size());

            Map<String, Object> mali = jdbc.queryForMap("test.user_info.find({name: 'mali'})");
            String json = (String) mali.get("_JSON");
            assertTrue(json.contains("\"name\": \"mali\""));
            assertTrue(json.contains("\"age\": 26"));

            jdbc.execute("test.user_info.update({name: 'mali'}, {$set: {age: 27}})");
            mali = jdbc.queryForMap("test.user_info.find({name: 'mali'})");
            json = (String) mali.get("_JSON");
            assertTrue(json.contains("\"age\": 27"));

            jdbc.execute("test.user_info.remove({name: 'mali'})");
            list = jdbc.queryForList("test.user_info.find({name: 'mali'})");
            assertTrue(list.isEmpty());

            list = jdbc.queryForList("test.user_info.find()");
            assertEquals(2, list.size());
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MONGO_JDBC_DSL_BATCH_INSERT_COUNT)
    public void mongoJdbcDsl_shouldExecuteBatchInsertAndCount() throws Exception {
        try (Connection c = newAdapterConnection()) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            dropUserInfo(jdbc);

            jdbc.execute("test.user_info.insert([{name: 'mali', age: 26}, {name: 'dative', age: 32}])");

            int count = jdbc.queryForInt("test.user_info.count()");
            assertEquals(2, count);
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MONGO_JDBC_DSL_RESULT_LIST)
    public void mongoJdbcDsl_shouldReadCalculatedResult() throws SQLException {
        try (Connection c = newAdapterConnection()) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            jdbc.execute("use test");
            try {
                jdbc.execute("lambda_sum.drop()");
            } catch (Throwable e) {
                // ignore
            }

            LambdaTemplate lambda = new LambdaTemplate(c);
            String groupId = UUID.randomUUID().toString();

            Map<String, Object> d1 = new HashMap<>();
            d1.put("group", groupId);
            d1.put("amount", 1);
            assertEquals(1, lambda.insertFreedom("lambda_sum").applyMap(d1).executeSumResult());

            Map<String, Object> d2 = new HashMap<>();
            d2.put("group", groupId);
            d2.put("amount", 2);
            assertEquals(1, lambda.insertFreedom("lambda_sum").applyMap(d2).executeSumResult());

            Map<String, Object> d3 = new HashMap<>();
            d3.put("group", groupId);
            d3.put("amount", 3);
            assertEquals(1, lambda.insertFreedom("lambda_sum").applyMap(d3).executeSumResult());

            // Mongo 方言不支持 applySelect("sum(...)") 这类自定义投影；用 aggregate 完成求和。
            String aggSql = "db.lambda_sum.aggregate([" + //
                    "{ $match: { group: '" + groupId + "' } }," + //
                    "{ $group: { _id: null, total: { $sum: '$amount' } } }" +//
                    "])";
            List<Map<String, Object>> rows = jdbc.queryForList(aggSql);
            assertEquals(1, rows.size());
            String json = (String) rows.get(0).get("_JSON");
            assertNotNull(json);
            assertTrue(json.contains("\"total\": 6") || json.contains("\"total\": 6.0"));
        }
    }

    private void dropUserInfo(JdbcTemplate jdbc) {
        try {
            jdbc.execute("test.user_info.drop()");
        } catch (Exception ignored) {
        }
    }
}
