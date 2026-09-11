/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapping.dto;

import java.util.Date;
import net.hasor.dbvisitor.mapping.*;

@Table(catalog = "master", schema = "dbo", table = "blob_resource", ddlAuto = DdlAuto.CreateDrop)
@TableDescribe(comment = "test table")
@IndexDescribe(name = "idx_a", columns = { "gmt_modified", "instanceId" })
@IndexDescribe(name = "uk_b", columns = { "instanceId" }, unique = true)
public class MixTableInfoBean1 {

    @Column(name = "id", primary = true, keyType = KeyType.Auto)
    @ColumnDescribe(sqlType = "bigint")
    private Long id;

    private Date gmtCreate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }
}
