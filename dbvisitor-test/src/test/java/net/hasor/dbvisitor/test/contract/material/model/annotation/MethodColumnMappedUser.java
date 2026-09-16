/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.material.model.annotation;

import java.util.Date;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/** Column annotations on getters and setters must participate in the same entity mapping. */
@Table("user_info")
public class MethodColumnMappedUser {
    private Integer id;
    private String  userName;
    private Integer age;
    private String  mailAddr;
    private Date    createTime;

    @Column(primary = true)
    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column("name")
    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getAge() {
        return this.age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getMailAddr() {
        return this.mailAddr;
    }

    @Column("email")
    public void setMailAddr(String mailAddr) {
        this.mailAddr = mailAddr;
    }

    @Column("create_time")
    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
