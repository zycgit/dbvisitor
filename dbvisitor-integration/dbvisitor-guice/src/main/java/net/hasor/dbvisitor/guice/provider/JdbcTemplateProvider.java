/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.guice.provider;
import javax.sql.DataSource;
import com.google.inject.Provider;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-07-18
 */
public class JdbcTemplateProvider implements Provider<JdbcTemplate> {
    private final Provider<DataSource> dataSource;

    public JdbcTemplateProvider(Provider<DataSource> dataSource) {
        this.dataSource = dataSource;
    }

    public JdbcTemplate get() {
        return new JdbcTemplate(this.dataSource.get());
    }
}
