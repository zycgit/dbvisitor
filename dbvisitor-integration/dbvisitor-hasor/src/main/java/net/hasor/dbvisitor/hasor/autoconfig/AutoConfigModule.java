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
package net.hasor.dbvisitor.hasor.autoconfig;

import net.hasor.core.ApiBinder;
import net.hasor.dbvisitor.hasor.mapper.MapperScannerConfigurer;
import net.hasor.dbvisitor.hasor.session.SessionConfigurer;

/** Creates and registers data sources declared by dbVisitor settings. */
public class AutoConfigModule implements net.hasor.core.Module {
    @Override
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        AutoConfigSettings settings = new AutoConfigSettings(apiBinder.getSettings(), null);
        for (String sourceName : settings.getDataSourceNames()) {
            apiBinder.installModule(new DataSourceBindingModule(sourceName));

            SessionConfigurer sessionConfigurer = new SessionConfigurer();
            sessionConfigurer.setSourceName(sourceName);
            apiBinder.installModule(sessionConfigurer);

            MapperScannerConfigurer mapperConfigurer = new MapperScannerConfigurer();
            mapperConfigurer.setSourceName(sourceName);
            apiBinder.installModule(mapperConfigurer);
        }
    }
}
