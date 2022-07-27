///*
// * Copyright 2015-2022 the original author or authors.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package net.hasor.dbvisitor.faker.strategy;
//import net.hasor.dbvisitor.faker.meta.JdbcColumn;
//import net.hasor.dbvisitor.faker.seed.SeedConfig;
//
///**
// * 极限的生成策略，该策略会力争将数据类型的极限范围涵盖进来。
// * @version : 2022-07-25
// * @author 赵永春 (zyc@hasor.net)
// */
//public class ExtremeStrategy implements Strategy {
//@Override
//public void applyConfig(SeedConfig seedConfig, JdbcColumn refer) {
//        if (Boolean.TRUE.equals(refer.getNullable())) {
//        seedConfig.setAllowNullable(true);
//        seedConfig.setNullableRatio(20f);
//        }
//
//        switch (seedConfig.getSeedType()) {
//        case Bytes: {
//        BytesSeedConfig bytesSeedConfig = (BytesSeedConfig) seedConfig;
//        Integer columnSize = refer.getColumnSize();
//
//        if (columnSize == null || columnSize < 0) {
//        columnSize = 200;
//        } else if (columnSize > 4096) {
//        columnSize = 4096;
//        }
//
//        bytesSeedConfig.setMinLength(Math.min(columnSize / 10, 10));
//        bytesSeedConfig.setMaxLength(columnSize);
//        return;
//        }
//        case Date: {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateFormatType.s_yyyyMMdd_HHmmss.getDatePattern());
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime rangeForm = now.plusYears(-200);
//        LocalDateTime rangeTo = now.plusYears(+200);
//
//        DateSeedConfig dateSeedConfig = (DateSeedConfig) seedConfig;
//        dateSeedConfig.setGenType(GenType.Random);
//        dateSeedConfig.setDateType(DateType.SqlDate);
//        dateSeedConfig.setRangeForm(formatter.format(rangeForm));
//        dateSeedConfig.setRangeTo(formatter.format(rangeTo));
//        return;
//        }
//        case Number: {
//        NumberSeedConfig numberSeedConfig = (NumberSeedConfig) seedConfig;
//        numberSeedConfig.setMin(BigDecimal.valueOf(0));
//        numberSeedConfig.setMax(BigDecimal.valueOf(100));
//        if (numberSeedConfig.getNumberType() == NumberType.Decimal) {
//        numberSeedConfig.setPrecision(9);
//        numberSeedConfig.setScale(4);
//        }
//        return;
//        }
//        case String: {
//        StringSeedConfig stringSeedConfig = (StringSeedConfig) seedConfig;
//        Set<Characters> characters = stringSeedConfig.getCharacterSet();
//        if (characters == null || characters.isEmpty()) {
//        stringSeedConfig.setCharacterSet(new HashSet<>(Arrays.asList(CAPITAL_LETTER, SMALL_LETTER, NUMERIC)));
//        stringSeedConfig.setMinLength(10);
//        stringSeedConfig.setMaxLength(100);
//        }
//        return;
//        }
//        case Enums:
//        case Boolean:
//default:
//        return;
//        }
//        }
//
//    @Override
//    public void applyConfig(SeedConfig seedConfig, JdbcColumn refer) {
//
//    }
//    // private SeedConfig createSeedConfig(JdbcSqlTypes jdbcType) {
//    //        switch (jdbcType) {
//    //            case BOOLEAN: {
//    //                return defaultBooleanSeedConfig();
//    //            }
//    //            case BIT: {
//    //                StringSeedConfig config = defaultStringSeedConfig();
//    //                config.setCharacterSet(new HashSet<>(Collections.singletonList(new BitCharacters())));
//    //                return config;
//    //            }
//    //            case TINYINT: {
//    //                NumberSeedConfig config = defaultNumberSeedConfig();
//    //                config.setNumberType(NumberType.Byte);
//    //                return config;
//    //            }
//    //            case SMALLINT: {
//    //                NumberSeedConfig config = defaultNumberSeedConfig();
//    //                config.setNumberType(NumberType.Sort);
//    //                return config;
//    //            }
//    //            case INTEGER: {
//    //                NumberSeedConfig config = defaultNumberSeedConfig();
//    //                config.setNumberType(NumberType.Integer);
//    //                return config;
//    //            }
//    //            case BIGINT: {
//    //                NumberSeedConfig config = new NumberSeedConfig();
//    //                config.setNumberType(NumberType.Long);
//    //                config.setMin(BigDecimal.valueOf(Long.MIN_VALUE));
//    //                config.setMax(BigDecimal.valueOf(Long.MAX_VALUE));
//    //                return config;
//    //            }
//    //            case FLOAT:
//    //            case REAL: {
//    //                NumberSeedConfig config = new NumberSeedConfig();
//    //                config.setNumberType(NumberType.Float);
//    //                config.setMin(BigDecimal.valueOf(Float.MIN_VALUE));
//    //                config.setMax(BigDecimal.valueOf(Float.MAX_VALUE));
//    //                return config;
//    //            }
//    //            case DOUBLE: {
//    //                NumberSeedConfig config = new NumberSeedConfig();
//    //                config.setNumberType(NumberType.Double);
//    //                config.setMin(BigDecimal.valueOf(Double.MIN_VALUE));
//    //                config.setMax(BigDecimal.valueOf(Double.MAX_VALUE));
//    //                return config;
//    //            }
//    //            case NUMERIC:
//    //            case DECIMAL: {
//    //                NumberSeedConfig config = new NumberSeedConfig();
//    //                config.setNumberType(NumberType.Decimal);
//    //                config.setMin(BigDecimal.valueOf(Double.MIN_VALUE));
//    //                config.setMax(BigDecimal.valueOf(Double.MAX_VALUE));
//    //                config.setPrecision(10);
//    //                config.setScale(4);
//    //                return config;
//    //            }
//    //            case CHAR:
//    //            case NCHAR: {
//    //                StringSeedConfig config = new StringSeedConfig();
//    //                config.setCharacterSet(new HashSet<>(Arrays.asList(CAPITAL_LETTER, SMALL_LETTER, NUMERIC)));
//    //                config.setMinLength(5);
//    //                config.setMaxLength(5);
//    //                return config;
//    //            }
//    //            case VARCHAR:
//    //            case NVARCHAR: {
//    //                StringSeedConfig config = new StringSeedConfig();
//    //                config.setCharacterSet(new HashSet<>(Arrays.asList(CAPITAL_LETTER, SMALL_LETTER, NUMERIC)));
//    //                config.setMinLength(10);
//    //                config.setMaxLength(100);
//    //                return config;
//    //            }
//    //            case LONGVARCHAR:
//    //            case LONGNVARCHAR:
//    //            case CLOB:
//    //            case NCLOB: {
//    //                StringSeedConfig config = new StringSeedConfig();
//    //                config.setCharacterSet(new HashSet<>(Arrays.asList(CAPITAL_LETTER, SMALL_LETTER, NUMERIC)));
//    //                config.setMinLength(512);
//    //                config.setMaxLength(2048);
//    //                return config;
//    //            }
//    //            case BINARY: {
//    //                BytesSeedConfig config = new BytesSeedConfig();
//    //                config.setMinLength(5);
//    //                config.setMaxLength(5);
//    //                return config;
//    //            }
//    //            case VARBINARY: {
//    //                BytesSeedConfig config = new BytesSeedConfig();
//    //                config.setMinLength(10);
//    //                config.setMaxLength(100);
//    //                return config;
//    //            }
//    //            case LONGVARBINARY:
//    //            case BLOB: {
//    //                BytesSeedConfig config = new BytesSeedConfig();
//    //                config.setMinLength(512);
//    //                config.setMaxLength(2048);
//    //                return config;
//    //            }
//    //            case DATE: {
//    //                DateSeedConfig config = new DateSeedConfig();
//    //                config.setGenType(GenType.Random);
//    //                config.setDateType(DateType.SqlDate);
//    //                return config;
//    //            }
//    //            case TIME: {
//    //                DateSeedConfig config = new DateSeedConfig();
//    //                config.setGenType(GenType.Random);
//    //                config.setDateType(DateType.SqlTime);
//    //                return config;
//    //            }
//    //            case TIMESTAMP: {
//    //                DateSeedConfig config = new DateSeedConfig();
//    //                config.setGenType(GenType.Random);
//    //                config.setDateType(DateType.SqlTimestamp);
//    //                return config;
//    //            }
//    //            case TIME_WITH_TIMEZONE: {
//    //                DateSeedConfig config = new DateSeedConfig();
//    //                config.setGenType(GenType.Random);
//    //                config.setDateType(DateType.OffsetTime);
//    //                return config;
//    //            }
//    //            case TIMESTAMP_WITH_TIMEZONE: {
//    //                DateSeedConfig config = new DateSeedConfig();
//    //                config.setGenType(GenType.Random);
//    //                config.setDateType(DateType.OffsetDateTime);
//    //                return config;
//    //            }
//    //            case SQLXML:
//    //            case STRUCT:
//    //            case ARRAY:
//    //            case DATALINK:
//    //            case NULL:
//    //            case OTHER:
//    //            case JAVA_OBJECT:
//    //            case DISTINCT:
//    //            case REF:
//    //            case ROWID:
//    //            case REF_CURSOR:
//    //            default:
//    //                return null;
//    //        }
//    //    }
//}
