package net.hasor.dbvisitor.test.oneapi.model.annotation;

import java.util.Date;
import net.hasor.dbvisitor.mapping.Column;

/** 基础父类（无 @Table，字段带 @Column，但子类无法继承这些注解） */
public class BaseEntity {
    @Column(primary = true)
    private Integer id;
    @Column("create_time")
    private Date    createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
