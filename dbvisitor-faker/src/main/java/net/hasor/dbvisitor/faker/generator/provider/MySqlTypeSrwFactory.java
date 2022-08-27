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

/**
 * 大小写敏感类别
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public class MySqlTypeSrwFactory extends DefaultTypeSrwFactory {

    //                // mysql TIMESTAMP has a range of '1970-01-01 00:00:01' UTC to '2038-01-19 03:14:07' UTC
    //                if (StringUtils.equalsIgnoreCase(JdbcUtils.MYSQL, dbType)) {
    //                    if (StringUtils.equalsIgnoreCase(refer.getColumnType(), "TIMESTAMP")) {
    //                        OffsetDateTime utcStart = OffsetDateTime.of(LocalDateTime.of(1970, 1, 1, 0, 0, 1), ZoneOffset.UTC);
    //                        OffsetDateTime utcEnd = OffsetDateTime.of(LocalDateTime.of(2038, 1, 19, 3, 14, 7), ZoneOffset.UTC);
    //
    //                        rangeForm = utcStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    //                        rangeTo = utcEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    //
    //                        dateSeedConfig.setRangeForm(formatter.format(rangeForm));
    //                        dateSeedConfig.setRangeTo(formatter.format(rangeTo));
    //                        dateSeedConfig.setZoneForm("Z");
    //                        dateSeedConfig.setZoneTo("Z");
    //                    }
    //                }
    @Override
    public TypeSrw createSeedFactory(JdbcColumn jdbcColumn, SettingNode columnConfig) {
        String columnType = jdbcColumn.getColumnType();
        if (StringUtils.isBlank(columnType)) {
            return defaultSeedFactory(jdbcColumn);
        }
        switch (columnType) {
            case "xml":
            default:
                return defaultSeedFactory(jdbcColumn);
        }
    }
}
