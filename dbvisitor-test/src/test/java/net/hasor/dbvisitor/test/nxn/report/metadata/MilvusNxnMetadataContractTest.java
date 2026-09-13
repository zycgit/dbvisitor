/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.nxn.report.metadata;

import java.util.Map;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import net.hasor.dbvisitor.test.nxn.report.NxnMetadataContractTest;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.capability.SupportStatus;

/** Fixed expectations for Milvus capability declarations; no database connection is needed. */
public class MilvusNxnMetadataContractTest extends NxnMetadataContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    protected Map<String, Boolean> expectedFeatures() {
        return Map.ofEntries(
                Map.entry(FeatureId.TRANSACTION, false),
                Map.entry(FeatureId.TRANSACTION_RELEASE_SAVEPOINT, false),
                Map.entry(FeatureId.TRANSACTION_REPEATABLE_READ, false),
                Map.entry(FeatureId.PROCEDURE, false),
                Map.entry(FeatureId.PROCEDURE_CURSOR_RESULT, false),
                Map.entry(FeatureId.PROCEDURE_RESULT_SET, false),
                Map.entry(FeatureId.XML_MAPPER_CALLABLE, false),
                Map.entry(FeatureId.SEQUENCE, false),
                Map.entry(FeatureId.XML_SELECT_KEY_USER_INFO_SEQUENCE, false),
                Map.entry(FeatureId.ARRAY, true),
                Map.entry(FeatureId.VECTOR, true),
                Map.entry(FeatureId.GENERATED_KEYS_NUMERIC, true),
                Map.entry(FeatureId.DUPLICATE_PRIMARY_KEY_REJECTED, false),
                Map.entry(FeatureId.GENERATED_KEY_COLUMN, true),
                Map.entry(FeatureId.GENERATED_KEY_RESULT_SET, false),
                Map.entry(FeatureId.BINARY, false),
                Map.entry(FeatureId.FUNCTION, false));
    }

    @Override
    protected Map<String, SupportStatus> expectedProfileStatuses() {
        return Map.ofEntries(
                Map.entry(CapabilityId.TRANSACTION_REQUIRED_COMMIT, SupportStatus.UNSUPPORTED_BY_DATABASE),
                Map.entry(CapabilityId.MAPPER_XML_CALLABLE_REFCURSOR, SupportStatus.UNSUPPORTED_BY_DATABASE),
                Map.entry(CapabilityId.KEYGEN_DUPLICATE_KEY, SupportStatus.UNSUPPORTED_BY_DATABASE),
                Map.entry(CapabilityId.MAPPER_ANNOTATION_DUPLICATE_KEY, SupportStatus.UNSUPPORTED_BY_DATABASE),
                Map.entry(CapabilityId.MAPPER_XML_KEYGEN_KEY_COLUMN, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_KEY_COLUMN, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.ADAPTER_MILVUS_MAPPER_ANNOTATION_KEYS, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.ADAPTER_MILVUS_LAMBDA_VECTOR_L2, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.JDBC_QUERY_LIST, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.JDBC_QUERY_SCALAR_SHORTCUTS, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.LAMBDA_QUERY_COUNT, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.LAMBDA_QUERY_COMPARE, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.LAMBDA_QUERY_IN, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.LAMBDA_QUERY_NULL, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.LAMBDA_QUERY_LIKE, SupportStatus.UNSUPPORTED_BY_DATABASE),
                Map.entry(CapabilityId.LAMBDA_QUERY_ORDER, SupportStatus.UNSUPPORTED_BY_DATABASE),
                Map.entry(CapabilityId.LAMBDA_QUERY_PAGE, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.JDBC_QUERY_SCALAR, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.ADAPTER_MILVUS_LAMBDA_COUNT, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.LAMBDA_EDGE_DELETE_NO_MATCH, SupportStatus.UNSUPPORTED_BY_DATABASE),
                Map.entry(CapabilityId.LAMBDA_EDGE_EMPTY_WHERE_DELETE_ALLOW, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.LAMBDA_EDGE_UPDATE_NO_MATCH, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.LAMBDA_EDGE_EMPTY_WHERE_UPDATE_ALLOW, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.TYPE_BINARY_BLOB, SupportStatus.UNSUPPORTED_BY_DATABASE),
                Map.entry(CapabilityId.TYPE_BINARY_NULL, SupportStatus.UNSUPPORTED_BY_DATABASE),
                Map.entry(CapabilityId.FUNCTION_QUERY_SCALAR, SupportStatus.UNSUPPORTED_BY_DATABASE),
                Map.entry(CapabilityId.SCHEMA_STANDARD_TABLES, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.SCHEMA_STANDARD_COLUMNS, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.MAPPER_ANNOTATION_RESULT_ENTITY, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.MAPPER_ANNOTATION_RESULT_MAP, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.MAPPER_ANNOTATION_RESULT_SCALAR, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.MAPPER_ANNOTATION_RESULT_NULL, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.MAPPER_ANNOTATION_RESULT_LIST, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.MAPPER_ANNOTATION_RESULT_DISTINCT_LIST, SupportStatus.UNSUPPORTED_BY_DATABASE),
                Map.entry(CapabilityId.MAPPER_ANNOTATION_RESULT_AGGREGATE, SupportStatus.UNSUPPORTED_BY_DATABASE),
                Map.entry(CapabilityId.MAPPER_ANNOTATION_RESULT_PAGE, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.LAMBDA_BATCH_MUTATION_UPDATE_DYNAMIC, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.LAMBDA_BATCH_MUTATION_UPDATE_MULTI_FIELD, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.LAMBDA_BATCH_MUTATION_DELETE_IN, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.LAMBDA_BATCH_MUTATION_DELETE_CHUNKS, SupportStatus.UNSUPPORTED_BY_DATABASE),
                Map.entry(CapabilityId.LAMBDA_LOGIC_MARKER_AND, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.LAMBDA_LOGIC_DYNAMIC_NESTED, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.LAMBDA_LOGIC_DYNAMIC_VALUE, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.LAMBDA_LOGIC_MARKER_NOT, SupportStatus.UNSUPPORTED_BY_DATABASE),
                Map.entry(CapabilityId.LAMBDA_LOGIC_DYNAMIC_NOT, SupportStatus.UNSUPPORTED_BY_DATABASE),
                Map.entry(CapabilityId.LAMBDA_SECURITY_VALUE_IN, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.LAMBDA_SECURITY_METHOD_REF, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.LAMBDA_SECURITY_VALUE_LIKE, SupportStatus.UNSUPPORTED_BY_DATABASE),
                Map.entry(CapabilityId.LAMBDA_SPECIAL_UNICODE_EMOJI, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.LAMBDA_SPECIAL_KEYWORD_WILDCARD, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.LAMBDA_SPECIAL_UNICODE_LIKE, SupportStatus.UNSUPPORTED_BY_DATABASE),
                Map.entry(CapabilityId.ADAPTER_MILVUS_LAMBDA_MULTI_UPDATE, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.ADAPTER_MILVUS_LAMBDA_CHUNK_DELETE, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.LAMBDA_PREDICATE_STRING_COLLECTION_NULL_RANGE, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.LAMBDA_PREDICATE_COMPARISON_DYNAMIC, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.LAMBDA_PREDICATE_IN_SINGLE, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.LAMBDA_PREDICATE_IN_LARGE, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.LAMBDA_PREDICATE_NOT_IN_NULL, SupportStatus.UNSUPPORTED_BY_DATABASE),
                Map.entry(CapabilityId.LAMBDA_PREDICATE_RANGE_NOT_VARIANTS, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.LAMBDA_PREDICATE_RANGE_NOT_HALF_OPEN, SupportStatus.UNSUPPORTED_BY_DATABASE),
                Map.entry(CapabilityId.MAPPER_ANNOTATION_SQL_ERROR, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.MAPPER_ANNOTATION_EXECUTE_ERROR, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.MAPPER_ANNOTATION_NULL_VALUE, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.MAPPER_ANNOTATION_SPECIAL_TEXT, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.MAPPER_ANNOTATION_NO_MATCH_AFFECTED_ROWS, SupportStatus.UNSUPPORTED_BY_DATABASE),
                Map.entry(CapabilityId.NAMING_CAMELCASE_ENTITY, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.NAMING_CASE_SENSITIVE_TABLE_ISOLATION, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.NAMING_CAMELCASE_DISABLED, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.NAMING_CAMELCASE_OPTIONS, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.NAMING_CASE_SENSITIVE_FIELD_MISMATCH, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.NAMING_CASE_INSENSITIVE_BATCH_MAPPING, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.NAMING_DELIMITED_SQL, SupportStatus.UNSUPPORTED_BY_DRIVER),
                Map.entry(CapabilityId.NAMING_KEYWORD_COLUMN_SQL, SupportStatus.UNSUPPORTED_BY_DRIVER),
                Map.entry(CapabilityId.LAMBDA_DUPLICATE_STRATEGY_UPDATE_BASIC, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.LAMBDA_DUPLICATE_STRATEGY_UPDATE_MIXED, SupportStatus.SUPPORTED),
                Map.entry(CapabilityId.LAMBDA_DUPLICATE_STRATEGY_IGNORE_BASIC, SupportStatus.UNSUPPORTED_BY_DATABASE),
                Map.entry(CapabilityId.LAMBDA_DUPLICATE_STRATEGY_TRANSITION, SupportStatus.UNSUPPORTED_BY_DATABASE),
                Map.entry(CapabilityId.JDBC_JOIN_INNER, SupportStatus.UNSUPPORTED_BY_DATABASE),
                Map.entry(CapabilityId.MAPPER_ANNOTATION_JOIN_DTO, SupportStatus.UNSUPPORTED_BY_DATABASE),
                Map.entry(CapabilityId.MAPPER_XML_JOIN_SELF, SupportStatus.UNSUPPORTED_BY_DATABASE),
                Map.entry(CapabilityId.LAMBDA_SORT_MULTI_COLUMN, SupportStatus.UNSUPPORTED_BY_DATABASE));
    }
}
