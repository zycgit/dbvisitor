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
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2022-12-06
 */
public class ColumnDescDef implements ColumnDescription {
    private String  sqlType;
    private String  length;
    private String  precision;
    private String  scale;
    private String  characterSet;
    private String  collation;
    private boolean nullable;
    private String  defaultValue;
    private String  comment;
    private String  other;

    public ColumnDescDef() {

    }

    public ColumnDescDef(String sqlType, String length, String precision, String scale, String characterSet, String collation, boolean nullable, String defaultValue, String comment, String other) {
        this.sqlType = sqlType;
        this.length = length;
        this.precision = precision;
        this.scale = scale;
        this.characterSet = characterSet;
        this.collation = collation;
        this.nullable = nullable;
        this.defaultValue = defaultValue;
        this.comment = comment;
        this.other = other;
    }

    public String getSqlType() {
        return this.sqlType;
    }

    public void setSqlType(String sqlType) {
        this.sqlType = sqlType;
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

    public String getCharacterSet() {
        return this.characterSet;
    }

    public void setCharacterSet(String characterSet) {
        this.characterSet = characterSet;
    }

    public String getCollation() {
        return this.collation;
    }

    public void setCollation(String collation) {
        this.collation = collation;
    }

    public boolean isNullable() {
        return this.nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public String getDefault() {
        return this.defaultValue;
    }

    public void setDefault(String defaultValue) {
        this.defaultValue = defaultValue;
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