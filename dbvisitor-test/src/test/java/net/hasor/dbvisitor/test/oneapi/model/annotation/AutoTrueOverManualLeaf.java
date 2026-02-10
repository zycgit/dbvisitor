package net.hasor.dbvisitor.test.oneapi.model.annotation;

import net.hasor.dbvisitor.mapping.Table;

/**
 * 叶子层：autoMapping=true，父类 ManualMiddleLayer 的 autoMapping=false。
 * 验证：只有叶子类的 autoMapping 生效，父类的 @Table 不会被继承。
 * 因此所有层级的字段（含未标注 @Column 的）都应被自动映射。
 */
@Table(value = "user_info", autoMapping = true)
public class AutoTrueOverManualLeaf extends ManualMiddleLayer {
    private String email; // 无 @Column，应被自动映射

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
