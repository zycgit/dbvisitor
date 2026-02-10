package net.hasor.dbvisitor.test.oneapi.model.types;

import java.util.Objects;
import net.hasor.dbvisitor.types.BindTypeHandler;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;

/**
 * 使用 @BindTypeHandler 注解的 JSON 测试 Bean
 * <p>通过在类上添加 @BindTypeHandler(JsonTypeHandler.class) 注解，
 * 整个 Bean 会自动使用 JsonTypeHandler 进行序列化和反序列化。</p>
 * <p>使用场景：</p>
 * <ul>
 *   <li>无需在 SQL 中手动指定 typeHandler</li>
 *   <li>Bean 作为参数或返回值时自动进行 JSON 转换</li>
 *   <li>简化了 JSON 字段的处理逻辑</li>
 * </ul>
 * <p>使用示例：</p>
 * <pre>{@code
 * // 插入时自动序列化为 JSON
 * JsonAnnotatedBean bean = new JsonAnnotatedBean("Product A", 99.99);
 * jdbcTemplate.execute("INSERT INTO products (id, data) VALUES (?, ?)",
 *     new Object[]{1, bean});
 * // 查询时自动反序列化为 Bean
 * JsonAnnotatedBean loaded = jdbcTemplate.queryForObject(
 *     "SELECT data FROM products WHERE id = ?",
 *     new Object[]{1}, JsonAnnotatedBean.class);
 * }</pre>
 */
@BindTypeHandler(JsonTypeHandler.class)
public class JsonAnnotatedBean {
    private String  productName;
    private Double  price;
    private Integer quantity;
    private String  category;

    public JsonAnnotatedBean() {
    }

    public JsonAnnotatedBean(String productName, Double price) {
        this.productName = productName;
        this.price = price;
    }

    public JsonAnnotatedBean(String productName, Double price, Integer quantity, String category) {
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        JsonAnnotatedBean that = (JsonAnnotatedBean) o;
        return Objects.equals(productName, that.productName) && Objects.equals(price, that.price) && Objects.equals(quantity, that.quantity) && Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productName, price, quantity, category);
    }

    @Override
    public String toString() {
        return "JsonAnnotatedBean{" + "productName='" + productName + '\'' + ", price=" + price + ", quantity=" + quantity + ", category='" + category + '\'' + '}';
    }
}
