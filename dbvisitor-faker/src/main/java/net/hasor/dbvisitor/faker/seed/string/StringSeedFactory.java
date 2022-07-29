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
package net.hasor.dbvisitor.faker.seed.string;

import net.hasor.cobble.RandomUtils;
import net.hasor.dbvisitor.faker.seed.SeedConfig;
import net.hasor.dbvisitor.faker.seed.SeedFactory;
import net.hasor.dbvisitor.faker.seed.string.characters.GroupCharacters;

import java.util.Set;
import java.util.function.Supplier;

/**
 * 字符串类型的 SeedFactory
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class StringSeedFactory implements SeedFactory<StringSeedConfig, String> {
    @Override
    public SeedConfig newConfig() {
        return new StringSeedConfig();
    }

    @Override
    public Supplier<String> createSeed(StringSeedConfig seedConfig) {
        int maxLength = seedConfig.getMaxLength();
        int minLength = seedConfig.getMinLength();

        boolean allowEmpty = seedConfig.isAllowEmpty();
        boolean allowNullable = seedConfig.isAllowNullable();
        Float nullableRatio = seedConfig.getNullableRatio();

        if ((allowEmpty || allowNullable) && nullableRatio == null) {
            throw new IllegalStateException("allowNullable is true but, nullableRatio missing.");
        }

        Set<Characters> characterSet = seedConfig.getCharacterSet();
        if (characterSet.isEmpty()) {
            throw new IllegalStateException("characterSet missing.");
        }

        GroupCharacters characters = new GroupCharacters(characterSet.toArray(new Characters[0]));
        int characterCount = characters.getSize();

        return () -> {
            if ((allowEmpty || allowNullable) && RandomUtils.nextFloat(0, 100) < nullableRatio) {
                if (allowEmpty && allowNullable) {
                    return RandomUtils.nextBoolean() ? "" : null;
                } else {
                    return allowEmpty ? "" : null;
                }
            } else {

                int length = RandomUtils.nextInt(minLength, maxLength);
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < length; i++) {
                    int codePoint = RandomUtils.nextInt(0, characterCount);
                    builder.append(characters.getChar(codePoint));
                }
                return builder.toString();
            }
        };
    }

}
