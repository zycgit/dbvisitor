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
package net.hasor.dbvisitor.dynamic.args;
import net.hasor.cobble.ArrayUtils;

/**
 *
 * @version : 2024-09-27
 * @author 赵永春 (zyc@hasor.net)
 */
public class ArraySqlArgSource extends BasicSqlArgSource {
    public ArraySqlArgSource() {
    }

    public ArraySqlArgSource(Object[] args) {
        if (ArrayUtils.isNotEmpty(args)) {
            for (int i = 0; i < args.length; i++) {
                this.putValue("arg" + i, args[i]);
            }
        }
    }

    public static Object[] toArgs(Object value) {
        if (value == null) {
            return ArrayUtils.EMPTY_OBJECT_ARRAY;
        }
        if (value.getClass().isArray()) {
            Class<?> componentType = value.getClass().getComponentType();
            if (componentType == char.class) {
                return ArrayUtils.toObject((char[]) value);
            } else if (componentType == Character.class) {
                return (Character[]) value;
            } else if (componentType == short.class) {
                return ArrayUtils.toObject((short[]) value);
            } else if (componentType == Short.class) {
                return (Short[]) value;
            } else if (componentType == int.class) {
                return ArrayUtils.toObject((int[]) value);
            } else if (componentType == Integer.class) {
                return (Integer[]) value;
            } else if (componentType == long.class) {
                return ArrayUtils.toObject((long[]) value);
            } else if (componentType == Long.class) {
                return (Long[]) value;
            } else if (componentType == float.class) {
                return ArrayUtils.toObject((float[]) value);
            } else if (componentType == Float.class) {
                return (Float[]) value;
            } else if (componentType == double.class) {
                return ArrayUtils.toObject((double[]) value);
            } else if (componentType == Double.class) {
                return (Double[]) value;
            } else if (componentType == boolean.class) {
                return ArrayUtils.toObject((boolean[]) value);
            } else if (componentType == Boolean.class) {
                return (Boolean[]) value;
            } else {
                return (Object[]) value;
            }
        } else {
            return new Object[] { value };
        }
    }
}