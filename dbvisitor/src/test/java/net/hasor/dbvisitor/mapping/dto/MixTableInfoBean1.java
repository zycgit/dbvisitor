package net.hasor.dbvisitor.mapping.dto;

import net.hasor.dbvisitor.mapping.*;

import java.util.Date;

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
