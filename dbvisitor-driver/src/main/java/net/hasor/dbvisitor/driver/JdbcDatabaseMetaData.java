/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.driver;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;
import net.hasor.cobble.StringUtils;

class JdbcDatabaseMetaData implements DatabaseMetaData {
    private final JdbcConnection    jdbcConnection;
    private final AdapterConnection adapterConnection;
    private final MetadataSupport   metadataSupport;

    JdbcDatabaseMetaData(JdbcConnection jdbcConnection, AdapterConnection adapterConnection) {
        this.jdbcConnection = jdbcConnection;
        this.adapterConnection = adapterConnection;
        this.metadataSupport = jdbcConnection.metadataSupport();
    }

    @Override
    public Connection getConnection() {
        return this.jdbcConnection;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        this.jdbcConnection.checkOpen();
        return this.adapterConnection.unwrap(iface, this);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        this.jdbcConnection.checkOpen();
        return this.adapterConnection.isWrapperFor(iface, this);
    }

    @Override
    public boolean supportsMixedCaseIdentifiers() {
        return false;
    }

    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() {
        return false;
    }

    @Override
    public boolean storesUpperCaseIdentifiers() {
        return false;
    }

    @Override
    public boolean storesUpperCaseQuotedIdentifiers() {
        return false;
    }

    @Override
    public boolean storesLowerCaseIdentifiers() {
        return false;
    }

    @Override
    public boolean storesLowerCaseQuotedIdentifiers() {
        return false;
    }

    @Override
    public boolean storesMixedCaseIdentifiers() {
        return false;
    }

    @Override
    public boolean storesMixedCaseQuotedIdentifiers() {
        return false;
    }

    @Override
    public String getURL() {
        return this.adapterConnection.getInfo().getUrl();
    }

    @Override
    public String getUserName() {
        return this.adapterConnection.getInfo().getUserName();
    }

    @Override
    public String getDatabaseProductName() {
        return this.adapterConnection.getInfo().getDbVersion().getName();
    }

    @Override
    public String getDatabaseProductVersion() {
        return this.adapterConnection.getInfo().getDbVersion().getVersion();
    }

    @Override
    public int getDatabaseMajorVersion() {
        return this.adapterConnection.getInfo().getDbVersion().getMajorVersion();
    }

    @Override
    public int getDatabaseMinorVersion() {
        return this.adapterConnection.getInfo().getDbVersion().getMinorVersion();
    }

    @Override
    public String getDriverName() {
        return this.adapterConnection.getInfo().getDriverVersion().getName();
    }

    @Override
    public String getDriverVersion() {
        return this.adapterConnection.getInfo().getDriverVersion().getVersion();
    }

    @Override
    public int getDriverMajorVersion() {
        return this.adapterConnection.getInfo().getDriverVersion().getMajorVersion();
    }

    @Override
    public int getDriverMinorVersion() {
        return this.adapterConnection.getInfo().getDriverVersion().getMinorVersion();
    }

    @Override
    public int getJDBCMajorVersion() {
        return JdbcDriver.JDBC_MAJOR;
    }

