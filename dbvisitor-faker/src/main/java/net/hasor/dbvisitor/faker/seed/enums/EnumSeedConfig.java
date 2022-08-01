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
package net.hasor.dbvisitor.faker.seed.enums;

import net.hasor.dbvisitor.faker.seed.SeedConfig;
import net.hasor.dbvisitor.faker.seed.SeedType;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 枚举类型的 SeedConfig
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class EnumSeedConfig extends SeedConfig {
    private Set<String> dict;

    public final SeedType getSeedType() {
        return SeedType.Enums;
    }

    @Override
    protected TypeHandler<?> defaultTypeHandler() {
        return TypeHandlerRegistry.DEFAULT.getTypeHandler(String.class);
    }

    public Set<String> getDict() {
        return dict;
    }

    public void setDict(Set<String> dict) {
        this.dict = dict;
    }

    public void setDictSet(String[] dictSet) {
        if (dictSet == null || dictSet.length == 0) {
            return;
        }
        setDict(new HashSet<>(Arrays.asList(dictSet)));
    }
}
