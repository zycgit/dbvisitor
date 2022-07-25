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
package net.hasor.dbvisitor.faker.seed.bytes;

import net.hasor.dbvisitor.faker.seed.RandomUtils;
import net.hasor.dbvisitor.faker.seed.SeedConfig;
import net.hasor.dbvisitor.faker.seed.SeedFactory;

import java.util.function.Supplier;

/**
 * byte[] 类型的 SeedFactory
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class BytesSeedFactory implements SeedFactory<BytesSeedConfig, byte[]> {

    @Override
    public SeedConfig newConfig() {
        return new BytesSeedConfig();
    }

    @Override
    public Supplier<byte[]> createSeed(BytesSeedConfig seedConfig) {
        int maxLength = seedConfig.getMaxLength();
        int minLength = seedConfig.getMinLength();

        boolean allowNullable = seedConfig.isAllowNullable();
        Float nullableRatio = seedConfig.getNullableRatio();

        if (allowNullable && nullableRatio == null) {
            throw new IllegalStateException("allowNullable is true but, nullableRatio missing.");
        }

        return () -> {
            if (allowNullable) {
                if (RandomUtils.nextFloat(0, 100) < nullableRatio) {
                    return null;
                }
            }

            int length = RandomUtils.nextInt(minLength, maxLength);
            return RandomUtils.nextBytes(length);
        };
    }

}
