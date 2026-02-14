package net.hasor.dbvisitor.test.model.annotation;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/** 使用 specialJavaType 指定具体实现类 */
@Table("user_info")
public class SpecialJavaTypeUser {
    @Column(primary = true)
    private Integer      id;
    @Column(value = "name", specialJavaType = String.class)
    private CharSequence name;
    private Integer      age;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CharSequence getName() {
        return name;
    }

    public void setName(CharSequence name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
