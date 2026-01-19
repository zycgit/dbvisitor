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
package net.hasor.dbvisitor.adapter.mongo;
import net.hasor.dbvisitor.driver.JdbcDriver;

public class MongoKeys {
    public static final String ADAPTER_NAME        = JdbcDriver.P_ADAPTER_NAME;
    public static final String ADAPTER_NAME_VALUE  = "mongo";
    public static final String START_URL           = JdbcDriver.START_URL + ADAPTER_NAME_VALUE + ":";
    public static final String DEFAULT_CLIENT_NAME = "Mongo-JDBC-Client";

    // for call
    public static final String CUSTOM_MONGO = "customMongo";

    // for client
    public static final String SERVER       = JdbcDriver.P_SERVER;
    public static final String TIME_ZONE    = JdbcDriver.P_TIME_ZONE;
    // auth
    public static final String USERNAME     = JdbcDriver.P_USER;
    public static final String PASSWORD     = JdbcDriver.P_PASSWORD;
    public static final String MECHANISM    = "mechanism";
    // options
    public static final String CLIENT_NAME  = "clientName";
    public static final String CONN_TIMEOUT = "connectTimeout";         // milliseconds
    public static final String SO_TIMEOUT   = "socketTimeout";          // milliseconds
    public static final String SO_SND_BUFF  = "socketSndBuffer";
    public static final String SO_RCV_BUFF  = "socketRcvBuffer";
    public static final String RETRY_WRITES = "retryWrites";
    public static final String RETRY_READS  = "retryReads";

    // pre-read options
    public static final String PREREAD_ENABLED       = "preRead";
    public static final String PREREAD_THRESHOLD     = "preReadThreshold"; // MB
    public static final String PREREAD_MAX_FILE_SIZE = "preReadMaxFileSize"; // MB
    public static final String PREREAD_CACHE_DIR     = "preReadCacheDir";
}
