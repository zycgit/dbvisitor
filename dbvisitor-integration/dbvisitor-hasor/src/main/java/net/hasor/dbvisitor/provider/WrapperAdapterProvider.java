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
import net.hasor.dbvisitor.wrapper.WrapperAdapter;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.function.Supplier;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-02-09
 */
public class WrapperAdapterProvider implements Supplier<WrapperAdapter> {
    private final Supplier<DataSource> dataSource;

    public WrapperAdapterProvider(DataSource dataSource) {
        this(() -> dataSource);
    }

    public WrapperAdapterProvider(Supplier<DataSource> dataSource) {
        this.dataSource = dataSource;
    }

    public WrapperAdapter get() {
        try {
            return new WrapperAdapter(this.dataSource.get());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
