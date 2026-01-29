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
package net.hasor.dbvisitor.adapter.milvus;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import io.milvus.client.MilvusClient;
import net.hasor.cobble.StringUtils;

public class MilvusCmd implements AutoCloseable {
    private final MilvusClient target;
    private final MilvusClient client;
    private final String       catalog;

    MilvusCmd(MilvusClient target, String catalog, InvocationHandler invocation) {
        this.client = this.tryProxy(target, MilvusClient.class, invocation);
        this.target = target;
        this.catalog = catalog;
    }

    public String getCatalog() {
        return this.catalog;
    }

    public void setCatalog(String catalog) {
        if (!StringUtils.equals(this.catalog, catalog)) {
            throw new UnsupportedOperationException("Milvus does not support changing catalogs.");
        }
    }

    private MilvusClient tryProxy(MilvusClient object, Class<MilvusClient> iface, InvocationHandler invocation) {
        if (object == null || invocation == null) {
            return object;
        } else {
            return (MilvusClient) Proxy.newProxyInstance(MilvusCmd.class.getClassLoader(), new Class<?>[] { iface }, (proxy, method, args) -> {
                return invocation.invoke(object, method, args);
            });
        }
    }

    @Override
    public void close() {
        if (this.target != null) {
            try {
                this.target.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    private <T> T verifyCommand(T command, Class<T> commandClass) {
        if (command == null) {
            throw new UnsupportedOperationException("The client " + commandClass.getName() + " is not supported.");
        } else {
            return command;
        }
    }

    public MilvusClient getClient() {
        return this.verifyCommand(this.client, MilvusClient.class);
    }
}