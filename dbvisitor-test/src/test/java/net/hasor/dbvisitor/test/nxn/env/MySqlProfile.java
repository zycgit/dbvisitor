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

public final class MySqlProfile extends AbstractDataSourceProfile {
    public static final MySqlProfile INSTANCE = new MySqlProfile();

    @Override
    public SupportStatus support(String capabilityId) {
        // The default Connector/J databaseTerm=CATALOG exposes databases as catalogs.
        if (CapabilityId.JDBC_METADATA_SCHEMAS.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DRIVER;
        }
        return super.support(capabilityId);
    }

    private MySqlProfile() {
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
            FeatureId.FUNCTION_RECORD_RESULT,
            FeatureId.FUNCTION_TABLE_RESULT,
            FeatureId.VECTOR,
            FeatureId.GENERATED_KEY_RESULT_SET,
            FeatureId.POSTGRES_ON_CONFLICT
        };
        // @formatter:on
    }

    @Override
    public DataSourceId id() {
        return DataSourceId.MYSQL;
    }

    @Override
    public String leftQualifier() {
        return "`";
    }

    @Override
    public String rightQualifier() {
        return "`";
    }

    @Override
    public String castToBigInt(String expression) {
        return "CAST(" + expression + " AS SIGNED)";
    }

    @Override
    public String datetimeColumnType() {
        return "DATETIME(3)";
    }
}
