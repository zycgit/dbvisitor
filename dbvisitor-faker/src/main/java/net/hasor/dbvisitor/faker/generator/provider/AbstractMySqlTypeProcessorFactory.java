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
import net.hasor.dbvisitor.types.handler.StringTypeHandler;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * https://dev.mysql.com/doc/refman/5.7/en/numeric-type-syntax.html
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public class AbstractMySqlTypeProcessorFactory extends DefaultTypeProcessorFactory {
    protected static int safeMaxLength(Integer number, int defaultNum, int maxNum) {
        if (number == null || number < 0) {
            return defaultNum;
        } else if (number > maxNum) {
            return maxNum;
        } else {
            return number;
        }
    }

    public static class MySqlBigDecimalAsStringTypeHandler extends BigDecimalTypeHandler {
        @Override
        public void setNonNullParameter(PreparedStatement ps, int i, BigDecimal parameter, Integer jdbcType) throws SQLException {
            ps.setString(i, parameter.toPlainString());
        }
    }

    public static class MySqlBitAsStringTypeHandler extends StringTypeHandler {
        @Override
        public void setNonNullParameter(PreparedStatement ps, int i, String parameter, Integer jdbcType) throws SQLException {
            ps.setInt(i, Integer.parseInt(parameter, 2));
        }
    }
}