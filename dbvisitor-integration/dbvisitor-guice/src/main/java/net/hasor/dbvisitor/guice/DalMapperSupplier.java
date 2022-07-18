/*
 * Copyright 2010-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
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
import net.hasor.cobble.logging.Logger;
import net.hasor.dbvisitor.dal.mapper.Mapper;
import net.hasor.dbvisitor.dal.repository.RefMapper;
import net.hasor.dbvisitor.dal.session.DalSession;

import java.io.IOException;
import java.sql.SQLException;

/**
 * BeanFactory that enables injection of DalSession.
 * @version : 2022-04-29
 * @author 赵永春 (zyc@hasor.net)
 * @see Mapper
 */
class DalMapperSupplier implements Provider<Object> {
    private final static Logger          logger = Logger.getLogger(DalMapperSupplier.class);
    private final        Class<?>        mapperInterface;
    private final        Key<DalSession> dalSessionInfo;
    private              Object          mapperObject;

    public DalMapperSupplier(Class<?> mapperInterface, Key<DalSession> dalSessionInfo) {
        this.mapperInterface = mapperInterface;
        this.dalSessionInfo = dalSessionInfo;
    }

    @Inject
    public void initDalSession(Injector injector) throws SQLException, IOException {
        if (this.mapperInterface == null) {
            throw new NullPointerException("mapperInterface is null.");
        }
        if (this.dalSessionInfo == null) {
            throw new IllegalStateException("dalSession is null.");
        }

        DalSession dalSession = injector.getInstance(this.dalSessionInfo);

        RefMapper refMapper = this.mapperInterface.getAnnotation(RefMapper.class);
        if (refMapper != null) {
            logger.info("mapper '" + this.mapperInterface + "' using '" + refMapper.value() + "'");
            dalSession.getDalRegistry().loadMapper(this.mapperInterface);
        } else {
            logger.info("mapper '" + this.mapperInterface + "' using default.");
        }

        this.mapperObject = dalSession.createMapper(this.mapperInterface);
    }

    @Override
    public Object get() {
        return mapperObject;
    }
}