    @Override
    public int getJDBCMinorVersion() {
        return JdbcDriver.JDBC_MINOR;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public String getSQLKeywords() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getNumericFunctions() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getStringFunctions() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getSystemFunctions() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getTimeDateFunctions() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getIdentifierQuoteString() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getCatalogSeparator() {
        return ".";
    }

    @Override
    public boolean usesLocalFiles() {
        return true;
    }

    @Override
    public boolean usesLocalFilePerTable() {
        return true;
    }

    @Override
    public boolean supportsSavepoints() {
        return false;
    }

    @Override
    public boolean supportsGetGeneratedKeys() {
        return this.adapterConnection.getFeatures().hasFeature(AdapterFeatureKey.ReturnGeneratedKeys);
    }

    @Override
    public boolean generatedKeyAlwaysReturned() {
        return this.adapterConnection.getFeatures().boolFeatureVal(AdapterFeatureKey.ReturnGeneratedKeys);
    }

    @Override
    public boolean nullsAreSortedHigh() {
        return false;
    }

    @Override
    public boolean nullsAreSortedLow() {
        return false;
    }

    @Override
    public boolean nullsAreSortedAtStart() {
        return false;
    }

    @Override
    public boolean nullsAreSortedAtEnd() {
        return false;
    }

    @Override
    public boolean nullPlusNonNullIsNull() {
        return false;
    }

    @Override
    public boolean supportsMinimumSQLGrammar() {
        return false;
    }

    @Override
    public boolean supportsCoreSQLGrammar() {
        return false;
    }

    @Override
    public boolean supportsExtendedSQLGrammar() {
        return false;
    }

    @Override
    public boolean supportsANSI92EntryLevelSQL() {
        return false;
    }

    @Override
    public boolean supportsANSI92IntermediateSQL() {
        return false;
    }

    @Override
    public boolean supportsANSI92FullSQL() {
        return false;
    }

    @Override
    public boolean supportsAlterTableWithAddColumn() {
        return false;
    }

    @Override
    public boolean supportsAlterTableWithDropColumn() {
        return false;
    }

    @Override
    public boolean supportsColumnAliasing() {
        return false;
    }

    @Override
    public boolean supportsGroupBy() {
        return false;
    }

    @Override
    public boolean supportsGroupByUnrelated() {
        return false;
    }

    @Override
    public boolean supportsGroupByBeyondSelect() {
        return false;
    }

    @Override
    public boolean supportsTableCorrelationNames() {
        return false;
    }

    @Override
    public boolean supportsDifferentTableCorrelationNames() {
        return false;
    }

    @Override
    public boolean supportsSubqueriesInComparisons() {
        return false;
    }

    @Override
    public boolean supportsSubqueriesInExists() {
        return false;
    }

    @Override
    public boolean supportsSubqueriesInIns() {
        return false;
    }

    @Override
    public boolean supportsSubqueriesInQuantifieds() {
        return false;
    }

    @Override
    public boolean supportsCorrelatedSubqueries() {
        return false;
    }

    @Override
    public boolean supportsOuterJoins() {
        return false;
    }

    @Override
    public boolean supportsLimitedOuterJoins() {
        return false;
    }

    @Override
    public boolean supportsFullOuterJoins() {
        return false;
    }

    @Override
    public boolean supportsSchemasInDataManipulation() {
        return false;
    }

    @Override
    public boolean supportsSchemasInProcedureCalls() {
        return false;
    }

    @Override
    public boolean supportsSchemasInTableDefinitions() {
        return false;
    }

    @Override
    public boolean supportsSchemasInIndexDefinitions() {
        return false;
    }

    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() {
        return false;
    }

    @Override
    public boolean supportsCatalogsInDataManipulation() {
        return false;
    }

    @Override
    public boolean supportsCatalogsInProcedureCalls() {
        return false;
    }

    @Override
    public boolean supportsCatalogsInTableDefinitions() {
        return false;
    }

    @Override
    public boolean supportsCatalogsInIndexDefinitions() {
        return false;
    }

    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() {
        return false;
    }

    @Override
    public boolean supportsUnion() {
        return false;
    }

    @Override
    public boolean supportsUnionAll() {
        return false;
    }

    @Override
    public boolean supportsExpressionsInOrderBy() {
        return false;
    }

    @Override
    public boolean supportsOrderByUnrelated() {
        return false;
    }

    @Override
    public boolean supportsLikeEscapeClause() {
        return false;
    }

    @Override
    public boolean supportsMultipleResultSets() {
        return true;
    }

    @Override
    public boolean supportsMultipleTransactions() {
        return false;
    }

    @Override
    public boolean supportsNonNullableColumns() {
        return false;
    }

    @Override
    public boolean supportsPositionedDelete() {
        return false;
    }

    @Override
    public boolean supportsPositionedUpdate() {
        return false;
    }

    @Override
    public boolean supportsSelectForUpdate() {
        return false;
    }

    @Override
    public boolean supportsStoredProcedures() {
        return false;
    }

    @Override
    public boolean supportsIntegrityEnhancementFacility() {
        return false;
    }

    @Override
    public boolean supportsOpenCursorsAcrossCommit() {
        return false;
    }

    @Override
    public boolean supportsOpenCursorsAcrossRollback() {
        return false;
    }

    @Override
    public boolean supportsOpenStatementsAcrossCommit() {
        return false;
    }

    @Override
    public boolean supportsOpenStatementsAcrossRollback() {
        return false;
    }

    @Override
    public int getMaxBinaryLiteralLength() {
        return 0;
    }

    @Override
    public int getMaxCharLiteralLength() {
        return 0;
    }

    @Override
    public int getMaxColumnNameLength() {
        return 0;
    }

    @Override
    public int getMaxColumnsInGroupBy() {
        return 0;
    }

    @Override
    public int getMaxColumnsInIndex() {
        return 0;
    }

    @Override
    public int getMaxColumnsInOrderBy() {
        return 0;
    }

    @Override
    public int getMaxColumnsInSelect() {
        return 0;
    }

    @Override
    public int getMaxColumnsInTable() {
        return 0;
    }

    @Override
    public int getMaxConnections() {
        return 0;
    }

    @Override
    public int getMaxCursorNameLength() {
        return 0;
    }

    @Override
    public int getMaxIndexLength() {
        return 0;
    }

    @Override
    public int getMaxSchemaNameLength() {
        return 0;
    }

    @Override
    public int getMaxProcedureNameLength() {
        return 0;
    }

    @Override
    public int getMaxCatalogNameLength() {
        return 0;
    }

    @Override
    public int getMaxRowSize() {
        return 0;
    }

    @Override
    public int getMaxStatementLength() {
        return 0;
    }

    @Override
    public int getMaxStatements() {
        return 0;
    }

    @Override
    public int getMaxTableNameLength() {
        return 0;
    }

    @Override
    public int getMaxTablesInSelect() {
        return 0;
    }

    @Override
    public int getMaxUserNameLength() {
        return 0;
    }

    @Override
    public boolean supportsTransactions() {
        return this.adapterConnection instanceof TransactionSupport;
    }

    @Override
    public boolean supportsTransactionIsolationLevel(int level) {
        if (this.adapterConnection instanceof TransactionSupport) {
            return ((TransactionSupport) this.adapterConnection).supportIsolation(level);
        } else {
            return false;
        }
    }

    @Override
    public int getDefaultTransactionIsolation() {
        return Connection.TRANSACTION_NONE;
    }

    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() {
        return false;
    }

    @Override
    public boolean supportsDataManipulationTransactionsOnly() {
        return false;
    }

    @Override
    public boolean dataDefinitionCausesTransactionCommit() {
        return false;
    }

    @Override
    public boolean dataDefinitionIgnoredInTransactions() {
        return false;
    }

    @Override
    public boolean supportsResultSetType(int type) {
        return type == ResultSet.TYPE_FORWARD_ONLY;
    }

    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) {
        return this.supportsResultSetType(type) && concurrency == ResultSet.CONCUR_READ_ONLY;
    }

