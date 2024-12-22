package net.hasor.dbvisitor.mapping.dto;

import net.hasor.dbvisitor.mapping.*;
import net.hasor.test.dto.ResourceType;

import java.util.Date;

@Table(catalog = "master", schema = "dbo", table = "blob_resource")
@TableDescribe(comment = "test table")
@IndexDescribe(name = "idx_a", columns = { "gmt_modified", "instanceId" })
@IndexDescribe(name = "uk_b", columns = { "instanceId" }, unique = true)
public class AnnoTableBean1 {

    @Column(name = "id", primary = true, keyType = KeyType.Auto)
    @ColumnDescribe(sqlType = "bigint")
    private Long id;

    @Column(name = "gmt_create", update = false)
    @ColumnDescribe(sqlType = "datetime", nullable = false, defaultValue = "CURRENT_TIMESTAMP")
    private Date gmtCreate;

    @Column(name = "gmt_modified")
    @ColumnDescribe(sqlType = "datetime", nullable = false, defaultValue = "CURRENT_TIMESTAMP")
    private Date gmtModified;

    @Column(name = "instance_id")
    @ColumnDescribe(sqlType = "varchar(64)")
    private String instanceId;

    @Column(name = "owner_name")
    @ColumnDescribe(sqlType = "varchar(255)")
    private String ownerName;

    @Column(name = "owner_type")
    @ColumnDescribe(sqlType = "varchar(64)")
    private ResourceType ownerType;

    @Column(name = "content")
    @ColumnDescribe(sqlType = "blob")
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
