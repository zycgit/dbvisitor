/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.nxn.env;

import net.hasor.dbvisitor.test.nxn.capability.SupportStatus;

public interface DataSourceProfile {
    DataSourceId id();

    default String env() {
        return id().env();
    }

    boolean supportsFeature(String featureId);

    default SupportStatus support(String capabilityId) {
        return SupportStatus.SUPPORTED;
    }

    String leftQualifier();

    String rightQualifier();

    String castToBigInt(String expression);

    String datetimeColumnType();

    default String currentTimestampExpression() {
        return "CURRENT_TIMESTAMP";
    }
}