    @Override
    public boolean ownUpdatesAreVisible(int type) {
        return false;
    }

    @Override
    public boolean ownDeletesAreVisible(int type) {
        return false;
    }

    @Override
    public boolean ownInsertsAreVisible(int type) {
        return false;
    }

    @Override
    public boolean othersUpdatesAreVisible(int type) {
        return false;
    }

    @Override
    public boolean othersDeletesAreVisible(int type) {
        return false;
    }

    @Override
    public boolean othersInsertsAreVisible(int type) {
        return false;
    }

    @Override
    public boolean updatesAreDetected(int type) {
        return false;
    }

    @Override
    public boolean deletesAreDetected(int type) {
        return false;
    }

    @Override
    public boolean insertsAreDetected(int type) {
        return false;
    }

    @Override
    public boolean supportsBatchUpdates() {
        return false;
    }

    @Override
    public boolean supportsNamedParameters() {
        return false;
    }

    @Override
    public boolean supportsMultipleOpenResults() {
        return false;
    }

    @Override
    public boolean supportsResultSetHoldability(int holdability) {
        return holdability == ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    @Override
    public int getResultSetHoldability() {
        return ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    @Override
    public int getSQLStateType() {
        return 0;
    }

    @Override
    public boolean locatorsUpdateCopy() {
        return false;
    }

    @Override
    public boolean supportsStatementPooling() {
        return false;
    }

    @Override
    public RowIdLifetime getRowIdLifetime() {
        return RowIdLifetime.ROWID_UNSUPPORTED;
    }

    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() {
        return false;
    }

    @Override
    public boolean autoCommitFailureClosesAllResultSets() {
        return false;
    }

    @Override
    public boolean supportsConvert() {
        return false;
    }

    @Override
    public boolean supportsConvert(int fromType, int toType) {
        return false;
    }

    @Override
    public String getSearchStringEscape() {
        return "\\";
    }

    @Override
    public String getExtraNameCharacters() {
        return "";
    }

    @Override
    public boolean isCatalogAtStart() {
        return false;
    }

    @Override
    public boolean doesMaxRowSizeIncludeBlobs() {
        return false;
    }

    @Override
    public String getSchemaTerm() {
        return "schema";
    }

    @Override
    public String getProcedureTerm() {
        return "procedure";
    }

    @Override
    public String getCatalogTerm() {
        return "database";
    }

    @Override
    public ResultSet getCatalogs() throws SQLException {
        this.jdbcConnection.checkOpen();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (MetadataNode node : queryMetadata(new MetadataPath(MetadataType.CATALOG))) {
            rows.add(metadataIdentity(node.name(), null, null));
        }
        rows.sort(metadataOrder("TABLE_CAT"));
        return metadataResult(metadataCursor(new String[] { "TABLE_CAT" }, rows));
    }

    @Override
    public ResultSet getSchemas() throws SQLException {
        this.jdbcConnection.checkOpen();
        return getSchemas(null, null);
    }

    @Override
    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
        this.jdbcConnection.checkOpen();
        List<Map<String, Object>> rows = new ArrayList<>();
        Set<MetadataType> kinds = metadataKinds();
        if (kinds.contains(MetadataType.SCHEMA)) {
            for (MetadataPath path : metadataCatalogs(catalog, kinds)) {
                for (MetadataNode node : queryMetadata(path.target(MetadataType.SCHEMA))) {
                    if (matchesMetadataPattern(node.name(), schemaPattern)) {
                        Map<String, Object> row = new HashMap<>();
                        row.put("TABLE_SCHEM", node.name());
                        row.put("TABLE_CATALOG", path.name(MetadataType.CATALOG));
                        rows.add(row);
                    }
                }
            }
        }
        rows.sort(metadataOrder("TABLE_CATALOG", "TABLE_SCHEM"));
        return metadataResult(metadataCursor(new String[] { "TABLE_SCHEM", "TABLE_CATALOG" }, rows));
    }

    @Override
    public boolean allProceduresAreCallable() {
        return false;
    }

    @Override
    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException {
        return emptySet(//
                "PROCEDURE_CAT",                      //
                "PROCEDURE_SCHEM",                    //
                "PROCEDURE_NAME",                     //
                "NUM_INPUT_PARAMS", JDBCType.INTEGER, //
                "NUM_OUTPUT_PARAMS", JDBCType.INTEGER,//
                "NUM_RESULT_SETS", JDBCType.INTEGER,  //
                "REMARKS",                            //
                "PROCEDURE_TYPE", JDBCType.SMALLINT,  //
                "SPECIFIC_NAME"                       //
        );
    }

    @Override
    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException {
        return emptySet(//
                "PROCEDURE_CAT",                      //
                "PROCEDURE_SCHEM",                    //
                "PROCEDURE_NAME",                     //
                "COLUMN_NAME",                        //
                "COLUMN_TYPE", JDBCType.SMALLINT,     //
                "DATA_TYPE", JDBCType.INTEGER,        //
                "TYPE_NAME",                          //
                "PRECISION", JDBCType.INTEGER,        //
                "LENGTH", JDBCType.INTEGER,           //
                "SCALE", JDBCType.SMALLINT,           //
                "RADIX", JDBCType.SMALLINT,           //
                "NULLABLE", JDBCType.SMALLINT,        //
                "REMARKS",                            //
                "COLUMN_DEF",                         //
                "SQL_DATA_TYPE", JDBCType.INTEGER,    //
                "SQL_DATETIME_SUB", JDBCType.INTEGER, //
                "CHAR_OCTET_LENGTH", JDBCType.INTEGER,//
                "ORDINAL_POSITION", JDBCType.INTEGER, //
                "IS_NULLABLE",                        //
                "SPECIFIC_NAME"                      //
        );
    }

    @Override
    public boolean allTablesAreSelectable() {
        return false;
    }

    @Override
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
        this.jdbcConnection.checkOpen();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (MetadataTable table : metadataTables(catalog, schemaPattern, tableNamePattern, types)) {
            Map<String, Object> row = metadataIdentity(table.path.name(MetadataType.CATALOG), table.path.name(MetadataType.SCHEMA), table.node.name());
            row.put("TABLE_TYPE", table.node.type().name());
            row.put("REMARKS", table.node.attributes().get(MetadataNode.REMARKS));
            rows.add(row);
        }
        rows.sort(metadataOrder("TABLE_TYPE", "TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME"));
        return metadataResult(metadataCursor(TABLE_COLUMNS, rows));
    }

