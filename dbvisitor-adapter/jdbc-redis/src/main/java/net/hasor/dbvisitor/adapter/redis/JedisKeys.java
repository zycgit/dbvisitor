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
package net.hasor.dbvisitor.adapter.redis;
import net.hasor.dbvisitor.driver.JdbcDriver;

public interface JedisKeys {
    String ADAPTER_NAME        = JdbcDriver.P_ADAPTER_NAME;
    String ADAPTER_NAME_VALUE  = "jedis";
    String START_URL           = JdbcDriver.START_URL + ADAPTER_NAME_VALUE + ":";
    String DEFAULT_CLIENT_NAME = "Jedis-JDBC-Client";

    // for call
    String INTERCEPTOR      = "interceptor";
    String CUSTOM_JEDIS     = "customJedis";
    String UNCHECK_NUM_KEYS = "uncheckNumKeys";
    String SEPARATOR_CHAR   = "separatorChar";
    // for client
    String SERVER           = JdbcDriver.P_SERVER;
    String TIME_ZONE        = JdbcDriver.P_TIME_ZONE;
    String CONN_TIMEOUT     = "connectTimeout";
    String SO_TIMEOUT       = "socketTimeout";
    String USERNAME         = JdbcDriver.P_USER;
    String PASSWORD         = JdbcDriver.P_PASSWORD;
    String DATABASE         = "database";
    String CLIENT_NAME      = "clientName";
    // for pool
    String MAX_TOTAL        = "maxTotal";
    String MAX_IDLE         = "maxIdle";
    String MIN_IDLE         = "minIdle";
    String TEST_WHILE_IDLE  = "testWhileIdle";
    String MAX_ATTEMPTS     = "maxAttempts";
}
