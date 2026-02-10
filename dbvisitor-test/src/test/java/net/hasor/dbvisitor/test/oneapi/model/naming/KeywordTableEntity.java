package net.hasor.dbvisitor.test.oneapi.model.naming;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/**
 * Entity mapped to a table whose name is a SQL keyword: "order".
 * useDelimited=true ensures the table name is quoted as "order" in SQL.
 */
@Table(value = "order", useDelimited = true)
public class KeywordTableEntity {
    @Column(primary = true)
    private Integer id;
    private String  name;
    private String  description;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
