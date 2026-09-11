/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
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
    public static final String SQL_STATE_SYNTAX_ERROR          = "S2009";
    public static final String SQL_STATE_IS_CANCELLED          = "S2010";
    // for Types
    public static final String SQL_STATE_GENERAL_ERROR         = "S3001";
}
