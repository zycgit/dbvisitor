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
package net.hasor.dbvisitor.solon;
import java.util.ArrayList;
import java.util.List;
import net.hasor.dbvisitor.session.Configuration;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-03-20
 */
public class MapperWrap {
    private final Configuration  conf;
    private final List<Class<?>> mapperType = new ArrayList<>();
    private final List<Class<?>> singleton  = new ArrayList<>();

    public MapperWrap(Configuration conf) {
        this.conf = conf;
    }

    public Configuration getConf() {
        return this.conf;
    }

    public void addMapper(Class<?> mapper, boolean singleton) {
        this.mapperType.add(mapper);
        if (singleton) {
            this.singleton.add(mapper);
        }
    }

    public List<Class<?>> getMapperType() {
        return this.mapperType;
    }

    public boolean isSingleton(Class<?> type) {
        return this.singleton.contains(type);
    }
}
