package net.hasor.dbvisitor.test.oneapi.model.complex;

import java.math.BigDecimal;

/**
 * Level 5: Nested object for ComplexOrder items
 */
public class OrderItem {
    private String     productName;
    private Integer    quantity;
    private BigDecimal price;

    public OrderItem() {
    }

    public OrderItem(String productName, Integer quantity, BigDecimal price) {
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
