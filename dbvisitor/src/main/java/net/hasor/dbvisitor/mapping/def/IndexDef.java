/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapping.def;
import java.util.List;

/**
 * 一个索引信息
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class IndexDef implements IndexDescription {
    private String       name;
    private boolean      unique;
    private List<String> columns;
    private String       comment;
    private String       other;

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isUnique() {
        return this.unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    @Override
    public List<String> getColumns() {
        return this.columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    @Override
    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String getOther() {
        return this.other;
    }

    public void setOther(String other) {
        this.other = other;
    }
}