    @Override
    public ResultSet getTableTypes() throws SQLException {
        this.jdbcConnection.checkOpen();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (MetadataType kind : metadataKinds()) {
            if (kind == MetadataType.TABLE || kind == MetadataType.VIEW) {
                rows.add(Map.of("TABLE_TYPE", kind.name()));
            }
        }
        rows.sort(metadataOrder("TABLE_TYPE"));
        return metadataResult(metadataCursor(new String[] { "TABLE_TYPE" }, rows));
    }

    @Override
    public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        return emptySet(//
                "TABLE_CAT",          //
                "TABLE_SCHEM",        //
                "TABLE_NAME",         //
                "GRANTOR",            //
                "GRANTEE",            //
                "PRIVILEGE",          //
                "IS_GRANTABLE"       //
        );
    }

    @Override
    public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        this.jdbcConnection.checkOpen();
        List<Map<String, Object>> rows = new ArrayList<>();
        if (metadataKinds().contains(MetadataType.COLUMN)) {
            for (MetadataTable table : metadataTables(catalog, schemaPattern, tableNamePattern, null)) {
                int ordinal = 0;
                for (MetadataNode column : queryMetadata(table.path.child(table.node, MetadataType.COLUMN))) {
                    ordinal++;
                    if (matchesMetadataPattern(column.name(), columnNamePattern)) {
                        rows.add(metadataColumn(table, column, ordinal));
                    }
                }
            }
        }
        rows.sort(metadataOrder("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME").thenComparingInt(row -> ((Number) row.get("ORDINAL_POSITION")).intValue()));
        return metadataResult(metadataCursor(FIELD_COLUMNS, rows));
    }

    @Override
    public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException {
        return emptySet(//
                "TABLE_CAT",          //
                "TABLE_SCHEM",        //
                "TABLE_NAME",         //
                "COLUMN_NAME",        //
                "GRANTOR",            //
                "GRANTEE",            //
                "PRIVILEGE",          //
                "IS_GRANTABLE"       //
        );
    }

    @Override
    public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException {
        return emptySet(//
                "SCOPE", JDBCType.SMALLINT,        //
                "COLUMN_NAME",                     //
                "DATA_TYPE", JDBCType.INTEGER,     //
                "TYPE_NAME",                       //
                "COLUMN_SIZE", JDBCType.INTEGER,   //
                "BUFFER_LENGTH", JDBCType.INTEGER, //
                "DECIMAL_DIGITS", JDBCType.INTEGER,//
                "PSEUDO_COLUMN", JDBCType.SMALLINT//
        );
    }

    @Override
    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException {
        return emptySet(//
                "FUNCTION_CAT",                    //
                "FUNCTION_SCHEM",                  //
                "FUNCTION_NAME",                   //
                "REMARKS",                         //
                "FUNCTION_TYPE", JDBCType.SMALLINT,//
                "SPECIFIC_NAME"                   //
        );
    }

    @Override
    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) throws SQLException {
        return emptySet(//
                "FUNCTION_CAT",                       //
                "FUNCTION_SCHEM",                     //
                "FUNCTION_NAME",                      //
                "COLUMN_NAME",                        //
                "COLUMN_TYPE", JDBCType.SMALLINT,     //
                "DATA_TYPE", JDBCType.INTEGER,        //
                "TYPE_NAME",                          //
                "PRECISION", JDBCType.INTEGER,        //
                "LENGTH", JDBCType.INTEGER,           //
                "SCALE", JDBCType.INTEGER,            //
                "RADIX", JDBCType.SMALLINT,           //
                "NULLABLE", JDBCType.SMALLINT,        //
                "REMARKS",                            //
                "CHAR_OCTET_LENGTH", JDBCType.INTEGER,//
                "ORDINAL_POSITION", JDBCType.INTEGER, //
                "IS_NULLABLE",                        //
                "SPECIFIC_NAME"                      //
        );
    }

    @Override
    public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) throws SQLException {
        return emptySet(//
                "SCOPE", JDBCType.SMALLINT,        //
                "COLUMN_NAME",                     //
                "DATA_TYPE", JDBCType.INTEGER,     //
                "TYPE_NAME",                       //
                "COLUMN_SIZE", JDBCType.INTEGER,   //
                "BUFFER_LENGTH", JDBCType.INTEGER, //
                "DECIMAL_DIGITS", JDBCType.INTEGER,//
                "PSEUDO_COLUMN", JDBCType.SMALLINT//
        );
    }

    @Override
    public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException {
        return emptySet(//
                "TABLE_CAT",                 //
                "TABLE_SCHEM",               //
                "TABLE_NAME",                //
                "COLUMN_NAME",               //
                "KEY_SEQ", JDBCType.SMALLINT,//
                "PK_NAME"                   //
        );
    }

    @Override
    public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
        return emptySet(//
                "PKTABLE_CAT",                    //
                "PKTABLE_SCHEM",                  //
                "PKTABLE_NAME",                   //
                "PKCOLUMN_NAME",                  //
                "FKTABLE_CAT",                    //
                "FKTABLE_SCHEM",                  //
                "FKTABLE_NAME",                   //
                "FKCOLUMN_NAME",                  //
                "KEY_SEQ", JDBCType.SMALLINT,     //
                "UPDATE_RULE", JDBCType.SMALLINT, //
                "DELETE_RULE", JDBCType.SMALLINT, //
                "FK_NAME",                        //
                "PKCOLUMN_NAME",                  //
                "PK_NAME",                        //
                "DEFERRABILITY", JDBCType.SMALLINT//
        );
    }

    @Override
    public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
        return emptySet(//
                "PKTABLE_CAT",                    //
                "PKTABLE_SCHEM",                  //
                "PKTABLE_NAME",                   //
                "PKCOLUMN_NAME",                  //
                "FKTABLE_CAT",                    //
                "FKTABLE_SCHEM",                  //
                "FKTABLE_NAME",                   //
                "FKCOLUMN_NAME",                  //
                "KEY_SEQ", JDBCType.SMALLINT,     //
                "UPDATE_RULE", JDBCType.SMALLINT, //
                "DELETE_RULE", JDBCType.SMALLINT, //
                "FK_NAME",                        //
                "PK_NAME",                        //
                "DEFERRABILITY", JDBCType.SMALLINT//
        );
    }

    @Override
    public ResultSet getCrossReference(String parentCatalog, String parentSchema, String parentTable, String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException {
        return emptySet(//
                "PKTABLE_CAT",                    //
                "PKTABLE_SCHEM",                  //
                "PKTABLE_NAME",                   //
                "PKCOLUMN_NAME",                  //
                "FKTABLE_CAT",                    //
                "FKTABLE_SCHEM",                  //
                "FKTABLE_NAME",                   //
                "FKCOLUMN_NAME",                  //
                "KEY_SEQ", JDBCType.SMALLINT,     //
                "UPDATE_RULE", JDBCType.SMALLINT, //
                "DELETE_RULE", JDBCType.SMALLINT, //
                "FK_NAME",                        //
                "PK_NAME",                        //
                "DEFERRABILITY", JDBCType.SMALLINT//
        );
    }

    @Override
    public ResultSet getTypeInfo() throws SQLException {
        return emptySet(//
                "TYPE_NAME",                           //
                "DATA_TYPE", JDBCType.INTEGER,         //
                "PRECISION", JDBCType.INTEGER,         //
                "LITERAL_PREFIX",                      //
                "LITERAL_SUFFIX",                      //
                "CREATE_PARAMS",                       //
                "NULLABLE", JDBCType.SMALLINT,         //
                "CASE_SENSITIVE", JDBCType.BOOLEAN,    //
                "SEARCHABLE", JDBCType.SMALLINT,       //
                "UNSIGNED_ATTRIBUTE", JDBCType.BOOLEAN,//
                "FIXED_PREC_SCALE", JDBCType.BOOLEAN,  //
                "AUTO_INCREMENT", JDBCType.BOOLEAN,    //
                "LOCAL_TYPE_NAME",                     //
                "MINIMUM_SCALE", JDBCType.SMALLINT,    //
                "MAXIMUM_SCALE", JDBCType.SMALLINT,    //
                "SQL_DATA_TYPE", JDBCType.INTEGER,     //
                "SQL_DATETIME_SUB", JDBCType.INTEGER,  //
                "NUM_PREC_RADIX", JDBCType.INTEGER    //
        );
    }

    @Override
    public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException {
        return emptySet(//
                "TABLE_CAT",                          //
                "TABLE_SCHEM",                        //
                "TABLE_NAME",                         //
                "NON_UNIQUE", JDBCType.BOOLEAN,       //
                "INDEX_QUALIFIER",                    //
                "INDEX_NAME", JDBCType.INTEGER,       //
                "TYPE", JDBCType.SMALLINT,            //
                "ORDINAL_POSITION", JDBCType.SMALLINT,//
                "COLUMN_NAME",                        //
                "ASC_OR_DESC",                        //
                "CARDINALITY", JDBCType.BIGINT,       //
                "PAGES", JDBCType.BIGINT,             //
                "FILTER_CONDITION"                   //
        );
    }

    @Override
    public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws SQLException {
        return emptySet(//
                "TYPE_CAT",                    //
                "TYPE_SCHEM",                  //
                "TYPE_NAME",                   //
                "CLASS_NAME",                  //
                "DATA_TYPE", JDBCType.INTEGER, //
                "REMARKS",                     //
                "BASE_TYPE", JDBCType.SMALLINT//
        );
    }

    @Override
    public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) throws SQLException {
        return emptySet(//
                "TYPE_CAT",           //
                "TYPE_SCHEM",         //
                "TYPE_NAME",          //
                "SUPERTYPE_CAT",      //
                "SUPERTYPE_SCHEM",    //
                "SUPERTYPE_NAME"     //
        );
    }

    @Override
    public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        return emptySet(//
                "TABLE_CAT",          //
                "TABLE_SCHEM",        //
                "TABLE_NAME",         //
                "SUPERTABLE_NAME"    //
        );
    }

    @Override
    public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern) throws SQLException {
        return emptySet(//
                "TYPE_CAT",                           //
                "TYPE_SCHEM",                         //
                "TYPE_NAME",                          //
                "ATTR_NAME",                          //
                "DATA_TYPE", JDBCType.INTEGER,        //
                "ATTR_TYPE_NAME",                     //
                "ATTR_SIZE", JDBCType.INTEGER,        //
                "DECIMAL_DIGITS", JDBCType.INTEGER,   //
                "NUM_PREC_RADIX", JDBCType.INTEGER,   //
                "NULLABLE", JDBCType.INTEGER,         //
                "REMARKS",                            //
                "ATTR_DEF",                           //
                "SQL_DATA_TYPE", JDBCType.INTEGER,    //
                "SQL_DATETIME_SUB", JDBCType.INTEGER, //
                "CHAR_OCTET_LENGTH", JDBCType.INTEGER,//
                "ORDINAL_POSITION", JDBCType.INTEGER, //
                "IS_NULLABLE",                        //
                "SCOPE_CATALOG",                      //
                "SCOPE_SCHEMA",                       //
                "SCOPE_TABLE",                        //
                "SOURCE_DATA_TYPE", JDBCType.SMALLINT //
        );
    }

    @Override
    public ResultSet getClientInfoProperties() throws SQLException {
        return emptySet(//
                "NAME",                     //
                "MAX_LEN", JDBCType.INTEGER,//
                "DEFAULT_VALUE",            //
                "DESCRIPTION"              //
        );
    }

    @Override
    public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        return emptySet(//
                "TABLE_CAT",                          //
                "TABLE_SCHEM",                        //
                "TABLE_NAME",                         //
                "COLUMN_NAME",                        //
                "DATA_TYPE", JDBCType.INTEGER,        //
                "COLUMN_SIZE", JDBCType.INTEGER,      //
                "DECIMAL_DIGITS", JDBCType.INTEGER,   //
                "NUM_PREC_RADIX", JDBCType.INTEGER,   //
                "COLUMN_USAGE",                       //
                "REMARKS",                            //
                "CHAR_OCTET_LENGTH", JDBCType.INTEGER,//
                "IS_NULLABLE"                        //
        );
    }

    private ResultSet metadataResult(AdapterCursor cursor) throws SQLException {
        JdbcStatement statement = new JdbcStatement(this.jdbcConnection);
        JdbcResultSet result = new JdbcResultSet(statement, cursor);
        statement.closeOnCompletion();
        return result;
    }

    private ResultSet emptySet(Object... cols) throws SQLException {
        this.jdbcConnection.checkOpen();
        JdbcStatement s = new JdbcStatement(this.jdbcConnection);
        return new JdbcResultSet(s, new AdapterMemoryCursor(columnInfo(cols), null));
    }

    private static List<JdbcColumn> columnInfo(Object... cols) {
        List<JdbcColumn> columns = new ArrayList<>();

        for (int i = 0; i < cols.length; i++) {
            Object obj = cols[i];
            if (obj instanceof String) {
                String name = obj.toString();
                String type = JDBCType.VARCHAR.name();
                if (i + 1 < cols.length) {
                    Object next = cols[i + 1];
                    if (next instanceof JDBCType) {
                        type = ((JDBCType) next).name();
                        i++;
                    }
                }

                columns.add(new JdbcColumn(name, type, "ROUTINES", "INFORMATION_SCHEMA", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array));
            }
        }
        return columns;
    }

    // @formatter:off
    private static final String[] TABLE_COLUMNS = {
        "TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "TABLE_TYPE", "REMARKS",
        "TYPE_CAT", "TYPE_SCHEM", "TYPE_NAME", "SELF_REFERENCING_COL_NAME", "REF_GENERATION"
    };
    private static final String[] FIELD_COLUMNS = {
        "TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "COLUMN_NAME", "DATA_TYPE", "TYPE_NAME",
        "COLUMN_SIZE", "BUFFER_LENGTH", "DECIMAL_DIGITS", "NUM_PREC_RADIX", "NULLABLE", "REMARKS",
        "COLUMN_DEF", "SQL_DATA_TYPE", "SQL_DATETIME_SUB", "CHAR_OCTET_LENGTH", "ORDINAL_POSITION",
        "IS_NULLABLE", "SCOPE_CATALOG", "SCOPE_SCHEMA", "SCOPE_TABLE", "SOURCE_DATA_TYPE",
        "IS_AUTOINCREMENT", "IS_GENERATEDCOLUMN"
    };
    private static final Set<String> INTEGER_FIELDS = Set.of("DATA_TYPE", "COLUMN_SIZE",
            "BUFFER_LENGTH", "DECIMAL_DIGITS", "NUM_PREC_RADIX", "NULLABLE", "SQL_DATA_TYPE",
            "SQL_DATETIME_SUB", "CHAR_OCTET_LENGTH", "ORDINAL_POSITION");
    // @formatter:on

    private Set<MetadataType> metadataKinds() throws SQLException {
        return metadataSupport == null ? Set.of() : metadataSupport.supportedTypes();
    }

    private List<MetadataNode> queryMetadata(MetadataPath path) throws SQLException {
        if (metadataSupport == null) {
            return List.of();
        }
        List<MetadataNode> nodes = metadataSupport.query(path);
        if (nodes == null) {
            throw new SQLException("MetadataSupport returned null for " + path);
        }
        for (MetadataNode node : nodes) {
            if (node == null || (node.type() != path.type() || node.name() == null)) {
                throw new SQLException("Invalid metadata node for " + path);
            }
        }
        return nodes;
    }

    private List<MetadataPath> metadataCatalogs(String catalog, Set<MetadataType> kinds) throws SQLException {
        MetadataPath root = new MetadataPath(MetadataType.TABLE);
        if (!kinds.contains(MetadataType.CATALOG)) {
            return catalog == null || catalog.isEmpty() ? List.of(root) : List.of();
        }
        if ("".equals(catalog)) {
            return List.of();
        }
        List<MetadataNode> catalogs = catalog == null ? queryMetadata(root.target(MetadataType.CATALOG)) : List.of(new MetadataNode(MetadataType.CATALOG, catalog));
        List<MetadataPath> paths = new ArrayList<>();
        for (MetadataNode node : catalogs) {
            paths.add(root.child(node, MetadataType.TABLE));
        }
        return paths;
    }

    private List<MetadataTable> metadataTables(String catalog, String schemaPattern, String tablePattern, String[] types) throws SQLException {
        List<MetadataTable> tables = new ArrayList<>();
        if (types != null && types.length == 0) {
            return tables;
        }
        Set<MetadataType> kinds = metadataKinds();
        List<MetadataType> tableKinds = new ArrayList<>();
        for (MetadataType kind : List.of(MetadataType.TABLE, MetadataType.VIEW)) {
            if (kinds.contains(kind) && (types == null || Arrays.asList(types).contains(kind.name()))) {
                tableKinds.add(kind);
            }
        }
        if (tableKinds.isEmpty()) {
            return tables;
        }
        for (MetadataPath catalogPath : metadataCatalogs(catalog, kinds)) {
            List<MetadataPath> namespaces = new ArrayList<>();
            if (kinds.contains(MetadataType.SCHEMA)) {
                for (MetadataNode schema : queryMetadata(catalogPath.target(MetadataType.SCHEMA))) {
                    if (matchesMetadataPattern(schema.name(), schemaPattern)) {
                        namespaces.add(catalogPath.child(schema, MetadataType.TABLE));
                    }
                }
            } else if (matchesMetadataPattern("", schemaPattern)) {
                namespaces.add(catalogPath);
            }
            for (MetadataPath namespace : namespaces) {
                for (MetadataType kind : tableKinds) {
                    for (MetadataNode table : queryMetadata(namespace.target(kind))) {
                        if (matchesMetadataPattern(table.name(), tablePattern)) {
                            tables.add(new MetadataTable(namespace, table));
                        }
                    }
                }
            }
        }
        return tables;
    }

    private Map<String, Object> metadataIdentity(String catalog, String schema, String table) {
        Map<String, Object> row = new HashMap<>();
        row.put("TABLE_CAT", catalog);
        row.put("TABLE_SCHEM", schema);
        row.put("TABLE_NAME", table);
        return row;
    }

    private Map<String, Object> metadataColumn(MetadataTable table, MetadataNode column, int ordinal) {
        Map<String, Object> row = metadataIdentity(table.path.name(MetadataType.CATALOG), table.path.name(MetadataType.SCHEMA), table.node.name());
        Map<String, Object> attributes = column.attributes();
        row.put("COLUMN_NAME", column.name());
        row.put("DATA_TYPE", attributes.getOrDefault(MetadataNode.JDBC_TYPE, Types.OTHER));
        row.put("TYPE_NAME", attributes.get(MetadataNode.TYPE_NAME));
        row.put("COLUMN_SIZE", attributes.get(MetadataNode.SIZE));
        row.put("DECIMAL_DIGITS", attributes.get(MetadataNode.SCALE));
        row.put("CHAR_OCTET_LENGTH", attributes.get(MetadataNode.OCTET_LENGTH));
        row.put("COLUMN_DEF", attributes.get(MetadataNode.DEFAULT_VALUE));
        row.put("REMARKS", attributes.get(MetadataNode.REMARKS));
        row.put("ORDINAL_POSITION", attributes.getOrDefault(MetadataNode.ORDINAL, ordinal));
        Object nullable = attributes.get(MetadataNode.NULLABLE);
        row.put("NULLABLE", nullable == null ? columnNullableUnknown : Boolean.TRUE.equals(nullable) ? columnNullable : columnNoNulls);
        row.put("IS_NULLABLE", metadataBoolean(nullable));
        row.put("IS_AUTOINCREMENT", metadataBoolean(attributes.get(MetadataNode.AUTO_INCREMENT)));
        row.put("IS_GENERATEDCOLUMN", metadataBoolean(attributes.get(MetadataNode.GENERATED)));
        return row;
    }

    private String metadataBoolean(Object value) {
        return value == null ? "" : Boolean.TRUE.equals(value) ? "YES" : "NO";
    }

    private Comparator<Map<String, Object>> metadataOrder(String... keys) {
        return (left, right) -> {
            for (String key : keys) {
                int order = Comparator.nullsFirst(String::compareTo).compare((String) left.get(key), (String) right.get(key));
                if (order != 0) {
                    return order;
                }
            }
            return 0;
        };
    }

    private record MetadataTable(MetadataPath path, MetadataNode node) {
    }

    /** JDBC patterns use percent and underscore wildcards, escaped with a backslash. */
    private boolean matchesMetadataPattern(String value, String pattern) {
        if (pattern == null) {
            return true;
        }
        if (value == null) {
            return false;
        }

        StringBuilder regex = new StringBuilder();
        for (int i = 0; i < pattern.length(); i++) {
            char item = pattern.charAt(i);
            if (item == '\\' && i + 1 < pattern.length()) {
                regex.append(Pattern.quote(String.valueOf(pattern.charAt(++i))));
            } else if (item == '%') {
                regex.append(".*");
            } else if (item == '_') {
                regex.append('.');
            } else {
                regex.append(Pattern.quote(String.valueOf(item)));
            }
        }

        return Pattern.compile(regex.toString(), Pattern.DOTALL).matcher(value).matches();
    }

    private AdapterCursor metadataCursor(String[] names, List<Map<String, Object>> rows) {
        List<JdbcColumn> columns = new ArrayList<>();
        for (String name : names) {
            String type = INTEGER_FIELDS.contains(name) ? AdapterType.Int : "SOURCE_DATA_TYPE".equals(name) ? AdapterType.Short : AdapterType.String;
            columns.add(new JdbcColumn(name, type, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Unknown));
        }

        Object[][] data = new Object[rows.size()][names.length];
        for (int row = 0; row < rows.size(); row++) {
            for (int column = 0; column < names.length; column++) {
                String name = names[column];
                Object value = rows.get(row).get(name);
                if (value == null && "NULLABLE".equals(name)) {
                    value = DatabaseMetaData.columnNullableUnknown;
                } else if (value == null && name.startsWith("IS_")) {
                    value = "";
                }
                data[row][column] = value;
            }
        }

        return new AdapterMemoryCursor(columns, data);
    }
}
