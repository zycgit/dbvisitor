package net.hasor.dbvisitor.test.model.annotation;

import net.hasor.dbvisitor.mapping.Table;

/**
 * 叶子层：autoMapping=false，父类 AutoMiddleLayer 的 autoMapping=true。
 * 验证：只有叶子类的 autoMapping 生效，父类的 @Table 不会被继承。
 * 因此只有标注了 @Column 的字段才会被映射。
 */
@Table(value = "user_info", autoMapping = false)
public class ManualOverAutoLeaf extends AutoMiddleLayer {
    private String email; // 无 @Column，不应被映射

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
