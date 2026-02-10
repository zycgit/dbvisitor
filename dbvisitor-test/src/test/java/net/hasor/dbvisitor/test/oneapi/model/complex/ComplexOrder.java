package net.hasor.dbvisitor.test.oneapi.model.complex;

import java.util.List;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/**
 * Level 5: Complex types with nested JSON objects
 * Tests JSON/JSONB storage and retrieval
 */
@Table("complex_order")
public class ComplexOrder {
    @Column(primary = true)
    private Integer id;

    @Column("order_no")
    private String orderNo;

    // Nested object - will be stored as JSON
    private Address address;

    // Nested array - will be stored as JSON array
    private List<OrderItem> items;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}
