/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.material.model.keygen;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.KeySeq;
import net.hasor.dbvisitor.mapping.KeyType;
import net.hasor.dbvisitor.mapping.Table;

/**
 * @KeySeq 的 value 为空字符串，用于测试异常情况
 */
@Table("user_info")
public class KeySequenceEmptyNameUser {
    @Column(primary = true, keyType = KeyType.Sequence)
    @KeySeq("")
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
