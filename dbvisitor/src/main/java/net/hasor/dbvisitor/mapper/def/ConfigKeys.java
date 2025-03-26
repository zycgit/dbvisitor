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
 * Segment SqlConfig
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-19
 */
public interface ConfigKeys {
    // COMMON
    String STATEMENT_TYPE       = "statementType";
    String TIMEOUT              = "timeout";
    // DQL
    String FETCH_SIZE           = "fetchSize";
    String RESULT_SET_TYPE      = "resultSetType";
    String RESULT_MAP_SPACE     = "resultMapSpace"; // inner config
    String RESULT_MAP_ID        = "resultMap";
    String RESULT_TYPE          = "resultType";
    String RESULT_SET_EXTRACTOR = "resultSetExtractor";
    String RESULT_ROW_CALLBACK  = "resultRowCallback";
    String RESULT_ROW_MAPPER    = "resultRowMapper";
    String BIND_OUT             = "bindOut";
    // INSERT
    String KEY_GENERATED        = "useGeneratedKeys";
    String KEY_PROPERTY         = "keyProperty";
    String KEY_COLUMN           = "keyColumn";
    // SELECT KEY
    String ORDER                = "order";
}