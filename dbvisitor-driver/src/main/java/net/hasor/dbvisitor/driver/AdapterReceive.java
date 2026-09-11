/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.driver;
public interface AdapterReceive {
    boolean responseFailed(AdapterRequest request, Throwable e);

    boolean responseResult(AdapterRequest request, AdapterCursor cursor);

    boolean responseResult(AdapterRequest request, AdapterCursor cursor, AdapterCursor generatedKeys);

    boolean responseUpdateCount(AdapterRequest request, long updateCount);

    boolean responseUpdateCount(AdapterRequest request, long updateCount, AdapterCursor generatedKeys);

    boolean responseParameter(AdapterRequest request, String paramName, String paramType, Object value);

    boolean responseFinish(AdapterRequest request);
}
