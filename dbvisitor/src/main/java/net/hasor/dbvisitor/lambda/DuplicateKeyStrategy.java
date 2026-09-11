/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.lambda;
/**
 * 遇到重复 Key 的策略
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-10-25
 */
public enum DuplicateKeyStrategy {
    /** 使用标准 insert into */
    Into,
    /** 新值替代旧值 例如 mysql 的 replace into ,以及 upsert */
    Update,
    /** 忽略插入 例如 mysql 的 insert ignore */
    Ignore,
}
