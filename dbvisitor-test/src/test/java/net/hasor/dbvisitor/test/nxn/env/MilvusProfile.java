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

public final class MilvusProfile extends AbstractDataSourceProfile {
    public static final MilvusProfile INSTANCE = new MilvusProfile();

    private MilvusProfile() {
        // Verified server baseline 2.6.2 requires a string literal on the right of LIKE.
        super(FeatureId.PARAMETERIZED_LIKE, FeatureId.PARAMETERIZED_NOT_COMPARISON, FeatureId.SCALAR_ORDER_BY,
                FeatureId.COMPOSITE_PRIMARY_KEY, FeatureId.BIT_CAST_NULL_VALUE, FeatureId.SQL_NOT_IN_NULL_SEMANTICS,
                FeatureId.TRANSACTION, FeatureId.TRANSACTION_RELEASE_SAVEPOINT, FeatureId.TRANSACTION_REPEATABLE_READ,
                FeatureId.PROCEDURE, FeatureId.PROCEDURE_CURSOR_RESULT, FeatureId.PROCEDURE_RESULT_SET,
                FeatureId.XML_MAPPER_CALLABLE, FeatureId.SEQUENCE, FeatureId.XML_SELECT_KEY_USER_INFO_SEQUENCE,
                FeatureId.BATCH_DUPLICATE_FAILURE_PROPAGATED, FeatureId.DUPLICATE_PRIMARY_KEY_REJECTED,
                FeatureId.GENERATED_KEY_COLUMN, FeatureId.GENERATED_KEY_RESULT_SET, FeatureId.BINARY,
                FeatureId.FUNCTION, FeatureId.FUNCTION_RECORD_RESULT, FeatureId.FUNCTION_TABLE_RESULT,
                FeatureId.FUNCTION_CALL_CALLBACK);
    }

