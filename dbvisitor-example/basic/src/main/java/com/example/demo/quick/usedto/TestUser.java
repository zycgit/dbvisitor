package com.example.demo.quick.usedto;
import net.hasor.dbvisitor.mapping.Table;

import java.util.Date;

@Table(mapUnderscoreToCamelCase = true)
public class TestUser {

    private int    id;
    private String name;
    private int    age;
    private Date   createTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
