/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.adapter.AdapterCase;
import net.hasor.dbvisitor.test.realdb.milvus.material.user.UserInfoMilvus;
import net.hasor.dbvisitor.test.realdb.milvus.material.user.UserInfoMilvusBaseMapper;
import net.hasor.dbvisitor.test.realdb.milvus.material.complex.Address;
import net.hasor.dbvisitor.test.realdb.milvus.material.complex.ComplexOrderMilvus;
import net.hasor.dbvisitor.test.realdb.milvus.material.complex.OrderItem;
import net.hasor.dbvisitor.test.realdb.milvus.material.complex.OrderItems;
import org.junit.Before;

import static org.junit.Assert.*;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;

public class MilvusLambdaTest extends AdapterCase {
    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Before
    public void before() throws SQLException {
        try (Connection c = newAdapterConnection()) {
            JdbcTemplate jdbc = new JdbcTemplate(c);

            // Recreate this test's fixtures so a previous run cannot retain a different schema/consistency.
            for (String table : new String[] { "tb_user_info_milvus", "tb_complex_order_milvus", "lambda_page", "lambda_sum" }) {
                jdbc.execute("DROP TABLE IF EXISTS " + table);
            }

            // 1. UserInfoMilvus
            jdbc.execute("CREATE TABLE IF NOT EXISTS tb_user_info_milvus (uid VARCHAR(64) PRIMARY KEY, name VARCHAR(64), loginName VARCHAR(64), loginPassword VARCHAR(64), v FLOAT_VECTOR(2)) WITH (consistency_level='Strong')");
            jdbc.execute("CREATE INDEX idx_user_v ON TABLE tb_user_info_milvus (v) USING \"IVF_FLAT\" WITH (nlist = 128, metric_type = 'L2')");
            loadTable(jdbc, "tb_user_info_milvus");

            // 2. ComplexOrderMilvus
            jdbc.execute("CREATE TABLE IF NOT EXISTS tb_complex_order_milvus (id VARCHAR(64) PRIMARY KEY, address JSON, items JSON, v FLOAT_VECTOR(2)) WITH (consistency_level='Strong')");
            jdbc.execute("CREATE INDEX idx_order_v ON TABLE tb_complex_order_milvus (v) USING \"IVF_FLAT\" WITH (nlist = 128, metric_type = 'L2')");
            loadTable(jdbc, "tb_complex_order_milvus");

            // 3. lambda_page
            jdbc.execute("CREATE TABLE IF NOT EXISTS lambda_page (uid VARCHAR(64) PRIMARY KEY, name VARCHAR(64), group_id VARCHAR(64), seq INT64, v FLOAT_VECTOR(2)) WITH (consistency_level='Strong')");
            jdbc.execute("CREATE INDEX idx_page_v ON TABLE lambda_page (v) USING FLAT WITH (metric_type = 'L2')");
            loadTable(jdbc, "lambda_page");

            // 4. lambda_sum
            jdbc.execute("CREATE TABLE IF NOT EXISTS lambda_sum (uid VARCHAR(64) PRIMARY KEY, group_id VARCHAR(64), amount INT64, v FLOAT_VECTOR(2)) WITH (consistency_level='Strong')");
            jdbc.execute("CREATE INDEX idx_sum_v ON TABLE lambda_sum (v) USING \"IVF_FLAT\" WITH (nlist = 128, metric_type = 'L2')");
            loadTable(jdbc, "lambda_sum");

        }
    }

    private void loadTable(JdbcTemplate jdbc, String tableName) throws SQLException {
        SQLException last = null;
        for (int i = 0; i < 3; i++) {
            try {
                jdbc.execute("LOAD TABLE " + tableName);
                return;
            } catch (SQLException e) {
                last = e;
            }
        }
        throw last;
    }

