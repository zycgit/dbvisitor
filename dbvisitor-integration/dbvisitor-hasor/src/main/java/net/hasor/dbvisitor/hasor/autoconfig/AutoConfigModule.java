/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
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
