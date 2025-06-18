package net.hasor.dbvisitor.driver;

import java.util.*;

public class AdapterManager {
    private static final Map<String, String[]>       propertyGroupBy = new HashMap<>();
    private static final Map<String, AdapterFactory> factoryMap      = new HashMap<>();

    public static AdapterFactory lookup(String adapter, ClassLoader cl) {
        if (factoryMap.containsKey(adapter)) {
            return factoryMap.get(adapter);
        } else {
            synchronized (factoryMap) {
                if (factoryMap.containsKey(adapter)) {
                    return factoryMap.get(adapter);
                }
                ServiceLoader<AdapterFactory> loader = ServiceLoader.load(AdapterFactory.class, cl);
                for (AdapterFactory factory : loader) {
                    String adapterName = factory.getAdapterName();
                    if (!factoryMap.containsKey(adapterName)) {
                        factoryMap.put(adapterName, factory);
                    }
                }
            }
        }

        if (factoryMap.containsKey(adapter)) {
            return factoryMap.get(adapter);
        } else {
            throw new UnsupportedOperationException("not found " + adapter + " driver adapter.");
        }
    }

    static String[] propertyNames(String adapter, Properties parse, ClassLoader cl) {
        String[] names;
        if (!propertyGroupBy.containsKey(adapter)) {
            synchronized (propertyGroupBy) {
                if (!propertyGroupBy.containsKey(adapter)) {
                    AdapterFactory factory = lookup(adapter, cl);
                    Set<String> propertyNameSet = new HashSet<>(Arrays.asList(factory.getPropertyNames()));
                    propertyNameSet.add(JdbcDriver.P_SERVER);
                    names = propertyNameSet.toArray(new String[0]);
                    propertyGroupBy.put(adapter, names);
                } else {
                    names = propertyGroupBy.get(adapter);
                }
            }
        } else {
            names = propertyGroupBy.get(adapter);
        }

        return Arrays.stream(names).filter(parse::containsKey).toArray(String[]::new);
    }
}
