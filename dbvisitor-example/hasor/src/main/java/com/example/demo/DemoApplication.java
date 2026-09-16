/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package com.example.demo;

import net.hasor.boot.Boot;
import net.hasor.core.ApiBinder;
import net.hasor.core.Module;
import net.hasor.dbvisitor.hasor.autoconfig.AutoConfigModule;

public class DemoApplication implements Module {
    public static void main(String[] args) throws Exception {
        String configFile = args.length == 0 ? "single-ds.properties" : args[0];
        new Boot().sources(DemoApplication.class).hconfigFile(configFile).arguments(args).start().join();
    }

    @Override
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        apiBinder.installModule(new AutoConfigModule());
    }
}
