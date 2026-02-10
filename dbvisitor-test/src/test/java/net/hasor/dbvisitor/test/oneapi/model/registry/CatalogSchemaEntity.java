package net.hasor.dbvisitor.test.oneapi.model.registry;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

@Table(value = "entity_with_catalog", catalog = "mycat", schema = "mysch")
public class CatalogSchemaEntity {
    @Column(value = "id", primary = true)
    private Integer id;

    public Integer getId() { return id; }

    public void setId(Integer id) { this.id = id; }
}
