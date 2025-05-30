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
package net.hasor.dbvisitor.lambda.segment;
import net.hasor.dbvisitor.dialect.SqlDialect;

import java.sql.SQLException;

/**
 * SQL 片段。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-11-02
 */
@FunctionalInterface
public interface Segment {
    /** 获取 SQL 内容 */
    String getSqlSegment(SqlDialect dialect) throws SQLException;
}
