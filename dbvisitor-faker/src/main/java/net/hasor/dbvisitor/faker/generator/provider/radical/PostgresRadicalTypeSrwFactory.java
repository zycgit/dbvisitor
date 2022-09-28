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
package net.hasor.dbvisitor.faker.generator.provider.radical;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.setting.SettingNode;
import net.hasor.dbvisitor.faker.FakerConfigEnum;
import net.hasor.dbvisitor.faker.generator.TypeSrw;
import net.hasor.dbvisitor.faker.generator.UseFor;
import net.hasor.dbvisitor.faker.generator.provider.AbstractPostgresTypeSrwFactory;
import net.hasor.dbvisitor.faker.meta.JdbcColumn;
import net.hasor.dbvisitor.faker.seed.bool.BooleanSeedConfig;
import net.hasor.dbvisitor.faker.seed.bool.BooleanSeedFactory;
import net.hasor.dbvisitor.faker.seed.bytes.BytesSeedConfig;
import net.hasor.dbvisitor.faker.seed.bytes.BytesSeedFactory;
import net.hasor.dbvisitor.faker.seed.date.DateSeedConfig;
import net.hasor.dbvisitor.faker.seed.date.DateSeedFactory;
import net.hasor.dbvisitor.faker.seed.date.DateType;
import net.hasor.dbvisitor.faker.seed.date.GenType;
import net.hasor.dbvisitor.faker.seed.geometry.FormatType;
import net.hasor.dbvisitor.faker.seed.geometry.GeometrySeedConfig;
import net.hasor.dbvisitor.faker.seed.geometry.GeometrySeedFactory;
import net.hasor.dbvisitor.faker.seed.geometry.GeometryType;
import net.hasor.dbvisitor.faker.seed.guid.GuidSeedConfig;
import net.hasor.dbvisitor.faker.seed.guid.GuidSeedFactory;
import net.hasor.dbvisitor.faker.seed.guid.GuidType;
import net.hasor.dbvisitor.faker.seed.number.NumberSeedConfig;
import net.hasor.dbvisitor.faker.seed.number.NumberSeedFactory;
import net.hasor.dbvisitor.faker.seed.number.NumberType;
import net.hasor.dbvisitor.faker.seed.string.CharacterSet;
import net.hasor.dbvisitor.faker.seed.string.StringSeedConfig;
import net.hasor.dbvisitor.faker.seed.string.StringSeedFactory;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Collections;
import java.util.HashSet;

