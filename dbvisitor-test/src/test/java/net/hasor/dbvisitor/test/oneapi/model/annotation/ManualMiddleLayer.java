package net.hasor.dbvisitor.test.oneapi.model.annotation;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/**
 * 中间层：autoMapping=false。
 * 用于测试当叶子类 autoMapping=true 时，是否能覆盖父层的 false 设置。
 */
@Table(value = "user_info", autoMapping = false)
public class ManualMiddleLayer extends BaseEntity {
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
