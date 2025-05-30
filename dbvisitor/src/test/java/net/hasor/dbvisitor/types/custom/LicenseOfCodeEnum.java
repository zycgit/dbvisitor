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
package net.hasor.dbvisitor.types.custom;
import net.hasor.dbvisitor.types.handler.string.EnumOfCode;

/**
 * 授权协议类型
 * @author 赵永春 (zyc@hasor.net)
 * @version 2016-08-11
 */
public enum LicenseOfCodeEnum implements EnumOfCode<LicenseOfCodeEnum> {
    Private(0, "Private"),//
    AGPLv3(1, "AGPLv3"),//
    GPLv3(2, "GPLv3"), //
    MPLv2(3, "MPLv2.0"), //
    Apache2(4, "Apache 2.0"),//
    MIT(5, "MIT"),//
    Unlicense(6, "Unlicense"),//
    Other(999, "其它"),//
    ;
    //
    private int    type;
    private String desc;

    LicenseOfCodeEnum(int type, String desc) {
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
    public String codeName() {
        return this.desc;
    }

    public LicenseOfCodeEnum valueOfCode(String name) {
        for (LicenseOfCodeEnum item : LicenseOfCodeEnum.values()) {
            if (item.codeName().equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }
}