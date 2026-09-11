/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.redis;
import net.hasor.dbvisitor.driver.AdapterRequest;

public class JedisRequest extends AdapterRequest {
    private final String  commandBody;
    private       boolean numKeysCheck = true;

    public JedisRequest(String commandBody) {
        this.commandBody = commandBody;
    }

    public String getCommandBody() {
        return commandBody;
    }

    public boolean isNumKeysCheck() {
        return this.numKeysCheck;
    }

    public void setNumKeysCheck(boolean numKeysCheck) {
        this.numKeysCheck = numKeysCheck;
    }
}
