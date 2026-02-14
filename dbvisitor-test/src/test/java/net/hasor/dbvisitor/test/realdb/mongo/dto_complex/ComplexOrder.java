package net.hasor.dbvisitor.test.realdb.mongo.dto_complex;
import java.util.List;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.KeyType;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.types.handler.json.BsonListTypeHandler;
import net.hasor.dbvisitor.types.handler.json.BsonTypeHandler;

@Table("complex_order")
public class ComplexOrder {
    @Column(value = "_id", primary = true, keyType = KeyType.Auto, whereValueTemplate = "ObjectId(?)")
    private String id;

    @Column(value = "address", typeHandler = BsonTypeHandler.class)
    private Address address;

    @Column(value = "items", typeHandler = BsonListTypeHandler.class)
    private List<OrderItem> items;

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
}
