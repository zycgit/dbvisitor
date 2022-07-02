/*
 * Copyright 2002-2008 the original author or authors.
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
package net.hasor.jdbc.parameter;
import java.util.LinkedList;
import java.util.List;
import net.hasor.core.Hasor;
/**
 * ����SQL����������������������������
 * @see java.sql.Types
 * @version : 2013-10-14
 * @author ������(zyc@hasor.net)
 */
public class SqlParameter {
    /*������*/
    private String    name;
    /*��������,�����java.sql.Types*/
    private final int sqlType;
    /*Ӧ�õ��������͵� scale ����*/
    private Integer   scale;
    //
    /**����һ�������� SQL ����.*/
    public SqlParameter(int sqlType) {
        this.sqlType = sqlType;
    }
    /**����һ�������� SQL ����.*/
    public SqlParameter(int sqlType, Integer scale) {
        this.sqlType = sqlType;
        this.scale = scale;
    }
    /**���ݲ����� �Ͳ������ʹ���һ�� SqlParameter.*/
    public SqlParameter(String name, int sqlType) {
        this.name = name;
        this.sqlType = sqlType;
    }
    /**���ݲ����� �Ͳ������ʹ���һ�� SqlParameter.*/
    public SqlParameter(String name, int sqlType, Integer scale) {
        this.name = name;
        this.sqlType = sqlType;
        this.scale = scale;
    }
    /**����һ�� SqlParameter ��������һ���µ� SqlParameter.*/
    public SqlParameter(SqlParameter otherParam) {
        Hasor.assertIsNotNull(otherParam, "SqlParameter object must not be null");
        this.name = otherParam.name;
        this.sqlType = otherParam.sqlType;
        this.scale = otherParam.scale;
    }
    //
    /**��������*/
    public String getName() {
        return this.name;
    }
    /**���� SQL���� ��<code>java.sql.Types</code>.*/
    public int getSqlType() {
        return this.sqlType;
    }
    /**Ӧ�õ��������͵� scale ����*/
    public Integer getScale() {
        return this.scale;
    }
    /**�� <code>java.sql.Types</code> ���Ͷ���ת����Ϊ SqlParameter �б�*/
    public static List<SqlParameter> sqlTypesToAnonymousParameterList(int[] types) {
        List<SqlParameter> result = new LinkedList<SqlParameter>();
        if (types != null)
            for (int type : types)
                result.add(new SqlParameter(type));
        return result;
    }
}