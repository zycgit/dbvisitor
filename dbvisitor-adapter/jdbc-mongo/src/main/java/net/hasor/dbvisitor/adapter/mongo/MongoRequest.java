/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.mongo;
import net.hasor.dbvisitor.driver.AdapterRequest;

public class MongoRequest extends AdapterRequest {
    private final String  commandBody;
    private final boolean preRead;

    public MongoRequest(String commandBody, boolean preRead) {
        this.commandBody = commandBody;
        this.preRead = preRead;
    }

    public boolean isPreRead() {
        return this.preRead;
    }

    public String getCommandBody() {
        return commandBody;
    }
}
