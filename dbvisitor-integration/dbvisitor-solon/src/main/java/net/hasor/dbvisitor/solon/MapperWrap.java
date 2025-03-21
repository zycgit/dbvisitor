package net.hasor.dbvisitor.solon;
import net.hasor.dbvisitor.session.Configuration;

import java.util.ArrayList;
import java.util.List;

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
