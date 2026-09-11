/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.custom;
import net.hasor.dbvisitor.types.handler.string.EnumOfCode;

/**
 * 大小写不敏感
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-11-29
 */
public enum CharacterSensitiveEnum implements EnumOfCode<CharacterSensitiveEnum> {
    a,
    A;

    public CharacterSensitiveEnum valueOfCode(String name) {
        for (CharacterSensitiveEnum item : CharacterSensitiveEnum.values()) {
            if (item.name().equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }
}
