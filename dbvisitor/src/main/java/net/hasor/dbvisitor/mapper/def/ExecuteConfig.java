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
import net.hasor.cobble.ArrayUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dynamic.logic.ArrayDynamicSql;

import java.util.function.Function;

/**
 * Execute SqlConfig
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-06-19
 */
public class ExecuteConfig extends SqlConfig {
    private String   resultType = null;
    private String[] bindOut    = ArrayUtils.EMPTY_STRING_ARRAY;

    public ExecuteConfig(ArrayDynamicSql target, Function<String, String> config) {
        super(target, config);

        if (config != null) {
            this.resultType = config.apply(RESULT_TYPE);
            this.bindOut = config.andThen(s -> StringUtils.isNotBlank(s) ? s.split(",") : ArrayUtils.EMPTY_STRING_ARRAY).apply(BIND_OUT);
        }
    }

    @Override
    public QueryType getType() {
        return QueryType.Execute;
    }

    public String getResultType() {
        return this.resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String[] getBindOut() {
        return this.bindOut;
    }

    public void setBindOut(String[] bindOut) {
        this.bindOut = bindOut;
    }
}