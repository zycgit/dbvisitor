package net.hasor.dbvisitor.test.realdb.elastic6.dto_complex;
import java.util.List;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;

@Table("complex_order")
public class ComplexOrder {
    @Column(value = "id", primary = true)
    private String id;

    @Column(value = "address", typeHandler = JsonTypeHandler.class)
    private Address address;

    @Column(value = "items", typeHandler = OrderItemListTypeHandler.class)
    private List<OrderItem> items;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
