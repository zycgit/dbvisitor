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
package net.hasor.dbvisitor.mapping;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.keyholder.CreateContext;
import net.hasor.dbvisitor.keyholder.KeySeqHolder;
import net.hasor.dbvisitor.keyholder.KeySeqHolderFactory;
import net.hasor.dbvisitor.keyholder.sequence.*;

/**
 * 键生成策略
 * @version : 2022-12-01
 * @author 赵永春 (zyc@hasor.net)
 */
public enum KeyTypeEnum {
    /** 不指定 */
    None(null),
    /** 不会主动生成自增，但会接收来自数据库的自增 */
    Auto(new AutoKeySeqHolderFactory()),
    /** 使用 32 位字符串的 UUID 填充数据 */
    UUID32(new Uuid32KeySeqHolderFactory()),
    /** 使用 36 位字符串的 UUID 填充数据 */
    UUID36(new Uuid36KeySeqHolderFactory()),
    /** 通过 @KeySequence 注解来确定数据库的 sequence  */
    Sequence(new SeqKeySeqHolderFactory()),
    /** 通过 @KeyHolder 注解来自定义生成策略 */
    Holder(new HolderKeySeqHolderFactory());

    private final KeySeqHolderFactory factory;

    KeyTypeEnum(KeySeqHolderFactory factory) {
        this.factory = factory;
    }

    public KeySeqHolder createHolder(CreateContext context) {
        if (this.factory == null) {
            return null;
        }
        return this.factory.createHolder(context);
    }

    public static KeyTypeEnum valueOfCode(String code) {
        for (KeyTypeEnum keyType : KeyTypeEnum.values()) {
            if (StringUtils.equalsIgnoreCase(keyType.name(), code)) {
                return keyType;
            }
        }
        return null;
    }
}
