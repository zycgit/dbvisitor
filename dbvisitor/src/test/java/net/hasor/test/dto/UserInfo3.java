/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.dto;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.types.handler.json.JsonUseForFastjson2TypeHandler;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
 */
@Table("user_info")
public class UserInfo3 extends UserInfo2 {

    @Column(value = "futures", typeHandler = JsonUseForFastjson2TypeHandler.class)
    private UserFutures futures;

    public UserFutures getFutures() {
        return this.futures;
    }

    public void setFutures(UserFutures futures) {
        this.futures = futures;
    }
}
