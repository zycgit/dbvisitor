/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.adapter.milvus.commands;

/** SQL hints, command options and Import protocol field names; not JDBC connection properties. */
public interface MilvusCommandKeys {
    // SQL hints; timeout controls synchronous IMPORT/LOAD/RELEASE waits, in milliseconds.
    String TIMEOUT                 = "timeout";
    String SYNC                    = "sync";
    String OVERWRITE_FIND_AS_COUNT = "overwrite_find_as_count";
    String OVERWRITE_FIND_LIMIT    = "overwrite_find_limit";
    String OVERWRITE_FIND_SKIP     = "overwrite_find_skip";

    // Search and index properties
    String METRIC_TYPE  = "metric_type";
    String METRIC       = "metric";
    String INDEX_TYPE   = "index_type";
    String RADIUS       = "radius";
    String RANGE_FILTER = "range_filter";
    String OFFSET       = "offset";
    String RERANKER     = "reranker";
    String RRF_K        = "k";
    String WEIGHTS      = "weights";

    // Collection and field schema properties; distinct from the JDBC consistencyLevel name.
    String COLLECTION_CONSISTENCY_LEVEL = "consistency_level";
    String DIMENSION                    = "dim";
    String MAX_LENGTH                   = "max_length";
    String MAX_CAPACITY                 = "max_capacity";
    String ENABLE_ANALYZER              = "enable_analyzer";
    String ENABLE_MATCH                 = "enable_match";
    String ANALYZER_PARAMS              = "analyzer_params";

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
