/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus.commands;

/** SQL hints, command options and Import protocol field names; not JDBC connection properties. */
public interface MilvusCommandKeys {
    // SQL hints; timeout controls synchronous IMPORT/LOAD/RELEASE waits, in milliseconds.
    String TIMEOUT                 = "timeout";
    String SYNC                    = "sync";
    String OVERWRITE_FIND_AS_COUNT = "overwrite_find_as_count";
    String PARTIAL_UPDATE          = "partial_update";
    String OVERWRITE_FIND_LIMIT    = "overwrite_find_limit";
    String OVERWRITE_FIND_SKIP     = "overwrite_find_skip";

    // Search and index properties
    String METRIC_TYPE       = "metric_type";
    String METRIC            = "metric";
    String INDEX_TYPE        = "index_type";
    String RADIUS            = "radius";
    String RANGE_FILTER      = "range_filter";
    String OFFSET            = "offset";
    String RERANKER          = "reranker";
    String RRF_K             = "k";
    String WEIGHTS           = "weights";
    String ROUND_DECIMAL     = "round_decimal";
    String IGNORE_GROWING    = "ignore_growing";
    String TIMEZONE          = "timezone";
    String GROUP_BY_FIELD    = "group_by_field";
    String GROUP_LIMIT       = "group_limit";
    String GROUP_OFFSET      = "group_offset";
    String GROUP_SIZE        = "group_size";
    String STRICT_GROUP_SIZE = "strict_group_size";

    // Native compaction options
    String IS_CLUSTERING = "is_clustering";
    String IS_L0         = "is_l0";
    String TARGET_SIZE   = "target_size";

    // Native collection/partition loading options
    String NUM_REPLICAS            = "num_replicas";
    String REFRESH                 = "refresh";
    String LOAD_FIELDS             = "load_fields";
    String SKIP_LOAD_DYNAMIC_FIELD = "skip_load_dynamic_field";
    String RESOURCE_GROUPS         = "resource_groups";

    // SDK password-update options
    String RESET_CONNECTION        = "reset_connection";
    String DESCRIPTION             = "description";
    String FORCE_DROP              = "force_drop";
    String WAIT_FLUSHED_TIMEOUT_MS = "wait_flushed_timeout_ms";

    // Collection and field schema properties; distinct from the JDBC consistencyLevel name.
    String COLLECTION_CONSISTENCY_LEVEL = "consistency_level";
    String NUM_PARTITIONS               = "num_partitions";
    String NUM_SHARDS                   = "num_shards";
    String DIMENSION                    = "dim";
    String MAX_LENGTH                   = "max_length";
    String MAX_CAPACITY                 = "max_capacity";
    String ENABLE_ANALYZER              = "enable_analyzer";
    String ENABLE_MATCH                 = "enable_match";
    String ANALYZER_PARAMS              = "analyzer_params";
    String ANALYZER_NAMES               = "analyzer_names";
    String WITH_DETAIL                  = "with_detail";
    String WITH_HASH                    = "with_hash";

    // SQL Import options; REST uses the camel-case names below.
    String IMPORT_PAGE_SIZE    = "page_size";
    String IMPORT_CURRENT_PAGE = "current_page";

    // Import REST request fields
    String REST_DB_NAME         = "dbName";
    String REST_COLLECTION_NAME = "collectionName";
    String REST_PARTITION_NAME  = "partitionName";
    String REST_FILES           = "files";
    String REST_OPTIONS         = "options";
    String REST_JOB_ID          = "jobId";
    String REST_PAGE_SIZE       = "pageSize";
    String REST_CURRENT_PAGE    = "currentPage";

    // Import REST response envelope and job fields
    String REST_CODE          = "code";
    String REST_MESSAGE       = "message";
    String REST_DATA          = "data";
    String REST_RECORDS       = "records";
    String REST_STATE         = "state";
    String REST_PROGRESS      = "progress";
    String REST_TOTAL_ROWS    = "totalRows";
    String REST_IMPORTED_ROWS = "importedRows";
    String REST_REASON        = "reason";
    String REST_FAILED_REASON = "failedReason";
}
