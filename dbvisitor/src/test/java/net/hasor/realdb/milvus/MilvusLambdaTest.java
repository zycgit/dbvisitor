package net.hasor.realdb.milvus;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import net.hasor.cobble.concurrent.ThreadUtils;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.realdb.milvus.dto.UserInfoMilvus;
import net.hasor.realdb.milvus.dto.UserInfoMilvusBaseMapper;
import net.hasor.realdb.milvus.dto_complex.Address;
import net.hasor.realdb.milvus.dto_complex.ComplexOrderMilvus;
import net.hasor.realdb.milvus.dto_complex.OrderItem;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.utils.DsUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class MilvusLambdaTest extends AbstractDbTest {

    private Connection createConnection() throws SQLException {
        try {
            String url = DsUtils.MILVUS_JDBC_URL;
            if (!url.contains("?")) {
                url += "?";
            } else {
                url += "&";
            }
            url += "connectTimeout=30000&rpcDeadline=30000"; // Increase timeout for tests
            return DriverManager.getConnection(url);
        } catch (Exception e) {
            return DsUtils.milvusConn();
        }
    }

    @Before
    public void before() {
        try (Connection c = createConnection()) {
            JdbcTemplate jdbc = new JdbcTemplate(c);

            // 1. UserInfoMilvus
            initTable(jdbc, "tb_user_info_milvus", "CREATE TABLE IF NOT EXISTS tb_user_info_milvus (uid VARCHAR(64) PRIMARY KEY, name VARCHAR(64), loginName VARCHAR(64), loginPassword VARCHAR(64), v FLOAT_VECTOR(2))");
            initIndex(jdbc, "idx_user_v", "tb_user_info_milvus", "CREATE INDEX idx_user_v ON TABLE tb_user_info_milvus (v) USING \"IVF_FLAT\" WITH (nlist = 128, metric_type = 'L2')");

            // 2. ComplexOrderMilvus
            initTable(jdbc, "tb_complex_order_milvus", "CREATE TABLE IF NOT EXISTS tb_complex_order_milvus (id VARCHAR(64) PRIMARY KEY, address JSON, items JSON, v FLOAT_VECTOR(2))");
            initIndex(jdbc, "idx_order_v", "tb_complex_order_milvus", "CREATE INDEX idx_order_v ON TABLE tb_complex_order_milvus (v) USING \"IVF_FLAT\" WITH (nlist = 128, metric_type = 'L2')");

            // 3. lambda_page
            initTable(jdbc, "lambda_page", "CREATE TABLE IF NOT EXISTS lambda_page (uid VARCHAR(64) PRIMARY KEY, name VARCHAR(64), group_id VARCHAR(64), seq INT64, v FLOAT_VECTOR(2))");
            initIndex(jdbc, "idx_page_v", "lambda_page", "CREATE INDEX idx_page_v ON TABLE lambda_page (v) USING \"IVF_FLAT\" WITH (nlist = 128, metric_type = 'L2')");

            // 4. lambda_sum
            initTable(jdbc, "lambda_sum", "CREATE TABLE IF NOT EXISTS lambda_sum (uid VARCHAR(64) PRIMARY KEY, group_id VARCHAR(64), amount INT64, v FLOAT_VECTOR(2))");
            initIndex(jdbc, "idx_sum_v", "lambda_sum", "CREATE INDEX idx_sum_v ON TABLE lambda_sum (v) USING \"IVF_FLAT\" WITH (nlist = 128, metric_type = 'L2')");

            // Allow time for async ops
            ThreadUtils.sleep(2000);

        } catch (Throwable e) {
            Assume.assumeNoException("Milvus setup failed or timed out - skipping tests", e);
        }
    }

    private void initTable(JdbcTemplate jdbc, String tableName, String createSql) {
        try {
            jdbc.execute(createSql);
            jdbc.execute("LOAD TABLE " + tableName);
        } catch (Exception e) {
            // Check if loaded?
            try {
                jdbc.execute("LOAD TABLE " + tableName);
            } catch (Exception ex) {
                // ignore
            }
        }
    }

    private void initIndex(JdbcTemplate jdbc, String indexName, String tableName, String createSql) {
        try {
            jdbc.execute(createSql);
        } catch (Exception e) {
            // Index likely exists
        }
    }

    private List<Float> sampleVector() {
        return Arrays.asList(0.1f, 0.2f);
    }

    @Test
    public void testLambdaCRUD() throws SQLException {
        try (Connection c = createConnection()) {
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
            assert r1 == 1;

            ThreadUtils.sleep(1000); // Wait for consistency

            // Select
            UserInfoMilvus l1 = lambda.query(UserInfoMilvus.class)//
                    .eq(UserInfoMilvus::getUid, user.getUid())//
                    .queryForObject();
            assert l1 != null;
            assert "test_user".equals(l1.getName());

            // Update
            int r2 = lambda.update(UserInfoMilvus.class)//
                    .eq(UserInfoMilvus::getUid, user.getUid())//
                    .updateTo(UserInfoMilvus::getName, "updated_user")//
                    .doUpdate();
            assert r2 == 1;

            ThreadUtils.sleep(1000); // Wait for consistency

            // Verify Update
            UserInfoMilvus l2 = lambda.query(UserInfoMilvus.class)//
                    .eq(UserInfoMilvus::getUid, user.getUid())//
                    .queryForObject();
            assert "updated_user".equals(l2.getName());

            // Delete
            int r3 = lambda.delete(UserInfoMilvus.class)//
                    .eq(UserInfoMilvus::getUid, user.getUid())//
                    .doDelete();
            assert r3 == 1;

            ThreadUtils.sleep(1000); // Wait for consistency

            UserInfoMilvus l3 = lambda.query(UserInfoMilvus.class)//
                    .eq(UserInfoMilvus::getUid, user.getUid())//
                    .queryForObject();
            assert l3 == null;
        }
    }

    @Test
    public void testGenericMapperCRUD() throws Exception {
        Configuration config = new Configuration();
        try (Session session = config.newSession(createConnection())) {
            UserInfoMilvusBaseMapper mapper = session.createMapper(UserInfoMilvusBaseMapper.class);

            // Insert
            UserInfoMilvus user = new UserInfoMilvus();
            user.setUid(UUID.randomUUID().toString());
            user.setName("mapper_user");
            user.setLoginName("mapper_login");
            user.setLoginPassword("password");
            user.setV(sampleVector());

            int r1 = mapper.insert(user);
            assert r1 == 1;

            ThreadUtils.sleep(1000);

            // Select
            UserInfoMilvus l1 = mapper.selectById(user.getUid());
            assert l1 != null;
            assert "mapper_user".equals(l1.getName());

            // Update
            user.setName("mapper_updated");
            int r2 = mapper.update(user);
            assert r2 == 1;

            ThreadUtils.sleep(1000);

            // Verify Update
            UserInfoMilvus l2 = mapper.selectById(user.getUid());
            assert "mapper_updated".equals(l2.getName());

            // Delete
            int r3 = mapper.deleteById(user.getUid());
            assert r3 == 1;

            ThreadUtils.sleep(1000);

            UserInfoMilvus l3 = mapper.selectById(user.getUid());
            assert l3 == null;
        }
    }

    @Test
    public void testComplexTypeMapping() throws SQLException {
        try (Connection c = createConnection()) {
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

            order.setItems(items);

            // Insert
            int r1 = lambda.insert(ComplexOrderMilvus.class)//
                    .applyEntity(order)//
                    .executeSumResult();
            assert r1 == 1;

            ThreadUtils.sleep(1000);

            // Query
            ComplexOrderMilvus loadedOrder = lambda.query(ComplexOrderMilvus.class)//
                    .eq(ComplexOrderMilvus::getId, order.getId())//
                    .queryForObject();

            assert loadedOrder != null;
            assert order.getId().equals(loadedOrder.getId());

            // Check Address
            assert loadedOrder.getAddress() != null;
            assert "New York".equals(loadedOrder.getAddress().getCity());
            assert "5th Avenue".equals(loadedOrder.getAddress().getStreet());

            // Check Items
            assert loadedOrder.getItems() != null;
            assert loadedOrder.getItems().size() == 2;
        }
    }

    @Test
    public void testLambdaPageQuery() throws SQLException {
        try (Connection c = createConnection()) {

            LambdaTemplate lambda = new LambdaTemplate(c);
            String groupId = UUID.randomUUID().toString();
            for (int i = 0; i < 5; i++) {
                Map<String, Object> doc = new HashMap<>();
                doc.put("uid", groupId + "_" + i);
                doc.put("name", "name_" + i);
                doc.put("group_id", groupId);
                doc.put("seq", (long) i);
                doc.put("v", sampleVector());

                int res = lambda.insertFreedom("lambda_page").applyMap(doc).executeSumResult();
                assert res == 1;
            }

            ThreadUtils.sleep(1000);

            PageObject pageInfo = new PageObject(0, 2);

            // "group_id" eq
            List<Map<String, Object>> page1 = lambda.queryFreedom("lambda_page")//
                    .eq("group_id", groupId).asc("seq").usePage(pageInfo).queryForMapList();

            assert page1.size() == 2;

            pageInfo.nextPage();
            List<Map<String, Object>> page2 = lambda.queryFreedom("lambda_page")//
                    .eq("group_id", groupId).asc("seq").usePage(pageInfo).queryForMapList();
            assert page2.size() == 2;

            pageInfo.nextPage();
            List<Map<String, Object>> page3 = lambda.queryFreedom("lambda_page")//
                    .eq("group_id", groupId).asc("seq").usePage(pageInfo).queryForMapList();
            assert page3.size() == 1;
        }
    }

    @Test
    public void testLambdaSumQuery() throws SQLException {
        try (Connection c = createConnection()) {

            LambdaTemplate lambda = new LambdaTemplate(c);
            String groupId = UUID.randomUUID().toString();

            Map<String, Object> d1 = new HashMap<>();
            d1.put("uid", UUID.randomUUID().toString());
            d1.put("group_id", groupId);
            d1.put("amount", 1L);
            d1.put("v", sampleVector());
            assert lambda.insertFreedom("lambda_sum").applyMap(d1).executeSumResult() == 1;

            Map<String, Object> d2 = new HashMap<>();
            d2.put("uid", UUID.randomUUID().toString());
            d2.put("group_id", groupId);
            d2.put("amount", 2L);
            d2.put("v", sampleVector());
            assert lambda.insertFreedom("lambda_sum").applyMap(d2).executeSumResult() == 1;

            Map<String, Object> d3 = new HashMap<>();
            d3.put("uid", UUID.randomUUID().toString());
            d3.put("group_id", groupId);
            d3.put("amount", 3L);
            d3.put("v", sampleVector());
            assert lambda.insertFreedom("lambda_sum").applyMap(d3).executeSumResult() == 1;

            ThreadUtils.sleep(1000);

            long count = lambda.queryFreedom("lambda_sum").eq("group_id", groupId).queryForCount();
            assert count == 3;
        }
    }
}
