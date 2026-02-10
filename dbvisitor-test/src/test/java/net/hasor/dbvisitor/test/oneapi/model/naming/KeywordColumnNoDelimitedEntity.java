package net.hasor.dbvisitor.test.oneapi.model.naming;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/**
 * Entity with SQL keyword columns but useDelimited=false (default).
 * Used to verify dialect auto-detection: fmtName should automatically add qualifiers
 * for columns whose names are SQL keywords (e.g. "order", "select").
 */
@Table("naming_keyword_test")
public class KeywordColumnNoDelimitedEntity {
    @Column(primary = true)
    private Integer id;
    @Column("order")
    private String  orderValue;  // 'order' is a SQL keyword
    @Column("select")
    private String  selectValue; // 'select' is a SQL keyword
    private String  name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrderValue() {
        return orderValue;
    }

    public void setOrderValue(String orderValue) {
        this.orderValue = orderValue;
    }

    public String getSelectValue() {
        return selectValue;
    }

    public void setSelectValue(String selectValue) {
        this.selectValue = selectValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
