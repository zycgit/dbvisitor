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
import net.hasor.dbvisitor.faker.generator.UseFor;
import net.hasor.dbvisitor.faker.generator.provider.AbstractSqlServerTypeSrwFactory;
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

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Collections;
import java.util.HashSet;

/**
 * https://docs.microsoft.com/zh-cn/sql/t-sql/data-types/data-types-transact-sql
 * https://docs.microsoft.com/zh-cn/sql/t-sql/data-types/time-transact-sql
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public class SqlServerCarefullyTypeSrwFactory extends AbstractSqlServerTypeSrwFactory {
    @Override
    public TypeSrw createSeedFactory(JdbcColumn jdbcColumn, SettingNode columnConfig) {
        String columnType = jdbcColumn.getColumnType().toLowerCase();
        if (StringUtils.isBlank(columnType)) {
            return defaultSeedFactory(jdbcColumn);
        }
        switch (columnType) {
            case "bit": {
                BooleanSeedFactory seedFactory = new BooleanSeedFactory();
                BooleanSeedConfig seedConfig = seedFactory.newConfig();
                return new TypeSrw(seedFactory, seedConfig, Types.BOOLEAN);
            }
            case "tinyint": {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.addMinMax(new BigDecimal("0"), new BigDecimal("100"));
                return new TypeSrw(seedFactory, seedConfig, Types.TINYINT);
            }
            case "smallint": {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.addMinMax(new BigDecimal("0"), new BigDecimal("10000"));
                return new TypeSrw(seedFactory, seedConfig, Types.SMALLINT);
            }
            case "int": {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.addMinMax(new BigDecimal("0"), new BigDecimal("1000000"));
                return new TypeSrw(seedFactory, seedConfig, Types.INTEGER);
            }
            case "bigint": {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Long);
                seedConfig.addMinMax(new BigDecimal("0"), new BigDecimal("100000000"));
                return new TypeSrw(seedFactory, seedConfig, Types.BIGINT);
            }
            case "smallmoney":
            case "money":
            case "float":
            case "real":
            case "numeric":
            case "decimal": {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Double);
                seedConfig.addMinMax(new BigDecimal("0.0"), new BigDecimal("9999.99"));
                seedConfig.setScale(safeMaxLength(jdbcColumn.getDecimalDigits(), 2, 2));
                seedConfig.setAbs(true);
                return new TypeSrw(seedFactory, seedConfig, Types.DOUBLE);
            }
            case "nchar":
            case "nvarchar":
            case "char":
            case "varchar":
            case "ntext":
            case "text": {
                StringSeedFactory seedFactory = new StringSeedFactory();
                StringSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(1);
                seedConfig.setMaxLength(safeMaxLength(jdbcColumn.getColumnSize(), 10, 100));
                seedConfig.setCharacterSet(new HashSet<>(Collections.singletonList(CharacterSet.LETTER_NUMBER)));

                if (StringUtils.contains(columnType, "text")) {
                    columnConfig.addValue(FakerConfigEnum.WHERE_COL_TEMPLATE.getConfigKey(), "cast({name} as nvarchar(max))");
                }
                return new TypeSrw(seedFactory, seedConfig, Types.VARCHAR);
            }
            case "date": {
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.String);
                seedConfig.setGenType(GenType.Random);
                seedConfig.setDateFormat("yyyy-MM-dd");
                seedConfig.setRangeForm("2000-01-01");
                seedConfig.setRangeTo("2030-12-31");
                return new TypeSrw(seedFactory, seedConfig, Types.DATE);
            }
            case "time": {
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.String);
                seedConfig.setGenType(GenType.Random);
                int p = Math.max(jdbcColumn.getDecimalDigits(), 3);
                seedConfig.setDateFormat("HH:mm:ss" + "." + StringUtils.repeat("S", p));
                seedConfig.setPrecision(p);
                seedConfig.setRangeForm("00:00:00.000");
                seedConfig.setRangeTo("23:59:59.999");
                return new TypeSrw(seedFactory, seedConfig, Types.TIME);
            }
            case "smalldatetime": {
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.LocalDateTime);
                seedConfig.setGenType(GenType.Random);
                seedConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
                seedConfig.setRangeForm("2000-01-01 00:00:00");
                seedConfig.setRangeTo("2030-12-31 23:59:59");
                return new TypeSrw(seedFactory, seedConfig, Types.TIMESTAMP);
            }
            case "datetime":
            case "datetime2": {
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.LocalDateTime);
                seedConfig.setGenType(GenType.Random);
                int p = Math.max(jdbcColumn.getDecimalDigits(), 3);
                seedConfig.setDateFormat("yyyy-MM-dd HH:mm:ss" + "." + StringUtils.repeat("S", p));
                seedConfig.setPrecision(p);
                seedConfig.setRangeForm("2000-01-01 00:00:00.000");
                seedConfig.setRangeTo("2030-12-31 23:59:59.999");
                return new TypeSrw(seedFactory, seedConfig, Types.TIMESTAMP);
            }
            case "datetimeoffset": {
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.OffsetDateTime);
                seedConfig.setTypeHandler(new SqlServerOffsetDateTimeTypeHandler());
                seedConfig.setGenType(GenType.Random);
                int p = Math.max(jdbcColumn.getDecimalDigits(), 3);
                seedConfig.setDateFormat("yyyy-MM-dd HH:mm:ss" + "." + StringUtils.repeat("S", p));
                seedConfig.setPrecision(p);
                seedConfig.setRangeForm("2000-01-01 00:00:00.000");
                seedConfig.setRangeTo("2030-12-31 23:59:59.999");
                seedConfig.setZoneForm("-08:00");
                seedConfig.setZoneTo("+08:00");
                return new TypeSrw(seedFactory, seedConfig, Types.TIMESTAMP_WITH_TIMEZONE);
            }
            case "timestamp": {
                BytesSeedFactory seedFactory = new BytesSeedFactory();
                BytesSeedConfig seedConfig = seedFactory.newConfig();
                TypeSrw seedAndWriter = new TypeSrw(seedFactory, seedConfig, Types.VARBINARY);
                seedAndWriter.getDefaultIgnoreAct().add(UseFor.Insert);
                seedAndWriter.getDefaultIgnoreAct().add(UseFor.UpdateSet);
                return seedAndWriter;
            }
            case "binary":
            case "varbinary":
            case "image": {
                BytesSeedFactory seedFactory = new BytesSeedFactory();
                BytesSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(1);
                seedConfig.setMaxLength(safeMaxLength(jdbcColumn.getColumnSize(), 32, 128));

                if (StringUtils.contains(columnType, "image")) {
                    columnConfig.addValue(FakerConfigEnum.WHERE_COL_TEMPLATE.getConfigKey(), "cast({name} as varbinary(max))");
                }
                return new TypeSrw(seedFactory, seedConfig, Types.VARBINARY);
            }
            case "uniqueidentifier": {
                GuidSeedFactory seedFactory = new GuidSeedFactory();
                GuidSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(GuidType.String36);
                return new TypeSrw(seedFactory, seedConfig, Types.VARCHAR);
            }
            case "sysname": {
                StringSeedFactory seedFactory = new StringSeedFactory();
                StringSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(4);
                seedConfig.setMaxLength(64);
                seedConfig.setCharacterSet(new HashSet<>(Collections.singletonList(CharacterSet.LETTER_SMALL)));
                return new TypeSrw(seedFactory, seedConfig, Types.VARCHAR);
            }
            case "geography":
            case "geometry":
            case "xml":
            case "hierarchyid":
            case "sql_variant":
            default: {
                throw new UnsupportedOperationException("unsupported columnName " + jdbcColumn.getColumnName()//
                        + ", columnType '" + columnType + "'");
            }
        }
    }
}
