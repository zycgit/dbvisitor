package net.hasor.dbvisitor.test.model.keygen;

import java.util.Date;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.KeyType;
import net.hasor.dbvisitor.mapping.Table;

/**
 * 错误类型的主键配置：UUID32 用在 Integer 字段上
 */
@Table("user_info")
public class KeyWrongTypeUuidUser {
    @Column(primary = true, keyType = KeyType.UUID32)
    private Integer id; // UUID32 应该用于 String 类型

    @Column(name = "name")
    private String name;

    @Column(name = "age")
    private Integer age;

    @Column(name = "create_time")
    private Date createTime;

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
