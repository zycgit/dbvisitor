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
package net.hasor.dbvisitor.faker.seed.guid;
import net.hasor.cobble.StringUtils;

/**
 * 数值 具体类型
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public enum GuidType {
    String32(String.class),
    String36(String.class),
    Timestamp(Long.class),
    Bytes(byte[].class);

    private final Class<?> dateType;

    GuidType(Class<?> dateType) {
        this.dateType = dateType;
    }

    public Class<?> getDateType() {
        return this.dateType;
    }

    public static GuidType valueOfCode(String name) {
        for (GuidType numberType : GuidType.values()) {
            if (StringUtils.equalsIgnoreCase(numberType.name(), name)) {
                return numberType;
            }
        }
        return null;
    }
}
