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
package net.hasor.dbvisitor.test;

import javax.sql.DataSource;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.dbvisitor.hasor.autoconfig.DefaultDataSource;
import net.hasor.dbvisitor.hasor.mapper.MapperScannerConfigurer;
import net.hasor.dbvisitor.hasor.session.SessionConfigurer;
import net.hasor.dbvisitor.mapper.Mapper;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.dao.user.UserMapper;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class ModuleCompositionTest {
    @Test
    public void shouldUseApplicationDataSourceWithRuntimeAndMapperModules() {
        DefaultDataSource dataSource = new DefaultDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setJdbcUrl("jdbc:h2:mem:module_composition;MODE=MySQL;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        AppContext context = Hasor.create().build(apiBinder -> {
            apiBinder.bindType(DataSource.class).toInstance(dataSource);
            apiBinder.installModule(new SessionConfigurer());

            MapperScannerConfigurer scanner = new MapperScannerConfigurer();
            scanner.setBasePackage("net.hasor.dbvisitor.test.dao");
            scanner.setMarkerInterface(Mapper.class);
            apiBinder.installModule(scanner);
        });
        try {
            assertSame(dataSource, context.getInstance(DataSource.class));
            assertNotNull(context.getInstance(Session.class));
            assertNotNull(context.getInstance(UserMapper.class));
        } finally {
            context.shutdown();
        }
    }
}
