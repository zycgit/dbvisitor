package net.hasor.dbvisitor.mapping.dto;

import java.util.Date;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.KeyType;
import net.hasor.dbvisitor.mapping.ResultMap;
import net.hasor.test.dto.ResourceType;

@ResultMap
public class AnnoResultMapBean1 {

    @Column(name = "id", primary = true, keyType = KeyType.Auto)
    private Long id;

    @Column(name = "gmt_create", update = false)
    private Date gmtCreate;

    @Column(name = "gmt_modified")
    private Date gmtModified;

    @Column(name = "instance_id")
    private String instanceId;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "owner_type")
    private ResourceType ownerType;

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
