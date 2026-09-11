/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus.material.user;

import java.util.List;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

@Table("tb_mapper_user_milvus")
public class UserInfoMilvus1 {
    @Column(value = "uid", primary = true)
    private String      uid;
    @Column("name")
    private String      name;
    @Column("loginName")
    private String      loginName;
    @Column("loginPassword")
    private String      loginPassword;
    @Column("v")
    private List<Float> v;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    public List<Float> getV() {
        return v;
    }

    public void setV(List<Float> v) {
        this.v = v;
    }
}
