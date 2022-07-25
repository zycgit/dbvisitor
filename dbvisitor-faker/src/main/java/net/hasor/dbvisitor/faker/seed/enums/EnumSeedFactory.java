/*
 * Copyright 2002-2010 the original author or authors.
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
package net.hasor.dbvisitor.faker.seed.enums;

import net.hasor.dbvisitor.faker.seed.RandomUtils;
import net.hasor.dbvisitor.faker.seed.SeedConfig;
import net.hasor.dbvisitor.faker.seed.SeedFactory;

import java.util.Set;
import java.util.function.Supplier;

/**
 * 枚举类型的 SeedFactory
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class EnumSeedFactory implements SeedFactory<EnumSeedConfig, String> {

    @Override
    public SeedConfig newConfig() {
        return new EnumSeedConfig();
    }

    @Override
    public Supplier<String> createSeed(EnumSeedConfig seedConfig) {
        Set<String> dict = seedConfig.getDict();
        boolean allowNullable = seedConfig.isAllowNullable();
        Float nullableRatio = seedConfig.getNullableRatio();

        if (!allowNullable && dict.isEmpty()) {
            throw new IllegalStateException("allowNullable is false but, dict is empty");
        }

        if (allowNullable && nullableRatio == null) {
            dict.add(null);
        }

        String[] dictArrays = dict.toArray(new String[0]);
        int max = dictArrays.length;

        return () -> {
            if (nullableRatio != null && RandomUtils.nextFloat(0, 100) < nullableRatio) {
                return null;
            } else {
                return dictArrays[RandomUtils.nextInt(0, max)];
            }
        };
    }

}
