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
package net.hasor.dbvisitor.template.jdbc.extractor;
import net.hasor.dbvisitor.template.ResultSetExtractor;
import net.hasor.dbvisitor.template.jdbc.mapper.ColumnMapRowMapper;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.util.Map;

/**
 * {@link ResultSetExtractor} 接口实现类，该类会将结果集中的每一行进行处理，并返回一个 List 用 Map 封装结果。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2016年1月11日
 */
public class ColumnMapResultSetExtractor extends RowMapperResultSetExtractor<Map<String, Object>> {
    /** 创建 {@link ColumnMapResultSetExtractor} 对象 */
    public ColumnMapResultSetExtractor() {
        this(0, TypeHandlerRegistry.DEFAULT, true);
    }

    /**
     * 创建 {@link ColumnMapResultSetExtractor} 对象
     * @param rowsExpected 预期结果集大小（实际得到的结果集条目不受此参数限制）。
     */
    public ColumnMapResultSetExtractor(int rowsExpected) {
        this(rowsExpected, TypeHandlerRegistry.DEFAULT, true);
    }

    /**
     * 创建 {@link ColumnMapResultSetExtractor} 对象
     * @param rowsExpected 预期结果集大小（实际得到的结果集条目不受此参数限制）。
     * @param typeHandler
     */
    public ColumnMapResultSetExtractor(int rowsExpected, TypeHandlerRegistry typeHandler) {
        this(rowsExpected, typeHandler, true);
    }

    /**
     * 创建 {@link ColumnMapResultSetExtractor} 对象
     * @param rowsExpected 预期结果集大小（实际得到的结果集条目不受此参数限制）。
     * @param typeHandler
     */
    public ColumnMapResultSetExtractor(int rowsExpected, TypeHandlerRegistry typeHandler, boolean caseInsensitive) {
        super(new ColumnMapRowMapper(caseInsensitive, typeHandler), rowsExpected);
    }
}
