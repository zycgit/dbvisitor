package net.hasor.test.entity2;

import net.hasor.dbvisitor.mapping.*;
import net.hasor.test.dto.ResourceType;

import java.util.Date;

@TableDescribe(comment = "test table")
@Table(catalog = "master", schema = "dbo", table = "blob_resource")
public class BlobResourceV6 {

    @ColumnDescribe(dbType = "bigint")
    @Column(name = "id", primary = true, keyType = KeyTypeEnum.Auto)
    private Long id;

    @ColumnDescribe(dbType = "datetime", nullable = false, defaultValue = "CURRENT_TIMESTAMP")
    @Column(name = "gmt_create", update = false)
    private Date gmtCreate;

    @ColumnDescribe(dbType = "datetime", nullable = false, defaultValue = "CURRENT_TIMESTAMP")
    @Column(name = "gmt_modified")
    private Date gmtModified;

    @ColumnDescribe(dbType = "varchar(64)")
    @Column(name = "instanceId")
    private String instanceId;

    @ColumnDescribe(dbType = "varchar(255)")
    @Column(name = "owner_name")
    private String ownerName;

    @ColumnDescribe(dbType = "varchar(64)")
    @Column(name = "owner_type")
    private ResourceType ownerType;

    @ColumnDescribe(dbType = "blob")
    @Column(name = "content")
    private byte[] content;

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

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public ResourceType getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(ResourceType ownerType) {
        this.ownerType = ownerType;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
