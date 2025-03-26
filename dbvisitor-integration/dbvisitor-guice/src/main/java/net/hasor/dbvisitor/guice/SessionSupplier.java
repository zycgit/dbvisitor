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
package net.hasor.dbvisitor.guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.dbvisitor.mapper.Mapper;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URI;
import java.util.Set;

/**
 * BeanFactory that enables injection of DalSession.
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-02-09
 * @see Mapper
 */
class SessionSupplier implements Provider<Session> {
    private final Key<Configuration> configInfo;
    private final Key<DataSource>    dsInfo;
    private final Set<URI>           mappers;
    private       Session            session = null;

    public SessionSupplier(                 //
            Key<Configuration> configInfo,  //
            Key<DataSource> dsInfo,         //
            Set<URI> mappers) {
        this.configInfo = configInfo;
        this.dsInfo = dsInfo;
        this.mappers = mappers;
    }

    @Inject
    public void initSession(Injector injector) throws Exception {
        Configuration config = injector.getInstance(this.configInfo);
        DataSource dataSource = injector.getInstance(this.dsInfo);

        if (!(this.mappers == null || this.mappers.isEmpty())) {
            try {
                for (URI mapper : this.mappers) {
                    config.loadMapper(mapper.toString());
                }
            } catch (IOException e) {
                throw ExceptionUtils.toRuntime(e);
            }
        }

        this.session = config.newSession(dataSource);
    }

    @Override
    public Session get() {
        return this.session;
    }
}