/**
 * https://www.postgresql.org/docs/13/datatype.html
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public class PostgresRadicalTypeSrwFactory extends AbstractPostgresTypeSrwFactory {
    @Override
    public TypeSrw createSeedFactory(JdbcColumn jdbcColumn, SettingNode columnConfig) {
        String columnType = jdbcColumn.getColumnType();
        if (StringUtils.isBlank(columnType)) {
            return defaultSeedFactory(jdbcColumn);
        }

        boolean isArray = columnType.charAt(0) == '_';
        if (isArray) {
            columnType = columnType.substring(1);
        }

        switch (columnType.toLowerCase()) {
            case "bool": {
                BooleanSeedFactory seedFactory = new BooleanSeedFactory();
                BooleanSeedConfig seedConfig = seedFactory.newConfig();
                return finalSrw(seedFactory, seedConfig, Types.BOOLEAN, isArray, columnConfig, columnType);
            }
            case "smallserial": {
                // 0 to 32767
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.addMinMax(new BigDecimal("0"), new BigDecimal("32767"));
                return finalSrw(seedFactory, seedConfig, Types.INTEGER, isArray, columnConfig, columnType);
            }
            case "serial": {
                // 0 to 2147483647
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.addMinMax(new BigDecimal("0"), new BigDecimal("2147483647"));
                return finalSrw(seedFactory, seedConfig, Types.INTEGER, isArray, columnConfig, columnType);
            }
            case "bigserial": {
                // 0 to 9223372036854775807
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Long);
                seedConfig.addMinMax(new BigDecimal("0"), new BigDecimal("9223372036854775807"));
                return finalSrw(seedFactory, seedConfig, Types.BIGINT, isArray, columnConfig, columnType);
            }
            case "int2": {
                // -32768 to +32767
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.addMinMax(new BigDecimal("-32768"), new BigDecimal("+32767"));
                return finalSrw(seedFactory, seedConfig, Types.INTEGER, isArray, columnConfig, columnType);
            }
            case "oid": {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Long);
                seedConfig.addMinMax(new BigDecimal("0"), new BigDecimal("100000000"));
                return finalSrw(seedFactory, seedConfig, Types.BIGINT, isArray, columnConfig, columnType);
            }
            case "int4": {
                // -2147483648 to +2147483647
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.addMinMax(new BigDecimal("-2147483648"), new BigDecimal("+2147483647"));
                return finalSrw(seedFactory, seedConfig, Types.INTEGER, isArray, columnConfig, columnType);
            }
            case "int8": {
                // -9223372036854775808 to +9223372036854775807
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Long);
                seedConfig.addMinMax(new BigDecimal("-9223372036854775808"), new BigDecimal("+9223372036854775807"));
                return finalSrw(seedFactory, seedConfig, Types.BIGINT, isArray, columnConfig, columnType);
            }
            case "numeric": {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Decimal);
                if (jdbcColumn.getColumnSize() == 0 && jdbcColumn.getDecimalDigits() == null) {
                    seedConfig.setPrecision(64); // 0 的情况下 Faker 不能正常工作，PG 允许值最大 1000 位，这里取一个较小值
                    seedConfig.setScale(12);
                } else if (isArray && jdbcColumn.getColumnSize() > 300) {
                    seedConfig.setPrecision(64); // 0 的情况下 Faker 不能正常工作，PG 允许值最大 1000 位，这里取一个较小值
                    seedConfig.setScale(12);
                } else {
                    seedConfig.setPrecision(jdbcColumn.getColumnSize());
                    seedConfig.setScale(jdbcColumn.getDecimalDigits());
                }
                seedConfig.setAbs(true);
                return finalSrw(seedFactory, seedConfig, Types.DECIMAL, isArray, columnConfig, columnType);
            }
            case "float4": {
                // 1E-37 至 1E+37, 精度 6 位小数
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Double);
                seedConfig.addMinMax(10, new BigDecimal("+1e-37"), new BigDecimal("+1e+37"));
                seedConfig.addMinMax(10, new BigDecimal("-1e-37"), new BigDecimal("-1e+37"));
                seedConfig.addMinMax(30, new BigDecimal("-999999999.999999999"), new BigDecimal("+999999999.999999999"));
                seedConfig.addMinMax(30, new BigDecimal("-0.999999999"), new BigDecimal("+0.999999999"));
                return finalSrw(seedFactory, seedConfig, Types.DOUBLE, isArray, columnConfig, columnType);
            }
            case "float8": {
                // 1E-307 至 1E+308, 精度 15位小数
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Double);
                seedConfig.addMinMax(10, new BigDecimal("+1e-307"), new BigDecimal("+1e+308"));
                seedConfig.addMinMax(10, new BigDecimal("-1e-307"), new BigDecimal("-1e-308"));
                seedConfig.addMinMax(30, new BigDecimal("-999999999.999999999"), new BigDecimal("+999999999.999999999"));
                seedConfig.addMinMax(30, new BigDecimal("-0.999999999"), new BigDecimal("+0.999999999"));
                return finalSrw(seedFactory, seedConfig, Types.DOUBLE, isArray, columnConfig, columnType);
            }
            case "money": {
                // -92233720368547758.08 to +92233720368547758.07
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Decimal);
                seedConfig.addMinMax(new BigDecimal("-92233720368547758.08"), new BigDecimal("+92233720368547758.07"));
                seedConfig.setScale(2);
                seedConfig.setTypeHandler(new PostgresMoneyTypeHandler());

                String fmtType = "?::" + fmtType(isArray, "numeric") + "::" + fmtType(isArray, "money");
                columnConfig.addValue(FakerConfigEnum.INSERT_TEMPLATE.getConfigKey(), fmtType);
                columnConfig.addValue(FakerConfigEnum.WHERE_VALUE_TEMPLATE.getConfigKey(), fmtType);
                columnConfig.addValue(FakerConfigEnum.SET_VALUE_TEMPLATE.getConfigKey(), fmtType);
                return finalSrw(seedFactory, seedConfig, Types.DECIMAL, isArray, columnConfig, columnType);
            }
            case "bpchar": {
                StringSeedFactory seedFactory = new StringSeedFactory();
                StringSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(1);
                seedConfig.setMaxLength(safeMaxLength(jdbcColumn.getColumnSize(), 100, 1000));
                seedConfig.setCharacterSet(new HashSet<>(Collections.singletonList(CharacterSet.LETTER_NUMBER)));
                return finalSrw(seedFactory, seedConfig, Types.VARCHAR, isArray, columnConfig, columnType);
            }
            case "text":
            case "varchar": {
                StringSeedFactory seedFactory = new StringSeedFactory();
                StringSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(0);
                seedConfig.setMaxLength(safeMaxLength(jdbcColumn.getColumnSize(), 100, 1000));
                seedConfig.setCharacterSet(new HashSet<>(Collections.singletonList(CharacterSet.LETTER_NUMBER)));
                return finalSrw(seedFactory, seedConfig, Types.VARCHAR, isArray, columnConfig, columnType);
            }
            case "name": {
                StringSeedFactory seedFactory = new StringSeedFactory();
                StringSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(1);
                seedConfig.setMaxLength(safeMaxLength(jdbcColumn.getColumnSize(), 10, 100));
                seedConfig.setCharacterSet(new HashSet<>(Collections.singletonList(CharacterSet.LETTER_NUMBER)));
                return finalSrw(seedFactory, seedConfig, Types.VARCHAR, isArray, columnConfig, columnType);
            }
            case "uuid": {
                GuidSeedFactory seedFactory = new GuidSeedFactory();
                GuidSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(GuidType.String36);

                String fmtType = "?::" + fmtType(isArray, "uuid");
                columnConfig.addValue(FakerConfigEnum.INSERT_TEMPLATE.getConfigKey(), fmtType);
                columnConfig.addValue(FakerConfigEnum.WHERE_VALUE_TEMPLATE.getConfigKey(), fmtType);
                columnConfig.addValue(FakerConfigEnum.SET_VALUE_TEMPLATE.getConfigKey(), fmtType);
                return finalSrw(seedFactory, seedConfig, Types.OTHER, isArray, columnConfig, columnType);
            }
            case "date": {
                // 4713BC to 5874897AD ,1 day
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.LocalDate);
                seedConfig.setGenType(GenType.Random);
                seedConfig.setDateFormat("yyyy-MM-dd");
                seedConfig.setRangeForm("0000-01-01"); // max is   -4713-01-01
                seedConfig.setRangeTo("9999-12-31"); // max is 5874897-12-31
                return finalSrw(seedFactory, seedConfig, Types.DATE, isArray, columnConfig, columnType);
            }
            case "time":
            case "timetz": {
                // 00:00:00.000000-15:59 to 23:59:59.999999+15:59
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.LocalTime);
                seedConfig.setGenType(GenType.Random);
                int p = safeMaxLength(jdbcColumn.getDecimalDigits(), 3, 6);
                seedConfig.setDateFormat("HH:mm:ss" + ((p > 0) ? ("." + StringUtils.repeat("S", p)) : ""));
                seedConfig.setPrecision(p);
                seedConfig.setRangeForm("00:00:00.000000");
                seedConfig.setRangeTo("23:59:59.999999");

                if (StringUtils.endsWith(columnType, "tz")) {
                    seedConfig.setDateType(DateType.OffsetTime);
                    seedConfig.setZoneForm("-14:00");
                    seedConfig.setZoneTo("+14:00");
                    return finalSrw(seedFactory, seedConfig, Types.TIME_WITH_TIMEZONE, isArray, columnConfig, columnType);
                } else {
                    return finalSrw(seedFactory, seedConfig, Types.TIME, isArray, columnConfig, columnType);
                }
            }
            case "interval": {
                // -178000000 years	178000000 years	1 microsecond
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.ISO8601);
                seedConfig.setGenType(GenType.Random);
                int p = safeMaxLength(jdbcColumn.getDecimalDigits(), 3, 6);
                seedConfig.setDateFormat("yyyy-MM-dd HH:mm:ss" + ((p > 0) ? ("." + StringUtils.repeat("S", p)) : ""));
                seedConfig.setPrecision(Math.max(p, 0));
                seedConfig.setRangeForm("0000-01-01 00:00:00.000000"); // max is   -4713-01-01 00:00:00.000000
                seedConfig.setRangeTo("9999-12-31 23:59:59.999999"); // max is  294276-12-31 23:59:59.999999

                String fmtType = "?::" + fmtType(isArray, "interval");
                columnConfig.addValue(FakerConfigEnum.INSERT_TEMPLATE.getConfigKey(), fmtType);
                columnConfig.addValue(FakerConfigEnum.WHERE_VALUE_TEMPLATE.getConfigKey(), fmtType);
                columnConfig.addValue(FakerConfigEnum.SET_VALUE_TEMPLATE.getConfigKey(), fmtType);
                return finalSrw(seedFactory, seedConfig, Types.OTHER, isArray, columnConfig, columnType);
            }
            case "timestamp":
            case "timestamptz": {
                // (BC)-4713-01-01 00:00:00.000000 to (AD)294276-12-31 23:59:59.999999 ,1 microsecond
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
                    seedConfig.setZoneForm("-14:00");
                    seedConfig.setZoneTo("+14:00");
                    return finalSrw(seedFactory, seedConfig, Types.TIMESTAMP_WITH_TIMEZONE, isArray, columnConfig, columnType);
                } else {
                    return finalSrw(seedFactory, seedConfig, Types.TIMESTAMP, isArray, columnConfig, columnType);
                }
            }
            case "bit":
            case "varbit": {
                StringSeedFactory seedFactory = new StringSeedFactory();
                StringSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(1);

                if (jdbcColumn.getColumnSize() == 0 && jdbcColumn.getDecimalDigits() == null) {
                    seedConfig.setMaxLength(8);
                } else if (isArray && jdbcColumn.getColumnSize() > 300) {
                    seedConfig.setMaxLength(8);
                } else {
                    seedConfig.setMaxLength(safeMaxLength(jdbcColumn.getColumnSize(), 24, 512));
                }
                seedConfig.setCharacterSet(new HashSet<>(Collections.singletonList(CharacterSet.BIT)));

                String temp = null;
                if (StringUtils.equals(columnType, "bit")) {
                    temp = "?::bit(" + jdbcColumn.getColumnSize() + ")";
                } else {
                    temp = "?::bit varying(" + seedConfig.getMaxLength() + ")";
                }

                columnConfig.addValue(FakerConfigEnum.INSERT_TEMPLATE.getConfigKey(), fmtType(isArray, temp));
                columnConfig.addValue(FakerConfigEnum.WHERE_VALUE_TEMPLATE.getConfigKey(), fmtType(isArray, temp));
                columnConfig.addValue(FakerConfigEnum.SET_VALUE_TEMPLATE.getConfigKey(), fmtType(isArray, temp));
                return finalSrw(seedFactory, seedConfig, Types.VARCHAR, isArray, columnConfig, columnType);
            }
            case "bytea": {
                BytesSeedFactory seedFactory = new BytesSeedFactory();
                BytesSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(1);
                seedConfig.setMaxLength(safeMaxLength(jdbcColumn.getColumnSize(), 100, 4096));
                return finalSrw(seedFactory, seedConfig, Types.VARBINARY, isArray, columnConfig, columnType);
            }
            case "point":
            case "line":
            case "lseg":
            case "box":
            case "path":
            case "polygon":
            case "circle":
            case "geometry": {
                GeometrySeedFactory seedFactory = new GeometrySeedFactory();
                GeometrySeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.addRange(50, 0.0, 0.0, 1000, 1000);
                seedConfig.setPrecision(15);
                seedConfig.setMinPointSize(5);
                seedConfig.setMaxPointSize(100);
                seedConfig.setFormatType(FormatType.WKT);

                switch (columnType) {
                    case "point":
                        seedConfig.setGeometryType(GeometryType.Point);
                        break;
                    case "line":
                        seedConfig.setGeometryType(GeometryType.Line);
                        break;
                    case "lseg":
                        seedConfig.setGeometryType(GeometryType.Lseg);
                        break;
                    case "box":
                        seedConfig.setGeometryType(GeometryType.Box);
                        break;
                    case "circle":
                        seedConfig.setGeometryType(GeometryType.Circle);
                        break;
                    case "path":
                        seedConfig.setGeometryType(GeometryType.Path);
                        break;
                    case "polygon":
                        seedConfig.setGeometryType(GeometryType.Polygon);
                        break;
                    case "geometry":
                        columnConfig.addValue(FakerConfigEnum.SELECT_TEMPLATE.getConfigKey(), "ST_AsEWKT({name})");
                        seedConfig.setGeometryType(GeometryType.MultiPolygon);
                        break;
                }

                String temp = "?::" + columnType;
                columnConfig.addValue(FakerConfigEnum.INSERT_TEMPLATE.getConfigKey(), fmtType(isArray, temp));
                columnConfig.addValue(FakerConfigEnum.SET_VALUE_TEMPLATE.getConfigKey(), fmtType(isArray, temp));
                TypeSrw typeSrw = finalSrw(seedFactory, seedConfig, Types.OTHER, isArray, columnConfig, columnType);
                typeSrw.getDefaultIgnoreAct().add(UseFor.DeleteWhere);
                typeSrw.getDefaultIgnoreAct().add(UseFor.UpdateWhere);
                return typeSrw;
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
}
