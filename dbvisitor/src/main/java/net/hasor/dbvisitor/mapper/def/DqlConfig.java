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
import net.hasor.dbvisitor.dynamic.DynamicSql;
import net.hasor.dbvisitor.mapper.ResultSetType;

import java.util.function.Function;

/**
 * has result query.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-06-19
 */
public abstract class DqlConfig extends SqlConfig {
    private String        resultMap     = null;
    private String        resultType    = null;
    private int           fetchSize     = 256;
    private ResultSetType resultSetType = ResultSetType.DEFAULT;
    private String[]      bindOut       = ArrayUtils.EMPTY_STRING_ARRAY;

    public DqlConfig(DynamicSql target, Function<String, String> config) {
        super(target, config);

        if (config != null) {
            this.resultMap = config.apply(RESULT_MAP);
            this.resultType = config.apply(RESULT_TYPE);
            this.fetchSize = Integer.parseInt(config.andThen(s -> StringUtils.isBlank(s) ? "256" : s).apply(FETCH_SIZE));
            this.resultSetType = config.andThen(s -> ResultSetType.valueOfCode(s, ResultSetType.DEFAULT)).apply(RESULT_SET_TYPE);
            this.bindOut = config.andThen(s -> StringUtils.isNotBlank(s) ? s.split(",") : ArrayUtils.EMPTY_STRING_ARRAY).apply(BIND_OUT);
        }
    }

    public String getResultMap() {
        return this.resultMap;
    }

    public void setResultMap(String resultMap) {
        this.resultMap = resultMap;
    }

    public String getResultType() {
        return this.resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public int getFetchSize() {
        return this.fetchSize;
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    public ResultSetType getResultSetType() {
        return this.resultSetType;
    }

    public void setResultSetType(ResultSetType resultSetType) {
        this.resultSetType = resultSetType;
    }

    public String[] getBindOut() {
        return this.bindOut;
    }

    public void setBindOut(String[] bindOut) {
        this.bindOut = bindOut;
    }
}
