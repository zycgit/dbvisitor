/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.guice.provider;
import java.sql.SQLException;
import javax.sql.DataSource;
import com.google.inject.Provider;
import net.hasor.dbvisitor.lambda.LambdaTemplate;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-07-18
 */
public class LambdaTemplateProvider implements Provider<LambdaTemplate> {
    private final Provider<DataSource> dataSource;

    public LambdaTemplateProvider(Provider<DataSource> dataSource) {
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
