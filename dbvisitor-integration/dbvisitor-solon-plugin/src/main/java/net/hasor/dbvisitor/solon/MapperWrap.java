/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
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
