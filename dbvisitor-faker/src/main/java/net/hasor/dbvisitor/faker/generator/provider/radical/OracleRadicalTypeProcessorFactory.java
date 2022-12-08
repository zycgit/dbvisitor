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
import net.hasor.dbvisitor.faker.generator.TypeProcessor;
import net.hasor.dbvisitor.faker.generator.UseFor;
import net.hasor.dbvisitor.faker.generator.provider.AbstractOracleTypeProcessorFactory;
import net.hasor.dbvisitor.faker.meta.JdbcColumn;
import net.hasor.dbvisitor.faker.seed.bytes.BytesSeedConfig;
import net.hasor.dbvisitor.faker.seed.bytes.BytesSeedFactory;
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

public class OracleRadicalTypeProcessorFactory extends AbstractOracleTypeProcessorFactory {

    @Override
    public TypeProcessor createSeedFactory(JdbcColumn jdbcColumn, SettingNode columnConfig) {
        String columnType = jdbcColumn.getColumnType().toLowerCase();
        if (StringUtils.isBlank(columnType)) {
            return defaultSeedFactory(jdbcColumn);
        }

        columnType = columnType.replaceAll("\\(\\d+\\)", "");
        switch (columnType) {
            case "date": {
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.String);
                seedConfig.setGenType(GenType.Random);
                seedConfig.setDateFormat("yyyy-MM-dd");
                seedConfig.setRangeForm("1000-01-01");
                seedConfig.setRangeTo("9999-12-31");
                TypeProcessor typeProcessor = new TypeProcessor(seedFactory, seedConfig, Types.DATE);
                typeProcessor.getDefaultIgnoreAct().add(UseFor.DeleteWhere);
                columnConfig.addValue(FakerConfigEnum.INSERT_TEMPLATE.getConfigKey(), "TO_DATE(?,'YYYY-MM-DD')");
                columnConfig.addValue(FakerConfigEnum.SET_VALUE_TEMPLATE.getConfigKey(), "TO_DATE(?,'YYYY-MM-DD')");
                columnConfig.addValue(FakerConfigEnum.WHERE_VALUE_TEMPLATE.getConfigKey(), "TO_DATE(?,'YYYY-MM-DD')");
                return typeProcessor;
            }
            case "timestamp":
            case "timestamp with local time zone":
            case "timestamp with time zone": {
                // https://www.iana.org/time-zones
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.LocalDateTime);
                seedConfig.setGenType(GenType.Random);
                int p = Math.max(jdbcColumn.getDecimalDigits(), 9);
                seedConfig.setDateFormat("yyyy-MM-dd HH:mm:ss" + "." + StringUtils.repeat("S", p));
                seedConfig.setPrecision(p);
                seedConfig.setRangeForm("1000-01-01 00:00:00.000");
                seedConfig.setRangeTo("9999-12-31 23:59:59.999");
                if (columnType.endsWith("zone")) {
                    seedConfig.setZoneForm("-14:00");
                    seedConfig.setZoneTo("+14:00");
                    seedConfig.setDateType(DateType.OffsetDateTime);
                    return new TypeProcessor(seedFactory, seedConfig, Types.TIMESTAMP_WITH_TIMEZONE);
                } else {
                    return new TypeProcessor(seedFactory, seedConfig, Types.TIMESTAMP);
                }
            }
            case "binary_float":
            case "float": {
                // -1.17549E-38 to -3.40282E+38, 0, and 1.17549E-38 to 3.40282E+38
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Decimal);
                seedConfig.addMinMax(10, new BigDecimal("-1.17549E-38"), new BigDecimal("-3.40282E+38"));
                seedConfig.addMinMax(10, new BigDecimal("1.17549E-38"), new BigDecimal("3.40282E+38"));
                seedConfig.addMinMax(30, new BigDecimal("-999999999.999999999"), new BigDecimal("+999999999.999999999"));
                seedConfig.addMinMax(30, new BigDecimal("-0.999999999"), new BigDecimal("+0.999999999"));
                seedConfig.setPrecision(jdbcColumn.getColumnSize());
                seedConfig.setScale(jdbcColumn.getDecimalDigits());
                return new TypeProcessor(seedFactory, seedConfig, Types.FLOAT);
            }
            case "binary_double": {
                // -1.17549E-38 to -3.40282E+38, 0, and 1.17549E-38 to 3.40282E+38
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Decimal);
                seedConfig.addMinMax(10, new BigDecimal("-2.22507485850720E-308"), new BigDecimal("-1.79769313486231E+308"));
                seedConfig.addMinMax(10, new BigDecimal("2.22507485850720E-308"), new BigDecimal("1.79769313486231E+308"));
                seedConfig.addMinMax(30, new BigDecimal("-999999999.999999999"), new BigDecimal("+999999999.999999999"));
                seedConfig.addMinMax(30, new BigDecimal("-0.999999999"), new BigDecimal("+0.999999999"));
                seedConfig.setPrecision(jdbcColumn.getColumnSize());
                seedConfig.setScale(jdbcColumn.getDecimalDigits());
                return new TypeProcessor(seedFactory, seedConfig, Types.DOUBLE);
            }
            case "number": {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Decimal);
                if (jdbcColumn.getColumnSize() == 0 && jdbcColumn.getDecimalDigits() < 0) {
                    seedConfig.setPrecision(40);
                    seedConfig.setScale(20);
                } else {
                    seedConfig.setPrecision(jdbcColumn.getColumnSize());
                    seedConfig.setScale(jdbcColumn.getDecimalDigits());
                }
                return new TypeProcessor(seedFactory, seedConfig, Types.DECIMAL);
            }
            case "char":
            case "nchar":
            case "varchar2":
            case "nvarchar2":
            case "nclob":
            case "clob": {
                StringSeedFactory seedFactory = new StringSeedFactory();
                StringSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(1);
                seedConfig.setMaxLength(safeMaxLength(jdbcColumn.getColumnSize(), 10, 100));
                seedConfig.setCharacterSet(new HashSet<>(Collections.singletonList(CharacterSet.LETTER_NUMBER)));
                seedConfig.setAllowEmpty(false);
                return new TypeProcessor(seedFactory, seedConfig, Types.VARCHAR);
            }
            case "long": {
                StringSeedFactory seedFactory = new StringSeedFactory();
                StringSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(1);
                seedConfig.setMaxLength(safeMaxLength(jdbcColumn.getColumnSize(), 10, 100));
                seedConfig.setCharacterSet(new HashSet<>(Collections.singletonList(CharacterSet.LETTER_NUMBER)));
                seedConfig.setAllowEmpty(false);
                TypeProcessor typeProcessor = new TypeProcessor(seedFactory, seedConfig, Types.VARCHAR);
                typeProcessor.getDefaultIgnoreAct().add(UseFor.DeleteWhere);
                return typeProcessor;
            }
            case "blob": {
                BytesSeedFactory seedFactory = new BytesSeedFactory();
                BytesSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(1);
                seedConfig.setMaxLength(safeMaxLength(jdbcColumn.getColumnSize(), 100, 4096));
                return new TypeProcessor(seedFactory, seedConfig, Types.BLOB);
            }
            case "raw":
            case "long raw": {
                BytesSeedFactory seedFactory = new BytesSeedFactory();
                BytesSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(1);
                seedConfig.setMaxLength(safeMaxLength(jdbcColumn.getColumnSize(), 100, 4096));
                TypeProcessor typeProcessor = new TypeProcessor(seedFactory, seedConfig, Types.BLOB);
                typeProcessor.getDefaultIgnoreAct().add(UseFor.DeleteWhere);
                return typeProcessor;
            }
            case "urowid":
            case "rowid": {
                StringSeedFactory seedFactory = new StringSeedFactory();
                StringSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setAllowEmpty(false);
                seedConfig.setCharacterSet(new HashSet<>(Collections.singletonList(CharacterSet.LETTER_NUMBER)));
                TypeProcessor typeProcessor = new TypeProcessor(seedFactory, seedConfig, Types.VARCHAR);
                typeProcessor.getDefaultIgnoreAct().add(UseFor.UpdateSet);
                typeProcessor.getDefaultIgnoreAct().add(UseFor.Insert);
                return typeProcessor;
            }
            case "interval year to month":
            case "interval day to second":
            case "xmltype":
            case "bfile":
            default: {
                throw new UnsupportedOperationException("unsupported columnName " + jdbcColumn.getColumnName()//
                        + ", columnType '" + columnType + "'");
            }
        }
    }
}