    private List<Float> sampleVector() {
        return Arrays.asList(0.1f, 0.2f);
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_LAMBDA_CRUD)
    public void testLambdaCRUD() throws SQLException {
        try (Connection c = newAdapterConnection()) {
            LambdaTemplate lambda = new LambdaTemplate(c);

            // Insert
            UserInfoMilvus user = new UserInfoMilvus();
            user.setUid(UUID.randomUUID().toString());
            user.setName("test_user");
            user.setLoginName("test_login");
            user.setLoginPassword("password");
            user.setV(sampleVector());

            int r1 = lambda.insert(UserInfoMilvus.class)//
                    .applyEntity(user)//
                    .executeSumResult();
            assertEquals(1, r1);

            // Select
            UserInfoMilvus l1 = lambda.query(UserInfoMilvus.class)//
                    .eq(UserInfoMilvus::getUid, user.getUid())//
                    .queryForObject();
            assertNotNull(l1);
            assertEquals("test_user", l1.getName());

            // Update
            int r2 = lambda.update(UserInfoMilvus.class)//
                    .eq(UserInfoMilvus::getUid, user.getUid())//
                    .updateTo(UserInfoMilvus::getName, "updated_user")//
                    .doUpdate();
            assertEquals(1, r2);

            // Verify Update
            UserInfoMilvus l2 = lambda.query(UserInfoMilvus.class)//
                    .eq(UserInfoMilvus::getUid, user.getUid())//
                    .queryForObject();
            assertEquals("updated_user", l2.getName());

            // Delete
            int r3 = lambda.delete(UserInfoMilvus.class)//
                    .eq(UserInfoMilvus::getUid, user.getUid())//
                    .doDelete();
            assertEquals(1, r3);

            UserInfoMilvus l3 = lambda.query(UserInfoMilvus.class)//
                    .eq(UserInfoMilvus::getUid, user.getUid())//
                    .queryForObject();
            assertNull(l3);
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_BASEMAPPER_CRUD)
    public void testGenericMapperCRUD() throws Exception {
        Configuration config = new Configuration();
        try (Session session = config.newSession(newAdapterConnection())) {
            UserInfoMilvusBaseMapper mapper = session.createMapper(UserInfoMilvusBaseMapper.class);

            // Insert
            UserInfoMilvus user = new UserInfoMilvus();
            user.setUid(UUID.randomUUID().toString());
            user.setName("mapper_user");
            user.setLoginName("mapper_login");
            user.setLoginPassword("password");
            user.setV(sampleVector());

            int r1 = mapper.insert(user);
            assertEquals(1, r1);

            // Select
            UserInfoMilvus l1 = mapper.selectById(user.getUid());
            assertNotNull(l1);
            assertEquals("mapper_user", l1.getName());

            // Update
            user.setName("mapper_updated");
            int r2 = mapper.update(user);
            assertEquals(1, r2);

            // Verify Update
            UserInfoMilvus l2 = mapper.selectById(user.getUid());
            assertEquals("mapper_updated", l2.getName());

            // Delete
            int r3 = mapper.deleteById(user.getUid());
            assertEquals(1, r3);

            UserInfoMilvus l3 = mapper.selectById(user.getUid());
            assertNull(l3);
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_COMPLEX_MAPPING)
    public void testComplexTypeMapping() throws SQLException {
        try (Connection c = newAdapterConnection()) {
            LambdaTemplate lambda = new LambdaTemplate(c);

            // Prepare Data
            ComplexOrderMilvus order = new ComplexOrderMilvus();
            order.setId(UUID.randomUUID().toString());
            order.setV(sampleVector());

            Address address = new Address();
            address.setCity("New York");
            address.setStreet("5th Avenue");
            order.setAddress(address);

            List<OrderItem> items = new ArrayList<>();
            OrderItem item1 = new OrderItem();
            item1.setItemName("Apple");
            item1.setQuantity(10);
            items.add(item1);

            OrderItem item2 = new OrderItem();
            item2.setItemName("Banana");
            item2.setQuantity(20);
            items.add(item2);

            OrderItems orderItems = new OrderItems();
            orderItems.setItems(items);
            order.setItems(orderItems);

            // Insert
            int r1 = lambda.insert(ComplexOrderMilvus.class)//
                    .applyEntity(order)//
                    .executeSumResult();
            assertEquals(1, r1);

            // Query
            ComplexOrderMilvus loadedOrder = lambda.query(ComplexOrderMilvus.class)//
                    .eq(ComplexOrderMilvus::getId, order.getId())//
                    .queryForObject();

            assertNotNull(loadedOrder);
            assertEquals(order.getId(), loadedOrder.getId());

            // Check Address
            assertNotNull(loadedOrder.getAddress());
            assertEquals("New York", loadedOrder.getAddress().getCity());
            assertEquals("5th Avenue", loadedOrder.getAddress().getStreet());

            // Check Items
            assertNotNull(loadedOrder.getItems());
            assertEquals(2, loadedOrder.getItems().getItems().size());
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_LAMBDA_PAGE)
    public void testLambdaPageQuery() throws SQLException {
        try (Connection c = newAdapterConnection()) {

            LambdaTemplate lambda = new LambdaTemplate(c);
            String groupId = UUID.randomUUID().toString();
            for (int i = 0; i < 5; i++) {
                Map<String, Object> doc = new HashMap<>();
                doc.put("uid", groupId + "_" + i);
                doc.put("name", "name_" + i);
                doc.put("group_id", groupId);
                doc.put("seq", (long) i);
                doc.put("v", Arrays.asList((float) i, 0.0f));

                int res = lambda.insertFreedom("lambda_page").applyMap(doc).executeSumResult();
                assertEquals(1, res);
            }

            PageObject pageInfo = new PageObject(0, 2);

            // Milvus supports vector-distance ordering, not scalar ORDER BY seq.
            List<Map<String, Object>> page1 = lambda.queryFreedom("lambda_page")//
                    .eq("group_id", groupId).orderByL2("v", new float[] { 0, 0 }).usePage(pageInfo).queryForMapList();

            assertEquals(2, page1.size());
            assertEquals(0L, ((Number) page1.get(0).get("seq")).longValue());
            assertEquals(1L, ((Number) page1.get(1).get("seq")).longValue());

            pageInfo.nextPage();
            List<Map<String, Object>> page2 = lambda.queryFreedom("lambda_page")//
                    .eq("group_id", groupId).orderByL2("v", new float[] { 0, 0 }).usePage(pageInfo).queryForMapList();
            assertEquals(2, page2.size());
            assertEquals(2L, ((Number) page2.get(0).get("seq")).longValue());
            assertEquals(3L, ((Number) page2.get(1).get("seq")).longValue());

            pageInfo.nextPage();
            List<Map<String, Object>> page3 = lambda.queryFreedom("lambda_page")//
                    .eq("group_id", groupId).orderByL2("v", new float[] { 0, 0 }).usePage(pageInfo).queryForMapList();
            assertEquals(1, page3.size());
            assertEquals(4L, ((Number) page3.get(0).get("seq")).longValue());
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_LAMBDA_COUNT)
    public void testLambdaSumQuery() throws SQLException {
        try (Connection c = newAdapterConnection()) {

            LambdaTemplate lambda = new LambdaTemplate(c);
            String groupId = UUID.randomUUID().toString();

            Map<String, Object> d1 = new HashMap<>();
            d1.put("uid", UUID.randomUUID().toString());
            d1.put("group_id", groupId);
            d1.put("amount", 1L);
            d1.put("v", sampleVector());
            assertEquals(1, lambda.insertFreedom("lambda_sum").applyMap(d1).executeSumResult());

            Map<String, Object> d2 = new HashMap<>();
            d2.put("uid", UUID.randomUUID().toString());
            d2.put("group_id", groupId);
            d2.put("amount", 2L);
            d2.put("v", sampleVector());
            assertEquals(1, lambda.insertFreedom("lambda_sum").applyMap(d2).executeSumResult());

            Map<String, Object> d3 = new HashMap<>();
            d3.put("uid", UUID.randomUUID().toString());
            d3.put("group_id", groupId);
            d3.put("amount", 3L);
            d3.put("v", sampleVector());
            assertEquals(1, lambda.insertFreedom("lambda_sum").applyMap(d3).executeSumResult());

            long count = lambda.queryFreedom("lambda_sum").eq("group_id", groupId).queryForCount();
            assertEquals(3, count);
        }
    }
}
