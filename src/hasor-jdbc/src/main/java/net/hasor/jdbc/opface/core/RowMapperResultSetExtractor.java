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
package net.hasor.jdbc.opface.core;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import net.hasor.Hasor;
import net.hasor.jdbc.opface.ResultSetExtractor;
import net.hasor.jdbc.opface.RowMapper;
/**
 * {@link ResultSetExtractor} �ӿ�ʵ���࣬����Ὣ������е�ÿһ�н��д���������һ�� List ���Է�װ����������
 *
 * <p>ע�⣺{@link RowMapper} Ӧ������״̬�ģ�����ýӿ��ڴ���ÿһ������ʱ�ſ��������д�������
 * <p>����
 *
 * <pre class="code">JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);  // reusable object
 * RowMapper rowMapper = new UserRowMapper();  // reusable object
 *
 * List allUsers = (List) jdbcTemplate.query(
 *     "select * from user",
 *     new RowMapperResultSetExtractor(rowMapper, 10));
 *
 * User user = (User) jdbcTemplate.queryForObject(
 *     "select * from user where id=?", new Object[] {id},
 *     new RowMapperResultSetExtractor(rowMapper, 1));</pre>
 * 
 * @author Juergen Hoeller
 * @author ������ (zyc@byshell.org)
 * @see RowMapper
 */
public class RowMapperResultSetExtractor<T> implements ResultSetExtractor<List<T>> {
    private final RowMapper<T> rowMapper;
    private final int          rowsExpected;
    /**
     * ���� {@link RowMapperResultSetExtractor} ����
     * @param rowMapper ��ӳ������
     */
    public RowMapperResultSetExtractor(RowMapper<T> rowMapper) {
        this(rowMapper, 0);
    }
    /**
     * ���� {@link RowMapperResultSetExtractor} ����
     * @param rowMapper ��ӳ������
     * @param rowsExpected Ԥ�ڽ������С��ʵ�ʵõ��Ľ������Ŀ���ܴ˲������ƣ���
     */
    public RowMapperResultSetExtractor(RowMapper<T> rowMapper, int rowsExpected) {
        Hasor.assertIsNotNull(rowMapper, "RowMapper is required");
        this.rowMapper = rowMapper;
        this.rowsExpected = rowsExpected;
    }
    public List<T> extractData(ResultSet rs) throws SQLException {
        List<T> results = (this.rowsExpected > 0 ? new ArrayList<T>(this.rowsExpected) : new ArrayList<T>());
        int rowNum = 0;
        while (rs.next()) {
            results.add(this.rowMapper.mapRow(rs, rowNum++));
        }
        return results;
    }
}