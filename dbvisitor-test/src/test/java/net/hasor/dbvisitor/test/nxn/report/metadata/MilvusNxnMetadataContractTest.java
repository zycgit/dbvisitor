/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.nxn.report.metadata;

import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import net.hasor.dbvisitor.test.nxn.report.NxnMetadataContractTest;
import org.junit.Test;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.capability.SupportStatus;
import static org.junit.Assert.*;

/** Metadata checks have their own entry point and are not real database SQL tests. */
public class MilvusNxnMetadataContractTest extends NxnMetadataContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Test
    @Capability(CapabilityId.NXN_METADATA_PROFILE_REGISTRY)
    public void nxnMetadata_shouldRegisterAllDatasourceProfiles() throws java.sql.SQLException {
        super.nxnMetadata_shouldRegisterAllDatasourceProfiles();
        for (String feature : new String[] { FeatureId.TRANSACTION, FeatureId.TRANSACTION_RELEASE_SAVEPOINT,
                FeatureId.TRANSACTION_REPEATABLE_READ, FeatureId.PROCEDURE, FeatureId.PROCEDURE_CURSOR_RESULT,
                FeatureId.PROCEDURE_RESULT_SET, FeatureId.XML_MAPPER_CALLABLE, FeatureId.SEQUENCE,
                FeatureId.XML_SELECT_KEY_USER_INFO_SEQUENCE }) {
            assertFalse(feature, profile().supportsFeature(feature));
        }
        assertEquals(SupportStatus.UNSUPPORTED_BY_DATABASE, profile().support(CapabilityId.TRANSACTION_REQUIRED_COMMIT));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DATABASE, profile().support(CapabilityId.MAPPER_XML_CALLABLE_REFCURSOR));
        assertTrue(profile().supportsFeature(FeatureId.ARRAY));
        assertTrue(profile().supportsFeature(FeatureId.VECTOR));
        assertTrue(profile().supportsFeature(FeatureId.GENERATED_KEYS_NUMERIC));
        assertFalse(profile().supportsFeature(FeatureId.DUPLICATE_PRIMARY_KEY_REJECTED));
        assertFalse(profile().supportsFeature(FeatureId.GENERATED_KEY_COLUMN));
        assertFalse(profile().supportsFeature(FeatureId.GENERATED_KEY_RESULT_SET));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DATABASE, profile().support(CapabilityId.KEYGEN_DUPLICATE_KEY));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DATABASE, profile().support(CapabilityId.MAPPER_ANNOTATION_DUPLICATE_KEY));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DRIVER, profile().support(CapabilityId.MAPPER_XML_KEYGEN_KEY_COLUMN));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DRIVER, profile().support(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_KEY_COLUMN));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.ADAPTER_MILVUS_MAPPER_ANNOTATION_KEYS));
        for (String capability : new String[] { CapabilityId.JDBC_JOIN_INNER, CapabilityId.MAPPER_ANNOTATION_JOIN_DTO,
                CapabilityId.MAPPER_XML_JOIN_SELF, CapabilityId.LAMBDA_SORT_MULTI_COLUMN }) {
            assertEquals(capability, SupportStatus.UNSUPPORTED_BY_DATABASE, profile().support(capability));
        }
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.ADAPTER_MILVUS_LAMBDA_VECTOR_L2));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.JDBC_QUERY_LIST));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.JDBC_QUERY_SCALAR_SHORTCUTS));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.LAMBDA_QUERY_COUNT));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.LAMBDA_QUERY_COMPARE));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.LAMBDA_QUERY_IN));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.LAMBDA_QUERY_NULL));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DATABASE, profile().support(CapabilityId.LAMBDA_QUERY_LIKE));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DATABASE, profile().support(CapabilityId.LAMBDA_QUERY_ORDER));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DATABASE, profile().support(CapabilityId.LAMBDA_QUERY_PAGE));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.JDBC_QUERY_SCALAR));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.ADAPTER_MILVUS_LAMBDA_COUNT));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DATABASE, profile().support(CapabilityId.LAMBDA_EDGE_DELETE_NO_MATCH));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.LAMBDA_EDGE_EMPTY_WHERE_DELETE_ALLOW));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.LAMBDA_EDGE_UPDATE_NO_MATCH));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.LAMBDA_EDGE_EMPTY_WHERE_UPDATE_ALLOW));
        assertFalse(profile().supportsFeature(FeatureId.BINARY));
        assertFalse(profile().supportsFeature(FeatureId.FUNCTION));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DATABASE, profile().support(CapabilityId.TYPE_BINARY_BLOB));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DATABASE, profile().support(CapabilityId.TYPE_BINARY_NULL));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DATABASE, profile().support(CapabilityId.FUNCTION_QUERY_SCALAR));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DRIVER, profile().support(CapabilityId.SCHEMA_STANDARD_TABLES));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DRIVER, profile().support(CapabilityId.SCHEMA_STANDARD_COLUMNS));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.MAPPER_ANNOTATION_RESULT_ENTITY));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.MAPPER_ANNOTATION_RESULT_MAP));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.MAPPER_ANNOTATION_RESULT_SCALAR));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.MAPPER_ANNOTATION_RESULT_NULL));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.MAPPER_ANNOTATION_RESULT_LIST));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DATABASE, profile().support(CapabilityId.MAPPER_ANNOTATION_RESULT_DISTINCT_LIST));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DATABASE, profile().support(CapabilityId.MAPPER_ANNOTATION_RESULT_AGGREGATE));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.MAPPER_ANNOTATION_RESULT_PAGE));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.LAMBDA_BATCH_MUTATION_UPDATE_DYNAMIC));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.LAMBDA_BATCH_MUTATION_UPDATE_MULTI_FIELD));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.LAMBDA_BATCH_MUTATION_DELETE_IN));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DATABASE, profile().support(CapabilityId.LAMBDA_BATCH_MUTATION_DELETE_CHUNKS));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.LAMBDA_LOGIC_MARKER_AND));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.LAMBDA_LOGIC_DYNAMIC_NESTED));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.LAMBDA_LOGIC_DYNAMIC_VALUE));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DATABASE, profile().support(CapabilityId.LAMBDA_LOGIC_MARKER_NOT));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DATABASE, profile().support(CapabilityId.LAMBDA_LOGIC_DYNAMIC_NOT));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.LAMBDA_SECURITY_VALUE_IN));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.LAMBDA_SECURITY_METHOD_REF));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DATABASE, profile().support(CapabilityId.LAMBDA_SECURITY_VALUE_LIKE));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.LAMBDA_SPECIAL_UNICODE_EMOJI));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.LAMBDA_SPECIAL_KEYWORD_WILDCARD));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DATABASE, profile().support(CapabilityId.LAMBDA_SPECIAL_UNICODE_LIKE));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.ADAPTER_MILVUS_LAMBDA_MULTI_UPDATE));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.ADAPTER_MILVUS_LAMBDA_CHUNK_DELETE));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.LAMBDA_PREDICATE_STRING_COLLECTION_NULL_RANGE));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.LAMBDA_PREDICATE_COMPARISON_DYNAMIC));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.LAMBDA_PREDICATE_IN_SINGLE));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.LAMBDA_PREDICATE_IN_LARGE));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DATABASE, profile().support(CapabilityId.LAMBDA_PREDICATE_NOT_IN_NULL));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.LAMBDA_PREDICATE_RANGE_NOT_VARIANTS));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DATABASE, profile().support(CapabilityId.LAMBDA_PREDICATE_RANGE_NOT_HALF_OPEN));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.MAPPER_ANNOTATION_SQL_ERROR));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.MAPPER_ANNOTATION_EXECUTE_ERROR));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.MAPPER_ANNOTATION_NULL_VALUE));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.MAPPER_ANNOTATION_SPECIAL_TEXT));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DATABASE, profile().support(CapabilityId.MAPPER_ANNOTATION_NO_MATCH_AFFECTED_ROWS));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.NAMING_CAMELCASE_ENTITY));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.NAMING_CASE_SENSITIVE_TABLE_ISOLATION));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.NAMING_CAMELCASE_DISABLED));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.NAMING_CAMELCASE_OPTIONS));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.NAMING_CASE_SENSITIVE_FIELD_MISMATCH));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.NAMING_CASE_INSENSITIVE_BATCH_MAPPING));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DRIVER, profile().support(CapabilityId.NAMING_DELIMITED_SQL));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DRIVER, profile().support(CapabilityId.NAMING_KEYWORD_COLUMN_SQL));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.LAMBDA_DUPLICATE_STRATEGY_UPDATE_BASIC));
        assertEquals(SupportStatus.SUPPORTED, profile().support(CapabilityId.LAMBDA_DUPLICATE_STRATEGY_UPDATE_MIXED));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DATABASE, profile().support(CapabilityId.LAMBDA_DUPLICATE_STRATEGY_IGNORE_BASIC));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DATABASE, profile().support(CapabilityId.LAMBDA_DUPLICATE_STRATEGY_TRANSITION));
    }
}
