/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.jdbc.mapper;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.types.TypeHandler;

/**
 * 用于 POJO 的 RowMapper，带有 ORM 能力
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class MapMappingRowMapper extends AbstractMapping<Map<String, Object>> implements RowMapper<Map<String, Object>> {
    private final boolean caseInsensitive;

    /**
     * 创建 {@link RowMapper} 对象
     * @param entityType 类型
     */
    public MapMappingRowMapper(final Class<?> entityType, MappingRegistry registry) {
        super(entityType, registry);
        this.caseInsensitive = this.tableMapping.isCaseInsensitive();
    }

    /**
     * 创建 {@link RowMapper} 对象
     * @param tableMapping 类型
     */
    public MapMappingRowMapper(TableMapping<?> tableMapping) {
        super(tableMapping);
        this.caseInsensitive = tableMapping.isCaseInsensitive();
    }

    /** 创建一个 Map 用于存放数据 */
    protected Map<String, Object> createColumnMap(final int columnCount) {
        if (this.caseInsensitive) {
            return new LinkedCaseInsensitiveMap<>(columnCount);
        } else {
            return new LinkedHashMap<>();
        }
    }

    @Override
    public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int nrOfColumns = rsmd.getColumnCount();
        List<String> columnList = new ArrayList<>();
        for (int i = 1; i <= nrOfColumns; i++) {
            columnList.add(lookupColumnName(rsmd, i));
        }

        return this.extractRow(columnList, rs, rowNum);
    }

    private static String lookupColumnName(final ResultSetMetaData resultSetMetaData, final int columnIndex) throws SQLException {
        String name = resultSetMetaData.getColumnLabel(columnIndex);
        if (name == null || name.length() < 1) {
            name = resultSetMetaData.getColumnName(columnIndex);
        }
        return name;
    }

    protected Map<String, Object> extractRow(List<String> columns, ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> target = this.createColumnMap(columns.size());

        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);

            List<ColumnMapping> list = this.tableMapping.getPropertyByColumn(column);
            if (list == null) {
                continue;
            }

            for (ColumnMapping mapping : list) {
                if (mapping == null || mapping.getHandler().isReadOnly()) {
                    continue;
                }

                TypeHandler<?> realHandler = mapping.getTypeHandler();
                Object result = realHandler.getResult(rs, i + 1);
                target.put(mapping.getProperty(), result);
            }
        }
        return target;
    }
}
