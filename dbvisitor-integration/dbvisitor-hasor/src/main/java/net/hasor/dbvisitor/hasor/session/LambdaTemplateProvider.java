/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.hasor.session;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;
import net.hasor.dbvisitor.lambda.LambdaTemplate;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-02-09
 */
public class LambdaTemplateProvider implements Supplier<LambdaTemplate> {
    private final Supplier<DataSource> dataSource;

    public LambdaTemplateProvider(DataSource dataSource) {
        this(() -> dataSource);
    }

    public LambdaTemplateProvider(Supplier<DataSource> dataSource) {
        this.dataSource = dataSource;
    }

    public LambdaTemplate get() {
        try {
            return new LambdaTemplate(this.dataSource.get());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
