/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.driver;
import java.util.HashMap;
import java.util.Map;

public class AdapterFeatures {

    private Map<AdapterFeatureKey, Boolean> boolFeatures = new HashMap<>();

    AdapterFeatures() {
    }

    public void addFeature(AdapterFeatureKey key, boolean value) {
        this.boolFeatures.put(key, value);
    }

    public boolean hasFeature(AdapterFeatureKey key) {
        return this.boolFeatures.containsKey(key);
    }

    public boolean boolFeatureVal(AdapterFeatureKey adapterFeatureKey) {
        return this.boolFeatures.getOrDefault(adapterFeatureKey, false);
    }
}
