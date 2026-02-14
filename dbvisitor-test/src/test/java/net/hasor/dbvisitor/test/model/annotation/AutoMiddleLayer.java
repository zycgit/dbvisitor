package net.hasor.dbvisitor.test.model.annotation;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/**
 * 中间层：autoMapping=true。
 * 用于测试当叶子类 autoMapping=false 时，是否能覆盖父层的 true 设置。
 */
@Table(value = "user_info", autoMapping = true)
public class AutoMiddleLayer extends BaseEntity {
    @Column("name")
    private String  name;
    private Integer age; // 无 @Column

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
