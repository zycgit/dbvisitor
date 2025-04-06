package net.hasor.dbvisitor.lambda.support.freedom;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.function.Property;
import net.hasor.dbvisitor.mapping.def.ColumnDef;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.util.Map;
import java.util.WeakHashMap;

class FreedomUtils {
    private static final TypeHandler<?>                     DEFAULT_TYPE_HANDLER = TypeHandlerRegistry.DEFAULT.getDefaultTypeHandler();
    private static final Class<?>                           DEFAULT_JAVA_TYPE    = Object.class;
    private static final int                                DEFAULT_JDBC_TYPE    = TypeHandlerRegistry.toSqlType(DEFAULT_JAVA_TYPE);
    private static final WeakHashMap<String, ColumnMapping> forMapKey            = new WeakHashMap<>();

    public static ColumnMapping initOrGetMapMapping(String mapKey, boolean toCamelCase) {
        return forMapKey.computeIfAbsent(mapKey + "$$$" + toCamelCase, s -> {
            String colName = mapKey;
            if (toCamelCase) {
                colName = StringUtils.humpToLine(mapKey);
            }
            return new ColumnDef(colName, s, DEFAULT_JDBC_TYPE, DEFAULT_JAVA_TYPE, DEFAULT_TYPE_HANDLER, new FreedomProperty(s));
        });
    }

    private static class FreedomProperty implements Property {
        private final String name;

        FreedomProperty(String name) {
            this.name = name;
        }

        @Override
        public boolean isReadOnly() {
            return false;
        }

        @Override
        public Object get(Object instance) {
            return ((Map) instance).get(this.name);
        }

        @Override
        public void set(Object instance, Object value) {
            ((Map) instance).put(this.name, value);
        }
    }
}
