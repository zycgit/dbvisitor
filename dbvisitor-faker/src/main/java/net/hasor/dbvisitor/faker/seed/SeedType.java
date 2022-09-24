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
package net.hasor.dbvisitor.faker.seed;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.faker.seed.array.ArraySeedFactory;
import net.hasor.dbvisitor.faker.seed.bool.BooleanSeedFactory;
import net.hasor.dbvisitor.faker.seed.bytes.BytesSeedFactory;
import net.hasor.dbvisitor.faker.seed.date.DateSeedFactory;
import net.hasor.dbvisitor.faker.seed.enums.EnumSeedFactory;
import net.hasor.dbvisitor.faker.seed.guid.GuidSeedFactory;
import net.hasor.dbvisitor.faker.seed.number.NumberSeedFactory;
import net.hasor.dbvisitor.faker.seed.string.StringSeedFactory;

import java.util.function.Supplier;

/**
 * 类型
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public enum SeedType {
    Boolean(BooleanSeedFactory::new),
    Date(DateSeedFactory::new),
    String(StringSeedFactory::new),
    Number(NumberSeedFactory::new),
    Enums(EnumSeedFactory::new),
    Bytes(BytesSeedFactory::new),
    GID(GuidSeedFactory::new),
    Array(ArraySeedFactory::new),
    //    Struts,
    //    RelationId,
    Custom(null);

    private final Supplier<SeedFactory<? extends SeedConfig>> supplier;

    SeedType(Supplier<SeedFactory<? extends SeedConfig>> supplier) {
        this.supplier = supplier;
    }

    public SeedFactory<? extends SeedConfig> getSupplier() {
        return this.supplier != null ? this.supplier.get() : null;
    }

    public static SeedType valueOfCode(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        for (SeedType seedType : SeedType.values()) {
            if (StringUtils.equalsIgnoreCase(seedType.name(), name)) {
                return seedType;
            }
        }
        return null;
    }
}
