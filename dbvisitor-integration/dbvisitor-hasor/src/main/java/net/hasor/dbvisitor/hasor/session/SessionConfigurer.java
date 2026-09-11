/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.hasor.session;
import net.hasor.core.ApiBinder;

/** Configures dbVisitor services for data sources already registered in Hasor. */
public class SessionConfigurer implements net.hasor.core.Module {
    private String sourceName;

    @Override
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        apiBinder.installModule(new SessionBindingModule(this.sourceName));
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
}
