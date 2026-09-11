/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dynamic.dto;
import net.hasor.dbvisitor.types.handler.string.EnumOfValue;

/**
 * 授权协议类型
 * @author 赵永春 (zyc@hasor.net)
 * @version 2016-08-11
 */
public enum LicenseOfValueEnum implements EnumOfValue<LicenseOfValueEnum> {
    Private(0, "Private"),//
    AGPLv3(1, "AGPLv3"),//
    GPLv3(2, "GPLv3"), //
    MPLv2(3, "MPLv2.0"), //
    Apache2(4, "Apache 2.0"),//
    MIT(5, "MIT"),//
    Unlicense(6, "Unlicense"),//
    Other(999, "其它"),//
    ;
    private final int    type;
    private final String desc;

    LicenseOfValueEnum(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public int getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public int codeValue() {
        return this.type;
    }

    @Override
    public LicenseOfValueEnum valueOfCode(int codeValue) {
        for (LicenseOfValueEnum item : LicenseOfValueEnum.values()) {
            if (item.getType() == codeValue) {
                return item;
            }
        }
        return null;
    }
}
