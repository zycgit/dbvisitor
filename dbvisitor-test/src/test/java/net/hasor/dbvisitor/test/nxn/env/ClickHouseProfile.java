/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.nxn.env;

import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.SupportStatus;
import org.jetbrains.annotations.NotNull;

public final class ClickHouseProfile extends AbstractDataSourceProfile {
    public static final ClickHouseProfile INSTANCE = new ClickHouseProfile();

    private ClickHouseProfile() {
        super(features());
    }

    @NotNull
    private static String[] features() {
        // @formatter:off
        return new String[] {
            FeatureId.SEQUENCE,
            FeatureId.GENERATED_KEY_COLUMN,
            FeatureId.GENERATED_KEY_RESULT_SET,
            FeatureId.XML_SELECT_KEY_USER_INFO_SEQUENCE,
            FeatureId.PROCEDURE,
            FeatureId.XML_MAPPER_CALLABLE,
            FeatureId.FUNCTION_CALL_CALLBACK,
            FeatureId.FUNCTION_RECORD_RESULT,
            FeatureId.POSTGRES_ON_CONFLICT,
            FeatureId.DUPLICATE_KEY_STRATEGY,
            FeatureId.DUPLICATE_PRIMARY_KEY_REJECTED,
            FeatureId.BATCH_DUPLICATE_FAILURE_PROPAGATED,
            FeatureId.EXACT_MUTATION_AFFECTED_ROWS,
            FeatureId.LENGTH_LIMIT_ENFORCED,
            FeatureId.NON_NULL_PRIMARY_KEY_REJECTED,
            FeatureId.TRANSACTION,
            FeatureId.SQL_NOT_IN_NULL_SEMANTICS,
            FeatureId.KEYGEN_AUTO_BATCH_EXPLICIT_NULL,
            FeatureId.GENERATED_KEYS_NUMERIC,
            FeatureId.TRANSACTION_RELEASE_SAVEPOINT,
            FeatureId.TRANSACTION_REPEATABLE_READ,
            FeatureId.MULTIPLE_RESULT_SETS
        };
        // @formatter:on
    }

    @Override
    public SupportStatus support(String capabilityId) {
        if (CapabilityId.MAPPER_XML_KEYGEN_SELECT_KEY_BEFORE.equals(capabilityId)
                || CapabilityId.MAPPER_XML_KEYGEN_SELECT_KEY_AFTER.equals(capabilityId)
                || CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_SELECT_KEY.equals(capabilityId)) {
            return SupportStatus.SUPPORTED;
        }
        if (CapabilityId.TYPE_ARRAY_NULL.equals(capabilityId)) {
            // Array elements can be nullable; the array itself cannot be Nullable(Array(...)).
            return SupportStatus.UNSUPPORTED_BY_DATABASE;
        }
        return super.support(capabilityId);
    }

    @Override
    public DataSourceId id() {
        return DataSourceId.CLICKHOUSE;
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
        return "CAST(" + expression + " AS Int64)";
    }

    @Override
    public String datetimeColumnType() {
        return "DateTime64(3)";
    }

    @Override
    public String currentTimestampExpression() {
        return "current_timestamp()";
    }
}
