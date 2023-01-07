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
import java.util.List;

/**
 * 接口 ColumnDescription 的实现类
 * @version : 2022-12-06
 * @author 赵永春 (zyc@hasor.net)
 */
public class ColumnDescDef implements ColumnDescription {
    private String       comment;
    private String       ddlType;
    private String       defaultValue;
    private Boolean      nullable;
    private List<String> belongIndex;
    private List<String> belongUnique;

    @Override
    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String getDdlType() {
        return this.ddlType;
    }

    public void setDdlType(String ddlType) {
        this.ddlType = ddlType;
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

    @Override
    public List<String> getBelongIndex() {
        return this.belongIndex;
    }

    public void setBelongIndex(List<String> belongIndex) {
        this.belongIndex = belongIndex;
    }

    @Override
    public List<String> belongUnique() {
        return this.belongUnique;
    }

    public void setBelongUnique(List<String> belongUnique) {
        this.belongUnique = belongUnique;
    }
}