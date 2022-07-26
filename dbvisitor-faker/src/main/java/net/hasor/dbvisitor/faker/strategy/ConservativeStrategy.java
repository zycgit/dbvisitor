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
import net.hasor.cobble.DateFormatType;
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
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static net.hasor.dbvisitor.faker.seed.string.StandardCharacterSet.*;

/**
 * 保守的生成策略，该策略会用一个相对最小的范围涵盖住大多数类型的数据范围。
 * <li>Date：基于系统当前时间，+/- 200年</li>
 * <li>String：大写字母、小写字母、数字</li>
 * <li>Number：0 - 100</li>
 * <li>Bytes：0 - 100 个字节</li>
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class ConservativeStrategy implements Strategy {
    @Override
    public void applyConfig(SeedConfig seedConfig, JdbcColumn refer) {
        if (Boolean.TRUE.equals(refer.getNullable())) {
            seedConfig.setAllowNullable(true);
            seedConfig.setNullableRatio(20f);
        }

        switch (seedConfig.getSeedType()) {
            case Bytes: {
                BytesSeedConfig bytesSeedConfig = (BytesSeedConfig) seedConfig;
                Integer columnSize = refer.getColumnSize();

                if (columnSize == null || columnSize < 0) {
                    columnSize = 200;
                } else if (columnSize > 4096) {
                    columnSize = 4096;
                }

                bytesSeedConfig.setMinLength(Math.min(columnSize / 10, 10));
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
                dateSeedConfig.setDateType(DateType.SqlDate);
                dateSeedConfig.setRangeForm(formatter.format(rangeForm));
                dateSeedConfig.setRangeTo(formatter.format(rangeTo));
                return;
            }
            case Number: {
                NumberSeedConfig numberSeedConfig = (NumberSeedConfig) seedConfig;
                numberSeedConfig.setMin(BigDecimal.valueOf(0));
                numberSeedConfig.setMax(BigDecimal.valueOf(100));
                if (numberSeedConfig.getNumberType() == NumberType.Decimal) {
                    numberSeedConfig.setPrecision(5);
                    numberSeedConfig.setScale(4);
                }
                return;
            }
            case String: {
                StringSeedConfig stringSeedConfig = (StringSeedConfig) seedConfig;
                Set<Characters> characters = stringSeedConfig.getCharacterSet();
                if (characters == null || characters.isEmpty()) {
                    stringSeedConfig.setCharacterSet(new HashSet<>(Arrays.asList(CAPITAL_LETTER, SMALL_LETTER, NUMERIC)));
                    stringSeedConfig.setMinLength(10);
                    stringSeedConfig.setMaxLength(100);
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