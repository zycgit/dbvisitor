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
package net.hasor.dbvisitor.dynamic.args;
import java.util.Map;

/**
 * 一个 Map 到 SqlParameterSource 的桥，同时支持自动识别 Supplier 接口以获取具体参数。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2014-3-31
 */
public class MapSqlArgSource extends BasicSqlArgSource {
    public MapSqlArgSource(final Map<String, ?> values) {
        super((Map<String, Object>) values);
    }
}