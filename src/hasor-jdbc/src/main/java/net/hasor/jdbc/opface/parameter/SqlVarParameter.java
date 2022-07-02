/*
 * Copyright 2002-2007 the original author or authors.
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
package net.hasor.jdbc.opface.parameter;
/**
 * SqlParameter �����࣬���ڱ�ʾ����ֵ�����������
 * @see java.sql.Types
 * @version : 2013-10-14
 * @author ������(zyc@hasor.net)
 */
public class SqlVarParameter extends SqlParameter {
    private final Object value;
    /**����һ�������� SQL ����.*/
    public SqlVarParameter(int sqlType, Object value) {
        super(sqlType);
        this.value = value;
    }
    /**���ݲ����� �Ͳ������ʹ���һ�� SqlParameter.*/
    public SqlVarParameter(String name, int sqlType, Object value) {
        super(name, sqlType);
        this.value = value;
    }
    /**
     * Create a new SqlParameterValue based on the given SqlParameter declaration.
     * @param declaredParam the declared SqlParameter to define a value for
     * @param value the value object
     */
    public SqlVarParameter(SqlParameter declaredParam, Object value) {
        super(declaredParam);
        this.value = value;
    }
    /**����ֵ.*/
    public Object getValue() {
        return this.value;
    }
}