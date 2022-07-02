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
package net.hasor.jdbc;
import java.sql.ResultSet;
import java.sql.SQLException;
/**
 * ����ӿ�����ӳ�� JDBC �������һ�����ݡ�
 * @version : 2013-10-9
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author ������(zyc@hasor.net)
 */
public interface RowMapper<T> {
    /**ʵ���������Ϊ�������һ�м�¼����ת������������ת��������ء�
     * �������Ϊ null ��ͬ�ں��Ը��С���Ҫע�⣬��Ҫ���ý������ next() ������*/
    public T mapRow(ResultSet rs, int rowNum) throws SQLException;
}