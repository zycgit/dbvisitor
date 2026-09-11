/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.material.model.tabledef;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.DdlAuto;
import net.hasor.dbvisitor.mapping.Table;

@Table(value = "simple_ddl_table", ddlAuto = DdlAuto.AddColumn)
public class SimpleDdlEntity {
    @Column(primary = true)
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
