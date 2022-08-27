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
import net.hasor.cobble.function.Property;
import net.hasor.dbvisitor.types.TypeHandler;

/**
 * 字段 or 列信息
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public class ColumnDef implements ColumnMapping {
    private final String         columnName;
    private final String         propertyName;
    private final Integer        jdbcType;
    private final Class<?>       javaType;
    private final TypeHandler<?> typeHandler;
    private final Property       handler;
    private final boolean        insert;
    private final boolean        update;
    private final boolean        primary;
    private final String         insertTemplate;
    private final String         setTemplate;
    private final String         whereTemplate;

    public ColumnDef(String columnName, String propertyName, Integer jdbcType, Class<?> javaType,//
            TypeHandler<?> typeHandler, Property handler, //
            boolean insert, boolean update, boolean primary,//
            String insertTemplate, String setTemplate, String whereTemplate) {
        this.columnName = columnName;
        this.propertyName = propertyName;
        this.jdbcType = jdbcType;
        this.javaType = javaType;
        this.typeHandler = typeHandler;
        this.handler = handler;
        this.insert = insert;
        this.update = update;
        this.primary = primary;
        this.insertTemplate = insertTemplate;
        this.setTemplate = setTemplate;
        this.whereTemplate = whereTemplate;
    }

    @Override
    public String getColumn() {
        return this.columnName;
    }

    @Override
    public String getProperty() {
        return this.propertyName;
    }

    public TypeHandler<?> getTypeHandler() {
        return typeHandler;
    }

    public Property getHandler() {
        return this.handler;
    }

    @Override
    public Integer getJdbcType() {
        return this.jdbcType;
    }

    @Override
    public Class<?> getJavaType() {
        return this.javaType;
    }

    @Override
    public boolean isUpdate() {
        return this.update;
    }

    @Override
    public boolean isInsert() {
        return this.insert;
    }

    @Override
    public String getInsertValueTemplate() {
        return this.insertTemplate;
    }

    @Override
    public String getSetValueTemplate() {
        return this.setTemplate;
    }

    @Override
    public String getWhereValueTemplate() {
        return this.whereTemplate;
    }

    @Override
    public boolean isPrimaryKey() {
        return this.primary;
    }

    @Override
    public String toString() {
        return "ColumnDef{" + "columnName='" + columnName + '\'' + ", propertyName='" + propertyName + '\'' + ", jdbcType=" + jdbcType + ", insert=" + insert + ", update=" + update + ", primary=" + primary + '}';
    }
}
