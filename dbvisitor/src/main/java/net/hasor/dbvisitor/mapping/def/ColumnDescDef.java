/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.mapping.def;
/**
 * 接口 ColumnDescription 的实现类
 * @version : 2022-12-06
 * @author 赵永春 (zyc@hasor.net)
 */
public class ColumnDescDef implements ColumnDescription {
    private String  comment;
    private String  dbType;
    private String  length;
    private String  precision;
    private String  scale;
    private String  defaultValue;
    private Boolean nullable;
    private String  other;

    public ColumnDescDef() {

    }

    public ColumnDescDef(String comment, String dbType, String length, String precision, String scale, String defaultValue, Boolean nullable, String other) {
        this.comment = comment;
        this.dbType = dbType;
        this.length = length;
        this.precision = precision;
        this.scale = scale;
        this.defaultValue = defaultValue;
        this.nullable = nullable;
        this.other = other;
    }

    @Override
    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String getDbType() {
        return this.dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    @Override
    public String getLength() {
        return this.length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    @Override
    public String getPrecision() {
        return this.precision;
    }

    public void setPrecision(String precision) {
        this.precision = precision;
    }

    @Override
    public String getScale() {
        return this.scale;
    }

    public void setScale(String scale) {
        this.scale = scale;
    }

    @Override
    public String getDefault() {
        return this.defaultValue;
    }

    public void setDefault(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public Boolean getNullable() {
        return this.nullable;
    }

    public void setNullable(Boolean nullable) {
        this.nullable = nullable;
    }

    public String getOther() {
        return this.other;
    }

    public void setOther(String other) {
        this.other = other;
    }
}