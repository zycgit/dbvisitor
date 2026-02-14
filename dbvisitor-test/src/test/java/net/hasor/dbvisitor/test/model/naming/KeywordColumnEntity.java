package net.hasor.dbvisitor.test.model.naming;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/**
 * Entity mapped to a table with SQL keyword column names.
 * Columns 'order' and 'select' are PostgreSQL reserved keywords.
 * useDelimited=true ensures all identifiers are properly quoted.
 */
@Table(value = "naming_keyword_test", useDelimited = true)
public class KeywordColumnEntity {
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
