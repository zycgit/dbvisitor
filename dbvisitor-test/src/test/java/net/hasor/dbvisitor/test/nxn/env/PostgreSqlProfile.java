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

public final class PostgreSqlProfile extends AbstractDataSourceProfile {
    public static final PostgreSqlProfile INSTANCE = new PostgreSqlProfile();

    private PostgreSqlProfile() {
        super(features());
    }

    @NotNull
    private static String[] features() {
        return new String[] { FeatureId.PROCEDURE_RESULT_SET };
    }

    @Override
    public SupportStatus support(String capabilityId) {
        // pgvector provides distance operators, not BM25 full-text scoring.
        if (CapabilityId.VECTOR_KNN_ORDER_BM25.equals(capabilityId) || CapabilityId.VECTOR_RANGE_BM25.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DATABASE;
        }
        return super.support(capabilityId);
    }

    @Override
    public DataSourceId id() {
        return DataSourceId.PG;
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
