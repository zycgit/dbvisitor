/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.elastic;

import java.lang.reflect.InvocationHandler;
import java.util.Map;
import org.elasticsearch.client.RestClient;
import org.mockito.Answers;
import org.powermock.api.mockito.PowerMockito;

public class ElasticCustomClient implements CustomElastic {

    @Override
    public RestClient createElasticClient(String jdbcUrl, Map<String, String> props) {
        return PowerMockito.mock(RestClient.class, invocation -> {
            if (!"performRequest".equals(invocation.getMethod().getName())) {
                return Answers.RETURNS_DEFAULTS.answer(invocation);
            }
            InvocationHandler interceptor = ElasticCommandInterceptor.getInterceptor(RestClient.class);
            if (interceptor == null) {
                throw new AssertionError("No interceptor installed for RestClient.performRequest");
            }
            return interceptor.invoke(invocation.getMock(), invocation.getMethod(), invocation.getArguments());
        });
    }
}
