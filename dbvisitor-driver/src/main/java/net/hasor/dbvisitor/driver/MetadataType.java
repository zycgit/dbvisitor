/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.driver;

/** Native object kinds understood by the JDBC metadata bridge. */
public enum MetadataType {
    CATALOG,
    SCHEMA,
    TABLE,
    VIEW,
    COLUMN
}
