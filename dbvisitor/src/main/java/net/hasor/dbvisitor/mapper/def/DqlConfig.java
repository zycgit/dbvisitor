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
import net.hasor.dbvisitor.mapper.ResultSetType;

import java.util.function.Function;

/**
 * has result query.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-06-19
 */
public abstract class DqlConfig extends SqlConfig {
    private int           fetchSize          = 256;
    private ResultSetType resultSetType      = ResultSetType.DEFAULT;
    private String        resultMapSpace     = null;
    private String        resultMapId        = null;
    private String        resultType         = null;
    private String        resultSetExtractor = null;
    private String        resultRowCallback  = null;
    private String        resultRowMapper    = null;
    private String[]      bindOut            = ArrayUtils.EMPTY_STRING_ARRAY;

    public DqlConfig(ArrayDynamicSql target, Function<String, String> config) {
        super(target, config);

        if (config != null) {
            this.fetchSize = Integer.parseInt(config.andThen(s -> StringUtils.isBlank(s) ? "256" : s).apply(FETCH_SIZE));
            this.resultSetType = config.andThen(s -> ResultSetType.valueOfCode(s, ResultSetType.DEFAULT)).apply(RESULT_SET_TYPE);
            this.resultMapSpace = config.apply(RESULT_MAP_SPACE);
            this.resultMapId = config.apply(RESULT_MAP_ID);
            this.resultType = config.apply(RESULT_TYPE);
            this.resultSetExtractor = config.apply(RESULT_SET_EXTRACTOR);
            this.resultRowCallback = config.apply(RESULT_ROW_CALLBACK);
            this.resultRowMapper = config.apply(RESULT_ROW_MAPPER);
            this.bindOut = config.andThen(s -> StringUtils.isNotBlank(s) ? s.split(",") : ArrayUtils.EMPTY_STRING_ARRAY).apply(BIND_OUT);
        }
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

    public String getResultMapSpace() {
        return this.resultMapSpace;
    }

    public void setResultMapSpace(String resultMapSpace) {
        this.resultMapSpace = resultMapSpace;
    }

    public String getResultMapId() {
        return this.resultMapId;
    }

    public void setResultMapId(String resultMapId) {
        this.resultMapId = resultMapId;
    }

    public String getResultType() {
        return this.resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String getResultSetExtractor() {
        return this.resultSetExtractor;
    }

    public void setResultSetExtractor(String resultSetExtractor) {
        this.resultSetExtractor = resultSetExtractor;
    }

    public String getResultRowCallback() {
        return this.resultRowCallback;
    }

    public void setResultRowCallback(String resultRowCallback) {
        this.resultRowCallback = resultRowCallback;
    }

    public String getResultRowMapper() {
        return this.resultRowMapper;
    }

    public void setResultRowMapper(String resultRowMapper) {
        this.resultRowMapper = resultRowMapper;
    }

    public String[] getBindOut() {
        return this.bindOut;
    }

    public void setBindOut(String[] bindOut) {
        this.bindOut = bindOut;
    }
}
