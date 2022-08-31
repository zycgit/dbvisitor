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
import net.hasor.cobble.setting.SettingNode;
import net.hasor.dbvisitor.faker.generator.TypeSrw;
import net.hasor.dbvisitor.faker.meta.JdbcColumn;
import net.hasor.dbvisitor.faker.seed.date.DateSeedConfig;
import net.hasor.dbvisitor.faker.seed.date.DateSeedFactory;
import net.hasor.dbvisitor.faker.seed.date.DateType;
import net.hasor.dbvisitor.faker.seed.date.GenType;
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
public class MySqlTypeSrwFactory extends DefaultTypeSrwFactory {
    @Override
    public TypeSrw createSeedFactory(JdbcColumn jdbcColumn, SettingNode columnConfig) {
        String columnType = jdbcColumn.getColumnType();
        if (StringUtils.isBlank(columnType)) {
            return defaultSeedFactory(jdbcColumn);
        }
        switch (columnType.toLowerCase()) {
            case "bit": {
                StringSeedFactory seedFactory = new StringSeedFactory();
                StringSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(0);
                seedConfig.setMaxLength(safeMaxLength(jdbcColumn.getColumnSize(), 24, 64));
                seedConfig.setCharacterSet(new HashSet<>(Collections.singletonList(CharacterSet.BIT)));
                return new TypeSrw(seedFactory, seedConfig, Types.VARCHAR);
            }
            case "tinyint": {
                // -128 to 127
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.setMin(new BigDecimal("-128"));
                seedConfig.setMax(new BigDecimal("127"));
                return new TypeSrw(seedFactory, seedConfig, Types.TINYINT);
            }
            case "tinyint unsigned": {
                // 0 to 255
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.setMin(new BigDecimal("0"));
                seedConfig.setMax(new BigDecimal("255"));
                return new TypeSrw(seedFactory, seedConfig, Types.SMALLINT);
            }
            case "smallint": {
                // -32768 to 32767
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.setMin(new BigDecimal("-32768"));
                seedConfig.setMax(new BigDecimal("+32767"));
                return new TypeSrw(seedFactory, seedConfig, Types.SMALLINT);
            }
            case "smallint unsigned": {
                // -32768 to 32767
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.setMin(new BigDecimal("0"));
                seedConfig.setMax(new BigDecimal("65535"));
                return new TypeSrw(seedFactory, seedConfig, Types.INTEGER);
            }
            case "mediumint": {
                // -8388608 to 8388607
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.setMin(new BigDecimal("-8388608"));
                seedConfig.setMax(new BigDecimal("+8388607"));
                return new TypeSrw(seedFactory, seedConfig, Types.INTEGER);
            }
            case "mediumint unsigned": {
                // 0 to 16777215
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.setMin(new BigDecimal("0"));
                seedConfig.setMax(new BigDecimal("16777215"));
                return new TypeSrw(seedFactory, seedConfig, Types.INTEGER);
            }
            case "int": {
                // -2147483648 to 2147483647
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.setMin(new BigDecimal("-2147483648"));
                seedConfig.setMax(new BigDecimal("+2147483647"));
                return new TypeSrw(seedFactory, seedConfig, Types.INTEGER);
            }
            case "int unsigned": {
                // 0 to 4294967295
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Long);
                seedConfig.setMin(new BigDecimal("0"));
                seedConfig.setMax(new BigDecimal("4294967295"));
                return new TypeSrw(seedFactory, seedConfig, Types.BIGINT);
            }
            case "bigint": {
                // -9223372036854775808 to 9223372036854775807
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.BigInt);
                seedConfig.setMin(new BigDecimal("-9223372036854775808"));
                seedConfig.setMax(new BigDecimal("+9223372036854775807"));
                return new TypeSrw(seedFactory, seedConfig, Types.BIGINT);
            }
            case "bigint unsigned": {
                // 0 to 18446744073709551615
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.BigInt);
                seedConfig.setMin(new BigDecimal("0"));
                seedConfig.setMax(new BigDecimal("18446744073709551615"));
                return new TypeSrw(seedFactory, seedConfig, Types.BIGINT);
            }
            case "decimal":
            case "decimal unsigned":
            case "float": {
                // -3.402823466E+38 to -1.175494351E-38, 0, and 1.175494351E-38 to 3.402823466E+38
            }
            case "float unsigned":
            case "double": {
                // -1.7976931348623157E+308 to -2.2250738585072014E-308, 0, and 2.2250738585072014E-308 to 1.7976931348623157E+308.
            }
            case "double unsigned":
            case "date":
            case "datetime":
            case "timestamp": {
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.SqlTimestamp);
                seedConfig.setGenType(GenType.Random);
                int p = safeMaxLength(jdbcColumn.getDecimalDigits(), 3, 7);
                seedConfig.setDateFormat("HH:mm:ss" + ((p > 0) ? ("." + StringUtils.repeat("S", p)) : ""));
                seedConfig.setPrecision(Math.max(p, 0));
                seedConfig.setRangeForm("1970-01-01 00:00:01");
                seedConfig.setRangeTo("2038-01-19 03:14:07");
                seedConfig.setZoneForm("00:00");
                seedConfig.setZoneTo("00:00");
                return new TypeSrw(seedFactory, seedConfig, Types.TIMESTAMP);
            }
            case "time":
            case "year":
            case "char":
            case "varchar":
            case "tinytext":
            case "text":
            case "mediumtext":
            case "longtext":
            case "binary":
            case "varbinary":
            case "mediumblob":
            case "tinyblob":
            case "blob":
            case "longblob":
            case "enum":
            case "set":
            case "json":
            case "geometry":
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
}
