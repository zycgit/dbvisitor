package net.hasor.dbvisitor.test.model.keygen;

import java.util.Date;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.KeyHolder;
import net.hasor.dbvisitor.mapping.KeyType;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.test.handler.keygen.BothKeyHolder;

/**
 * KeyType.Holder - 使用 BothKeyHolder 生成器
 * 测试 onBefore/onAfter 优先级
 */
@Table("user_info")
public class KeyHolderBothUser {

    @KeyHolder(BothKeyHolder.class)
    @Column(primary = true, keyType = KeyType.Holder)
    private Integer id;

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
