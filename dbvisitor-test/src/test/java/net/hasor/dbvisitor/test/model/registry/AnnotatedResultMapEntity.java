package net.hasor.dbvisitor.test.model.registry;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.ResultMap;

@ResultMap(value = "MyResultMap", space = "myMapSpace")
public class AnnotatedResultMapEntity {
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
