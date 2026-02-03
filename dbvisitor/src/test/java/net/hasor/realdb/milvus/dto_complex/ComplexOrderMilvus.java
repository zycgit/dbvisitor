package net.hasor.realdb.milvus.dto_complex;

import java.util.List;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;

@Table("tb_complex_order_milvus")
public class ComplexOrderMilvus {
    @Column(value = "id", primary = true)
    private String id;

    @Column(value = "address", typeHandler = JsonTypeHandler.class)
    private Address address;

    @Column(value = "items", typeHandler = JsonTypeHandler.class)
    private List<OrderItem> items;

    @Column("v")
    private List<Float> v;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public List<Float> getV() {
        return v;
    }

    public void setV(List<Float> v) {
        this.v = v;
    }
}
