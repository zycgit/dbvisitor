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
package net.hasor.dbvisitor.adapter.milvus;
import net.hasor.dbvisitor.driver.JdbcDriver;

public interface MilvusKeys {
    String ADAPTER_NAME        = JdbcDriver.P_ADAPTER_NAME;
    String ADAPTER_NAME_VALUE  = "milvus";
    String START_URL           = JdbcDriver.START_URL + ADAPTER_NAME_VALUE + ":";
    String DEFAULT_CLIENT_NAME = "Milvus-JDBC-Client";

    // for call
    String INTERCEPTOR              = "interceptor";
    String CUSTOM_MILVUS            = "customMilvus";
    // for client
    String SERVER                   = JdbcDriver.P_SERVER;
    String TIME_ZONE                = JdbcDriver.P_TIME_ZONE;
    String DATABASE                 = "database";
    // auth
    String TOKEN                    = "token";
    String USERNAME                 = JdbcDriver.P_USER;
    String PASSWORD                 = JdbcDriver.P_PASSWORD;
    // options
    String TIMEOUT                  = "timeout"; // milliseconds
    String CONNECT_TIMEOUT          = "connectTimeout";
    String KEEP_ALIVE_TIME          = "keepAliveTime";
    String KEEP_ALIVE_TIMEOUT       = "keepAliveTimeout";
    String KEEP_ALIVE_WITHOUT_CALLS = "keepAliveWithoutCalls";
    String IDLE_TIMEOUT             = "idleTimeout";
    String RPC_DEADLINE             = "rpcDeadline";
}
