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
package net.hasor.dbvisitor.faker.generator.provider.carefully;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.setting.SettingNode;
import net.hasor.dbvisitor.faker.FakerConfigEnum;
import net.hasor.dbvisitor.faker.generator.TypeSrw;
import net.hasor.dbvisitor.faker.generator.provider.DefaultTypeSrwFactory;
import net.hasor.dbvisitor.faker.meta.JdbcColumn;
import net.hasor.dbvisitor.faker.seed.bool.BooleanSeedConfig;
import net.hasor.dbvisitor.faker.seed.bool.BooleanSeedFactory;
import net.hasor.dbvisitor.faker.seed.bytes.BytesSeedConfig;
import net.hasor.dbvisitor.faker.seed.bytes.BytesSeedFactory;
import net.hasor.dbvisitor.faker.seed.date.DateSeedConfig;
import net.hasor.dbvisitor.faker.seed.date.DateSeedFactory;
import net.hasor.dbvisitor.faker.seed.date.DateType;
import net.hasor.dbvisitor.faker.seed.date.GenType;
import net.hasor.dbvisitor.faker.seed.guid.GuidSeedConfig;
import net.hasor.dbvisitor.faker.seed.guid.GuidSeedFactory;
import net.hasor.dbvisitor.faker.seed.guid.GuidType;
import net.hasor.dbvisitor.faker.seed.number.NumberSeedConfig;
import net.hasor.dbvisitor.faker.seed.number.NumberSeedFactory;
import net.hasor.dbvisitor.faker.seed.number.NumberType;
import net.hasor.dbvisitor.faker.seed.string.CharacterSet;
import net.hasor.dbvisitor.faker.seed.string.StringSeedConfig;
import net.hasor.dbvisitor.faker.seed.string.StringSeedFactory;
import net.hasor.dbvisitor.types.handler.BigDecimalTypeHandler;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Collections;
import java.util.HashSet;

