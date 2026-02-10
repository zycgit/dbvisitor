package net.hasor.dbvisitor.test.oneapi.model.registry;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.KeyType;
import net.hasor.dbvisitor.mapping.Table;

@Table(value = "test_orders", catalog = "test_cat", schema = "test_sch")
public class OrderEntity {
    @Column(primary = true, keyType = KeyType.Auto)
    private Integer id;
    @Column("order_no")
    private String  orderNo;

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
}
