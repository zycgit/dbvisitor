package net.hasor.dbvisitor.test.oneapi.model.registry;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

@Table(value = "test_products", autoMapping = false, useDelimited = true)
public class ProductEntity {
    @Column(primary = true)
    private Integer id;
    @Column(value = "product_name", insert = false)
    private String  name;
    @Column(value = "price", update = false)
    private Double  price;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
