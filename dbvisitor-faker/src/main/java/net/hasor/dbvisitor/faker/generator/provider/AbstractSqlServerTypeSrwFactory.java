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
package net.hasor.dbvisitor.faker.generator.provider;
import net.hasor.dbvisitor.types.handler.BigDecimalTypeHandler;
import net.hasor.dbvisitor.types.handler.OffsetDateTimeForSqlTypeHandler;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * https://docs.microsoft.com/zh-cn/sql/t-sql/data-types/data-types-transact-sql
 * https://docs.microsoft.com/zh-cn/sql/t-sql/data-types/time-transact-sql
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public class AbstractSqlServerTypeSrwFactory extends DefaultTypeSrwFactory {

    protected static int safeMaxLength(Integer number, int defaultNum, int maxNum) {
        if (number == null || number < 0) {
            return defaultNum;
        } else if (number > maxNum) {
            return maxNum;
        } else {
            return number;
        }
    }

    public static class SqlServerOffsetDateTimeTypeHandler extends OffsetDateTimeForSqlTypeHandler {

        private static final boolean enableDriverType;

        static {
            boolean foundDriverType = false;
            try {
                foundDriverType = SqlServerOffsetDateTimeTypeHandler.class.getClassLoader().loadClass("microsoft.sql.DateTimeOffset") != null;
            } catch (ClassNotFoundException ignored) {
            } finally {
                enableDriverType = foundDriverType;
            }
        }

        @Override
        public void setNonNullParameter(PreparedStatement ps, int i, OffsetDateTime parameter, Integer jdbcType) throws SQLException {
            if (!enableDriverType) {
                ps.setObject(i, parameter.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
                super.setNonNullParameter(ps, i, parameter, jdbcType);
            } else {
                Timestamp timestamp = new Timestamp(parameter.toInstant().toEpochMilli());
                int minutesOffset = parameter.getOffset().getTotalSeconds() / 60;
                microsoft.sql.DateTimeOffset offset = microsoft.sql.DateTimeOffset.valueOf(timestamp, minutesOffset);
                ps.setObject(i, offset);
            }
        }
    }

    public static class SqlServerBigDecimalAsStringTypeHandler extends BigDecimalTypeHandler {

        @Override
        public void setNonNullParameter(PreparedStatement ps, int i, BigDecimal parameter, Integer jdbcType) throws SQLException {
            ps.setString(i, parameter.toPlainString());
        }
    }
}
