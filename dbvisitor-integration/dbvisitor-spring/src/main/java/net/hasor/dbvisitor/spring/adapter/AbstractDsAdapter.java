/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.spring.adapter;
import javax.sql.DataSource;
import net.hasor.dbvisitor.jdbc.DynamicConnection;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-29
 */
public abstract class AbstractDsAdapter implements DynamicConnection {
    private DataSource dataSource;

    public DataSource getDataSource() {
        return this.dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
