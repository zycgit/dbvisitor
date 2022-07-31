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

import net.hasor.dbvisitor.faker.seed.SeedConfig;
import net.hasor.dbvisitor.faker.seed.SeedType;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.util.HashSet;
import java.util.Set;

/**
 * 字符串类型的 SeedConfig
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class StringSeedConfig extends SeedConfig {
    private final TypeHandler<?>  TYPE_HANDLER = TypeHandlerRegistry.DEFAULT.getTypeHandler(String.class);
    private       Set<Characters> characterSet;
    private       int             minLength;
    private       int             maxLength;
    private       boolean         allowEmpty;

    public final SeedType getSeedType() {
        return SeedType.String;
    }

    @Override
    public TypeHandler<?> getTypeHandler() {
        return TYPE_HANDLER;
    }

    public Set<Characters> getCharacterSet() {
        return characterSet;
    }

    public void addCharacter(Characters character) {
        if (this.characterSet == null) {
            this.characterSet = new HashSet<>();
        }
        if (!this.characterSet.contains(character)) {
            this.characterSet.add(character);
        }
    }

    public void setCharacterSet(Set<Characters> characterSet) {
        this.characterSet = characterSet;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public boolean isAllowEmpty() {
        return allowEmpty;
    }

    public void setAllowEmpty(boolean allowEmpty) {
        this.allowEmpty = allowEmpty;
    }
}
