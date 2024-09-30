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
package net.hasor.dbvisitor.jdbc.callback;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.RowCallbackHandler;
import net.hasor.dbvisitor.jdbc.RowMapper;

public class ProcedureArg {
    public static final String                CFG_KEY_NAME      = "name";
    public static final String                CFG_KEY_JAVA_TYPE = "javaType";
    public static final String                CFG_KEY_MAPPER    = "mapper";
    public static final String                CFG_KEY_HANDLER   = "handler";
    public static final String                CFG_KEY_EXTRACTOR = "extractor";
    private             String                name;
    private             Class<?>              javaType;
    private             RowMapper<?>          rowMapper;
    private             RowCallbackHandler    handler;
    private             ResultSetExtractor<?> extractor;

    public ProcedureArg(String name) {
        this.name = name;
    }

    public ProcedureArg(String name, Class<?> javaType, RowMapper<?> rowMapper, RowCallbackHandler handler, ResultSetExtractor<?> extractor) {
        this.name = name;
        this.javaType = javaType;
        this.rowMapper = rowMapper;
        this.handler = handler;
        this.extractor = extractor;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getJavaType() {
        return this.javaType;
    }

    public void setJavaType(Class<?> javaType) {
        this.javaType = javaType;
    }

    public RowMapper<?> getRowMapper() {
        return this.rowMapper;
    }

    public void setRowMapper(RowMapper<?> rowMapper) {
        this.rowMapper = rowMapper;
    }

    public RowCallbackHandler getHandler() {
        return this.handler;
    }

    public void setHandler(RowCallbackHandler handler) {
        this.handler = handler;
    }

    public ResultSetExtractor<?> getExtractor() {
        return this.extractor;
    }

    public void setExtractor(ResultSetExtractor<?> extractor) {
        this.extractor = extractor;
    }
}
