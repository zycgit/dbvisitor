package net.hasor.dbvisitor.mapping.dto;

import net.hasor.dbvisitor.mapping.ResultMap;

import java.util.Date;

@ResultMap(space = "abc")
public class AnnoResultMapBean3 {

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
