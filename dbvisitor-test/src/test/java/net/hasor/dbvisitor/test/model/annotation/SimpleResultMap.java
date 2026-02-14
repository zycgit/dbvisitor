package net.hasor.dbvisitor.test.model.annotation;

import net.hasor.dbvisitor.mapping.ResultMap;

@ResultMap(value = "simpleResult")
public class SimpleResultMap {
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
