package net.hasor.dbvisitor.test.model.keygen;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.KeyType;
import net.hasor.dbvisitor.mapping.Table;

/**
 * 缺少 @KeySeq 注解的序列实体，用于测试异常情况
 */
@Table("user_strict_none")
public class KeySequenceNoAnnotationUser {
    @Column(primary = true, keyType = KeyType.Sequence)
    private Integer id;
    private String  name;

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
}
