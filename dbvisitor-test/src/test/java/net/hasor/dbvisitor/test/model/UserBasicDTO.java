package net.hasor.dbvisitor.test.model;

/**
 * 简单 DTO，用于 SELECT 投影测试（仅包含 name 和 age 字段）
 */
public class UserBasicDTO {
    private String  name;
    private Integer age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
