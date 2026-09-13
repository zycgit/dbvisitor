/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.nxn.env;

import java.util.Set;

import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.capability.SupportStatus;

public final class RedisProfile extends AbstractDataSourceProfile {
    public static final RedisProfile INSTANCE = new RedisProfile();

    // These annotation scenarios execute builder CRUD; annotation metadata itself remains available.
    private static final Set<String> BUILDER_MAPPING = Set.of(
            CapabilityId.MAPPING_ANNOTATION_PRIMARY_KEY_CONDITION_UPDATE,
            CapabilityId.MAPPING_ANNOTATION_COLUMN_FIELD_CRUD,
            CapabilityId.MAPPING_ANNOTATION_COLUMN_NAME_VALUE_EQUIVALENCE,
            CapabilityId.MAPPING_ANNOTATION_MAP_QUERY_RESULT,
            CapabilityId.MAPPING_ANNOTATION_BASIC_VALUE_ROUND_TRIP,
            CapabilityId.MAPPING_ANNOTATION_INSERT_FALSE,
            CapabilityId.MAPPING_ANNOTATION_UPDATE_FALSE,
            CapabilityId.MAPPING_ANNOTATION_READ_ONLY_FIELD,
            CapabilityId.MAPPING_ANNOTATION_IGNORE_LIFECYCLE,
            CapabilityId.MAPPING_ANNOTATION_IGNORE_MULTIPLE_FIELDS,
            CapabilityId.MAPPING_ANNOTATION_IGNORE_OVERRIDES_COLUMN,
            CapabilityId.MAPPING_ANNOTATION_IGNORE_ON_METHOD,
            CapabilityId.MAPPING_ANNOTATION_IGNORE_SAMPLE,
            CapabilityId.MAPPING_ANNOTATION_AUTO_MAPPING_FALSE,
            CapabilityId.MAPPING_ANNOTATION_NULL_VALUE_ROUND_TRIP,
            CapabilityId.MAPPING_ANNOTATION_UPDATE_NULL_VALUE,
            CapabilityId.MAPPING_ANNOTATION_PARTIAL_INSERT,
            CapabilityId.MAPPING_ANNOTATION_PARTIAL_UPDATE,
            CapabilityId.MAPPING_ANNOTATION_EMPTY_STRING_ROUND_TRIP,
            CapabilityId.MAPPING_ANNOTATION_SPECIAL_TYPE_JSON_MAP,
            CapabilityId.MAPPING_ANNOTATION_SPECIAL_TYPE_JSON_LIST,
            CapabilityId.MAPPING_ANNOTATION_SPECIAL_TYPE_JSON_SET,
            CapabilityId.MAPPING_ANNOTATION_SPECIAL_TYPE_ARRAY);

    private static final Set<String> AUTOMATIC_PAGING = Set.of(
            CapabilityId.MAPPER_ANNOTATION_RESULT_PAGE, CapabilityId.BASEMAPPER_STATEMENT_QUERY_PAGE,
            CapabilityId.MAPPER_XML_QUERY_PAGE, CapabilityId.MAPPER_XML_PAGE_STATEMENT,
            CapabilityId.MAPPER_XML_PAGE_STATEMENT_BOUNDARY, CapabilityId.SESSION_STATEMENT_QUERY_PAGE,
            CapabilityId.SESSION_STATEMENT_PAGE_RESULT);

    private RedisProfile() {
        super(FeatureId.ARRAY, FeatureId.SEQUENCE, FeatureId.GENERATED_KEY_COLUMN, FeatureId.GENERATED_KEYS_NUMERIC,
                FeatureId.PROCEDURE, FeatureId.PROCEDURE_CURSOR_RESULT, FeatureId.PROCEDURE_RESULT_SET,
                FeatureId.XML_MAPPER_CALLABLE, FeatureId.FUNCTION_RECORD_RESULT, FeatureId.FUNCTION_TABLE_RESULT,
                FeatureId.VECTOR, FeatureId.KNN, FeatureId.DUPLICATE_PRIMARY_KEY_REJECTED,
                FeatureId.BATCH_DUPLICATE_FAILURE_PROPAGATED, FeatureId.TRANSACTION,
                FeatureId.TRANSACTION_SAVEPOINT, FeatureId.TRANSACTION_RELEASE_SAVEPOINT,
                FeatureId.TRANSACTION_REPEATABLE_READ);
    }

