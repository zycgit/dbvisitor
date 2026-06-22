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
