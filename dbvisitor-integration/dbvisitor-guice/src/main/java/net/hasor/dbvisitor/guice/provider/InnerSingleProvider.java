/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.guice.provider;
import com.google.inject.Provider;

/**
 * 单例对象的{@link Provider}封装形式。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2024-02-06
 */
class InnerSingleProvider<T> implements Provider<T> {
    private          Provider<T> provider = null;
    private volatile T           instance = null;
    private final    Object      lock     = new Object();

    public InnerSingleProvider(Provider<T> provider) {
        this.provider = provider;
    }

    @Override
    public T get() {
        if (this.instance == null) {
            synchronized (this.lock) {
                if (this.instance == null) {
                    this.instance = this.provider.get();
                }
            }
        }
        return this.instance;
    }

    public String toString() {
        return "SingleProvider->" + provider.toString();
    }
}
