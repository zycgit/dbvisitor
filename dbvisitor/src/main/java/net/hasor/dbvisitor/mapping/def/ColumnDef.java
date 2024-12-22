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
import net.hasor.cobble.reflect.Annotations;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandler;
import net.hasor.dbvisitor.types.TypeHandler;

/**
 * 字段 or 列信息
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-10-31
 */
public class ColumnDef implements ColumnMapping {
    private final String              columnName;
    private final String              propertyName;
    private final Integer             jdbcType;
    private final Class<?>            javaType;
    private final TypeHandler<?>      typeHandler;
    private final Property            handler;
    private       Annotations         annotations;
    private       GeneratedKeyHandler keySeqHolder;
    //
    private       boolean             insert;
    private       boolean             update;
    private       boolean             primary;
    private       String              selectTemplate;
    private       String              insertTemplate;
    private       String              setColTemplate;
    private       String              setValueTemplate;
    private       String              whereColTemplate;
    private       String              whereValueTemplate;
    private       String              groupByColTemplate;
    private       String              orderByColTemplate;

    private ColumnDescription description;

    public ColumnDef(String columnName, String propertyName, Integer jdbcType, Class<?> javaType,//
            TypeHandler<?> typeHandler, Property handler) {
        this.columnName = columnName;
        this.propertyName = propertyName;
        this.jdbcType = jdbcType;
        this.javaType = javaType;
        this.typeHandler = typeHandler;
        this.handler = handler;
        this.insert = true;
        this.update = true;
        this.primary = false;
    }

    @Override
    public String getColumn() {
        return this.columnName;
    }

    @Override
    public String getProperty() {
        return this.propertyName;
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
    public TypeHandler<?> getTypeHandler() {
        return this.typeHandler;
    }

    @Override
    public Property getHandler() {
        return this.handler;
    }

    @Override
    public Annotations getAnnotations() {
        return this.annotations;
    }

    public void setAnnotations(Annotations annotations) {
        this.annotations = annotations;
    }

    @Override
    public GeneratedKeyHandler getKeySeqHolder() {
        return this.keySeqHolder;
    }

    public void setKeySeqHolder(GeneratedKeyHandler keySeqHolder) {
        this.keySeqHolder = keySeqHolder;
    }

    @Override
    public boolean isInsert() {
        return this.insert;
    }

    public void setInsert(boolean insert) {
        this.insert = insert;
    }

    @Override
    public boolean isUpdate() {
        return this.update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    @Override
    public boolean isPrimaryKey() {
        return this.primary;
    }

    public void setPrimaryKey(boolean primary) {
        this.primary = primary;
    }

    @Override
    public String getSelectTemplate() {
        return this.selectTemplate;
    }

    public void setSelectTemplate(String selectTemplate) {
        this.selectTemplate = selectTemplate;
    }

    @Override
    public String getInsertTemplate() {
        return this.insertTemplate;
    }

    public void setInsertTemplate(String insertTemplate) {
        this.insertTemplate = insertTemplate;
    }

    @Override
    public String getSetColTemplate() {
        return this.setColTemplate;
    }

    public void setSetColTemplate(String setColTemplate) {
        this.setColTemplate = setColTemplate;
    }

    @Override
    public String getSetValueTemplate() {
        return this.setValueTemplate;
    }

    public void setSetValueTemplate(String setValueTemplate) {
        this.setValueTemplate = setValueTemplate;
    }

    @Override
    public String getWhereColTemplate() {
        return this.whereColTemplate;
    }

    public void setWhereColTemplate(String whereColTemplate) {
        this.whereColTemplate = whereColTemplate;
    }

    @Override
    public String getWhereValueTemplate() {
        return this.whereValueTemplate;
    }

    public void setWhereValueTemplate(String whereValueTemplate) {
        this.whereValueTemplate = whereValueTemplate;
    }

    @Override
    public String getGroupByColTemplate() {
        return this.groupByColTemplate;
    }

    public void setGroupByColTemplate(String groupByColTemplate) {
        this.groupByColTemplate = groupByColTemplate;
    }

    @Override
    public String getOrderByColTemplate() {
        return this.orderByColTemplate;
    }

    public void setOrderByColTemplate(String orderByColTemplate) {
        this.orderByColTemplate = orderByColTemplate;
    }

    @Override
    public ColumnDescription getDescription() {
        return this.description;
    }

    public void setDescription(ColumnDescription description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ColumnDef{" + "columnName='" + columnName + '\'' + ", propertyName='" + propertyName + '\'' + ", jdbcType=" + jdbcType + ", insert=" + insert + ", update=" + update + ", primary=" + primary + '}';
    }
}
