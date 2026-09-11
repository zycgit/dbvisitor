/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.driver;
public enum JdbcArgMode {
    In(true, false),
    Out(false, true),
    InOut(true, true),
    ;

    private final boolean supportIn;
    private final boolean supportOut;

    JdbcArgMode(boolean supportIn, boolean supportOut) {
        this.supportIn = supportIn;
        this.supportOut = supportOut;
    }

    public boolean isIn() {
        return this.supportIn;
    }

    public boolean isOut() {
        return this.supportOut;
    }

}
