package net.hasor.dbvisitor.test.model.keygen;

import java.util.Date;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.KeyType;
import net.hasor.dbvisitor.mapping.Table;

/**
 * KeyType.UUID32 - 使用 32 位字符串的 UUID 填充数据
 * 格式：4d68040901d24b70bd10c1c8119001e2（无连字符）
 */
@Table("user_info")
public class KeyUuid32User {

    @Column(primary = true, keyType = KeyType.Auto)
    private Integer id;

    @Column(name = "name", keyType = KeyType.UUID32)
    private String name;

    @Column(name = "age")
    private Integer age;

    @Column(name = "create_time")
    private Date createTime;

    // Getters and Setters
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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
