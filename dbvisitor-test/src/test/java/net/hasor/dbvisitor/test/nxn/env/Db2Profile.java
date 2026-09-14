/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.nxn.env;

import org.jetbrains.annotations.NotNull;

import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.SupportStatus;

public final class Db2Profile extends AbstractDataSourceProfile {
    public static final Db2Profile INSTANCE = new Db2Profile();

    @Override
    public SupportStatus support(String capabilityId) {
        // JCC returns a null catalog placeholder, not a list of named databases.
        if (CapabilityId.JDBC_METADATA_CATALOGS.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DRIVER;
        }
        return super.support(capabilityId);
    }

    private Db2Profile() {
        super(features());
    }

    @NotNull
    private static String[] features() {
        // @formatter:off
        return new String[] {
            FeatureId.ARRAY,
            FeatureId.KNN,
            FeatureId.PROCEDURE_CURSOR_RESULT,
            FeatureId.VECTOR,
            FeatureId.POSTGRES_ON_CONFLICT,
            FeatureId.DELIMITED_LOWERCASE_STANDARD_TABLE,
            FeatureId.BIT_CAST_NULL_VALUE,
            FeatureId.LOWERCASE_STANDARD_RESULT_COLUMNS,
            FeatureId.MULTIPLE_RESULT_SETS
        };
        // @formatter:on
    }

    @Override
    public DataSourceId id() {
        return DataSourceId.DB2;
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
