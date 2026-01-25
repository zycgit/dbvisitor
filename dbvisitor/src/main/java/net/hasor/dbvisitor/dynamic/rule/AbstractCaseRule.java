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
package net.hasor.dbvisitor.dynamic.rule;

public abstract class AbstractCaseRule implements SqlRule {
    protected static final String CASE_KEY_PREFIX     = "CASE_";
    protected static final String CURRENT_CASE_ID_KEY = "CURRENT_CASE_ID";
    protected static final String TEST_EXPR_SUFFIX    = "_TEST_EXPR"; // Stores the calculated value
    protected static final String HAS_TEST_EXPR_KEY   = "_HAS_TEST_EXPR"; // Flag indicating if activeExpr was present
}