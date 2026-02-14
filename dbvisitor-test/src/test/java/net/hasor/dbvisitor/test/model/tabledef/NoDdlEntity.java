package net.hasor.dbvisitor.test.model.tabledef;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

@Table("user_info")
public class NoDdlEntity {
    @Column(primary = true)
    private Integer id;
    private String  name;

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
}
