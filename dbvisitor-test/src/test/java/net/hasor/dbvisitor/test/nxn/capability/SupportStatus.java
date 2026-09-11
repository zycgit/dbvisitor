/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.nxn.capability;

public enum SupportStatus {
    SUPPORTED,
    UNSUPPORTED_BY_DATABASE,
    UNSUPPORTED_BY_DRIVER,
    UNSUPPORTED_BY_DBVISITOR,
    NOT_IMPLEMENTED
}
