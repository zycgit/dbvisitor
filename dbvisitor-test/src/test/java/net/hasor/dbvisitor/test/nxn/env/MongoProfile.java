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

public final class MongoProfile extends AbstractDataSourceProfile {
    public static final MongoProfile INSTANCE = new MongoProfile();

    private MongoProfile() {
        super(FeatureId.TRANSACTION, FeatureId.TRANSACTION_SAVEPOINT, FeatureId.TRANSACTION_RELEASE_SAVEPOINT, FeatureId.TRANSACTION_REPEATABLE_READ, FeatureId.PROCEDURE, FeatureId.PROCEDURE_CURSOR_RESULT, FeatureId.PROCEDURE_RESULT_SET, FeatureId.XML_MAPPER_CALLABLE, FeatureId.FUNCTION_RECORD_RESULT, FeatureId.FUNCTION_TABLE_RESULT, FeatureId.FUNCTION_CALL_CALLBACK, FeatureId.SEQUENCE, FeatureId.GENERATED_KEYS_NUMERIC, FeatureId.KEYGEN_AUTO_BATCH_EXPLICIT_NULL, FeatureId.GENERATED_KEY_RESULT_SET, FeatureId.KNN, FeatureId.SQL_NOT_IN_NULL_SEMANTICS);
    }

    @Override
    public SupportStatus support(String capabilityId) {
        // This datasource has no separate JDBC namespace at this level.
        if (CapabilityId.JDBC_METADATA_SCHEMAS.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DATABASE;
        }
        if (capabilityId == null) {
            return super.support(null);
        }
        // MongoDialect currently emits find().sort() without translating an explicit null rank.
        if (CapabilityId.LAMBDA_SORT_NULL_FIRST_ASC.equals(capabilityId) || CapabilityId.LAMBDA_SORT_NULL_FIRST_DESC.equals(capabilityId) || CapabilityId.LAMBDA_SORT_NULL_LAST_ASC.equals(capabilityId) || CapabilityId.LAMBDA_SORT_NULL_LAST_DESC.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DBVISITOR;
        }
        if (CapabilityId.TRANSACTION_SUPPORTS_NO_TX.equals(capabilityId) || CapabilityId.TRANSACTION_NEVER_NO_TX.equals(capabilityId) || CapabilityId.TRANSACTION_NOT_SUPPORTED_NO_TX.equals(capabilityId)) {
            return super.support(capabilityId);
        }
        // MongoDB replica sets support transactions; this JDBC adapter has no ClientSession integration.
        if (capabilityId.startsWith("transaction.")) {
            return SupportStatus.UNSUPPORTED_BY_DRIVER;
        }
        if (CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_SCROLL_INSENSITIVE.equals(capabilityId) || CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_SCROLL_SENSITIVE.equals(capabilityId) || CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_EXTRACTOR_OPTIONS.equals(capabilityId) || CapabilityId.MAPPER_XML_STATEMENT_RESULT_SET_TYPE.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DRIVER;
        }
        // insertOne/insertMany return generated keys, not a SQL RETURNING-style current result set.
        if (CapabilityId.MAPPER_XML_KEYGEN_RESULT_SET_SOURCE.equals(capabilityId) || CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_RESULT_SET_KEY_SOURCE.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DRIVER;
        }
        // These shared APIs use real ObjectId/String models, not numeric IDENTITY fixtures.
        if (CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_GENERATED_KEYS.equals(capabilityId) || CapabilityId.MAPPER_XML_KEYGEN_GENERATED_KEYS.equals(capabilityId) || CapabilityId.MAPPER_XML_KEYGEN_DISTINCT_GENERATED_KEYS.equals(capabilityId) || CapabilityId.KEYGEN_HOLDER_AFTER.equals(capabilityId)) {
            return SupportStatus.SUPPORTED;
        }
        if (CapabilityId.SCHEMA_STANDARD_COLUMNS.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DRIVER;
        }
        // MongoDB 6.0 standalone is the test baseline; no Atlas Search/mongot is installed.
        if (capabilityId.startsWith("vector.") && !CapabilityId.VECTOR_CRUD_ROUND_TRIP.equals(capabilityId) && !CapabilityId.VECTOR_BATCH_QUERY.equals(capabilityId) && !CapabilityId.VECTOR_CRUD_DELETE_AND_SCALAR_UPDATE.equals(capabilityId) && !CapabilityId.VECTOR_PRECISION_BOUNDARY.equals(capabilityId) && !CapabilityId.VECTOR_NULL_ROUND_TRIP.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DATABASE;
        }
        // SQL-generating rules are distinct from native BSON collection binding.
        if (CapabilityId.JDBC_PARAM_RULE_SET.equals(capabilityId) || CapabilityId.JDBC_PARAM_RULE_AND_IN_SET.equals(capabilityId) || CapabilityId.MAPPER_ANNOTATION_DYNAMIC_IN.equals(capabilityId) || CapabilityId.MAPPER_ANNOTATION_PARAM_IN_LIST.equals(capabilityId) || capabilityId.startsWith("mapper.xml.dynamic-rule.")) {
            return SupportStatus.UNSUPPORTED_BY_DBVISITOR;
        }
        // Mongo templates do not translate SQL MD5/LOWER/UPPER expressions into aggregation/update pipelines.
        if (capabilityId.startsWith("mapping.annotation.sql-template.")) {
            return SupportStatus.UNSUPPORTED_BY_DRIVER;
        }
        // Reading a nonexistent collection produces an empty cursor, not a missing-table exception.
        if (CapabilityId.MAPPER_ANNOTATION_EXECUTE_ERROR.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DATABASE;
        }
        return super.support(capabilityId);
    }

    @Override
    public DataSourceId id() {
        return DataSourceId.MONGO;
    }

    @Override
    public String leftQualifier() {
        return "";
    }

    @Override
    public String rightQualifier() {
        return "";
    }

    @Override
    public String castToBigInt(String expression) {
        return expression;
    }

    @Override
    public String datetimeColumnType() {
        return "DATE";
    }
}
