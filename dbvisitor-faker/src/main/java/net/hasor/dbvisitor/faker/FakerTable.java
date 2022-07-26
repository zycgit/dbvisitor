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
package net.hasor.dbvisitor.faker;
import java.util.List;

/**
 * Table
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class FakerTable {
    private String            catalog;
    private String            schema;
    private String            table;
    private List<FakerColumn> columns;
    private List<String>      peggingCol;

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public List<FakerColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<FakerColumn> columns) {
        this.columns = columns;
    }

    public List<String> getPeggingCol() {
        return peggingCol;
    }

    public void setPeggingCol(List<String> peggingCol) {
        this.peggingCol = peggingCol;
    }
}