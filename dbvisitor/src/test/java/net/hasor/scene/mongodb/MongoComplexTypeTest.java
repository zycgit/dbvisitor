package net.hasor.scene.mongodb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.scene.mongodb.dto_complex.Address;
import net.hasor.scene.mongodb.dto_complex.ComplexOrder;
import net.hasor.scene.mongodb.dto_complex.OrderItem;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

public class MongoComplexTypeTest extends AbstractDbTest {

    @Test
    public void testComplexTypeMapping() throws SQLException {
        try (Connection c = DsUtils.mongoConn()) {
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
}
