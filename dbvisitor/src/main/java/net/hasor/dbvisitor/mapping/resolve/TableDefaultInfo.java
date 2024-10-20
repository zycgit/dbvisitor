///*
// * Copyright 2015-2022 the original author or authors.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package net.hasor.dbvisitor.mapping.resolve;
//import net.hasor.cobble.StringUtils;
//import net.hasor.dbvisitor.mapping.DdlAuto;
//import net.hasor.dbvisitor.mapping.MappingOptions;
//import net.hasor.dbvisitor.mapping.Table;
//import net.hasor.dbvisitor.mapping.TableDescribe;
//
//import java.lang.annotation.Annotation;
//import java.util.Map;
//
///**
//
//
// 可是通过 MappingOptions 来提供默认选项获取的能力。
//
//
// * 承载 @TableDefault 配置信息
// * @author 赵永春 (zyc@hasor.net)
// * @version : 2021-06-21
// */
//class TableDefaultInfo implements Table, TableDescribe {
//    private final String  catalog;
//    private final String  schema;
//    private final boolean autoMapping;
//    private final boolean useDelimited;
//    private final boolean caseInsensitive;
//    private final boolean mapUnderscoreToCamelCase;
//    private final DdlAuto ddlAuto;
//    //
//    private final String  characterSet;
//    private final String  collation;
//    private final String  comment;
//    private final String  other;
//
//    TableDefaultInfo(Map<String, String> attrMaps, MappingOptions options) {
//        String catalog = attrMaps.get("catalog");
//        String schema = attrMaps.get("schema");
//        String autoMapping = attrMaps.get("autoMapping");
//        String useDelimited = attrMaps.get("useDelimited");
//        String caseInsensitive = attrMaps.get("caseInsensitive");
//        String mapUnderscoreToCamelCase = attrMaps.get("mapUnderscoreToCamelCase");
//        String characterSet = attrMaps.get("character-set");
//        String collation = attrMaps.get("collation");
//        String comment = attrMaps.get("comment");
//        String other = attrMaps.get("other");
//        String ddlAuto = attrMaps.get("ddlAuto");
//
//        this.catalog = (catalog == null) ? "" : catalog;
//        this.schema = (schema == null) ? "" : schema;
//
//        if (StringUtils.isNotBlank(autoMapping)) {
//            this.autoMapping = Boolean.parseBoolean(autoMapping);
//        } else {
//            this.autoMapping = options.getAutoMapping() == null || options.getAutoMapping();
//        }
//
//        if (StringUtils.isNotBlank(useDelimited)) {
//            this.useDelimited = Boolean.parseBoolean(useDelimited);
//        } else {
//            this.useDelimited = Boolean.TRUE.equals(options.getUseDelimited());
//        }
//
//        if (StringUtils.isNotBlank(caseInsensitive)) {
//            this.caseInsensitive = Boolean.parseBoolean(caseInsensitive);
//        } else {
//            this.caseInsensitive = options.getCaseInsensitive() == null || options.getCaseInsensitive();
//        }
//
//        if (StringUtils.isNotBlank(mapUnderscoreToCamelCase)) {
//            this.mapUnderscoreToCamelCase = Boolean.parseBoolean(mapUnderscoreToCamelCase);
//        } else {
//            this.mapUnderscoreToCamelCase = Boolean.TRUE.equals(options.getMapUnderscoreToCamelCase());
//        }
//
//        this.ddlAuto = DdlAuto.valueOfCode(ddlAuto);
//        this.characterSet = characterSet;
//        this.collation = collation;
//        this.comment = comment;
//        this.other = other;
//    }
//
//    @Override
//    public String catalog() {
//        return this.catalog;
//    }
//
//    @Override
//    public String schema() {
//        return this.schema;
//    }
//
//    @Override
//    public String value() {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public String table() {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public boolean autoMapping() {
//        return this.autoMapping;
//    }
//
//    @Override
//    public boolean useDelimited() {
//        return this.useDelimited;
//    }
//
//    @Override
//    public boolean caseInsensitive() {
//        return this.caseInsensitive;
//    }
//
//    @Override
//    public boolean mapUnderscoreToCamelCase() {
//        return this.mapUnderscoreToCamelCase;
//    }
//
//    @Override
//    public DdlAuto ddlAuto() {
//        return this.ddlAuto;
//    }
//
//    @Override
//    public Class<? extends Annotation> annotationType() {
//        return Table.class;
//    }
//
//    @Override
//    public String characterSet() {
//        return this.characterSet;
//    }
//
//    @Override
//    public String collation() {
//        return this.collation;
//    }
//
//    @Override
//    public String comment() {
//        return this.comment;
//    }
//
//    @Override
//    public String other() {
//        return this.other;
//    }
//}