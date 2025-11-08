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
package net.hasor.dbvisitor.mapper.def;
/**
 * SQL配置键常量接口，定义了Mapper配置中使用的各种键名
 * 这些常量用于统一管理SQL语句的配置属性
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-19
 */
public interface ConfigKeys {
    // 通用配置键
    String STATEMENT_TYPE = "statementType";            // SQL语句类型
    String TIMEOUT        = "timeout";                  // 执行超时时间

    // 查询(DQL)相关配置键
    String FETCH_SIZE           = "fetchSize";          // 获取记录数
    String RESULT_SET_TYPE      = "resultSetType";      // 结果集类型
    String RESULT_MAP_SPACE     = "resultMapSpace";     // 结果映射空间(内部配置)
    String RESULT_MAP_ID        = "resultMap";          // 结果映射ID
    String RESULT_TYPE          = "resultType";         // 返回结果类型
    String RESULT_SET_EXTRACTOR = "resultSetExtractor"; // 结果集提取器
    String RESULT_ROW_CALLBACK  = "resultRowCallback";  // 行回调处理器
    String RESULT_ROW_MAPPER    = "resultRowMapper";    // 行映射器
    String RESULT_TYPE_HANDLER  = "resultTypeHandler";  // 结果类型处理器
    String BIND_OUT             = "bindOut";            // 输出参数绑定

    // 插入(INSERT)相关配置键
    String KEY_GENERATED = "useGeneratedKeys";          // 是否使用生成键
    String KEY_PROPERTY  = "keyProperty";               // 键属性名
    String KEY_COLUMN    = "keyColumn";                 // 键列名

    // SELECT KEY相关配置键
    String ORDER = "order";                             // 执行顺序
}