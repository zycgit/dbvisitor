/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapping.def;
import net.hasor.dbvisitor.mapping.DdlAuto;

/**
 * 接口 TableDescription 的实现
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-12-06
 */
public class TableDescDef implements TableDescription {
    private DdlAuto ddlAuto;
    private String  characterSet;
    private String  collation;
    private String  comment;
    private String  other;

    @Override
    public DdlAuto getDdlAuto() {
        return this.ddlAuto;
    }

    public void setDdlAuto(DdlAuto ddlAuto) {
        this.ddlAuto = ddlAuto;
    }

    @Override
    public String getCharacterSet() {
        return this.characterSet;
    }

    public void setCharacterSet(String characterSet) {
        this.characterSet = characterSet;
    }

    @Override
    public String getCollation() {
        return this.collation;
    }

    public void setCollation(String collation) {
        this.collation = collation;
    }

    @Override
    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getOther() {
        return this.other;
    }

    public void setOther(String other) {
        this.other = other;
    }
}
