package net.hasor.dbvisitor.test.model.tabledef;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

@Table("has_table_anno")
public class HasTableAnno {
    @Column(value = "id", primary = true)
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
