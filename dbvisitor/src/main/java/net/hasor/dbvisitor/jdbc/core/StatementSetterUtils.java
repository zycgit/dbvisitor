/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.jdbc.core;
import java.util.Arrays;
import java.util.Collection;
import net.hasor.dbvisitor.dynamic.args.SqlArgDisposer;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2014-3-29
 */
public class StatementSetterUtils {
    /**
     * Clean up all resources held by parameter values which were passed to an execute method. This is for example important for closing LOB values.
     * @param paramValues parameter values supplied. May be <code>null</code>.
     * @see SqlArgDisposer#cleanupParameters()
     */
    public static void cleanupParameters(final Object[] paramValues) {
        if (paramValues != null) {
            cleanupParameters(Arrays.asList(paramValues));
        }
    }

    /**
     * Clean up all resources held by parameter values which were passed to an execute method. This is for example important for closing LOB values.
     * @param paramValues parameter values supplied. May be <code>null</code>.
     * @see SqlArgDisposer#cleanupParameters()
     */
    public static void cleanupParameters(final Collection<Object> paramValues) {
        if (paramValues == null) {
            return;
        }
        for (Object inValue : paramValues) {
            cleanupParameter(inValue);
        }
    }

    public static void cleanupParameter(final Object paramValue) {
        if (paramValue == null) {
            return;
        }
        if (paramValue instanceof SqlArgDisposer) {
            ((SqlArgDisposer) paramValue).cleanupParameters();
        }
    }
}
