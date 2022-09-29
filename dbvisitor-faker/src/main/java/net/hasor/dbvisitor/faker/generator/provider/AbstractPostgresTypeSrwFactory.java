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
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.codec.HexadecimalUtils;
import net.hasor.cobble.setting.SettingNode;
import net.hasor.dbvisitor.faker.generator.TypeSrw;
import net.hasor.dbvisitor.faker.seed.SeedConfig;
import net.hasor.dbvisitor.faker.seed.SeedFactory;
import net.hasor.dbvisitor.faker.seed.array.ArraySeedConfig;
import net.hasor.dbvisitor.faker.seed.array.ArraySeedFactory;
import net.hasor.dbvisitor.types.handler.ArrayTypeHandler;
import net.hasor.dbvisitor.types.handler.BigDecimalTypeHandler;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKBReader;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * https://www.postgresql.org/docs/13/datatype.html
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public class AbstractPostgresTypeSrwFactory extends DefaultTypeSrwFactory {
    protected static TypeSrw finalSrw(SeedFactory<? extends SeedConfig> seedFactory, SeedConfig seedConfig, Integer jdbcType, //
            boolean isArray, SettingNode columnConfig, String elementType) {
        if (!isArray) {
            return new TypeSrw(seedFactory, seedConfig, jdbcType);
        }

        ArraySeedFactory arrayFactory = new ArraySeedFactory(seedFactory);
        ArraySeedConfig arrayConfig = new ArraySeedConfig(seedConfig);
        arrayConfig.setMinSize(0);
        arrayConfig.setMaxSize(10);

        switch (elementType) {
            case "money":
                arrayConfig.setTypeHandler(new PostgresArrayTypeHandler("money", rs -> toNumber(rs.getString("VALUE"))));
                break;
            case "bit":
                arrayConfig.setTypeHandler(new PostgresArrayTypeHandler("bit", rs -> rs.getString("VALUE")));
                break;
            case "varbit":
                arrayConfig.setTypeHandler(new PostgresArrayTypeHandler("varbit", rs -> rs.getString("VALUE")));
                break;
            case "bytea":
                arrayConfig.setTypeHandler(new PostgresArrayTypeHandler("bytea", rs -> rs.getBytes("VALUE")));
                break;
            case "geometry":
                arrayConfig.setTypeHandler(new PostgresArrayTypeHandler(elementType, rs -> geometryString(rs.getString("VALUE"))));
                break;
            default:
                arrayConfig.setTypeHandler(new PostgresArrayTypeHandler(elementType, rs -> rs.getObject("VALUE")));
                break;
        }
        return new TypeSrw(arrayFactory, arrayConfig, Types.ARRAY);
    }

    protected static int safeMaxLength(Integer number, int defaultNum, int maxNum) {
        if (number == null || number < 0) {
            return defaultNum;
        } else if (number > maxNum) {
            return maxNum;
        } else {
            return number;
        }
    }

    protected static String fmtType(boolean isArray, String type) {
        return isArray ? (type + "[]") : type;
    }

    protected static String filerMoneySign(String mStr) {
        if (StringUtils.isBlank(mStr)) {
            return null;
        }
        char[] chars = mStr.toCharArray();
        int index = -1;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (Character.isDigit(c)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            return StringUtils.replace(mStr, ",", "");
        } else {
            return StringUtils.replace(mStr.substring(index), ",", "");
        }
    }

    protected static BigDecimal toNumber(String moneyValue) {
        String moneySign = filerMoneySign(moneyValue);
        return StringUtils.isBlank(moneySign) ? null : new BigDecimal(moneySign);
    }

    private static final GeometryFactory factory = new GeometryFactory();

    protected static String geometryString(String geometryString) {
        try {
            byte[] geometryBytes = HexadecimalUtils.hex2bytes(geometryString);
            Geometry object = new WKBReader(factory).read(geometryBytes);
            return object.toText();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return geometryString;
        }
    }

    public static class PostgresMoneyTypeHandler extends BigDecimalTypeHandler {

        @Override
        public BigDecimal getNullableResult(ResultSet rs, String columnName) throws SQLException {
            return toNumber(rs.getString(columnName));
        }

        @Override
        public BigDecimal getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
            return toNumber(rs.getString(columnIndex));
        }

        @Override
        public BigDecimal getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
            return toNumber(cs.getString(columnIndex));
        }

        @Override
        public void setNonNullParameter(PreparedStatement ps, int i, BigDecimal parameter, Integer jdbcType) throws SQLException {
            ps.setString(i, parameter.toPlainString());
        }
    }

    public static class PostgresArrayTypeHandler extends ArrayTypeHandler {
        private final String                   typeName;
        private final PostgresReadArrayHandler readArrayHandler;

        public PostgresArrayTypeHandler(String typeName, PostgresReadArrayHandler readArrayHandler) {
            this.typeName = typeName;
            this.readArrayHandler = readArrayHandler;
        }

        protected Object[] objects(Object parameter) {
            Object[] oriData = (Object[]) parameter;

            List<Object> copy = new ArrayList<>();
            for (Object oriDatum : oriData) {
                if (this.typeName.equals("bytea")) {
                    copy.add(HexadecimalUtils.bytes2hex((byte[]) oriDatum));
                } else {
                    copy.add(oriDatum);
                }
            }
            return copy.toArray();
        }

        @Override
        public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, Integer jdbcType) throws SQLException {
            if (parameter instanceof Array) {
                ps.setArray(i, (Array) parameter);// it's the user's responsibility to properly free() the Array instance
            } else {
                Array array = ps.getConnection().createArrayOf(this.typeName, objects(parameter));
                ps.setArray(i, array);
                array.free();
            }
        }

        protected Object extractArray(Array array) throws SQLException {
            if (array == null) {
                return null;
            }
            List<Object> data = new ArrayList<>();
            try (ResultSet rs = array.getResultSet()) {
                while (rs.next()) {
                    if (readArrayHandler == null) {
                        data.add(rs.getObject("VALUE"));
                    } else {
                        data.add(readArrayHandler.readElement(rs));
                    }
                }
                array.free();
                return data.toArray();
            }
        }
    }

    public static interface PostgresReadArrayHandler {
        Object readElement(ResultSet rs) throws SQLException;
    }
}
