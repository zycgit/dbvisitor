/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import net.hasor.dbvisitor.faker.FakerConfigEnum;
import net.hasor.dbvisitor.faker.generator.TypeSrw;
import net.hasor.dbvisitor.faker.generator.UseFor;
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
import net.hasor.dbvisitor.types.handler.OffsetDateTimeForSqlTypeHandler;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashSet;

/**
 * https://docs.microsoft.com/zh-cn/sql/t-sql/data-types/data-types-transact-sql
 * https://docs.microsoft.com/zh-cn/sql/t-sql/data-types/time-transact-sql
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public class SqlServerTypeSrwFactory extends DefaultTypeSrwFactory {
    @Override
    public TypeSrw createSeedFactory(JdbcColumn jdbcColumn, SettingNode columnConfig) {
        String columnType = jdbcColumn.getColumnType();
        if (StringUtils.isBlank(columnType)) {
            return defaultSeedFactory(jdbcColumn);
        }
        switch (columnType) {
            case "bit": {
                // 1,0,NULL.
                BooleanSeedFactory seedFactory = new BooleanSeedFactory();
                BooleanSeedConfig seedConfig = seedFactory.newConfig();
                return new TypeSrw(seedFactory, seedConfig, Types.BOOLEAN);
            }
            case "tinyint": {
                // 0 to 255
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.setMin(new BigDecimal("0"));
                seedConfig.setMax(new BigDecimal("255"));
                return new TypeSrw(seedFactory, seedConfig, Types.TINYINT);
            }
            case "smallint": {
                // -2^15 (-32,768) to 2^15-1 (32,767)
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.setMin(new BigDecimal("-32768"));
                seedConfig.setMax(new BigDecimal("+32767"));
                return new TypeSrw(seedFactory, seedConfig, Types.SMALLINT);
            }
            case "int": {
                // -2^31 (-2,147,483,648) to 2^31-1 (2,147,483,647)
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.setMin(new BigDecimal("-2147483648"));
                seedConfig.setMax(new BigDecimal("+2147483647"));
                return new TypeSrw(seedFactory, seedConfig, Types.INTEGER);
            }
            case "bigint": {
                // -2^63 (-9,223,372,036,854,775,808) to 2^63-1 (9,223,372,036,854,775,807)
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Long);
                seedConfig.setMin(new BigDecimal("-9223372036854775808"));
                seedConfig.setMax(new BigDecimal("+9223372036854775807"));
                return new TypeSrw(seedFactory, seedConfig, Types.BIGINT);
            }
            case "smallmoney": {
                // - 214,748.3648 to 214,748.3647
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Float);
                seedConfig.setMin(new BigDecimal("-214748.3648"));//
                seedConfig.setMax(new BigDecimal("+214748.3647"));
                seedConfig.setScale(4);
                return new TypeSrw(seedFactory, seedConfig, Types.FLOAT);
            }
            case "money": {
                // -922,337,203,685,477.5808 to 922,337,203,685,477.5807
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Double);
                seedConfig.setMin(new BigDecimal("-922337203685477.5808"));
                seedConfig.setMax(new BigDecimal("+922337203685477.5807"));
                seedConfig.setScale(4);
                return new TypeSrw(seedFactory, seedConfig, Types.DECIMAL);
            }
            case "float": {
                // -1.79E+308 to -2.23E-308, 0 and 2.23E-308 to 1.79E+308
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Double);
                seedConfig.setMin(new BigDecimal("2.23E-308"));
                seedConfig.setMax(new BigDecimal("1.79E+308"));
                return new TypeSrw(seedFactory, seedConfig, Types.DECIMAL);
            }
            case "real": {
                // -3.40E+38 to -1.18E-38, 0 and 1.18E-38 to 3.40E+38
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Double);
                seedConfig.setMin(new BigDecimal("1.18E-38"));
                seedConfig.setMax(new BigDecimal("3.40E+38"));
                return new TypeSrw(seedFactory, seedConfig, Types.REAL);
            }
            case "numeric":
            case "decimal": {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Decimal);
                seedConfig.setPrecision(jdbcColumn.getColumnSize());
                seedConfig.setScale(jdbcColumn.getDecimalDigits());
                return new TypeSrw(seedFactory, seedConfig, Types.DECIMAL);
            }
            case "nchar":
            case "nvarchar": {
                StringSeedFactory seedFactory = new StringSeedFactory();
                StringSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(0);
                seedConfig.setMaxLength(safeMaxLength(jdbcColumn.getColumnSize(), 100, 1000));
                seedConfig.setCharacterSet(new HashSet<>(Collections.singletonList(CharacterSet.LETTER_NUMBER)));
                return new TypeSrw(seedFactory, seedConfig, Types.NVARCHAR);
            }
            case "ntext": {
                StringSeedFactory seedFactory = new StringSeedFactory();
                StringSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(0);
                seedConfig.setMaxLength(safeMaxLength(jdbcColumn.getColumnSize(), 100, 1000));
                seedConfig.setCharacterSet(new HashSet<>(Collections.singletonList(CharacterSet.LETTER_NUMBER)));
                columnConfig.addValue(FakerConfigEnum.WHERE_COL_TEMPLATE.getConfigKey(), "cast({name} as nvarchar(max))");
                return new TypeSrw(seedFactory, seedConfig, Types.LONGNVARCHAR);
            }
            case "char":
            case "varchar": {
                StringSeedFactory seedFactory = new StringSeedFactory();
                StringSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(0);
                seedConfig.setMaxLength(safeMaxLength(jdbcColumn.getColumnSize(), 100, 1000));
                seedConfig.setCharacterSet(new HashSet<>(Collections.singletonList(CharacterSet.LETTER_NUMBER)));
                return new TypeSrw(seedFactory, seedConfig, Types.VARCHAR);
            }
            case "text": {
                StringSeedFactory seedFactory = new StringSeedFactory();
                StringSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(0);
                seedConfig.setMaxLength(safeMaxLength(jdbcColumn.getColumnSize(), 100, 1000));
                seedConfig.setCharacterSet(new HashSet<>(Collections.singletonList(CharacterSet.LETTER_NUMBER)));
                columnConfig.addValue(FakerConfigEnum.WHERE_COL_TEMPLATE.getConfigKey(), "cast({name} as varchar(max))");
                return new TypeSrw(seedFactory, seedConfig, Types.LONGVARCHAR);
            }
            case "date": {
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.String);
                seedConfig.setGenType(GenType.Random);
                seedConfig.setDateFormat("yyyy-MM-dd");
                seedConfig.setRangeForm("0001-01-01");
                seedConfig.setRangeTo("9999-12-31");
                return new TypeSrw(seedFactory, seedConfig, Types.DATE);
            }
            case "time": {
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.String);
                seedConfig.setGenType(GenType.Random);
                int p = safeMaxLength(jdbcColumn.getDecimalDigits(), 3, 7);
                seedConfig.setDateFormat("HH:mm:ss" + ((p > 0) ? ("." + StringUtils.repeat("S", p)) : ""));
                seedConfig.setPrecision(Math.max(p, 0));
                seedConfig.setRangeForm("00:00:00.0000000");
                seedConfig.setRangeTo("23:59:59.9999999");
                //columnConfig.addValue(FakerConfigEnum.WHERE_COL_TEMPLATE.getConfigKey(), "cast({name} as datetime2(7))");
                return new TypeSrw(seedFactory, seedConfig, Types.TIME);
            }
            case "smalldatetime": {
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.LocalDateTime);
                seedConfig.setGenType(GenType.Random);
                seedConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
                seedConfig.setPrecision(0);
                seedConfig.setRangeForm("1900-01-01 00:00:00");
                seedConfig.setRangeTo("2079-06-06 23:59:00");
                return new TypeSrw(seedFactory, seedConfig, Types.TIMESTAMP);
            }
            case "datetime": {
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.LocalDateTime);
                seedConfig.setGenType(GenType.Random);
                int p = safeMaxLength(jdbcColumn.getDecimalDigits(), 1, 3);
                seedConfig.setDateFormat("yyyy-MM-dd HH:mm:ss" + ((p > 0) ? ("." + StringUtils.repeat("S", p)) : ""));
                seedConfig.setPrecision(Math.max(p, 0));
                seedConfig.setRangeForm("1753-01-01 00:00:00.000");
                seedConfig.setRangeTo("9999-12-31 23:59:59.997");
                return new TypeSrw(seedFactory, seedConfig, Types.TIMESTAMP);
            }
            case "datetime2": {
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.LocalDateTime);
                seedConfig.setGenType(GenType.Random);
                int p = safeMaxLength(jdbcColumn.getDecimalDigits(), 3, 7);
                seedConfig.setDateFormat("yyyy-MM-dd HH:mm:ss" + ((p > 0) ? ("." + StringUtils.repeat("S", p)) : ""));
                seedConfig.setPrecision(Math.max(p, 0));
                seedConfig.setRangeForm("0001-01-01 00:00:00.0000000");
                seedConfig.setRangeTo("9999-12-31 23:59:59.9999999");
                return new TypeSrw(seedFactory, seedConfig, Types.TIMESTAMP);
            }
            case "datetimeoffset": {
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.OffsetDateTime);
                seedConfig.setTypeHandler(new SqlServerOffsetDateTimeTypeHandler());
                seedConfig.setGenType(GenType.Random);
                int p = safeMaxLength(jdbcColumn.getDecimalDigits(), 3, 7);
                seedConfig.setDateFormat("yyyy-MM-dd HH:mm:ss" + ((p > 0) ? ("." + StringUtils.repeat("S", p)) : ""));
                seedConfig.setPrecision(Math.max(p, 0));
                seedConfig.setRangeForm("0001-01-01 00:00:00.0000000");
                seedConfig.setRangeTo("9999-12-31 23:59:59.9999999");
                seedConfig.setZoneForm("-14:00");
                seedConfig.setZoneTo("+14:00");
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
            case "varbinary": {
                BytesSeedFactory seedFactory = new BytesSeedFactory();
                BytesSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(0);
                seedConfig.setMaxLength(safeMaxLength(jdbcColumn.getColumnSize(), 100, 4096));
                return new TypeSrw(seedFactory, seedConfig, Types.VARBINARY);
            }
            case "image": {
                BytesSeedFactory seedFactory = new BytesSeedFactory();
                BytesSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(0);
                seedConfig.setMaxLength(safeMaxLength(jdbcColumn.getColumnSize(), 100, 4096));
                columnConfig.addValue(FakerConfigEnum.WHERE_COL_TEMPLATE.getConfigKey(), "cast({name} as varbinary(max))");
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
                seedConfig.setMinLength(1);
                seedConfig.setMaxLength(128);
                seedConfig.setCharacterSet(new HashSet<>(Collections.singletonList(CharacterSet.LETTER_SMALL)));
                return new TypeSrw(seedFactory, seedConfig, Types.NVARCHAR);
            }
            case "xml":
            case "geography":
            case "geometry":
            case "hierarchyid":
            case "sql_variant":
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

    private static class SqlServerOffsetDateTimeTypeHandler extends OffsetDateTimeForSqlTypeHandler {
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
}