    @Override
    public SupportStatus support(String capabilityId) {
        if (capabilityId == null) {
            return super.support(null);
        }
        // These propagation modes deliberately do not start a database transaction.
        if (CapabilityId.TRANSACTION_SUPPORTS_NO_TX.equals(capabilityId)
                || CapabilityId.TRANSACTION_NEVER_NO_TX.equals(capabilityId)) {
            return SupportStatus.SUPPORTED;
        }
        // Redis commands have no SQL-builder dialect or PageSqlDialect translation.
        if (capabilityId.startsWith("lambda.") || capabilityId.startsWith("map-query.")
                || capabilityId.startsWith("keygen.") || capabilityId.startsWith("naming.")
                || capabilityId.startsWith("mapping.annotation.sql-template.")
                || BUILDER_MAPPING.contains(capabilityId) || AUTOMATIC_PAGING.contains(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DRIVER;
        }
        if (capabilityId.startsWith("basemapper.") && !capabilityId.startsWith("basemapper.statement.")
                && !CapabilityId.BASEMAPPER_ACCESSORS.equals(capabilityId)
                && !CapabilityId.BASEMAPPER_NATIVE_ACCESSORS.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DRIVER;
        }
        // MULTI/EXEC is not the JDBC commit/rollback model; RESP has no JDBC OUT parameters.
        if (capabilityId.startsWith("transaction.") || capabilityId.startsWith("procedure.")
                || capabilityId.startsWith("mapper.xml.callable.")
                || capabilityId.startsWith("vector.") || capabilityId.startsWith("type.array.")) {
            return SupportStatus.UNSUPPORTED_BY_DRIVER;
        }
        // SQL fragment rules are not general-purpose native-command collection binding.
        if (CapabilityId.MAPPER_ANNOTATION_DYNAMIC_IN.equals(capabilityId)
                || CapabilityId.MAPPER_ANNOTATION_PARAM_IN_LIST.equals(capabilityId)
                || CapabilityId.JDBC_PARAM_RULE_AND_IN_SET.equals(capabilityId)
                || CapabilityId.JDBC_PARAM_RULE_SET.equals(capabilityId)
                || CapabilityId.MAPPER_XML_DYNAMIC_WHERE.equals(capabilityId)
                || CapabilityId.MAPPER_XML_DYNAMIC_SET.equals(capabilityId)
                || CapabilityId.MAPPER_XML_DYNAMIC_COMPLEX.equals(capabilityId)
                || CapabilityId.MAPPER_XML_REF_DYNAMIC.equals(capabilityId)
                || capabilityId.startsWith("mapper.xml.dynamic-rule.")) {
            return SupportStatus.UNSUPPORTED_BY_DBVISITOR;
        }
        // Missing keys return nil; repeated keys overwrite rather than violate a primary-key constraint.
        if (CapabilityId.MAPPER_ANNOTATION_EXECUTE_ERROR.equals(capabilityId)
                || CapabilityId.MAPPER_ANNOTATION_DUPLICATE_KEY.equals(capabilityId)
                || CapabilityId.SCHEMA_STANDARD_TABLES.equals(capabilityId)
                || CapabilityId.SCHEMA_STANDARD_COLUMNS.equals(capabilityId)
                || CapabilityId.TYPE_BASIC_BOOLEAN_NULL.equals(capabilityId)
                || CapabilityId.TYPE_BASIC_CHARACTER_NULL_EMPTY.equals(capabilityId)
                || CapabilityId.TYPE_TIME_NULL.equals(capabilityId)
                || CapabilityId.TYPE_JSON_BIND_ANNOTATION_NULL.equals(capabilityId)
                || CapabilityId.TYPE_JSON_NULL.equals(capabilityId)
                || CapabilityId.TYPE_JSON_NULL_ROW.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DATABASE;
        }
        if (CapabilityId.TYPE_BINARY_NULL.equals(capabilityId) || CapabilityId.TYPE_ENUM_NULL.equals(capabilityId)
                || CapabilityId.JDBC_PARAM_NULL.equals(capabilityId)
                || CapabilityId.JDBC_BOUND_NULL_EMPTY.equals(capabilityId)
                || CapabilityId.JDBC_BATCH_PARTIAL_FAILURE.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DATABASE;
        }
        if (CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_EXTRACTOR_OPTIONS.equals(capabilityId)
                || CapabilityId.MAPPER_XML_STATEMENT_RESULT_SET_TYPE.equals(capabilityId)
                || CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_SCROLL_INSENSITIVE.equals(capabilityId)
                || CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_SCROLL_SENSITIVE.equals(capabilityId)
                || CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_GENERATED_KEYS.equals(capabilityId)
                || CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_KEY_COLUMN.equals(capabilityId)
                || CapabilityId.MAPPER_XML_KEYGEN_GENERATED_KEYS.equals(capabilityId)
                || CapabilityId.MAPPER_XML_KEYGEN_DISTINCT_GENERATED_KEYS.equals(capabilityId)
                || CapabilityId.MAPPER_XML_KEYGEN_KEY_COLUMN.equals(capabilityId)
                || CapabilityId.SESSION_COMPONENT_LAMBDA.equals(capabilityId)
                || CapabilityId.SESSION_BASEMAPPER_CRUD.equals(capabilityId)
                || CapabilityId.SESSION_BASEMAPPER_MULTI_ENTITY.equals(capabilityId)
                || CapabilityId.SESSION_BASEMAPPER_COMPOSITE_KEY.equals(capabilityId)
                || CapabilityId.SESSION_BASEMAPPER_MULTI_INSTANCE.equals(capabilityId)
                || CapabilityId.SESSION_MAPPER_DECLARATIVE.equals(capabilityId)
                || CapabilityId.SESSION_MAPPER_MIXED.equals(capabilityId)
                || CapabilityId.MAPPING_ANNOTATION_TYPE_HANDLER_INSERT.equals(capabilityId)
                || CapabilityId.MAPPING_ANNOTATION_JDBC_TYPE_ROUND_TRIP.equals(capabilityId)
                || CapabilityId.MAPPING_ANNOTATION_SPECIAL_JAVA_TYPE_ROUND_TRIP.equals(capabilityId)
                || CapabilityId.MAPPER_XML_RESULTMAP_CAMELCASE.equals(capabilityId)
                || CapabilityId.FUNCTION_QUERY_OUT_RECORD.equals(capabilityId)
                || CapabilityId.FUNCTION_QUERY_TABLE_RESULT.equals(capabilityId)
                || CapabilityId.FUNCTION_QUERY_NULL_FALLBACK.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DRIVER;
        }
        return super.support(capabilityId);
    }

    @Override
    public DataSourceId id() {
        return DataSourceId.REDIS;
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
        return "STRING";
    }
}
