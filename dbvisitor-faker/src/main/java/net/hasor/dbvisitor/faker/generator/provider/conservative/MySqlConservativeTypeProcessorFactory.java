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
package net.hasor.dbvisitor.faker.generator.provider.conservative;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.setting.SettingNode;
import net.hasor.dbvisitor.faker.FakerConfigEnum;
import net.hasor.dbvisitor.faker.generator.TypeProcessor;
import net.hasor.dbvisitor.faker.generator.UseFor;
import net.hasor.dbvisitor.faker.generator.provider.AbstractMySqlTypeProcessorFactory;
import net.hasor.dbvisitor.faker.meta.JdbcColumn;
import net.hasor.dbvisitor.faker.seed.bytes.BytesSeedConfig;
import net.hasor.dbvisitor.faker.seed.bytes.BytesSeedFactory;
import net.hasor.dbvisitor.faker.seed.date.*;
import net.hasor.dbvisitor.faker.seed.enums.EnumSeedConfig;
import net.hasor.dbvisitor.faker.seed.enums.EnumSeedFactory;
import net.hasor.dbvisitor.faker.seed.geometry.*;
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
public class MySqlConservativeTypeProcessorFactory extends AbstractMySqlTypeProcessorFactory {
    @Override
    public TypeProcessor createSeedFactory(JdbcColumn jdbcColumn, SettingNode columnConfig) {
        String columnType = jdbcColumn.getColumnType().toLowerCase();
        if (StringUtils.isBlank(columnType)) {
            return defaultSeedFactory(jdbcColumn);
        }
        switch (columnType) {
            case "bit": {
                StringSeedFactory seedFactory = new StringSeedFactory();
                StringSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(1);
                seedConfig.setMaxLength(safeMaxLength(jdbcColumn.getColumnSize(), 8, 24));
                seedConfig.setCharacterSet(new HashSet<>(Collections.singletonList(CharacterSet.BIT)));
                seedConfig.setTypeHandler(new MySqlBitAsStringTypeHandler());
                return new TypeProcessor(seedFactory, seedConfig, Types.VARCHAR);
            }
            case "tinyint":
            case "tinyint unsigned": {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.addMinMax(new BigDecimal("0"), new BigDecimal("100"));
                return new TypeProcessor(seedFactory, seedConfig, Types.INTEGER);
            }
            case "smallint":
            case "smallint unsigned":
            case "mediumint":
            case "mediumint unsigned": {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.addMinMax(new BigDecimal("0"), new BigDecimal("10000"));
                return new TypeProcessor(seedFactory, seedConfig, Types.INTEGER);
            }
            case "int":
            case "int unsigned": {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.addMinMax(new BigDecimal("0"), new BigDecimal("1000000"));

                if (StringUtils.contains(columnType, "unsigned")) {
                    seedConfig.setNumberType(NumberType.Long);
                    return new TypeProcessor(seedFactory, seedConfig, Types.BIGINT);
                } else {
                    return new TypeProcessor(seedFactory, seedConfig, Types.INTEGER);
                }
            }
            case "bigint":
            case "bigint unsigned": {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Long);
                seedConfig.addMinMax(new BigDecimal("0"), new BigDecimal("100000000"));

                if (StringUtils.contains(columnType, "unsigned")) {
                    seedConfig.setNumberType(NumberType.BigInt);
                }
                return new TypeProcessor(seedFactory, seedConfig, Types.BIGINT);
            }
            case "decimal":
            case "decimal unsigned":
            case "float":
            case "float unsigned":
            case "double":
            case "double unsigned": {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Decimal);
                seedConfig.addMinMax(new BigDecimal("0.0"), new BigDecimal("9999.999"));
                seedConfig.setScale(Math.min(jdbcColumn.getDecimalDigits(), 3));
                seedConfig.setAbs(true);
                return new TypeProcessor(seedFactory, seedConfig, Types.DECIMAL);
            }
            case "date": {
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.String);
                seedConfig.setGenType(GenType.Random);
                seedConfig.setDateFormat("yyyy-MM-dd");
                seedConfig.setRangeForm("2000-01-01");
                seedConfig.setRangeTo("2030-12-31");
                return new TypeProcessor(seedFactory, seedConfig, Types.DATE);
            }
            case "datetime":
            case "timestamp": {
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.LocalDateTime);
                seedConfig.setGenType(GenType.Random);
                seedConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
                seedConfig.setPrecision(0);
                seedConfig.setRangeForm("2000-01-01 00:00:00");
                seedConfig.setRangeTo("2030-12-31 23:59:59");
                return new TypeProcessor(seedFactory, seedConfig, Types.TIMESTAMP);
            }
            case "time": {
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.String);
                seedConfig.setGenType(GenType.Random);
                seedConfig.setDateFormat("HH:mm:ss");
                seedConfig.setPrecision(0);
                seedConfig.setRangeForm("00:00:00");
                seedConfig.setRangeTo("23:59:59");
                return new TypeProcessor(seedFactory, seedConfig, Types.TIME);
            }
            case "year": {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                seedConfig.addMinMax(new BigDecimal("2000"), new BigDecimal("2030"));
                return new TypeProcessor(seedFactory, seedConfig, Types.INTEGER);
            }
            case "char":
            case "varchar":
            case "tinytext":
            case "text":
            case "mediumtext":
            case "longtext": {
                StringSeedFactory seedFactory = new StringSeedFactory();
                StringSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(1);
                seedConfig.setMaxLength(safeMaxLength(jdbcColumn.getColumnSize(), 10, 100));
                seedConfig.setCharacterSet(new HashSet<>(Collections.singletonList(CharacterSet.LETTER_NUMBER)));
                return new TypeProcessor(seedFactory, seedConfig, Types.VARCHAR);
            }
            case "binary":
            case "varbinary":
            case "mediumblob":
            case "tinyblob":
            case "blob":
            case "longblob": {
                BytesSeedFactory seedFactory = new BytesSeedFactory();
                BytesSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setMinLength(1);
                seedConfig.setMaxLength(safeMaxLength(jdbcColumn.getColumnSize(), 4, 16));
                return new TypeProcessor(seedFactory, seedConfig, Types.VARBINARY);
            }
            case "enum":
            case "set": {
                EnumSeedFactory seedFactory = new EnumSeedFactory();
                EnumSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDict(new HashSet<>());
                return new TypeProcessor(seedFactory, seedConfig, Types.VARCHAR);
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
                TypeProcessor typeSrw = new TypeProcessor(seedFactory, seedConfig, Types.VARCHAR);
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
