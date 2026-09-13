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

public final class Elastic6Profile extends AbstractDataSourceProfile {
    public static final Elastic6Profile INSTANCE = new Elastic6Profile();

    private Elastic6Profile() {
        super(FeatureId.TRANSACTION, FeatureId.SEQUENCE, FeatureId.GENERATED_KEYS_NUMERIC,
                FeatureId.VECTOR, FeatureId.KNN,
                FeatureId.LENGTH_LIMIT_ENFORCED, FeatureId.SQL_NOT_IN_NULL_SEMANTICS,
                FeatureId.KEYGEN_AUTO_BATCH_EXPLICIT_NULL, FeatureId.NON_NULL_PRIMARY_KEY_REJECTED,
                FeatureId.GENERATED_KEY_RESULT_SET, FeatureId.PROCEDURE, FeatureId.PROCEDURE_CURSOR_RESULT,
                FeatureId.PROCEDURE_RESULT_SET, FeatureId.XML_MAPPER_CALLABLE,
                FeatureId.FUNCTION_CALL_CALLBACK, FeatureId.FUNCTION_RECORD_RESULT, FeatureId.FUNCTION_TABLE_RESULT);
    }

    @Override
    public SupportStatus support(String capabilityId) {
        // Index names must be lowercase; field names still preserve their case.
        if (CapabilityId.NAMING_CASE_SENSITIVE_TABLE_ISOLATION.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DATABASE;
        }
        // These templates emit SQL conditions/expressions, not native JSON queries or scripts.
        if (capabilityId != null && (capabilityId.startsWith("mapping.annotation.sql-template.")
                || capabilityId.startsWith("mapper.xml.dynamic-rule.")
                || CapabilityId.MAPPER_ANNOTATION_DYNAMIC_IN.equals(capabilityId)
                || CapabilityId.MAPPER_ANNOTATION_PARAM_IN_LIST.equals(capabilityId)
                || CapabilityId.JDBC_PARAM_RULE_AND_IN_SET.equals(capabilityId)
                || CapabilityId.JDBC_PARAM_RULE_SET.equals(capabilityId))) {
            return SupportStatus.UNSUPPORTED_BY_DBVISITOR;
        }
        if (CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_SCROLL_INSENSITIVE.equals(capabilityId)
                || CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_SCROLL_SENSITIVE.equals(capabilityId)
                || CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_EXTRACTOR_OPTIONS.equals(capabilityId)
                || CapabilityId.MAPPER_XML_STATEMENT_RESULT_SET_TYPE.equals(capabilityId)) {
            return SupportStatus.UNSUPPORTED_BY_DRIVER;
        }
        // Elasticsearch generates string document IDs, not numeric identity columns.
        if (CapabilityId.MAPPER_XML_KEYGEN_GENERATED_KEYS.equals(capabilityId)
                || CapabilityId.MAPPER_XML_KEYGEN_DISTINCT_GENERATED_KEYS.equals(capabilityId)
                || CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_GENERATED_KEYS.equals(capabilityId)) {
            return SupportStatus.SUPPORTED;
        }
        return super.support(capabilityId);
    }

    @Override
    public DataSourceId id() {
        return DataSourceId.ELASTIC6;
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
        return "date";
    }
}
