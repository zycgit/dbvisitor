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
import net.hasor.core.AppContext;
import static net.hasor.core.AppContext.ContextEvent_Started;
import net.hasor.core.BindInfo;
import net.hasor.core.EventListener;
import net.hasor.dbvisitor.mapper.Mapper;
import net.hasor.dbvisitor.session.Session;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * BeanFactory that enables injection of DalSession.
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-02-09
 * @see Mapper
 */
class MapperSupplier implements Supplier<Object>, EventListener<AppContext> {
    private final Class<?>          mapperInterface;
    private final BindInfo<Session> sessionKey;
    private       Object            mapperObject;

    public MapperSupplier(Class<?> mapperInterface, BindInfo<Session> sessionKey) {
        this.mapperInterface = Objects.requireNonNull(mapperInterface, "mapperInterface is null.");
        this.sessionKey = Objects.requireNonNull(sessionKey, "sessionKey is null.");
    }

    @Override
    public void onEvent(String event, AppContext eventData) throws Exception {
        if (!ContextEvent_Started.equals(event)) {
            return;
        }

        Session session = eventData.getInstance(this.sessionKey);
        this.mapperObject = session.createMapper(this.mapperInterface);
    }

    @Override
    public Object get() {
        return mapperObject;
    }
}