/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.custom;
import java.util.Date;
import net.hasor.dbvisitor.mapping.Column;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
 */
public class UserTable {
    @Column
    private Integer id;
    @Column
    private String  name;
    @Column
    private Integer age;

    @Column(value = "create_time", typeHandler = MyDateTypeHandler.class)
    private String createTime1;

    @Column(value = "create_time")
    private Date createTime2;

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

    public String getCreateTime1() {
        return createTime1;
    }

    public void setCreateTime1(String createTime1) {
        this.createTime1 = createTime1;
    }

    public Date getCreateTime2() {
        return createTime2;
    }

    public void setCreateTime2(Date createTime2) {
        this.createTime2 = createTime2;
    }
}
