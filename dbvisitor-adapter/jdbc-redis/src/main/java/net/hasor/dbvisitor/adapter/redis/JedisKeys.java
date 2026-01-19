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

public class JedisKeys {
    public static final String ADAPTER_NAME        = JdbcDriver.P_ADAPTER_NAME;
    public static final String ADAPTER_NAME_VALUE  = "jedis";
    public static final String START_URL           = JdbcDriver.START_URL + ADAPTER_NAME_VALUE + ":";
    public static final String DEFAULT_CLIENT_NAME = "Jedis-JDBC-Client";

    // for call
    public static final String INTERCEPTOR      = "interceptor";
    public static final String CUSTOM_JEDIS     = "customJedis";
    public static final String UNCHECK_NUM_KEYS = "uncheckNumKeys";
    public static final String SEPARATOR_CHAR   = "separatorChar";

    // for client
    public static final String SERVER          = JdbcDriver.P_SERVER;
    public static final String TIME_ZONE       = JdbcDriver.P_TIME_ZONE;
    public static final String CONN_TIMEOUT    = "connectTimeout";
    public static final String SO_TIMEOUT      = "socketTimeout";
    public static final String USERNAME        = JdbcDriver.P_USER;
    public static final String PASSWORD        = JdbcDriver.P_PASSWORD;
    public static final String DATABASE        = "database";
    public static final String CLIENT_NAME     = "clientName";
    // for pool
    public static final String MAX_TOTAL       = "maxTotal";
    public static final String MAX_IDLE        = "maxIdle";
    public static final String MIN_IDLE        = "minIdle";
    public static final String TEST_WHILE_IDLE = "testWhileIdle";
    public static final String MAX_ATTEMPTS    = "maxAttempts";
}
