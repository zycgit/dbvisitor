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
package net.hasor.dbvisitor.keyholder;
import net.hasor.dbvisitor.mapping.ColumnMapping;
import net.hasor.dbvisitor.mapping.TableMapping;
import net.hasor.dbvisitor.mapping.resolve.MappingOptions;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.util.Map;

/**
 * KeySeqHolderFactory 的调用参数
 * @version : 2022-12-01
 * @author 赵永春 (zyc@hasor.net)
 */
public class CreateContext {
    private final MappingOptions      options;
    private final TypeHandlerRegistry typeRegistry;
    private final TableMapping<?>     tableDef;
    private final ColumnMapping       colDef;
    private final Map<String, Object> context;

    public CreateContext(MappingOptions options, TypeHandlerRegistry typeRegistry, TableMapping<?> tableDef, ColumnMapping colDef, Map<String, Object> context) {
        this.options = options;
        this.typeRegistry = typeRegistry;
        this.tableDef = tableDef;
        this.colDef = colDef;
        this.context = context;
    }

    public MappingOptions getOptions() {
        return this.options;
    }

    public TypeHandlerRegistry getTypeRegistry() {
        return this.typeRegistry;
    }

    public String getCatalog() {
        return this.tableDef.getCatalog();
    }

    public String getSchema() {
        return this.tableDef.getSchema();
    }

    public String getTable() {
        return this.tableDef.getTable();
    }

    public String getColumn() {
        return this.colDef.getColumn();
    }

    public Class<?> getEntityType() {
        return this.tableDef.entityType();
    }

    public String getProperty() {
        return this.colDef.getProperty();
    }

    public TypeHandler<?> getTypeHandler() {
        return this.colDef.getTypeHandler();
    }

    public Integer getJdbcType() {
        return this.colDef.getJdbcType();
    }

    public Class<?> getJavaType() {
        return this.colDef.getJavaType();
    }

    public Map<String, Object> getContext() {
        return this.context;
    }
}
