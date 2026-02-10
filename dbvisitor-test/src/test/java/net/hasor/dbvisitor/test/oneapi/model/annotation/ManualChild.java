package net.hasor.dbvisitor.test.oneapi.model.annotation;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/** autoMapping=false 时只有当前类 @Column 声明的字段 */
@Table(value = "user_info", autoMapping = false)
public class ManualChild extends BaseEntity {
    @Column(value = "name", primary = true)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