/**
 * https://www.postgresql.org/docs/13/datatype.html
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public class PostgresCarefullyTypeSrwFactory extends DefaultTypeSrwFactory {
    @Override
    public TypeSrw createSeedFactory(JdbcColumn jdbcColumn, SettingNode columnConfig) {
        String columnType = jdbcColumn.getColumnType().toLowerCase();
        if (StringUtils.isBlank(columnType)) {
            return defaultSeedFactory(jdbcColumn);
        }
        boolean isArray = columnType.charAt(0) == '_';
        if (isArray) {
            //97 = "_int2,2003"
            columnType = columnType.substring(1);
            throw new UnsupportedOperationException("unsupported columnName " + jdbcColumn.getColumnName()//
                    + ", columnType '" + columnType + "'");
        }
        switch (columnType) {
            case "bool": {
                BooleanSeedFactory seedFactory = new BooleanSeedFactory();
                BooleanSeedConfig seedConfig = seedFactory.newConfig();
                return new TypeSrw(seedFactory, seedConfig, Types.BOOLEAN);
            }
            case "smallserial":
            case "int2": {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.addMinMax(new BigDecimal("0"), new BigDecimal("10000"));
                return new TypeSrw(seedFactory, seedConfig, Types.INTEGER);
            }
            case "serial":
            case "int4": {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.addMinMax(new BigDecimal("0"), new BigDecimal("1000000"));
                return new TypeSrw(seedFactory, seedConfig, Types.INTEGER);
            }
            case "oid":
            case "bigserial":
            case "int8": {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.addMinMax(new BigDecimal("0"), new BigDecimal("100000000"));
                return new TypeSrw(seedFactory, seedConfig, Types.INTEGER);
            }
            case "numeric": {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Decimal);
                if (jdbcColumn.getColumnSize() == 0 && jdbcColumn.getDecimalDigits() == null) {
                    seedConfig.setPrecision(6);
                    seedConfig.setScale(2);
                } else {
                    seedConfig.setPrecision(jdbcColumn.getColumnSize());
                    seedConfig.setScale(jdbcColumn.getDecimalDigits());
                }
                seedConfig.setAbs(true);
                return new TypeSrw(seedFactory, seedConfig, Types.DECIMAL);
            }
            case "float4":
            case "float8": {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Double);
                seedConfig.addMinMax(new BigDecimal("0.0"), new BigDecimal("9999.999"));
                seedConfig.setScale(3);
                seedConfig.setAbs(true);
                return new TypeSrw(seedFactory, seedConfig, Types.DOUBLE);
            }
            case "money": {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Decimal);
                seedConfig.addMinMax(new BigDecimal("0.0"), new BigDecimal("9999.99"));
                seedConfig.setScale(2);
                seedConfig.setAbs(true);
                seedConfig.setTypeHandler(new PostgresMoneyTypeHandler());

                columnConfig.addValue(FakerConfigEnum.INSERT_TEMPLATE.getConfigKey(), "?::numeric::money");
                columnConfig.addValue(FakerConfigEnum.WHERE_VALUE_TEMPLATE.getConfigKey(), "?::numeric::money");
                columnConfig.addValue(FakerConfigEnum.SET_VALUE_TEMPLATE.getConfigKey(), "?::numeric::money");
                return new TypeSrw(seedFactory, seedConfig, Types.DECIMAL);
            }
            case "name":
            case "bpchar":
            case "text":
            case "varchar": {
                StringSeedFactory seedFactory = new StringSeedFactory();
                StringSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(1);
                seedConfig.setMaxLength(safeMaxLength(jdbcColumn.getColumnSize(), 10, 100));
                seedConfig.setCharacterSet(new HashSet<>(Collections.singletonList(CharacterSet.LETTER_NUMBER)));
                return new TypeSrw(seedFactory, seedConfig, Types.VARCHAR);
            }
            case "uuid": {
                GuidSeedFactory seedFactory = new GuidSeedFactory();
                GuidSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(GuidType.String36);
                columnConfig.addValue(FakerConfigEnum.INSERT_TEMPLATE.getConfigKey(), "?::uuid");
                columnConfig.addValue(FakerConfigEnum.WHERE_VALUE_TEMPLATE.getConfigKey(), "?::uuid");
                columnConfig.addValue(FakerConfigEnum.SET_VALUE_TEMPLATE.getConfigKey(), "?::uuid");
                return new TypeSrw(seedFactory, seedConfig, Types.OTHER);
            }
            case "date": {
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.LocalDate);
                seedConfig.setGenType(GenType.Random);
                seedConfig.setDateFormat("yyyy-MM-dd");
                seedConfig.setRangeForm("2000-01-01");
                seedConfig.setRangeTo("2030-12-31");
                return new TypeSrw(seedFactory, seedConfig, Types.DATE);
            }
            case "time":
            case "timetz": {
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.LocalTime);
                seedConfig.setGenType(GenType.Random);
                int p = Math.max(jdbcColumn.getDecimalDigits(), 3);
                seedConfig.setDateFormat("HH:mm:ss" + "." + StringUtils.repeat("S", p));
                seedConfig.setPrecision(p);
                seedConfig.setRangeForm("00:00:00.000");
                seedConfig.setRangeTo("23:59:59.999");

                if (StringUtils.endsWith(columnType, "tz")) {
                    seedConfig.setDateType(DateType.OffsetTime);
                    seedConfig.setZoneForm("-08:00");
                    seedConfig.setZoneTo("+08:00");
                    return new TypeSrw(seedFactory, seedConfig, Types.TIME_WITH_TIMEZONE);
                } else {
                    return new TypeSrw(seedFactory, seedConfig, Types.TIME);
                }
            }
            case "interval":
            case "timestamp":
            case "timestamptz": {
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.LocalDateTime);
                seedConfig.setGenType(GenType.Random);
                int p = Math.max(jdbcColumn.getDecimalDigits(), 3);
                seedConfig.setDateFormat("yyyy-MM-dd HH:mm:ss" + "." + StringUtils.repeat("S", p));
                seedConfig.setPrecision(p);
                seedConfig.setRangeForm("2000-01-01 00:00:00.000");
                seedConfig.setRangeTo("2030-12-31 23:59:59.999");

                if (StringUtils.endsWith(columnType, "tz")) {
                    seedConfig.setDateType(DateType.OffsetDateTime);
                    seedConfig.setZoneForm("-08:00");
                    seedConfig.setZoneTo("+08:00");
                    return new TypeSrw(seedFactory, seedConfig, Types.TIMESTAMP_WITH_TIMEZONE);
                } else if (StringUtils.endsWith(columnType, "interval")) {
                    seedConfig.setDateType(DateType.ISO8601);
                    columnConfig.addValue(FakerConfigEnum.INSERT_TEMPLATE.getConfigKey(), "?::interval");
                    columnConfig.addValue(FakerConfigEnum.WHERE_VALUE_TEMPLATE.getConfigKey(), "?::interval");
                    columnConfig.addValue(FakerConfigEnum.SET_VALUE_TEMPLATE.getConfigKey(), "?::interval");
                    return new TypeSrw(seedFactory, seedConfig, Types.OTHER);
                } else {
                    return new TypeSrw(seedFactory, seedConfig, Types.TIMESTAMP);
                }
            }
            case "bit":
            case "varbit": {
                StringSeedFactory seedFactory = new StringSeedFactory();
                StringSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(1);
                seedConfig.setMaxLength(safeMaxLength(jdbcColumn.getColumnSize(), 8, 24));
                seedConfig.setCharacterSet(new HashSet<>(Collections.singletonList(CharacterSet.BIT)));

                String temp = null;
                if (StringUtils.equals(columnType, "bit")) {
                    temp = "?::bit(" + jdbcColumn.getColumnSize() + ")";
                } else {
                    temp = "?::bit varying(" + seedConfig.getMaxLength() + ")";
                }

                columnConfig.addValue(FakerConfigEnum.INSERT_TEMPLATE.getConfigKey(), temp);
                columnConfig.addValue(FakerConfigEnum.WHERE_VALUE_TEMPLATE.getConfigKey(), temp);
                columnConfig.addValue(FakerConfigEnum.SET_VALUE_TEMPLATE.getConfigKey(), temp);
                return new TypeSrw(seedFactory, seedConfig, Types.VARCHAR);
            }
            case "bytea": {
                BytesSeedFactory seedFactory = new BytesSeedFactory();
                BytesSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(1);
                seedConfig.setMaxLength(safeMaxLength(jdbcColumn.getColumnSize(), 32, 128));
                return new TypeSrw(seedFactory, seedConfig, Types.VARBINARY);
            }
            case "json":
            case "jsonb":
                //86 = "json,jsonb,1111"
            case "xml":
                //84 = "xml,2009"
            case "cidr":
            case "inet":
            case "macaddr":
            case "macaddr8":
                // ip address,1111
            case "point":
            case "line":
            case "lseg":
            case "box":
            case "path":
            case "polygon":
            case "circle":
                // gis,1111
            case "int4range":
            case "int8range":
            case "numrange":
            case "tsrange":
            case "tstzrange":
            case "daterange":
                // range,1111
            case "tsvector":
            case "tsquery":
            case "pg_lsn":
            case "txid_snapshot":
                //63 = "1111"
            default: {
                throw new UnsupportedOperationException("unsupported columnName " + jdbcColumn.getColumnName()//
                        + ", columnType '" + columnType + "'");
            }
        }
    }

    private static int safeMaxLength(Integer number, int defaultNum, int maxNum) {
        if (number == null || number < 0) {
            return defaultNum;
        } else if (number > maxNum) {
            return maxNum;
        } else {
            return number;
        }
    }

    private static class PostgresMoneyTypeHandler extends BigDecimalTypeHandler {

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

        private BigDecimal toNumber(String moneyValue) {
            String moneySign = filerMoneySign(moneyValue);
            return StringUtils.isBlank(moneySign) ? null : new BigDecimal(moneySign);
        }

        @Override
        public void setNonNullParameter(PreparedStatement ps, int i, BigDecimal parameter, Integer jdbcType) throws SQLException {
            ps.setString(i, parameter.toPlainString());
        }
    }

    private static String filerMoneySign(String mStr) {
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
}
