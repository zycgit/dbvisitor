/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.driver;
import java.util.*;

public class AdapterManager {
    private static final Map<String, String[]>       propertyGroupBy = new HashMap<>();
    private static final Map<String, AdapterFactory> factoryMap      = new HashMap<>();

    public static void register(ClassLoader cl) {
        if (cl == null) {
            return;
        }
        ServiceLoader<AdapterFactory> loader = ServiceLoader.load(AdapterFactory.class, cl);
        for (AdapterFactory factory : loader) {
            register(factory.getAdapterName(), factory);
        }
    }

    public static void register(String adapter, AdapterFactory factory) {
        if (adapter == null || adapter.trim().isEmpty() || factory == null) {
            return;
        }

        synchronized (factoryMap) {
            factoryMap.put(adapter, factory);
            Set<String> propertyNameSet = new HashSet<>(Arrays.asList(factory.getPropertyNames()));
            propertyNameSet.add(JdbcDriver.P_SERVER);
            propertyGroupBy.put(adapter, propertyNameSet.toArray(new String[0]));
        }
    }

    public static AdapterFactory lookup(String adapter) {
        AdapterFactory factory = registeredFactory(adapter);
        if (factory != null) {
            return factory;
        }

        throw new UnsupportedOperationException("not found " + adapter + " driver adapter.");
    }

    private static AdapterFactory registeredFactory(String adapter) {
        if (factoryMap.containsKey(adapter)) {
            return factoryMap.get(adapter);
        }
        return null;
    }

    public static String[] propertyNames(String adapter, Properties parse) {
        String[] names;
        if (!propertyGroupBy.containsKey(adapter)) {
            synchronized (propertyGroupBy) {
                if (!propertyGroupBy.containsKey(adapter)) {
                    AdapterFactory factory = lookup(adapter);
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
