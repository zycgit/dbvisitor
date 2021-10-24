/*
 * Copyright 2002-2010 the original author or authors.
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
package net.hasor.db.mapping.def;
import net.hasor.cobble.function.Property;
import net.hasor.db.types.TypeHandler;

/**
 * 字段映射信息
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public interface ColumnMapping {
    /** 列名 */
    public String getColumn();

    /** 属性名 */
    public String getProperty();

    /** 对应的 javaType */
    public Class<?> getJavaType();

    /** 使用的 jdbcType,如果没有配置那么会通过 javaType 来自动推断 */
    public Integer getJdbcType();

    public TypeHandler<?> getTypeHandler();

    public Property getHandler();

    /** 是否为主键 */
    public boolean isPrimaryKey();

    /** 参与更新 */
    public boolean isUpdate();

    /** 参与新增 */
    public boolean isInsert();
}
