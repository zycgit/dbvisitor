/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.nxn.env;

import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import org.jetbrains.annotations.NotNull;

public final class MsSqlProfile extends AbstractDataSourceProfile {
    public static final MsSqlProfile INSTANCE = new MsSqlProfile();

    private MsSqlProfile() {
        super(features());
    }

    @NotNull
    private static String[] features() {
        // @formatter:off
        return new String[] {
            FeatureId.ARRAY,
            FeatureId.SEQUENCE,
            FeatureId.KNN,
            FeatureId.PROCEDURE_CURSOR_RESULT,
            FeatureId.XML_MAPPER_CALLABLE,
            FeatureId.VECTOR,
            FeatureId.POSTGRES_ON_CONFLICT,
            FeatureId.REPEATED_ORDER_BY_COLUMN,
            FeatureId.TRANSACTION_RELEASE_SAVEPOINT,
            FeatureId.CASE_SENSITIVE_IDENTIFIERS
        };
        // @formatter:on
    }

    @Override
    public DataSourceId id() {
        return DataSourceId.MSSQL;
    }

    @Override
    public String leftQualifier() {
        return "[";
    }

    @Override
    public String rightQualifier() {
        return "]";
    }

    @Override
    public String castToBigInt(String expression) {
        return "CAST(" + expression + " AS BIGINT)";
    }

    @Override
    public String datetimeColumnType() {
        return "DATETIME2(3)";
    }
}
