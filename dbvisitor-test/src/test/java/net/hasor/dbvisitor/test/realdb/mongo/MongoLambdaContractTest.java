package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.adapter.AbstractAdapterContractTest;
import net.hasor.dbvisitor.test.realdb.mongo.material.user.UserInfo1;
import net.hasor.dbvisitor.test.realdb.mongo.material.user.UserInfo1BaseMapper;
import net.hasor.dbvisitor.test.realdb.mongo.material.complex.Address;
import net.hasor.dbvisitor.test.realdb.mongo.material.complex.ComplexOrder;
import net.hasor.dbvisitor.test.realdb.mongo.material.complex.OrderItem;
import static org.junit.Assert.*;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;

public class MongoLambdaContractTest extends AbstractAdapterContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MONGO_LAMBDA_CRUD)
    public void testLambdaCRUD() throws SQLException {
        try (Connection c = newAdapterConnection()) {
            LambdaTemplate lambda = new LambdaTemplate(c);

            // Insert
            UserInfo1 user = new UserInfo1();
            user.setUid(UUID.randomUUID().toString());
            user.setName("test_user");
            user.setLoginName("test_login");
            user.setLoginPassword("password");
            int r1 = lambda.insert(UserInfo1.class).applyEntity(user).executeSumResult();
            assertEquals(1, r1);

            // Select
            UserInfo1 l1 = lambda.query(UserInfo1.class).eq(UserInfo1::getUid, user.getUid()).queryForObject();
            assertNotNull(l1);
            assertEquals("test_user", l1.getName());

            // Update
            int r2 = lambda.update(UserInfo1.class).eq(UserInfo1::getUid, user.getUid()).updateTo(UserInfo1::getName, "updated_user").doUpdate();
            assertEquals(1, r2);

            // Verify Update
            UserInfo1 l2 = lambda.query(UserInfo1.class).eq(UserInfo1::getUid, user.getUid()).queryForObject();
            assertEquals("updated_user", l2.getName());

            // Delete
            int r3 = lambda.delete(UserInfo1.class).eq(UserInfo1::getUid, user.getUid()).doDelete();
            assertEquals(1, r3);
            UserInfo1 l3 = lambda.query(UserInfo1.class).eq(UserInfo1::getUid, user.getUid()).queryForObject();
            assertNull(l3);
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MONGO_BASEMAPPER_CRUD)
    public void testGenericMapperCRUD() throws Exception {
        Configuration config = new Configuration();
        try (Session session = config.newSession(newAdapterConnection())) {
            UserInfo1BaseMapper mapper = session.createMapper(UserInfo1BaseMapper.class);

            // Insert
            UserInfo1 user = new UserInfo1();
            user.setUid(UUID.randomUUID().toString());
            user.setName("mapper_user");
            user.setLoginName("mapper_login");
            user.setLoginPassword("password");
            int r1 = mapper.insert(user);
            assertEquals(1, r1);

            // Select
            UserInfo1 l1 = mapper.selectById(user.getUid());
            assertNotNull(l1);
            assertEquals("mapper_user", l1.getName());

            // Update
            user.setName("mapper_updated");
            int r2 = mapper.update(user);
            assertEquals(1, r2);

            // Verify Update
            UserInfo1 l2 = mapper.selectById(user.getUid());
            assertEquals("mapper_updated", l2.getName());

            // Delete
            int r3 = mapper.deleteById(user.getUid());
            assertEquals(1, r3);
            UserInfo1 l3 = mapper.selectById(user.getUid());
            assertNull(l3);
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MONGO_COMPLEX_MAPPING)
    public void testComplexTypeMapping() throws SQLException {
        try (Connection c = newAdapterConnection()) {
            LambdaTemplate lambda = new LambdaTemplate(c);

            // Prepare Data
            ComplexOrder order = new ComplexOrder();

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

            order.setItems(items);

            // Insert
            int r1 = lambda.insert(ComplexOrder.class).applyEntity(order).executeSumResult();
            assertEquals(1, r1);

            // Query
            ComplexOrder loadedOrder = lambda.query(ComplexOrder.class).eq(ComplexOrder::getId, order.getId()).queryForObject();

            // Assertions
            assertNotNull(loadedOrder);
            assertEquals(order.getId(), loadedOrder.getId());

            // Check Address
            assertNotNull(loadedOrder.getAddress());
            assertEquals("New York", loadedOrder.getAddress().getCity());
            assertEquals("5th Avenue", loadedOrder.getAddress().getStreet());

            // Check Items
            assertNotNull(loadedOrder.getItems());
            assertEquals(2, loadedOrder.getItems().size());
            assertEquals("Apple", loadedOrder.getItems().get(0).getItemName());
            assertEquals(10, loadedOrder.getItems().get(0).getQuantity());
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MONGO_LAMBDA_PAGE)
    public void testLambdaPageQuery() throws SQLException {
        try (Connection c = newAdapterConnection()) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            jdbc.execute("use test");
            try {
                jdbc.execute("lambda_page.drop()");
            } catch (Throwable e) {
                // ignore
            }

            LambdaTemplate lambda = new LambdaTemplate(c);
            String groupId = UUID.randomUUID().toString();
            for (int i = 0; i < 5; i++) {
                Map<String, Object> doc = new HashMap<>();
                doc.put("uid", groupId + "_" + i);
                doc.put("name", "name_" + i);
                doc.put("group", groupId);
                doc.put("seq", i);
                int res = lambda.insertFreedom("lambda_page").applyMap(doc).executeSumResult();
                assertEquals(1, res);
            }

            PageObject pageInfo = new PageObject(0, 2);
            List<Map<String, Object>> page1 = lambda.queryFreedom("lambda_page")//
                    .eq("group", groupId).asc("seq").usePage(pageInfo).queryForMapList();
            assertEquals(2, page1.size());
            assertEquals(0, ((Number) page1.get(0).get("seq")).intValue());
            assertEquals(1, ((Number) page1.get(1).get("seq")).intValue());

            pageInfo.nextPage();
            List<Map<String, Object>> page2 = lambda.queryFreedom("lambda_page")//
                    .eq("group", groupId).asc("seq").usePage(pageInfo).queryForMapList();
            assertEquals(2, page2.size());
            assertEquals(2, ((Number) page2.get(0).get("seq")).intValue());
            assertEquals(3, ((Number) page2.get(1).get("seq")).intValue());

            pageInfo.nextPage();
            List<Map<String, Object>> page3 = lambda.queryFreedom("lambda_page")//
                    .eq("group", groupId).asc("seq").usePage(pageInfo).queryForMapList();
            assertEquals(1, page3.size());
            assertEquals(4, ((Number) page3.get(0).get("seq")).intValue());
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MONGO_LAMBDA_AGGREGATE)
    public void testLambdaSumQuery() throws SQLException {
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
            List<Map<String, Object>> rows = lambda.jdbc().queryForList(aggSql);
            assertEquals(1, rows.size());
            String json = (String) rows.get(0).get("_JSON");
            assertNotNull(json);
            assertTrue(json.contains("\"total\": 6") || json.contains("\"total\": 6.0"));
        }
    }
}
