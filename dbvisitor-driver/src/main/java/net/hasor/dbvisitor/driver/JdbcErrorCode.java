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
package net.hasor.dbvisitor.driver;
public class JdbcErrorCode {
    // for Driver
    public static final String CODE_URL_FORMAT_ERROR           = "S1001";
    // for Statement
    public static final String SQL_STATE_IS_CLOSED             = "S2001";
    public static final String SQL_STATE_ILLEGAL_ARGUMENT      = "S2002";
    public static final String SQL_STATE_QUERY_IS_UPDATE_COUNT = "S2003";
    public static final String SQL_STATE_QUERY_IS_RESULT       = "S2004";
    public static final String SQL_STATE_QUERY_IS_PENDING      = "S2005";
    public static final String SQL_STATE_QUERY_TIMEOUT         = "S2006";
    public static final String SQL_STATE_QUERY_IS_FINISH       = "S2007";
    public static final String SQL_STATE_QUERY_EMPTY           = "S2008";
    // for Types
    public static final String SQL_STATE_GENERAL_ERROR         = "S3001";
}