    @Override
    public SupportStatus support(String capabilityId) {
        // The shared composite-key contracts require two stored primary-key columns.
        if (capabilityId != null && capabilityId.startsWith("basemapper.composite.")) {
            return SupportStatus.UNSUPPORTED_BY_DATABASE;
        }
        // These template fixtures execute MD5/LOWER/UPPER/CONCAT expressions, not plain field mappings.
        if (capabilityId != null && capabilityId.startsWith("mapping.annotation.sql-template.")) {
            return SupportStatus.UNSUPPORTED_BY_DRIVER;
        }
        // Both scenarios explicitly request scrollable results in addition to forward-only results.
        if (CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_RESULT_SET_TYPE.equals(capabilityId)
                || CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_COMBINED.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DRIVER;
        }
        // The driver supports forward-only results; native regression cases verify scroll rejection.
        if (CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_EXTRACTOR_OPTIONS.equals(capabilityId)
                || CapabilityId.MAPPER_XML_STATEMENT_RESULT_SET_TYPE.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DRIVER;
        }
        // Milvus has no atomic insert-if-absent operation; partial upsert only covers Update.
        if (CapabilityId.LAMBDA_DUPLICATE_STRATEGY_IGNORE_BASIC.equals(capabilityId)
                || CapabilityId.LAMBDA_DUPLICATE_STRATEGY_IGNORE_MIXED.equals(capabilityId)
                || CapabilityId.LAMBDA_DUPLICATE_STRATEGY_TRANSITION.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DATABASE;
        }
        // The SQL grammar/dialect does not implement quoted identifiers or automatic keyword quoting.
        if (CapabilityId.NAMING_DELIMITED_SQL.equals(capabilityId)
                || CapabilityId.NAMING_DELIMITED_CRUD.equals(capabilityId)
                || CapabilityId.NAMING_KEYWORD_COLUMN_SQL.equals(capabilityId)
                || CapabilityId.NAMING_KEYWORD_TABLE_SQL.equals(capabilityId)
                || CapabilityId.NAMING_KEYWORD_COLUMN_CRUD.equals(capabilityId)
                || CapabilityId.NAMING_KEYWORD_TABLE_CRUD.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DRIVER;
        }
        // The complete scenario asserts zero for a missing primary-key DELETE as well as UPDATE.
        if (CapabilityId.MAPPER_ANNOTATION_NO_MATCH_AFFECTED_ROWS.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DATABASE;
        }
        // LIKE-specific cases remain separate from comparisons, ranges and IN expansion.
        if (CapabilityId.LAMBDA_PREDICATE_LIKE_VARIANTS.equals(capabilityId)
                || CapabilityId.LAMBDA_PREDICATE_NOT_LIKE_VARIANTS.equals(capabilityId)
                || CapabilityId.LAMBDA_PREDICATE_LIKE_NULL_AND_MULTI.equals(capabilityId)
                || CapabilityId.LAMBDA_PREDICATE_STRING_LIKE.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DATABASE;
        }
        // Computed projections require DISTINCT or aggregates; page fixtures use native vector ordering.
        if (CapabilityId.MAPPER_ANNOTATION_RESULT_DISTINCT_LIST.equals(capabilityId)
                || CapabilityId.MAPPER_XML_REF_AGGREGATE_MAP.equals(capabilityId)
                || CapabilityId.LAMBDA_RESULT_CALCULATED_COLUMN.equals(capabilityId)
                || CapabilityId.MAPPER_ANNOTATION_RESULT_AGGREGATE.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DATABASE;
        }
        // BLOB/VARBINARY fixtures cannot be replaced by fixed-dimension vector fields, even for NULL tests.
        if (CapabilityId.TYPE_BINARY_NULL.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DATABASE;
        }
        // JDBC table/column metadata currently exposes empty result sets, not SDK collection descriptions.
        if (CapabilityId.SCHEMA_STANDARD_TABLES.equals(capabilityId)
                || CapabilityId.SCHEMA_STANDARD_COLUMNS.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DRIVER;
        }
        // Simple primary-key DELETE acknowledges submitted keys even when no entity exists.
        // Do not disable exact counts for UPDATE, which uses a different selection/write path.
        if (CapabilityId.LAMBDA_EDGE_DELETE_NO_MATCH.equals(capabilityId)
                || CapabilityId.SESSION_STATEMENT_DELETE_NO_MATCH.equals(capabilityId)
                || CapabilityId.BASEMAPPER_STATEMENT_DELETE_NO_MATCH.equals(capabilityId)
                || CapabilityId.BASEMAPPER_DELETE_BOUNDARY.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DATABASE;
        }
        // The multiple-result query requires scalar ORDER BY id.
        if (CapabilityId.LAMBDA_EDGE_QUERY_MULTI_RESULT.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DATABASE;
        }
        // These contracts require relational joins or scalar ordering, not vector ranking.
        if (capabilityId != null && (capabilityId.startsWith("jdbc.join.")
                || capabilityId.startsWith("mapper.annotation.join.")
                || capabilityId.startsWith("mapper.xml.join.")
                || capabilityId.startsWith("lambda.sort."))) {
            return SupportStatus.UNSUPPORTED_BY_DATABASE;
        }
        // JDBC generated-key column selection is explicitly unsupported, unlike RETURN_GENERATED_KEYS.
        if (CapabilityId.MAPPER_XML_KEYGEN_KEY_COLUMN.equals(capabilityId)
                || CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_KEY_COLUMN.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DRIVER;
        }
        // TruncateCollection was introduced in server 2.6.11, after the 2.6.2 test baseline.
        if (CapabilityId.ADAPTER_MILVUS_SQL_TRUNCATE.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DATABASE;
        }
        // 2.6.2 filters dbNames but loops over dbsRsp.DbNames in GetFlushAllState.
        // Native SDK reproduces a scoped flush waiting on unrelated databases until timeout.
        if (CapabilityId.ADAPTER_MILVUS_SQL_FLUSH_ALL.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DATABASE;
        }
        // 2.6.2 rejects logical AND between a field predicate and a folded constant value.
        // The unchanged native SDK query/delete both reject "id in {ids} AND 1 == 1".
        if (CapabilityId.LAMBDA_SECURITY_APPLY_SCOPED_TRUE.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DATABASE;
        }
        // Native SDK reproduces a 2.6.2 QueryNode assertion for NOT of a templated integer comparison.
        // Plain !=, literal NOT, and NOT(IS NULL) are not affected by this gate.
        if (CapabilityId.LAMBDA_LOGIC_MARKER_NOT.equals(capabilityId)
                || CapabilityId.LAMBDA_LOGIC_DYNAMIC_NOT.equals(capabilityId)
                || CapabilityId.LAMBDA_LOGIC_DOUBLE_NOT.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DATABASE;
        }
        // These shared Lambda scenarios contain LIKE with bound values, unsupported on 2.6.2.
        if (CapabilityId.LAMBDA_LOGIC_NOT_NESTED.equals(capabilityId)
                || CapabilityId.LAMBDA_LOGIC_NOT_IN_LIKE.equals(capabilityId)
                || CapabilityId.LAMBDA_EMPTY_LIKE_EMPTY_STRING.equals(capabilityId)
                || CapabilityId.LAMBDA_SECURITY_VALUE_LIKE.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DATABASE;
        }
        // Scalar DISTINCT, grouped aggregation and SUM/MAX are not 2.6.2 query capabilities.
        // Vector grouping is a different operation and cannot implement these contracts.
        if (CapabilityId.LAMBDA_SELECT_DISTINCT.equals(capabilityId)
                || CapabilityId.LAMBDA_RESULT_AGGREGATE_SCALAR.equals(capabilityId)
                || CapabilityId.LAMBDA_RESULT_AGGREGATE_ROW_MAPPER.equals(capabilityId)
                || CapabilityId.LAMBDA_SELECT_DISTINCT_COUNT.equals(capabilityId)
                || CapabilityId.LAMBDA_SELECT_GROUP_BY.equals(capabilityId)
                || CapabilityId.LAMBDA_SELECT_AGGREGATE.equals(capabilityId)
                || CapabilityId.LAMBDA_EMPTY_GROUP_BY.equals(capabilityId)
                || CapabilityId.LAMBDA_EMPTY_AGGREGATE.equals(capabilityId)
                || CapabilityId.LAMBDA_EMPTY_DISTINCT.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DATABASE;
        }
        // Milvus collections have exactly one primary-key field, not a composite key.
        if (CapabilityId.SESSION_BASEMAPPER_COMPOSITE_KEY.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DATABASE;
        }
        return super.support(capabilityId);
    }

    @Override
    public DataSourceId id() {
        return DataSourceId.MILVUS;
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
        return "VARCHAR(32)";
    }
}
