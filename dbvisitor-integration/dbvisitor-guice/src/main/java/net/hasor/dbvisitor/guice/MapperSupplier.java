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
import net.hasor.dbvisitor.mapper.Mapper;
import net.hasor.dbvisitor.session.Session;

import java.util.Objects;

/**
 * BeanFactory that enables injection of DalSession.
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-02-09
 * @see Mapper
 */
class MapperSupplier implements Provider<Object> {
    private final Class<?>     mapperInterface;
    private final Key<Session> sessionKey;
    private       Object       mapperObject;

    public MapperSupplier(Class<?> mapperInterface, Key<Session> sessionKey) {
        this.mapperInterface = Objects.requireNonNull(mapperInterface, "mapperInterface is null.");
        this.sessionKey = Objects.requireNonNull(sessionKey, "sessionKey is null.");
    }

    @Inject
    public void initSession(Injector injector) throws Exception {
        Session session = injector.getInstance(this.sessionKey);
        this.mapperObject = session.createMapper(this.mapperInterface);
    }

    @Override
    public Object get() {
        return mapperObject;
    }
}