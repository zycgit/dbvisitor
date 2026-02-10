package net.hasor.dbvisitor.test.oneapi.model.tabledef;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/** 指定 catalog/schema 的实体 */
@Table(value = "user_info", catalog = "my_catalog", schema = "my_schema")
public class CatalogSchemaUser {
    @Column(primary = true)
    private Integer id;
    private String  name;

    public Integer getId() { return id; }

    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }
}
