package net.hasor.realdb.elastic6;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.realdb.elastic6.dto1.UserInfo1BaseMapper;
import net.hasor.realdb.elastic6.dto1.UserInfo1a;
import net.hasor.realdb.elastic6.dto_complex.Address;
import net.hasor.realdb.elastic6.dto_complex.ComplexOrder;
import net.hasor.realdb.elastic6.dto_complex.OrderItem;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.utils.DsUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Elastic6LambdaTest extends AbstractDbTest {

    @Before
    public void before() throws SQLException {
        try (Connection c = DsUtils.es7Conn()) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            try {
                jdbc.execute("DELETE /user_info");
            } catch (Exception e) {
            }
            try {
                jdbc.execute("DELETE /complex_order");
            } catch (Exception e) {
            }
            try {
                jdbc.execute("DELETE /lambda_page");
            } catch (Exception e) {
            }

        }
    }

    @After
    public void after() throws SQLException {
        before();
    }

    @Test
    public void testLambdaCRUD() throws SQLException {
        try (Connection c = DsUtils.es7Conn()) {
            LambdaTemplate lambda = new LambdaTemplate(c);

            // Insert
            UserInfo1a user = new UserInfo1a();
            user.setUid(UUID.randomUUID().toString());
            user.setName("test_user");
            user.setLoginName("test_login");
            user.setLoginPassword("password");
            int r1 = lambda.insert(UserInfo1a.class).applyEntity(user).executeSumResult();
            assert r1 == 1;

            // Select
            UserInfo1a l1 = lambda.query(UserInfo1a.class).eq(UserInfo1a::getUid, user.getUid()).queryForObject();
            assert l1 != null;
            assert "test_user".equals(l1.getName());

            // Update
            int r2 = lambda.update(UserInfo1a.class).eq(UserInfo1a::getUid, user.getUid()).updateTo(UserInfo1a::getName, "updated_user").doUpdate();
            assert r2 == 1;

            // Verify Update
            UserInfo1a l2 = lambda.query(UserInfo1a.class).eq(UserInfo1a::getUid, user.getUid()).queryForObject();
            assert "updated_user".equals(l2.getName());

            // Delete
            int r3 = lambda.delete(UserInfo1a.class).eq(UserInfo1a::getUid, user.getUid()).doDelete();
            assert r3 == 1;
            UserInfo1a l3 = lambda.query(UserInfo1a.class).eq(UserInfo1a::getUid, user.getUid()).queryForObject();
            assert l3 == null;
        }
    }

    @Test
    public void testGenericMapperCRUD() throws Exception {
        Configuration config = new Configuration();
        try (Session session = config.newSession(DsUtils.es7Conn())) {
            UserInfo1BaseMapper mapper = session.createMapper(UserInfo1BaseMapper.class);

            // Insert
            UserInfo1a user = new UserInfo1a();
            user.setUid(UUID.randomUUID().toString());
            user.setName("mapper_user");
            user.setLoginName("mapper_login");
            user.setLoginPassword("password");
            int r1 = mapper.insert(user);
            assert r1 == 1;

            // Select
            UserInfo1a l1 = mapper.selectById(user.getUid());
            assert l1 != null;
            assert "mapper_user".equals(l1.getName());

            // Update
            user.setName("mapper_updated");
            int r2 = mapper.update(user);
            assert r2 == 1;

            // Verify Update
            UserInfo1a l2 = mapper.selectById(user.getUid());
            assert "mapper_updated".equals(l2.getName());

            // Delete
            int r3 = mapper.deleteById(user.getUid());
            assert r3 == 1;
            UserInfo1a l3 = mapper.selectById(user.getUid());
            assert l3 == null;
        }
    }

    @Test
    public void testComplexTypeMapping() throws SQLException {
        try (Connection c = DsUtils.es7Conn()) {
            LambdaTemplate lambda = new LambdaTemplate(c);

            // Prepare Data
            ComplexOrder order = new ComplexOrder();
            order.setId(UUID.randomUUID().toString());

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
        try (Connection c = DsUtils.es7Conn()) {
            LambdaTemplate lambda = new LambdaTemplate(c);
            String groupId = UUID.randomUUID().toString();
            for (int i = 0; i < 5; i++) {
                Map<String, Object> doc = new HashMap<>();
                doc.put("uid", groupId + "_" + i);
                doc.put("name", "name_" + i);
                doc.put("group", groupId);
                doc.put("seq", i);
                lambda.insertFreedom("lambda_page").applyMap(doc).executeSumResult();
            }

            PageObject pageInfo = new PageObject(0, 2);
            List<Map<String, Object>> page1 = lambda.queryFreedom("lambda_page")//
                    .eq("group", groupId).asc("seq").usePage(pageInfo).queryForMapList();
            assert page1.size() == 2;
            assert Integer.parseInt(page1.get(0).get("seq").toString()) == 0;
            assert Integer.parseInt(page1.get(1).get("seq").toString()) == 1;

            pageInfo.nextPage();
            List<Map<String, Object>> page2 = lambda.queryFreedom("lambda_page")//
                    .eq("group", groupId).asc("seq").usePage(pageInfo).queryForMapList();
            assert page2.size() == 2;
            assert Integer.parseInt(page2.get(0).get("seq").toString()) == 2;
            assert Integer.parseInt(page2.get(1).get("seq").toString()) == 3;

            pageInfo.nextPage();
            List<Map<String, Object>> page3 = lambda.queryFreedom("lambda_page")//
                    .eq("group", groupId).asc("seq").usePage(pageInfo).queryForMapList();
            assert page3.size() == 1;
            assert Integer.parseInt(page3.get(0).get("seq").toString()) == 4;
        }
    }
}
