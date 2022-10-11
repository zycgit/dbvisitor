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
import net.hasor.dbvisitor.faker.generator.provider.AbstractMySqlTypeSrwFactory;
import net.hasor.dbvisitor.faker.meta.JdbcColumn;
import net.hasor.dbvisitor.faker.seed.bytes.BytesSeedConfig;
import net.hasor.dbvisitor.faker.seed.bytes.BytesSeedFactory;
import net.hasor.dbvisitor.faker.seed.custom.special.MySqlTimeSeedConfig;
import net.hasor.dbvisitor.faker.seed.custom.special.MySqlTimeSeedFactory;
import net.hasor.dbvisitor.faker.seed.date.DateSeedConfig;
import net.hasor.dbvisitor.faker.seed.date.DateSeedFactory;
import net.hasor.dbvisitor.faker.seed.date.DateType;
import net.hasor.dbvisitor.faker.seed.date.GenType;
import net.hasor.dbvisitor.faker.seed.enums.EnumSeedConfig;
import net.hasor.dbvisitor.faker.seed.enums.EnumSeedFactory;
import net.hasor.dbvisitor.faker.seed.geometry.FormatType;
import net.hasor.dbvisitor.faker.seed.geometry.GeometrySeedConfig;
import net.hasor.dbvisitor.faker.seed.geometry.GeometrySeedFactory;
import net.hasor.dbvisitor.faker.seed.geometry.GeometryType;
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
 * https://dev.mysql.com/doc/refman/5.7/en/numeric-type-syntax.html
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public class MySqlRadicalTypeSrwFactory extends AbstractMySqlTypeSrwFactory {
    @Override
    public TypeSrw createSeedFactory(JdbcColumn jdbcColumn, SettingNode columnConfig) {
        String columnType = jdbcColumn.getColumnType().toLowerCase();
        if (StringUtils.isBlank(columnType)) {
            return defaultSeedFactory(jdbcColumn);
        }
        switch (columnType) {
            case "bit": {
                StringSeedFactory seedFactory = new StringSeedFactory();
                StringSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(1);
                seedConfig.setMaxLength(safeMaxLength(jdbcColumn.getColumnSize(), 24, 64));
                seedConfig.setCharacterSet(new HashSet<>(Collections.singletonList(CharacterSet.BIT)));
                seedConfig.setTypeHandler(new MySqlBitAsStringTypeHandler());
                return new TypeSrw(seedFactory, seedConfig, Types.VARCHAR);
            }
            case "tinyint": {
                // -128 to 127
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.addMinMax(new BigDecimal("-128"), new BigDecimal("127"));
                return new TypeSrw(seedFactory, seedConfig, Types.TINYINT);
            }
            case "tinyint unsigned": {
                // 0 to 255
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.addMinMax(new BigDecimal("0"), new BigDecimal("255"));
                return new TypeSrw(seedFactory, seedConfig, Types.SMALLINT);
            }
            case "smallint": {
                // -32768 to 32767
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.addMinMax(new BigDecimal("-32768"), new BigDecimal("+32767"));
                return new TypeSrw(seedFactory, seedConfig, Types.SMALLINT);
            }
            case "smallint unsigned": {
                // 0 to 65535
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.addMinMax(new BigDecimal("0"), new BigDecimal("65535"));
                return new TypeSrw(seedFactory, seedConfig, Types.INTEGER);
            }
            case "mediumint": {
                // -8388608 to 8388607
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.addMinMax(new BigDecimal("-8388608"), new BigDecimal("+8388607"));
                return new TypeSrw(seedFactory, seedConfig, Types.INTEGER);
            }
            case "mediumint unsigned": {
                // 0 to 16777215
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.addMinMax(new BigDecimal("0"), new BigDecimal("16777215"));
                return new TypeSrw(seedFactory, seedConfig, Types.INTEGER);
            }
            case "int": {
                // -2147483648 to 2147483647
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.addMinMax(new BigDecimal("-2147483648"), new BigDecimal("+2147483647"));
                return new TypeSrw(seedFactory, seedConfig, Types.INTEGER);
            }
            case "int unsigned": {
                // 0 to 4294967295
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Long);
                seedConfig.addMinMax(new BigDecimal("0"), new BigDecimal("4294967295"));
                return new TypeSrw(seedFactory, seedConfig, Types.BIGINT);
            }
            case "bigint": {
                // -9223372036854775808 to 9223372036854775807
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Long);
                seedConfig.addMinMax(new BigDecimal("-9223372036854775808"), new BigDecimal("+9223372036854775807"));
                return new TypeSrw(seedFactory, seedConfig, Types.BIGINT);
            }
            case "bigint unsigned": {
                // 0 to 18446744073709551615
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.BigInt);
                seedConfig.addMinMax(new BigDecimal("0"), new BigDecimal("18446744073709551615"));
                return new TypeSrw(seedFactory, seedConfig, Types.BIGINT);
            }
            case "decimal":
            case "decimal unsigned": {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Decimal);
                seedConfig.setPrecision(jdbcColumn.getColumnSize());
                seedConfig.setScale(jdbcColumn.getDecimalDigits());
                seedConfig.setAbs(StringUtils.contains(columnType, "unsigned"));
                return new TypeSrw(seedFactory, seedConfig, Types.DECIMAL);
            }
            case "float":
            case "float unsigned": {
                // -3.402823466E+38 to -1.175494351E-38, 0, and 1.175494351E-38 to 3.402823466E+38
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Decimal);
                seedConfig.addMinMax(10, new BigDecimal("-3.402823466E+38"), new BigDecimal("-1.175494351E-38"));
                seedConfig.addMinMax(10, new BigDecimal("1.175494351E-38"), new BigDecimal("3.402823466E+38"));
                seedConfig.addMinMax(30, new BigDecimal("-999999999.999999999"), new BigDecimal("+999999999.999999999"));
                seedConfig.addMinMax(30, new BigDecimal("-0.999999999"), new BigDecimal("+0.999999999"));
                seedConfig.setPrecision(jdbcColumn.getColumnSize());
                seedConfig.setScale(jdbcColumn.getDecimalDigits());
                seedConfig.setAbs(StringUtils.contains(columnType, "unsigned"));
                return new TypeSrw(seedFactory, seedConfig, Types.FLOAT);
            }
            case "double":
            case "double unsigned": {
                // -1.7976931348623157E+308 to -2.2250738585072014E-308, 0, and 2.2250738585072014E-308 to 1.7976931348623157E+308.
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Decimal);
                seedConfig.addMinMax(10, new BigDecimal("-2.2250738585072014E-308"), new BigDecimal("-1.7976931348623157E+308"));
                seedConfig.addMinMax(10, new BigDecimal("2.2250738585072014E-308"), new BigDecimal("1.7976931348623157E+308"));
                seedConfig.addMinMax(30, new BigDecimal("-999999999.999999999"), new BigDecimal("+999999999.999999999"));
                seedConfig.addMinMax(30, new BigDecimal("-0.999999999"), new BigDecimal("+0.999999999"));
                seedConfig.setTypeHandler(new MySqlBigDecimalAsStringTypeHandler());
                seedConfig.setAbs(StringUtils.contains(columnType, "unsigned"));
                seedConfig.setPrecision(jdbcColumn.getColumnSize());
                seedConfig.setScale(jdbcColumn.getDecimalDigits());
                return new TypeSrw(seedFactory, seedConfig, Types.FLOAT);
            }
            case "date": {
                // '1000-01-01' to '9999-12-31'
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.String);
                seedConfig.setGenType(GenType.Random);
                seedConfig.setDateFormat("yyyy-MM-dd");
                seedConfig.setRangeForm("1000-01-01");
                seedConfig.setRangeTo("9999-12-31");
                return new TypeSrw(seedFactory, seedConfig, Types.DATE);
            }
            case "datetime": {
                // '1000-01-01 00:00:00.000000' to '9999-12-31 23:59:59.999999'
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.LocalDateTime);
                seedConfig.setGenType(GenType.Random);
                int p = safeMaxLength(jdbcColumn.getDecimalDigits(), 3, 6);
                seedConfig.setDateFormat("yyyy-MM-dd HH:mm:ss" + ((p > 0) ? ("." + StringUtils.repeat("S", p)) : ""));
                seedConfig.setPrecision(Math.max(p, 0));
                seedConfig.setRangeForm("1000-01-01 00:00:00.000000");
                seedConfig.setRangeTo("9999-12-31 23:59:59.999999");
                return new TypeSrw(seedFactory, seedConfig, Types.TIMESTAMP);
            }
            case "timestamp": {
                // '1970-01-01 00:00:01.000000' UTC to '2038-01-19 03:14:07.999999'
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.LocalDateTime);
                seedConfig.setGenType(GenType.Random);
                int p = safeMaxLength(jdbcColumn.getDecimalDigits(), 3, 6);
                seedConfig.setDateFormat("yyyy-MM-dd HH:mm:ss" + ((p > 0) ? ("." + StringUtils.repeat("S", p)) : ""));
                seedConfig.setPrecision(Math.max(p, 0));
                seedConfig.setRangeForm("1970-01-01 00:00:01.000000");
                seedConfig.setRangeTo("2038-01-19 03:14:07.999999");
                return new TypeSrw(seedFactory, seedConfig, Types.TIMESTAMP);
            }
            case "time": {
                // '-838:59:59.000000' to '838:59:59.000000'
                MySqlTimeSeedFactory seedFactory = new MySqlTimeSeedFactory();
                MySqlTimeSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setGenType(GenType.Random);
                int p = safeMaxLength(jdbcColumn.getDecimalDigits(), 3, 6);
                seedConfig.setPrecision(Math.max(p, 0));
                seedConfig.setRangeForm("-838:59:59.000000");
                seedConfig.setRangeTo("838:59:59.000000");
                return new TypeSrw(seedFactory, seedConfig, Types.TIME);
            }
            case "year": {
                // 1901 to 2155, and 0000
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.addMinMax(10, BigDecimal.ZERO, BigDecimal.ZERO);
                seedConfig.addMinMax(90, new BigDecimal("1901"), new BigDecimal("2155"));
                return new TypeSrw(seedFactory, seedConfig, Types.INTEGER);
            }
            case "char":
            case "varchar":
            case "tinytext":
            case "text":
            case "mediumtext":
            case "longtext": {
                StringSeedFactory seedFactory = new StringSeedFactory();
                StringSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(0);
                seedConfig.setMaxLength(safeMaxLength(jdbcColumn.getColumnSize(), 100, 1000));
                seedConfig.setCharacterSet(new HashSet<>(Collections.singletonList(CharacterSet.LETTER_NUMBER)));
                return new TypeSrw(seedFactory, seedConfig, Types.LONGVARCHAR);
            }
            case "binary":
            case "varbinary":
            case "mediumblob":
            case "tinyblob":
            case "blob":
            case "longblob": {
                BytesSeedFactory seedFactory = new BytesSeedFactory();
                BytesSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(0);
                seedConfig.setMaxLength(safeMaxLength(jdbcColumn.getColumnSize(), 100, 4096));
                return new TypeSrw(seedFactory, seedConfig, Types.VARBINARY);
            }
            case "enum":
            case "set": {
                EnumSeedFactory seedFactory = new EnumSeedFactory();
                EnumSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDict(new HashSet<>());
                return new TypeSrw(seedFactory, seedConfig, Types.VARCHAR);
            }
            case "geometry": {
                GeometrySeedFactory seedFactory = new GeometrySeedFactory();
                GeometrySeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.addRange(50, 0.0, 0.0, 1000, 1000);
                seedConfig.setPrecision(5);
                seedConfig.setMinPointSize(2);
                seedConfig.setMaxPointSize(10);
                seedConfig.setGeometryType(GeometryType.MultiPolygon);
                seedConfig.setFormatType(FormatType.WKT);

                columnConfig.addValue(FakerConfigEnum.SELECT_TEMPLATE.getConfigKey(), "ST_AsWKT({name})");
                columnConfig.addValue(FakerConfigEnum.INSERT_TEMPLATE.getConfigKey(), "ST_MultiPolygonFromText(?)");
                columnConfig.addValue(FakerConfigEnum.SET_VALUE_TEMPLATE.getConfigKey(), "ST_MultiPolygonFromText(?)");
                TypeSrw typeSrw = new TypeSrw(seedFactory, seedConfig, Types.VARCHAR);
                typeSrw.getDefaultIgnoreAct().add(UseFor.DeleteWhere);
                typeSrw.getDefaultIgnoreAct().add(UseFor.UpdateWhere);
                return typeSrw;
            }
            case "point":
            case "linestring":
            case "polygon":
            case "multipoint":
            case "multilinestring":
            case "multipolygon":
            case "geomcollection":
            case "json":
            default: {
                throw new UnsupportedOperationException("unsupported columnName " + jdbcColumn.getColumnName()//
                        + ", columnType '" + columnType + "'");
            }
        }
    }
}
