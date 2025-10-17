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
package net.hasor.dbvisitor;
import java.net.URI;
import java.util.Set;
import java.util.function.Supplier;
import javax.sql.DataSource;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.core.AppContext;
import net.hasor.core.BindInfo;
import net.hasor.core.spi.AppContextAware;
import net.hasor.dbvisitor.mapper.Mapper;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;

/**
 * BeanFactory that enables injection of DalSession.
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-02-09
 * @see Mapper
 */
class SessionSupplier implements Supplier<Session>, AppContextAware {
    private final BindInfo<Configuration> configInfo;
    private final BindInfo<DataSource>    dsInfo;
    private final Set<URI>                mappers;
    private       Session                 session = null;

    public SessionSupplier(                 //
            BindInfo<Configuration> configInfo,  //
            BindInfo<DataSource> dsInfo,         //
            Set<URI> mappers) {
        this.configInfo = configInfo;
        this.dsInfo = dsInfo;
        this.mappers = mappers;
    }

    @Override
    public void setAppContext(AppContext appContext) {
        Configuration config = appContext.getInstance(this.configInfo);
        DataSource dataSource = appContext.getInstance(this.dsInfo);

        if (!(this.mappers == null || this.mappers.isEmpty())) {
            try {
                for (URI mapper : this.mappers) {
                    config.loadMapper(mapper.toString());
                }
            } catch (Exception e) {
                throw ExceptionUtils.toRuntime(e);
            }
        }

        try {
            this.session = config.newSession(dataSource);
        } catch (Exception e) {
            throw ExceptionUtils.toRuntime(e);
        }
    }

    @Override
    public Session get() {
        return this.session;
    }
}
