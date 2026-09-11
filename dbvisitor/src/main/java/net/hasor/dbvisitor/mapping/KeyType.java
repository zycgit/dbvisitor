/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapping;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.mapping.keyseq.*;

/**
 * 主键生成策略枚举
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-12-01
 */
public enum KeyType {
    /** 不指定主键生成策略 */
    None(null),
    /** 不会主动生成自增，但会接收来自数据库的自增 */
    Auto(new AutoKeySeqHolderFactory()),
    /** 使用 32 位字符串的 UUID 填充数据 */
    UUID32(new Uuid32KeySeqHolderFactory()),
    /** 使用 36 位字符串的 UUID 填充数据 */
    UUID36(new Uuid36KeySeqHolderFactory()),
    /** 通过 @KeySequence 注解来确定数据库的 sequence */
    Sequence(new SeqKeySeqHolderFactory()),
    /** 通过 @KeyHolder 注解来自定义生成策略 */
    Holder(new HolderKeySeqHolderFactory());

    private final GeneratedKeyHandlerFactory factory;

    KeyType(GeneratedKeyHandlerFactory factory) {
        this.factory = factory;
    }

    public GeneratedKeyHandler createHolder(GeneratedKeyHandlerContext context) throws ClassNotFoundException {
        if (this.factory == null) {
            return null;
        }
        return this.factory.createHolder(context);
    }

    public static KeyType valueOfCode(String code) {
        for (KeyType keyType : KeyType.values()) {
            if (StringUtils.equalsIgnoreCase(keyType.name(), code)) {
                return keyType;
            }
        }
        return null;
    }
}
