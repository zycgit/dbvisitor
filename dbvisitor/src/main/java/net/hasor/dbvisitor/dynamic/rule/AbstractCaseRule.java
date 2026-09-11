/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dynamic.rule;

public abstract class AbstractCaseRule implements SqlRule {
    protected static final String CASE_KEY_PREFIX     = "CASE_";
    protected static final String CURRENT_CASE_ID_KEY = "CURRENT_CASE_ID";
    protected static final String TEST_EXPR_SUFFIX    = "_TEST_EXPR"; // Stores the calculated value
    protected static final String HAS_TEST_EXPR_KEY   = "_HAS_TEST_EXPR"; // Flag indicating if activeExpr was present
}
