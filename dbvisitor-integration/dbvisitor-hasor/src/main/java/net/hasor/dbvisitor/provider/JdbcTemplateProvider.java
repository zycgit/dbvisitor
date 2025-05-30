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
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.function.Supplier;

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