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
package net.hasor.dbvisitor.dal.session;
import net.hasor.cobble.CollectionUtils;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * 可以将多个Map合并成一个Map对象进行操作。
 * @version : 2016-07-17
 * @author 赵永春 (zyc@hasor.net)
 */
class MergedMap<K, T> extends AbstractMap<K, T> {
    private final Map<K, T>       unmerged  = new HashMap<>();
    private final List<Map<K, T>> merged    = new ArrayList<>();
    private final List<Boolean>   keyLocked = new ArrayList<>();

    @Override
    public int size() {
        int size = this.unmerged.size();
        for (Map<K, T> item : this.merged) {
            size += item.size();
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
        if (!this.unmerged.isEmpty()) {
            return false;
        }
        for (Map<K, T> item : this.merged) {
            if (!item.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean containsValue(Object value) {
        if (this.unmerged.containsValue(value)) {
            return true;
        }
        for (Map<K, T> item : this.merged) {
            if (item.containsValue(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        if (this.unmerged.containsKey(key)) {
            return true;
        }
        for (Map<K, T> item : this.merged) {
            if (item.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public T get(Object key) {
        if (this.unmerged.containsKey(key)) {
            return this.unmerged.get(key);
        }
        for (Map<K, T> item : this.merged) {
            if (item.containsKey(key)) {
                return item.get(key);
            }
        }
        return null;
    }

    @Override
    public T put(K key, T value) {
        if (this.unmerged.containsKey(key)) {
            return this.unmerged.put(key, value);
        }
        for (Map<K, T> item : this.merged) {
            if (item.containsKey(key)) {
                return item.put(key, value);
            }
        }
        return this.unmerged.put(key, value);
    }

    @Override
    public T remove(Object key) {
        if (this.unmerged.containsKey(key)) {
            return this.unmerged.remove(key);
        }
        for (int i = 0; i < this.merged.size(); i++) {
            Map<K, T> item = this.merged.get(i);
            Boolean keyLocked = this.keyLocked.get(i);
            if (item.containsKey(key) && !keyLocked) {
                if (keyLocked) {
                    return item.put((K) key, null);
                } else {
                    return item.remove(key);
                }
            }
        }
        return this.unmerged.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends T> m) {
        m.forEach((BiConsumer<K, T>) this::put);
    }

    @Override
    public void clear() {
        this.unmerged.clear();
        for (int i = 0; i < this.merged.size(); i++) {
            Map<K, T> item = this.merged.get(i);
            Boolean keyLocked = this.keyLocked.get(i);
            if (keyLocked) {
                Set<K> keys = item.keySet();
                for (K key : keys) {
                    item.put(key, null);
                }
            } else {
                item.clear();
            }
        }
    }

    @Override
    public Set<K> keySet() {
        Set<K> keySet = new HashSet<>(this.unmerged.keySet());
        for (Map<K, T> item : this.merged) {
            keySet.addAll(item.keySet());
        }
        return keySet;
    }

    @Override
    public Collection<T> values() {
        ArrayList<T> keySet = new ArrayList<>(this.unmerged.values());
        for (Map<K, T> item : this.merged) {
            keySet.addAll(item.values());
        }
        return keySet;
    }

    @Override
    public Set<Entry<K, T>> entrySet() {
        Iterator<Entry<K, T>> basic = this.unmerged.entrySet().iterator();
        for (Map<K, T> item : this.merged) {
            basic = CollectionUtils.mergeIterator(basic, item.entrySet().iterator());
        }

        final int size = size();
        Iterator<Entry<K, T>> finalBasic = basic;
        return new AbstractSet<Entry<K, T>>() {
            @Override
            public Iterator<Entry<K, T>> iterator() {
                return finalBasic;
            }

            @Override
            public int size() {
                return size;
            }
        };
    }

    public void appendMap(Map<? extends K, ? extends T> object, boolean keyLocked) {
        if (object != null) {
            this.merged.add((Map<K, T>) object);
            this.keyLocked.add(keyLocked);
        }
    }
}