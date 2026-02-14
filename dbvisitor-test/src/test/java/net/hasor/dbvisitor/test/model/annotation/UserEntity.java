package net.hasor.dbvisitor.test.model.annotation;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/** 子类继承父类字段 */
@Table("user_info")
public class UserEntity extends BaseEntity {
    @Column("name")
    private String  name;
    @Column("age")
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
