/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus;
import java.util.Map;
import io.milvus.v2.client.MilvusClientV2;
import org.powermock.api.mockito.PowerMockito;

public class MilvusCustomClient implements CustomMilvus {

    @Override
    public MilvusClientV2 createMilvusClient(String jdbcUrl, Map<String, String> props) {
        return PowerMockito.mock(MilvusClientV2.class);
    }
}
