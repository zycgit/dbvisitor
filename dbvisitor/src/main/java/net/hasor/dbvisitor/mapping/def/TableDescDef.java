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
import net.hasor.dbvisitor.mapping.DdlAuto;

/**
 * 接口 TableDescription 的实现
 * @version : 2022-12-06
 * @author 赵永春 (zyc@hasor.net)
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