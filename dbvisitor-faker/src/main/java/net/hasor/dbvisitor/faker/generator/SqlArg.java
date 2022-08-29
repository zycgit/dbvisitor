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
package net.hasor.dbvisitor.faker.generator;
import net.hasor.dbvisitor.types.TypeHandler;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * 生成的数据
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class SqlArg {
    private final        String            column;
    private final        Integer           jdbcType;
    private final        TypeHandler       handler;
    private final        Object            object;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");

    public SqlArg(String column, Integer jdbcType, TypeHandler<?> handler, Object object) {
        this.column = column;
        this.jdbcType = jdbcType;
        this.handler = handler;
        this.object = object;
    }

    public String getColumn() {
        return this.column;
    }

    public Integer getJdbcType() {
        return this.jdbcType;
    }

    public TypeHandler<?> getHandler() {
        return this.handler;
    }

    public Object getObject() {
        return this.object;
    }

    @Override
    public String toString() {
        if (this.object instanceof TemporalAccessor) {
            try {
                if (this.object instanceof OffsetDateTime) {
                    LocalDateTime dateTime = ((OffsetDateTime) this.object).toLocalDateTime();
                    ZoneOffset offset = ((OffsetDateTime) this.object).getOffset();
                    return "[" + this.jdbcType + "]" + formatter.format(dateTime) + " " + offset;
                } else {
                    return "[" + this.jdbcType + "]" + formatter.format((TemporalAccessor) this.object);
                }
            } catch (Exception ignored) {
            }
        }

        return "[" + this.jdbcType + "]" + this.object;
    }

    public void setParameter(PreparedStatement ps, int i) throws SQLException {
        try {
            if (this.object == null) {
                ps.setNull(i, this.jdbcType);
            } else {
                this.handler.setParameter(ps, i, this.object, this.jdbcType);
            }
        } catch (SQLException e) {
            throw e;
        }
    }
}
