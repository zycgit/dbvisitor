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
package net.hasor.dbvisitor.dynamic;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

/**
 * Interface that defines common functionality for objects that can
 * offer parameter values for named SQL parameters, serving as argument.
 *
 * <p>This interface allows for the specification of SQL type in addition
 * to parameter values. All parameter values and types are identified by
 * specifying the name of the parameter.
 *
 * <p>Intended to wrap various implementations like a Map or a JavaBean
 * with a consistent interface.
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.0
 * @see JdbcTemplate
 */
public interface SqlArgSource {
    /**
     * Determine whether there is a value for the specified named parameter.
     * @param paramName the name of the parameter
     * @return whether there is a value defined
     */
    boolean hasValue(String paramName);

    /**
     * Return the parameter value for the requested named parameter.
     * @param paramName the name of the parameter
     * @return the value of the specified parameter
     * @throws IllegalArgumentException if there is no value for the requested parameter
     */
    Object getValue(String paramName) throws IllegalArgumentException;

    void putValue(String paramName, Object value);

    /**
     * Enumerate all available parameter names if possible.
     * @return the array of parameter names, or {@code null} if not determinable
     */
    String[] getParameterNames();

    default Map<String, Object> toMap() {
        return new AbstractMap<String, Object>() {
            @Override
            public Object get(Object key) {
                return getValue(key.toString());
            }

            @Override
            public boolean containsKey(Object key) {
                return hasValue(key.toString());
            }

            @Override
            public Set<Entry<String, Object>> entrySet() {
                throw new UnsupportedOperationException();
            }

            @Override
            public String toString() {
                return SqlArgSource.this.toString();
            }
        };
    }
}