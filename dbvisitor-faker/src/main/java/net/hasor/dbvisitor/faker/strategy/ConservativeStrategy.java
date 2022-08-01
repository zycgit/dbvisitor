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
package net.hasor.dbvisitor.faker.strategy;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.DateFormatType;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.JdbcUtils;
import net.hasor.dbvisitor.faker.generator.FakerTable;
import net.hasor.dbvisitor.faker.meta.JdbcColumn;
import net.hasor.dbvisitor.faker.seed.SeedConfig;
import net.hasor.dbvisitor.faker.seed.bytes.BytesSeedConfig;
import net.hasor.dbvisitor.faker.seed.date.DateSeedConfig;
import net.hasor.dbvisitor.faker.seed.date.DateType;
import net.hasor.dbvisitor.faker.seed.date.GenType;
import net.hasor.dbvisitor.faker.seed.number.NumberSeedConfig;
import net.hasor.dbvisitor.faker.seed.number.NumberType;
import net.hasor.dbvisitor.faker.seed.string.Characters;
import net.hasor.dbvisitor.faker.seed.string.StringSeedConfig;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static net.hasor.dbvisitor.faker.seed.string.CharacterSet.LETTER_NUMBER;

/**
 * 保守的生成策略，该策略会用一个相对最小的范围涵盖住大多数类型的数据范围。以下带 * 的均视表字段情况为准
 * <li>Date：基于系统当前时间，+/- 200年</li>
 * <li>String(*)：0 - 最大不超过 100 个字符（默认字符集：大写字母、小写字母、数字）</li>
 * <li>Number：0 - 100</li>
 * <li>Decimal(*)：最大不超过 9 位精度，默认 4 位小数</li>
 * <li>Bytes(*)：0 - 最大不超过 100 个字节</li>
 * <li>NULL：如果列允许为空，则生成的数据 20% 为空</li>
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class ConservativeStrategy implements Strategy {
    @Override
    public boolean ignoreColumn(String dbType, FakerTable fakerTable, JdbcColumn refer) {
        return false;
    }

    @Override
    public void applyConfig(String dbType, FakerTable fakerTable, SeedConfig seedConfig, JdbcColumn refer) {
        if (Boolean.TRUE.equals(refer.getNullable())) {
            seedConfig.setAllowNullable(true);
            seedConfig.setNullableRatio(20f);
        }

        switch (seedConfig.getSeedType()) {
            case Bytes: {
                BytesSeedConfig bytesSeedConfig = (BytesSeedConfig) seedConfig;
                Integer columnSize = refer.getColumnSize();

                if (columnSize == null || columnSize > 100) {
                    columnSize = 100;
                }

                bytesSeedConfig.setMinLength(0);
                bytesSeedConfig.setMaxLength(columnSize);
                return;
            }
            case Date: {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateFormatType.s_yyyyMMdd_HHmmss.getDatePattern());
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime rangeForm = now.plusYears(-200);
                LocalDateTime rangeTo = now.plusYears(+200);

                DateSeedConfig dateSeedConfig = (DateSeedConfig) seedConfig;
                dateSeedConfig.setGenType(GenType.Random);
                dateSeedConfig.setDateType(DateType.JavaDate);
                dateSeedConfig.setRangeForm(formatter.format(rangeForm));
                dateSeedConfig.setRangeTo(formatter.format(rangeTo));

                // mysql TIMESTAMP has a range of '1970-01-01 00:00:01' UTC to '2038-01-19 03:14:07' UTC
                if (StringUtils.equalsIgnoreCase(JdbcUtils.MYSQL, dbType)) {
                    if (StringUtils.equalsIgnoreCase(refer.getColumnType(), "TIMESTAMP")) {
                        OffsetDateTime utcStart = OffsetDateTime.of(LocalDateTime.of(1970, 1, 1, 0, 0, 1), ZoneOffset.UTC);
                        OffsetDateTime utcEnd = OffsetDateTime.of(LocalDateTime.of(2038, 1, 19, 3, 14, 7), ZoneOffset.UTC);

                        rangeForm = utcStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                        rangeTo = utcEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

                        dateSeedConfig.setRangeForm(formatter.format(rangeForm));
                        dateSeedConfig.setRangeTo(formatter.format(rangeTo));
                        dateSeedConfig.setZoneForm("Z");
                        dateSeedConfig.setZoneTo("Z");
                    }
                }
                return;
            }
            case Number: {
                NumberSeedConfig numberSeedConfig = (NumberSeedConfig) seedConfig;
                numberSeedConfig.setMin(BigDecimal.valueOf(0));
                numberSeedConfig.setMax(BigDecimal.valueOf(100));
                if (numberSeedConfig.getNumberType() == NumberType.BigDecimal) {
                    Integer precision = refer.getColumnSize();
                    Integer scale = refer.getDecimalDigits();
                    precision = precision == null ? 9 : Math.min(precision, 9);
                    scale = scale == null ? 4 : scale;

                    if ((precision - scale) < 0) {
                        scale = precision / 2;
                    }

                    numberSeedConfig.setPrecision(precision);
                    numberSeedConfig.setScale(scale);
                }
                return;
            }
            case String: {
                StringSeedConfig stringSeedConfig = (StringSeedConfig) seedConfig;
                Set<Characters> characters = stringSeedConfig.getCharacterSet();
                if (CollectionUtils.isEmpty(characters)) {
                    stringSeedConfig.setCharacterSet(new HashSet<>(Collections.singletonList(LETTER_NUMBER)));
                }
                Integer columnSize = refer.getColumnSize();
                if (columnSize == null || columnSize > 100 || columnSize < 0) {
                    columnSize = 100;
                }
                stringSeedConfig.setMinLength(0);
                stringSeedConfig.setMaxLength(columnSize);

                // oracle null and "" is same
                if (StringUtils.equalsIgnoreCase(JdbcUtils.ORACLE, dbType) && stringSeedConfig.isAllowNullable()) {
                    stringSeedConfig.setAllowEmpty(false);
                }

                return;
            }
            case Enums:
            case Boolean:
            default:
                return;
        }
    }
}