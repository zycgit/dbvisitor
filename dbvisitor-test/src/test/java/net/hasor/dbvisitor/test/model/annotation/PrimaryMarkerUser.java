package net.hasor.dbvisitor.test.model.annotation;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Primary;
import net.hasor.dbvisitor.mapping.Table;

/** 同一列映射到多个属性，@Primary 标记哪个是主属性 */
@Table("user_info")
public class PrimaryMarkerUser {
    @Column(primary = true)
    private Integer id;
    @Column("name")
    private String  aliasName;
    @Primary
    @Column("name")
    private String  primaryName;
    private Integer age;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getPrimaryName() {
        return primaryName;
    }

    public void setPrimaryName(String primaryName) {
        this.primaryName = primaryName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
