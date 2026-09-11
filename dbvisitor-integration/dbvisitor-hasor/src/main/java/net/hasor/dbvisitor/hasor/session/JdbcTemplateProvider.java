/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.hasor.session;
import java.util.function.Supplier;
import javax.sql.DataSource;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2017-07-12
 */
public class JdbcTemplateProvider implements Supplier<JdbcTemplate> {
    private final Supplier<DataSource> dataSource;

    public JdbcTemplateProvider(DataSource dataSource) {
        this(() -> dataSource);
    }

    public JdbcTemplateProvider(Supplier<DataSource> dataSource) {
        this.dataSource = dataSource;
    }

    public JdbcTemplate get() {
        return new JdbcTemplate(this.dataSource.get());
    }
}
