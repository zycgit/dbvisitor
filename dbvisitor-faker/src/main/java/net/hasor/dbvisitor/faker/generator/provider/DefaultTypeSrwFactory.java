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
import net.hasor.cobble.setting.SettingNode;
import net.hasor.dbvisitor.faker.generator.TypeSrw;
import net.hasor.dbvisitor.faker.meta.JdbcColumn;
import net.hasor.dbvisitor.faker.seed.bool.BooleanSeedConfig;
import net.hasor.dbvisitor.faker.seed.bool.BooleanSeedFactory;
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

public class DefaultTypeSrwFactory {
    public TypeSrw createSeedFactory(JdbcColumn jdbcColumn, SettingNode columnConfig) {
        return defaultSeedFactory(jdbcColumn);
    }

    protected TypeSrw defaultSeedFactory(JdbcColumn jdbcColumn) {
        Integer jdbcType = jdbcColumn.getJdbcType();
        if (jdbcType == null) {
            jdbcType = Types.OTHER;
        }

        switch (jdbcType) {
            case Types.BIT:
            case Types.BOOLEAN: {
                BooleanSeedFactory seedFactory = new BooleanSeedFactory();
                BooleanSeedConfig seedConfig = seedFactory.newConfig();
                return new TypeSrw(seedFactory, seedConfig, jdbcType);
            }
            case Types.TINYINT:
            case Types.SMALLINT: {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.addMinMax(new BigDecimal("0"), new BigDecimal("100"));
                return new TypeSrw(seedFactory, seedConfig, Types.INTEGER);
            }
            case Types.INTEGER: {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.addMinMax(new BigDecimal("0"), new BigDecimal("10000"));
                return new TypeSrw(seedFactory, seedConfig, Types.INTEGER);
            }
            case Types.BIGINT: {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Long);
                seedConfig.addMinMax(new BigDecimal("0"), new BigDecimal("1000000"));
                return new TypeSrw(seedFactory, seedConfig, Types.BIGINT);
            }
            case Types.FLOAT:
            case Types.REAL:
            case Types.DOUBLE:
            case Types.NUMERIC:
            case Types.DECIMAL: {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Decimal);
                seedConfig.addMinMax(new BigDecimal("0.0"), new BigDecimal("9999.999"));
                seedConfig.setScale(Math.min(jdbcColumn.getDecimalDigits(), 3));
                seedConfig.setAbs(true);
                return new TypeSrw(seedFactory, seedConfig, Types.DECIMAL);
            }
            case Types.CHAR:
            case Types.NCHAR:
            case Types.VARCHAR:
            case Types.NVARCHAR:
            case Types.LONGVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.CLOB:
            case Types.NCLOB: {
                StringSeedFactory seedFactory = new StringSeedFactory();
                StringSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setCharacterSet(new HashSet<>(Collections.singletonList(CharacterSet.LETTER_NUMBER)));
                seedConfig.setMinLength(1);
                if (jdbcColumn.getColumnSize() == null) {
                    seedConfig.setMaxLength(10);
                } else {
                    seedConfig.setMaxLength(Math.min(jdbcColumn.getColumnSize(), 100));
                }
                return new TypeSrw(seedFactory, seedConfig, jdbcType);
            }
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB: {
                BytesSeedFactory seedFactory = new BytesSeedFactory();
                BytesSeedConfig seedConfig = seedFactory.newConfig();
                return new TypeSrw(seedFactory, seedConfig, jdbcType);
            }
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
            case Types.TIME_WITH_TIMEZONE:
            case Types.TIMESTAMP_WITH_TIMEZONE: {
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.JavaDate);
                seedConfig.setGenType(GenType.Random);
                seedConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
                seedConfig.setPrecision(3);
                seedConfig.setRangeForm("2000-01-01 00:00:00.000");
                seedConfig.setRangeTo("2030-12-31 23:59:59.999");
                return new TypeSrw(seedFactory, seedConfig, jdbcType);
            }
            case Types.SQLXML:
            case Types.STRUCT:
            case Types.ARRAY:
            case Types.DATALINK:
            case Types.NULL:
            case Types.JAVA_OBJECT:
            case Types.DISTINCT:
            case Types.REF:
            case Types.ROWID:
            case Types.REF_CURSOR:
            case Types.OTHER:
            default:
                throw new UnsupportedOperationException("unsupported columnName " + jdbcColumn.getColumnName()//
                        + ", sqlType '" + jdbcColumn.getColumnType()//
                        + "' and jdbcType '" + jdbcType + "'");
        }
    }
}
