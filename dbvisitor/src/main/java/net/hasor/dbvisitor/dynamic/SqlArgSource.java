/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dynamic;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;

/**
 * Interface that defines common functionality for objects that can
 * offer parameter values for named SQL parameters, serving as argument.
 * <p>This interface allows for the specification of SQL type in addition
 * to parameter values. All parameter values and types are identified by
 * specifying the name of the parameter.
 * <p>Intended to wrap various implementations like a Map or a JavaBean
 * with a consistent interface.
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @see JdbcTemplate
 * @since 2.0
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
