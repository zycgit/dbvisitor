/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.nxn.env;

public final class MongoProfile extends AbstractDataSourceProfile {
    public static final MongoProfile INSTANCE = new MongoProfile();

    private MongoProfile() {
    }

    @Override
    public DataSourceId id() {
        return DataSourceId.MONGO;
    }

    @Override
    public String leftQualifier() {
        return "";
    }

    @Override
    public String rightQualifier() {
        return "";
    }

    @Override
    public String castToBigInt(String expression) {
        return expression;
    }

    @Override
    public String datetimeColumnType() {
        return "DATE";
    }
}
