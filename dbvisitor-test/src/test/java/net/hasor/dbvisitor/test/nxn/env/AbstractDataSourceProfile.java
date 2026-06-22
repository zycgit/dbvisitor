package net.hasor.dbvisitor.test.nxn.env;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.capability.SupportStatus;

public abstract class AbstractDataSourceProfile implements DataSourceProfile {
    private final Set<String> unsupportedFeatures;

    protected AbstractDataSourceProfile(String... unsupportedFeatures) {
        this.unsupportedFeatures = unsupportedFeatures == null ? Collections.emptySet() : new HashSet<>(Arrays.asList(unsupportedFeatures));
    }

    @Override
    public boolean supportsFeature(String featureId) {
        return !this.unsupportedFeatures.contains(featureId);
    }

    @Override
    public SupportStatus support(String capabilityId) {
        String requiredFeature = requiredFeature(capabilityId);
        if (requiredFeature == null || supportsFeature(requiredFeature)) {
            return SupportStatus.SUPPORTED;
        }
        return SupportStatus.UNSUPPORTED_BY_DATABASE;
    }

    private String requiredFeature(String capabilityId) {
        if (capabilityId == null) {
            return null;
        }
        if (CapabilityId.TYPE_ARRAY_NULL.equals(capabilityId)) {
            return null;
        }
        if (capabilityId.startsWith("type.array.")) {
            return FeatureId.ARRAY;
        }
        if (CapabilityId.TYPE_JSON_NULL.equals(capabilityId)) {
            return null;
        }
        if (capabilityId.startsWith("type.json.")) {
            return FeatureId.JSON;
        }
        if (CapabilityId.TYPE_BINARY_NULL.equals(capabilityId)) {
            return null;
        }
        if (capabilityId.startsWith("type.binary.")) {
            return FeatureId.BINARY;
        }
        if (capabilityId.startsWith("type.time.") && !CapabilityId.TYPE_TIME_SQL_DATE.equals(capabilityId) && !CapabilityId.TYPE_TIME_LOCAL_DATE.equals(capabilityId) && !CapabilityId.TYPE_TIME_PARTIAL.equals(capabilityId) && !CapabilityId.TYPE_TIME_JULIAN_DAY.equals(capabilityId) && !CapabilityId.TYPE_TIME_NULL.equals(capabilityId) && !CapabilityId.TYPE_TIME_EXTREME_DATE.equals(capabilityId)) {
            return FeatureId.TIME_ZONE_STABLE_ROUND_TRIP;
        }
        if (capabilityId.startsWith("transaction.")) {
            return FeatureId.TRANSACTION;
        }
        if (capabilityId.startsWith("mapping.annotation.special-type.json")) {
            return FeatureId.JSON;
        }
        if (capabilityId.startsWith("mapping.annotation.special-type.array")) {
            return FeatureId.ARRAY;
        }
        if (capabilityId.startsWith("keygen.sequence")) {
            return FeatureId.SEQUENCE;
        }
        if (CapabilityId.KEYGEN_AUTO_BATCH.equals(capabilityId)) {
            return FeatureId.KEYGEN_AUTO_BATCH_EXPLICIT_NULL;
        }
        if (CapabilityId.KEYGEN_AUTO_SINGLE.equals(capabilityId) || CapabilityId.KEYGEN_AUTO_MANUAL.equals(capabilityId) || CapabilityId.KEYGEN_AUTO_LONG.equals(capabilityId) || CapabilityId.KEYGEN_HOLDER_AFTER.equals(capabilityId) || CapabilityId.MAPPER_XML_KEYGEN_GENERATED_KEYS.equals(capabilityId) || CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_GENERATED_KEYS.equals(capabilityId)) {
            return FeatureId.GENERATED_KEYS_NUMERIC;
        }
        if (CapabilityId.KEYGEN_UUID_WRONG_TYPE.equals(capabilityId)) {
            return FeatureId.KEYGEN_UUID_WRONG_TYPE_REJECTED;
        }
        if (CapabilityId.LAMBDA_EMPTY_STRING_VS_NULL.equals(capabilityId) || CapabilityId.MAPPER_ANNOTATION_PARAM_NAMED.equals(capabilityId) || CapabilityId.MAPPING_ANNOTATION_BASIC_VALUE_ROUND_TRIP.equals(capabilityId)) {
            return FeatureId.DISTINCT_EMPTY_STRING;
        }
        if (CapabilityId.LAMBDA_PREDICATE_IN_SINGLE_AND_LARGE.equals(capabilityId)) {
            return FeatureId.LARGE_IN_LIST;
        }
        if (CapabilityId.LAMBDA_PREDICATE_NOT_IN_NULL.equals(capabilityId)) {
            return FeatureId.SQL_NOT_IN_NULL_SEMANTICS;
        }
        if (CapabilityId.MAPPER_XML_DYNAMIC_FOREACH.equals(capabilityId)) {
            return FeatureId.XML_FOREACH_BATCH_INSERT_VALUES;
        }
        if (CapabilityId.NAMING_CASE_SENSITIVE_FIELD_MISMATCH.equals(capabilityId) || CapabilityId.NAMING_CASE_SENSITIVE_FREEDOM_MAP.equals(capabilityId)) {
            return FeatureId.LOWERCASE_STANDARD_RESULT_COLUMNS;
        }
        if (CapabilityId.TRANSACTION_NESTED_COMMIT.equals(capabilityId) || CapabilityId.TRANSACTION_NESTED_OUTER_ROLLBACK.equals(capabilityId) || CapabilityId.TRANSACTION_ANNOTATION_NESTED.equals(capabilityId) || CapabilityId.TRANSACTION_PROXY_REQUIRED_NESTED.equals(capabilityId)) {
            return FeatureId.TRANSACTION_RELEASE_SAVEPOINT;
        }
        if (CapabilityId.TRANSACTION_ISOLATION_REPEATABLE_READ.equals(capabilityId)) {
            return FeatureId.TRANSACTION_REPEATABLE_READ;
        }
        if (CapabilityId.MAPPER_ANNOTATION_NO_MATCH_AFFECTED_ROWS.equals(capabilityId) || CapabilityId.LAMBDA_EDGE_UPDATE_NO_MATCH.equals(capabilityId) || CapabilityId.LAMBDA_EDGE_DELETE_NO_MATCH.equals(capabilityId)) {
            return FeatureId.EXACT_MUTATION_AFFECTED_ROWS;
        }
        if (CapabilityId.LAMBDA_EDGE_EMPTY_WHERE_DELETE_ALLOW.equals(capabilityId) || CapabilityId.LAMBDA_EDGE_EMPTY_WHERE_UPDATE_ALLOW.equals(capabilityId)) {
            return FeatureId.EMPTY_WHERE_MUTATION;
        }
        if (CapabilityId.KEYGEN_NONE_MISSING.equals(capabilityId)) {
            return FeatureId.NON_NULL_PRIMARY_KEY_REJECTED;
        }
        if (CapabilityId.JDBC_BATCH_PARTIAL_FAILURE.equals(capabilityId) || CapabilityId.BASEMAPPER_EDGE_BATCH_FAILURE.equals(capabilityId)) {
            return FeatureId.BATCH_DUPLICATE_FAILURE_PROPAGATED;
        }
        if (CapabilityId.KEYGEN_DUPLICATE_KEY.equals(capabilityId) || CapabilityId.MAPPER_ANNOTATION_DUPLICATE_KEY.equals(capabilityId) || CapabilityId.LAMBDA_DUPLICATE_STRATEGY_INTO.equals(capabilityId) || CapabilityId.LAMBDA_DUPLICATE_STRATEGY_DEFAULT.equals(capabilityId)) {
            return FeatureId.DUPLICATE_PRIMARY_KEY_REJECTED;
        }
        if (CapabilityId.MAPPER_XML_KEYGEN_KEY_COLUMN.equals(capabilityId) || CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_KEY_COLUMN.equals(capabilityId)) {
            return FeatureId.GENERATED_KEY_COLUMN;
        }
        if (CapabilityId.MAPPER_XML_KEYGEN_SELECT_KEY_BEFORE.equals(capabilityId) || CapabilityId.MAPPER_XML_KEYGEN_SELECT_KEY_AFTER.equals(capabilityId) || CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_SELECT_KEY.equals(capabilityId)) {
            return FeatureId.XML_SELECT_KEY_USER_INFO_SEQUENCE;
        }
        if (CapabilityId.PROCEDURE_CALL_CURSOR_RESULT.equals(capabilityId)) {
            return FeatureId.PROCEDURE_CURSOR_RESULT;
        }
        if (capabilityId.startsWith("mapper.xml.callable.")) {
            return FeatureId.XML_MAPPER_CALLABLE;
        }
        if (capabilityId.startsWith("procedure.")) {
            return FeatureId.PROCEDURE;
        }
        if (CapabilityId.FUNCTION_QUERY_OUT_RECORD.equals(capabilityId) || CapabilityId.FUNCTION_QUERY_NULL_FALLBACK.equals(capabilityId)) {
            return FeatureId.FUNCTION_RECORD_RESULT;
        }
        if (CapabilityId.FUNCTION_QUERY_TABLE_RESULT.equals(capabilityId)) {
            return FeatureId.FUNCTION_TABLE_RESULT;
        }
        if (capabilityId.startsWith("function.")) {
            return FeatureId.FUNCTION;
        }
        if (CapabilityId.MAPPING_ANNOTATION_SQL_TEMPLATE_MD5.equals(capabilityId)) {
            return FeatureId.SQL_MD5_FUNCTION;
        }
        if (capabilityId.startsWith("vector.knn.") || capabilityId.startsWith("vector.range.") || CapabilityId.VECTOR_RANGE_FILTER.equals(capabilityId)) {
            return FeatureId.KNN;
        }
        if (capabilityId.startsWith("vector.")) {
            return FeatureId.VECTOR;
        }
        switch (capabilityId) {
            case CapabilityId.LAMBDA_DUPLICATE_STRATEGY_IGNORE_BASIC,   //
                    CapabilityId.LAMBDA_DUPLICATE_STRATEGY_IGNORE_MIXED,//
                    CapabilityId.LAMBDA_DUPLICATE_STRATEGY_UPDATE_BASIC,//
                    CapabilityId.LAMBDA_DUPLICATE_STRATEGY_UPDATE_MIXED,//
                    CapabilityId.LAMBDA_DUPLICATE_STRATEGY_TRANSITION -> {
                return FeatureId.DUPLICATE_KEY_STRATEGY;
            }
            case CapabilityId.TYPE_TIME_LOCAL_DATE -> {
                return FeatureId.TIME_LOCAL_DATE;
            }
            case CapabilityId.TYPE_TIME_EXTREME_DATE -> {
                return FeatureId.TIME_EXTREME_DATE;
            }
            case CapabilityId.NAMING_DELIMITED_CRUD -> {
                return FeatureId.DELIMITED_LOWERCASE_STANDARD_TABLE;
            }
            case CapabilityId.NAMING_CASE_INSENSITIVE_MIXED_CASE_CRUD,  //
                    CapabilityId.NAMING_CASE_SENSITIVE_FIELD_MISMATCH,  //
                    CapabilityId.NAMING_CASE_SENSITIVE_TABLE_ISOLATION, //
                    CapabilityId.NAMING_CASE_INSENSITIVE_FREEDOM_MAP,   //
                    CapabilityId.NAMING_CASE_SENSITIVE_FREEDOM_MAP,//
                    CapabilityId.NAMING_CASE_SENSITIVE_FREEDOM_MIXED_CASE,//
                    CapabilityId.NAMING_CASE_INSENSITIVE_BATCH_MAPPING -> {
                return FeatureId.CASE_SENSITIVE_IDENTIFIERS;
            }
            case CapabilityId.JDBC_CRUD_UPSERT_ON_CONFLICT -> {
                return FeatureId.POSTGRES_ON_CONFLICT;
            }
            case CapabilityId.JDBC_JOIN_SELF, CapabilityId.MAPPER_XML_JOIN_SELF -> {
                return FeatureId.JOIN_NON_EQUI_CONDITION;
            }
            case CapabilityId.JDBC_JOIN_LEFT_NULL, CapabilityId.MAPPER_XML_JOIN_LEFT_NULL, CapabilityId.MAPPER_XML_JOIN_AGGREGATE, CapabilityId.MAPPER_ANNOTATION_JOIN_LEFT_NULL, CapabilityId.MAPPER_ANNOTATION_JOIN_AGGREGATE -> {
                return FeatureId.LEFT_JOIN_NULL_VALUES;
            }
            case CapabilityId.TYPE_BASIC_BIT_CAST_NULL -> {
                return FeatureId.BIT_CAST_NULL_VALUE;
            }
        }
        if (capabilityId.startsWith("jdbc.multiple.")) {
            return FeatureId.MULTIPLE_RESULT_SETS;
        }
        return null;
    }
}
