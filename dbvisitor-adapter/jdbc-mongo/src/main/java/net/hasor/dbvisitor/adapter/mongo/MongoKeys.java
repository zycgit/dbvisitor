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

public interface MongoKeys {
    String ADAPTER_NAME        = JdbcDriver.P_ADAPTER_NAME;
    String ADAPTER_NAME_VALUE  = "mongo";
    String START_URL           = JdbcDriver.START_URL + ADAPTER_NAME_VALUE + ":";
    String DEFAULT_CLIENT_NAME = "Mongo-JDBC-Client";

    // for call
    String CUSTOM_MONGO          = "customMongo";
    // for client
    String SERVER                = JdbcDriver.P_SERVER;
    String TIME_ZONE             = JdbcDriver.P_TIME_ZONE;
    String DATABASE              = "database";
    // auth
    String USERNAME              = JdbcDriver.P_USER;
    String PASSWORD              = JdbcDriver.P_PASSWORD;
    String MECHANISM             = "mechanism";
    // options
    String CLIENT_NAME           = "clientName";
    String CONN_TIMEOUT          = "connectTimeout";         // milliseconds
    String SO_TIMEOUT            = "socketTimeout";          // milliseconds
    String SO_SND_BUFF           = "socketSndBuffer";
    String SO_RCV_BUFF           = "socketRcvBuffer";
    String RETRY_WRITES          = "retryWrites";
    String RETRY_READS           = "retryReads";
    // pre-read options
    String PREREAD_ENABLED       = "preRead";
    String PREREAD_THRESHOLD     = "preReadThreshold"; // MB
    String PREREAD_MAX_FILE_SIZE = "preReadMaxFileSize"; // MB
    String PREREAD_CACHE_DIR     = "preReadCacheDir";
}
