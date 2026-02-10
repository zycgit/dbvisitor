package net.hasor.dbvisitor.test.oneapi.model.registry;

/** 无 @Table 注解的普通类（id/name） */
public class PlainPojo {
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
