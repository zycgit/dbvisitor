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
import net.hasor.dbvisitor.faker.seed.number.NumberSeedConfig;
import net.hasor.dbvisitor.faker.seed.number.NumberSeedFactory;
import net.hasor.dbvisitor.faker.seed.number.NumberType;
import net.hasor.dbvisitor.faker.seed.string.CharacterSet;
import net.hasor.dbvisitor.faker.seed.string.StringSeedConfig;
import net.hasor.dbvisitor.faker.seed.string.StringSeedFactory;

import java.sql.Types;
import java.util.Collections;
import java.util.HashSet;

/**
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
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
            case Types.BIT: {
                StringSeedFactory seedFactory = new StringSeedFactory();
                StringSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setCharacterSet(new HashSet<>(Collections.singletonList(CharacterSet.BIT)));
                return new TypeSrw(seedFactory, seedConfig, jdbcType);
            }
            case Types.BOOLEAN: {
                BooleanSeedFactory seedFactory = new BooleanSeedFactory();
                BooleanSeedConfig seedConfig = seedFactory.newConfig();
                return new TypeSrw(seedFactory, seedConfig, jdbcType);
            }
            case Types.TINYINT: {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Byte);
                return new TypeSrw(seedFactory, seedConfig, jdbcType);
            }
            case Types.SMALLINT: {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Short);
                return new TypeSrw(seedFactory, seedConfig, jdbcType);
            }
            case Types.INTEGER: {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Integer);
                return new TypeSrw(seedFactory, seedConfig, jdbcType);
            }
            case Types.BIGINT: {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Long);
                return new TypeSrw(seedFactory, seedConfig, jdbcType);
            }
            case Types.FLOAT:
            case Types.REAL: {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Float);
                return new TypeSrw(seedFactory, seedConfig, jdbcType);
            }
            case Types.DOUBLE: {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Double);
                return new TypeSrw(seedFactory, seedConfig, jdbcType);
            }
            case Types.NUMERIC:
            case Types.DECIMAL: {
                NumberSeedFactory seedFactory = new NumberSeedFactory();
                NumberSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setNumberType(NumberType.Decimal);
                return new TypeSrw(seedFactory, seedConfig, jdbcType);
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
            case Types.DATE: {
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.SqlDate);
                return new TypeSrw(seedFactory, seedConfig, jdbcType);
            }
            case Types.TIME: {
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.SqlTime);
                return new TypeSrw(seedFactory, seedConfig, jdbcType);
            }
            case Types.TIMESTAMP: {
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.SqlTimestamp);
                return new TypeSrw(seedFactory, seedConfig, jdbcType);
            }
            case Types.TIME_WITH_TIMEZONE: {
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.OffsetTime);
                return new TypeSrw(seedFactory, seedConfig, jdbcType);
            }
            case Types.TIMESTAMP_WITH_TIMEZONE: {
                DateSeedFactory seedFactory = new DateSeedFactory();
                DateSeedConfig seedConfig = seedFactory.newConfig();
                seedConfig.setDateType(DateType.OffsetDateTime);
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
