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
package net.hasor.dbvisitor.provider;
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
