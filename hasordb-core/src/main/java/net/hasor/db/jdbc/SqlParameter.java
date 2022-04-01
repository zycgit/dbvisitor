/*
 * Copyright 2008-2009 the original author or authors.
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
package net.hasor.db.jdbc;
import net.hasor.db.types.TypeHandler;

/**
 * Object to represent an SQL parameter definition.
 *
 * <p>Parameters may be anonymous, in which case "name" is {@code null}.
 * However, all parameters must define an SQL type according to {@link java.sql.JDBCType}.
 *
 * @author 赵永春 (zyc@hasor.net)
 * @see java.sql.JDBCType
 */
public interface SqlParameter {
    /** Return the name of the parameter, or {@code null} if anonymous. */
    String getName();

    interface ValueSqlParameter extends SqlParameter {
        /** Return the SQL type of the parameter. */
        Integer getJdbcType();

        /** Return the type name of the parameter, if any. */
        String getTypeName();

        /** Return the scale of the parameter, if any. */
        Integer getScale();
    }

    interface OutSqlParameter extends ValueSqlParameter {
        TypeHandler<?> getTypeHandler();
    }

    interface InSqlParameter extends ValueSqlParameter {
        TypeHandler<?> getTypeHandler();

        Object getValue();
    }

    interface ReturnSqlParameter extends SqlParameter {
        /** Return the ResultSetExtractor held by this parameter, if any. */
        ResultSetExtractor<?> getResultSetExtractor();

        /** Return the RowCallbackHandler held by this parameter, if any. */
        RowCallbackHandler getRowCallbackHandler();

        /** Return the RowMapper held by this parameter, if any. */
        RowMapper<?> getRowMapper();
    }
}
