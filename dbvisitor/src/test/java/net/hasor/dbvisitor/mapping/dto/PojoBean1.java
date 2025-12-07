package net.hasor.dbvisitor.mapping.dto;

import java.util.Date;
import net.hasor.test.dto.ResourceType;

public class PojoBean1 {

    private Long         id;
    private Date         gmtCreate;
    private Date         gmtModified;
    private String       instanceId;
    private String       ownerName;
    private ResourceType ownerType;
    private byte[]       content;

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
