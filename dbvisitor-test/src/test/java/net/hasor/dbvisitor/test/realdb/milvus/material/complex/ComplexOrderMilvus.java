/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus.material.complex;

import java.util.List;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;

@Table("tb_complex_order_milvus")
public class ComplexOrderMilvus {
    @Column(value = "id", primary = true)
    private String          id;
    @Column(value = "address", typeHandler = JsonTypeHandler.class)
    private Address         address;
    @Column(value = "items", typeHandler = JsonTypeHandler.class)
    private OrderItems      items;
    @Column("v")
    private List<Float>     v;

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

    public OrderItems getItems() {
        return items;
    }

    public void setItems(OrderItems items) {
        this.items = items;
    }

    public List<Float> getV() {
        return v;
    }

    public void setV(List<Float> v) {
        this.v = v;
    }
}
