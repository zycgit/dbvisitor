/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dynamic.rule;

/** Shared names for internal CASE state stored in the argument source. */
public abstract class AbstractCaseRule implements SqlRule {
    protected static final String INNER_KEY_CASE_PREFIX          = "INNER_KEY_CASE_";
    protected static final String INNER_KEY_CURRENT_CASE_ID      = "INNER_KEY_CURRENT_CASE_ID";
    protected static final String INNER_KEY_TEST_EXPR_SUFFIX     = "_TEST_EXPR"; // Appended to the prefixed case ID.
    protected static final String INNER_KEY_HAS_TEST_EXPR_SUFFIX = "_HAS_TEST_EXPR";
}
