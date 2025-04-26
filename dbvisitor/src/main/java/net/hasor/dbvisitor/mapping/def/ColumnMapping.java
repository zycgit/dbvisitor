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
import net.hasor.dbvisitor.mapping.KeyType;
import net.hasor.dbvisitor.types.TypeHandler;

/**
 * 字段映射信息
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public interface ColumnMapping {
    /** 列名 */
    String getColumn();

    /** 属性名 */
    String getProperty();

    /** 对应的 javaType */
    Class<?> getJavaType();

    /** 使用的 jdbcType,如果没有配置那么会通过 javaType 来自动推断 */
    Integer getJdbcType();

    /** 获取类型处理器 */
    TypeHandler<?> getTypeHandler();

    /** 获取属性处理器 */
    Property getHandler();

    /** 获取注解信息 */
    Annotations getAnnotations();

    /** 是否为主键 */
    boolean isPrimaryKey();

    /** 参与更新 */
    boolean isUpdate();

    /** 参与新增 */
    boolean isInsert();

    /**
     * 用作 select 语句时 column name 的写法，默认是空。
     * 注意：由于语句模版会直接参与 SQL 语句生成，因此在使用语句模版时请评估注入风险。
     */
    String getSelectTemplate();

    /**
     * 用作 insert 语句时 value 的参数写法，默认是 ?
     * 注意：由于语句模版会直接参与 SQL 语句生成，因此在使用语句模版时请评估注入风险。
     */
    String getInsertTemplate();

    /**
     * 用作 update 的 set 语句时 column name 的写法，默认是空
     * 注意：由于语句模版会直接参与 SQL 语句生成，因此在使用语句模版时请评估注入风险。
     */
    String getSetColTemplate();

    /**
     * 用作 update set 语句时 value 的参数写法，默认是 ?
     * 注意：由于语句模版会直接参与 SQL 语句生成，因此在使用语句模版时请评估注入风险。
     */
    String getSetValueTemplate();

    /**
     * 用作 update/delete 的 where 语句时 column name 的写法，默认是空
     * 注意：由于语句模版会直接参与 SQL 语句生成，因此在使用语句模版时请评估注入风险。
     */
    String getWhereColTemplate();

    /**
     * 用作 update/delete 的 where 语句时 value 的参数写法，默认是 ?
     * 注意：由于语句模版会直接参与 SQL 语句生成，因此在使用语句模版时请评估注入风险。
     */
    String getWhereValueTemplate();

    /**
     * 用作 group by 时 column name 的写法，默认是空
     * 注意：由于语句模版会直接参与 SQL 语句生成，因此在使用语句模版时请评估注入风险。
     */
    String getGroupByColTemplate();

    /**
     * 用作 order by 时 column name 的写法，默认是空
     * 注意：由于语句模版会直接参与 SQL 语句生成，因此在使用语句模版时请评估注入风险。
     */
    String getOrderByColTemplate();

    /** 获取主键生成策略类型 */
    KeyType getKeyTpe();

    /** 获取主键生成处理器实例 */
    GeneratedKeyHandler getKeySeqHolder();

    /** 获取补充描述信息 */
    ColumnDescription getDescription();
}
