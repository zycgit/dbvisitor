package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.realdb.mongo.dto1.UserInfo1;
import net.hasor.dbvisitor.test.realdb.mongo.dto1.UserInfo1BaseMapper;
import net.hasor.dbvisitor.test.realdb.mongo.dto_complex.Address;
import net.hasor.dbvisitor.test.realdb.mongo.dto_complex.ComplexOrder;
import net.hasor.dbvisitor.test.realdb.mongo.dto_complex.OrderItem;
import org.junit.Test;

public class MongoLambdaTest {
    @Test
    public void testLambdaCRUD() throws SQLException {
        try (Connection c = OneApiDataSourceManager.getConnection("mongo")) {
            LambdaTemplate lambda = new LambdaTemplate(c);

            // Insert
            UserInfo1 user = new UserInfo1();
            user.setUid(UUID.randomUUID().toString());
            user.setName("test_user");
            user.setLoginName("test_login");
            user.setLoginPassword("password");
            int r1 = lambda.insert(UserInfo1.class).applyEntity(user).executeSumResult();
            assert r1 == 1;

            // Select
            UserInfo1 l1 = lambda.query(UserInfo1.class).eq(UserInfo1::getUid, user.getUid()).queryForObject();
            assert l1 != null;
            assert "test_user".equals(l1.getName());

            // Update
            int r2 = lambda.update(UserInfo1.class).eq(UserInfo1::getUid, user.getUid()).updateTo(UserInfo1::getName, "updated_user").doUpdate();
            assert r2 == 1;

            // Verify Update
            UserInfo1 l2 = lambda.query(UserInfo1.class).eq(UserInfo1::getUid, user.getUid()).queryForObject();
            assert "updated_user".equals(l2.getName());

            // Delete
            int r3 = lambda.delete(UserInfo1.class).eq(UserInfo1::getUid, user.getUid()).doDelete();
            assert r3 == 1;
            UserInfo1 l3 = lambda.query(UserInfo1.class).eq(UserInfo1::getUid, user.getUid()).queryForObject();
            assert l3 == null;
        }
    }

    @Test
    public void testGenericMapperCRUD() throws Exception {
        Configuration config = new Configuration();
        try (Session session = config.newSession(OneApiDataSourceManager.getConnection("mongo"))) {
            UserInfo1BaseMapper mapper = session.createMapper(UserInfo1BaseMapper.class);

            // Insert
            UserInfo1 user = new UserInfo1();
            user.setUid(UUID.randomUUID().toString());
            user.setName("mapper_user");
            user.setLoginName("mapper_login");
            user.setLoginPassword("password");
            int r1 = mapper.insert(user);
            assert r1 == 1;

            // Select
            UserInfo1 l1 = mapper.selectById(user.getUid());
            assert l1 != null;
            assert "mapper_user".equals(l1.getName());

            // Update
            user.setName("mapper_updated");
            int r2 = mapper.update(user);
            assert r2 == 1;

            // Verify Update
            UserInfo1 l2 = mapper.selectById(user.getUid());
            assert "mapper_updated".equals(l2.getName());

            // Delete
            int r3 = mapper.deleteById(user.getUid());
            assert r3 == 1;
            UserInfo1 l3 = mapper.selectById(user.getUid());
            assert l3 == null;
        }
    }

    @Test
    public void testComplexTypeMapping() throws SQLException {
        try (Connection c = OneApiDataSourceManager.getConnection("mongo")) {
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
            assert r1 == 1;

            // Query
            ComplexOrder loadedOrder = lambda.query(ComplexOrder.class).eq(ComplexOrder::getId, order.getId()).queryForObject();

            // Assertions
            assert loadedOrder != null;
            assert order.getId().equals(loadedOrder.getId());

            // Check Address
            assert loadedOrder.getAddress() != null;
            assert "New York".equals(loadedOrder.getAddress().getCity());
            assert "5th Avenue".equals(loadedOrder.getAddress().getStreet());

            // Check Items
            assert loadedOrder.getItems() != null;
            assert loadedOrder.getItems().size() == 2;
            assert "Apple".equals(loadedOrder.getItems().get(0).getItemName());
            assert 10 == loadedOrder.getItems().get(0).getQuantity();
        }
    }

    @Test
    public void testLambdaPageQuery() throws SQLException {
        try (Connection c = OneApiDataSourceManager.getConnection("mongo")) {
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
                assert res == 1;
            }

            PageObject pageInfo = new PageObject(0, 2);
            List<Map<String, Object>> page1 = lambda.queryFreedom("lambda_page")//
                    .eq("group", groupId).asc("seq").usePage(pageInfo).queryForMapList();
            assert page1.size() == 2;
            assert ((Number) page1.get(0).get("seq")).intValue() == 0;
            assert ((Number) page1.get(1).get("seq")).intValue() == 1;

            pageInfo.nextPage();
            List<Map<String, Object>> page2 = lambda.queryFreedom("lambda_page")//
                    .eq("group", groupId).asc("seq").usePage(pageInfo).queryForMapList();
            assert page2.size() == 2;
            assert ((Number) page2.get(0).get("seq")).intValue() == 2;
            assert ((Number) page2.get(1).get("seq")).intValue() == 3;

            pageInfo.nextPage();
            List<Map<String, Object>> page3 = lambda.queryFreedom("lambda_page")//
                    .eq("group", groupId).asc("seq").usePage(pageInfo).queryForMapList();
            assert page3.size() == 1;
            assert ((Number) page3.get(0).get("seq")).intValue() == 4;
        }
    }

    @Test
    public void testLambdaSumQuery() throws SQLException {
        try (Connection c = OneApiDataSourceManager.getConnection("mongo")) {
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
            assert lambda.insertFreedom("lambda_sum").applyMap(d1).executeSumResult() == 1;

            Map<String, Object> d2 = new HashMap<>();
            d2.put("group", groupId);
            d2.put("amount", 2);
            assert lambda.insertFreedom("lambda_sum").applyMap(d2).executeSumResult() == 1;

            Map<String, Object> d3 = new HashMap<>();
            d3.put("group", groupId);
            d3.put("amount", 3);
            assert lambda.insertFreedom("lambda_sum").applyMap(d3).executeSumResult() == 1;

            // Mongo 方言不支持 applySelect("sum(...)") 这类自定义投影；用 aggregate 完成求和。
            String aggSql = "db.lambda_sum.aggregate([" + //
                    "{ $match: { group: '" + groupId + "' } }," + //
                    "{ $group: { _id: null, total: { $sum: '$amount' } } }" +//
                    "])";
            List<Map<String, Object>> rows = lambda.jdbc().queryForList(aggSql);
            assert rows.size() == 1;
            String json = (String) rows.get(0).get("_JSON");
            assert json != null;
            assert json.contains("\"total\": 6") || json.contains("\"total\": 6.0");
        }
    }
}
