/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.nxn.env;

import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.capability.SupportStatus;
import org.jetbrains.annotations.NotNull;

public final class H2Profile extends AbstractDataSourceProfile {
    public static final H2Profile INSTANCE = new H2Profile();

    private H2Profile() {
        super(features());
    }

    @NotNull
    private static String[] features() {
        // @formatter:off
        return new String[] {
            FeatureId.KNN,
            FeatureId.PROCEDURE,
            // H2 callable functions expose row values, not REF_CURSOR OUT parameters.
            FeatureId.PROCEDURE_CURSOR_RESULT,
            FeatureId.XML_MAPPER_CALLABLE,
            FeatureId.VECTOR,
            FeatureId.DELIMITED_LOWERCASE_STANDARD_TABLE,
            FeatureId.POSTGRES_ON_CONFLICT,
            // H2 JDBC getDate loses the historical Asia/Shanghai offset; LocalDate is unaffected.
            FeatureId.TIME_EXTREME_DATE,
            FeatureId.LOWERCASE_STANDARD_RESULT_COLUMNS,
            FeatureId.MULTIPLE_RESULT_SETS
        };
        // @formatter:on
    }

    @Override
    public DataSourceId id() {
        return DataSourceId.H2;
    }

    @Override
    public SupportStatus support(String capabilityId) {
        if (CapabilityId.TYPE_TIME_EXTREME_DATE.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DRIVER;
        }
        return super.support(capabilityId);
    }

    @Override
    public String leftQualifier() {
        return "\"";
    }

    @Override
    public String rightQualifier() {
        return "\"";
    }

    @Override
    public String castToBigInt(String expression) {
        return "CAST(" + expression + " AS BIGINT)";
    }

    @Override
    public String datetimeColumnType() {
        return "TIMESTAMP";
    }
}
