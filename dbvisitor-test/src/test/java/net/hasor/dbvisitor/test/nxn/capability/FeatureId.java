package net.hasor.dbvisitor.test.nxn.capability;

public final class FeatureId {
    public static final String ARRAY                              = "array";
    public static final String JSON                               = "json";
    public static final String BINARY                             = "binary";
    public static final String SEQUENCE                           = "sequence";
    public static final String GENERATED_KEY_COLUMN               = "generated-key-column";
    public static final String XML_SELECT_KEY_USER_INFO_SEQUENCE  = "xml-select-key-user-info-sequence";
    public static final String PROCEDURE                          = "procedure";
    public static final String PROCEDURE_CURSOR_RESULT            = "procedure-cursor-result";
    public static final String PROCEDURE_RESULT_SET                = "procedure-result-set";
    public static final String XML_MAPPER_CALLABLE                = "xml-mapper-callable";
    public static final String FUNCTION                           = "function";
    public static final String FUNCTION_CALL_CALLBACK             = "function-call-callback";
    public static final String FUNCTION_RECORD_RESULT             = "function-record-result";
    public static final String FUNCTION_TABLE_RESULT              = "function-table-result";
    public static final String VECTOR                             = "vector";
    public static final String KNN                                = "knn";
    public static final String TIME_LOCAL_DATE                    = "time-local-date";
    public static final String TIME_EXTREME_DATE                  = "time-extreme-date";
    public static final String DELIMITED_LOWERCASE_STANDARD_TABLE = "delimited-lowercase-standard-table";
    public static final String POSTGRES_ON_CONFLICT               = "postgres-on-conflict";
    public static final String DUPLICATE_KEY_STRATEGY             = "duplicate-key-strategy";
    public static final String DUPLICATE_PRIMARY_KEY_REJECTED     = "duplicate-primary-key-rejected";
    public static final String BATCH_DUPLICATE_FAILURE_PROPAGATED = "batch-duplicate-failure-propagated";
    public static final String EXACT_MUTATION_AFFECTED_ROWS       = "exact-mutation-affected-rows";
    public static final String LENGTH_LIMIT_ENFORCED              = "length-limit-enforced";
    public static final String NON_NULL_PRIMARY_KEY_REJECTED      = "non-null-primary-key-rejected";
    public static final String EMPTY_WHERE_MUTATION               = "empty-where-mutation";
    public static final String TRANSACTION                        = "transaction";
    public static final String TIME_ZONE_STABLE_ROUND_TRIP        = "time-zone-stable-round-trip";
    public static final String JOIN_NON_EQUI_CONDITION            = "join-non-equi-condition";
    public static final String LEFT_JOIN_NULL_VALUES              = "left-join-null-values";
    public static final String SQL_NOT_IN_NULL_SEMANTICS          = "sql-not-in-null-semantics";
    public static final String BIT_CAST_NULL_VALUE                = "bit-cast-null-value";
    public static final String MULTIPLE_RESULT_SETS               = "multiple-result-sets";
    public static final String SQL_MD5_FUNCTION                   = "sql-md5-function";
    public static final String KEYGEN_AUTO_BATCH_EXPLICIT_NULL    = "keygen-auto-batch-explicit-null";
    public static final String KEYGEN_UUID_WRONG_TYPE_REJECTED    = "keygen-uuid-wrong-type-rejected";
    public static final String CASE_SENSITIVE_IDENTIFIERS         = "case-sensitive-identifiers";
    public static final String DISTINCT_EMPTY_STRING              = "distinct-empty-string";
    public static final String LARGE_IN_LIST                      = "large-in-list";
    public static final String XML_FOREACH_BATCH_INSERT_VALUES    = "xml-foreach-batch-insert-values";
    public static final String GENERATED_KEYS_NUMERIC             = "generated-keys-numeric";
    public static final String LOWERCASE_STANDARD_RESULT_COLUMNS  = "lowercase-standard-result-columns";
    public static final String TRANSACTION_RELEASE_SAVEPOINT      = "transaction-release-savepoint";
    public static final String TRANSACTION_REPEATABLE_READ        = "transaction-repeatable-read";

    private FeatureId() {
    }
}
