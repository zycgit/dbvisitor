package net.hasor.dbvisitor.test.oneapi.model.tabledef;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.ResultMap;

@ResultMap(value = "MyResult", space = "customSpace")
public class ResultMapEntity {
    @Column("id")
    private Integer id;
    @Column("name")
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
