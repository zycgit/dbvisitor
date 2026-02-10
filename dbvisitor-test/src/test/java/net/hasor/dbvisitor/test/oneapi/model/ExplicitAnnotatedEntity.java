package net.hasor.dbvisitor.test.oneapi.model;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

@Table(value = "explicit_annotated_table", autoMapping = true)
public class ExplicitAnnotatedEntity {
    @Column(primary = true)
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
