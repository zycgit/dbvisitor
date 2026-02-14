package net.hasor.dbvisitor.driver;

import java.sql.*;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Comprehensive tests for JdbcDatabaseMetaData — covers all methods returning defaults, empty results, and feature queries. */
public class JdbcDatabaseMetaDataTest {

    private JdbcConnection   conn;
    private DatabaseMetaData md;

    @Before
    public void setUp() throws Exception {
        Class.forName("net.hasor.dbvisitor.driver.JdbcDriver");
        Properties props = new Properties();
        props.setProperty(JdbcDriver.P_ADAPTER_NAME, "mock");
        conn = new JdbcConnection("jdbc:dbvisitor:mock://localhost", props, null);
        md = conn.getMetaData();
    }

    @After
    public void tearDown() throws Exception {
        if (conn != null && !conn.isClosed())
            conn.close();
    }

    // ==================== connection and driver info ====================
    @Test
    public void getConnection_test() throws Exception {
        assertSame(conn, md.getConnection());
    }

    @Test
    public void getURL_test() throws Exception {
        assertNotNull(md.getURL());
    }

    @Test
    public void getUserName_test() throws Exception {
        md.getUserName(); /* may return null */
    }

    @Test
    public void getDatabaseProductName_test() throws Exception {
        assertNotNull(md.getDatabaseProductName());
    }

    @Test
    public void getDatabaseProductVersion_test() throws Exception {
        assertNotNull(md.getDatabaseProductVersion());
    }

    @Test
    public void getDatabaseMajorVersion_test() throws Exception {
        md.getDatabaseMajorVersion();
    }

    @Test
    public void getDatabaseMinorVersion_test() throws Exception {
        md.getDatabaseMinorVersion();
    }

    @Test
    public void getDriverName_test() throws Exception {
        assertNotNull(md.getDriverName());
    }

    @Test
    public void getDriverVersion_test() throws Exception {
        assertNotNull(md.getDriverVersion());
    }

    @Test
    public void getDriverMajorVersion_test() {
        md.getDriverMajorVersion();
    }

    @Test
    public void getDriverMinorVersion_test() {
        md.getDriverMinorVersion();
    }

    @Test
    public void getJDBCMajorVersion_test() throws Exception {
        assertEquals(4, md.getJDBCMajorVersion());
    }

    @Test
    public void getJDBCMinorVersion_test() throws Exception {
        assertEquals(2, md.getJDBCMinorVersion());
    }

    // ==================== boolean feature methods ====================
    @Test
    public void supportsTransactions_test() throws Exception {
        md.supportsTransactions();
    }

    @Test
    public void supportsMultipleResultSets_test() throws Exception {
        assertTrue(md.supportsMultipleResultSets());
    }

    @Test
    public void supportsGetGeneratedKeys_test() throws Exception {
        md.supportsGetGeneratedKeys();
    }

    @Test
    public void generatedKeyAlwaysReturned_test() throws Exception {
        md.generatedKeyAlwaysReturned();
    }

    @Test
    public void allBooleanFalseMethods() throws Exception {
        assertFalse(md.supportsMixedCaseIdentifiers());
        assertFalse(md.supportsMixedCaseQuotedIdentifiers());
        assertFalse(md.storesUpperCaseIdentifiers());
        assertFalse(md.storesUpperCaseQuotedIdentifiers());
        assertFalse(md.storesLowerCaseIdentifiers());
        assertFalse(md.storesLowerCaseQuotedIdentifiers());
        assertFalse(md.storesMixedCaseIdentifiers());
        assertFalse(md.storesMixedCaseQuotedIdentifiers());
        assertFalse(md.isReadOnly());
        assertFalse(md.nullsAreSortedHigh());
        assertFalse(md.nullsAreSortedLow());
        assertFalse(md.nullsAreSortedAtStart());
        assertFalse(md.nullsAreSortedAtEnd());
        assertFalse(md.nullPlusNonNullIsNull());
        assertFalse(md.supportsColumnAliasing());
        assertFalse(md.supportsTableCorrelationNames());
        assertFalse(md.supportsDifferentTableCorrelationNames());
        assertFalse(md.supportsExpressionsInOrderBy());
        assertFalse(md.supportsOrderByUnrelated());
        assertFalse(md.supportsGroupBy());
        assertFalse(md.supportsGroupByUnrelated());
        assertFalse(md.supportsGroupByBeyondSelect());
        assertFalse(md.supportsLikeEscapeClause());
        assertFalse(md.supportsMultipleTransactions());
        assertFalse(md.supportsNonNullableColumns());
        assertFalse(md.supportsMinimumSQLGrammar());
        assertFalse(md.supportsCoreSQLGrammar());
        assertFalse(md.supportsExtendedSQLGrammar());
        assertFalse(md.supportsANSI92EntryLevelSQL());
        assertFalse(md.supportsANSI92IntermediateSQL());
        assertFalse(md.supportsANSI92FullSQL());
        assertFalse(md.supportsOuterJoins());
        assertFalse(md.supportsFullOuterJoins());
        assertFalse(md.supportsLimitedOuterJoins());
        assertFalse(md.supportsStoredProcedures());
        assertFalse(md.supportsSubqueriesInComparisons());
        assertFalse(md.supportsSubqueriesInExists());
        assertFalse(md.supportsSubqueriesInIns());
        assertFalse(md.supportsSubqueriesInQuantifieds());
        assertFalse(md.supportsCorrelatedSubqueries());
        assertFalse(md.supportsUnion());
        assertFalse(md.supportsUnionAll());
        assertFalse(md.supportsSelectForUpdate());
        assertFalse(md.supportsPositionedDelete());
        assertFalse(md.supportsPositionedUpdate());
        assertFalse(md.supportsIntegrityEnhancementFacility());
        assertFalse(md.supportsAlterTableWithAddColumn());
        assertFalse(md.supportsAlterTableWithDropColumn());
        assertFalse(md.isCatalogAtStart());
        assertFalse(md.doesMaxRowSizeIncludeBlobs());
        assertFalse(md.supportsConvert());
        assertFalse(md.supportsConvert(Types.INTEGER, Types.VARCHAR));
        assertFalse(md.supportsBatchUpdates());
        assertFalse(md.supportsNamedParameters());
        assertFalse(md.supportsMultipleOpenResults());
        assertFalse(md.supportsSavepoints());
        assertFalse(md.supportsStatementPooling());
        assertFalse(md.supportsStoredFunctionsUsingCallSyntax());
        assertFalse(md.autoCommitFailureClosesAllResultSets());
        assertFalse(md.allProceduresAreCallable());
        assertFalse(md.allTablesAreSelectable());
        assertFalse(md.locatorsUpdateCopy());
    }

    @Test
    public void supportsTransactionIsolation_test() throws Exception {
        md.supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
    }

    @Test
    public void supportsSchemasIn_test() throws Exception {
        assertFalse(md.supportsSchemasInDataManipulation());
        assertFalse(md.supportsSchemasInProcedureCalls());
        assertFalse(md.supportsSchemasInTableDefinitions());
        assertFalse(md.supportsSchemasInIndexDefinitions());
        assertFalse(md.supportsSchemasInPrivilegeDefinitions());
    }

    @Test
    public void supportsCatalogsIn_test() throws Exception {
        assertFalse(md.supportsCatalogsInDataManipulation());
        assertFalse(md.supportsCatalogsInProcedureCalls());
        assertFalse(md.supportsCatalogsInTableDefinitions());
        assertFalse(md.supportsCatalogsInIndexDefinitions());
        assertFalse(md.supportsCatalogsInPrivilegeDefinitions());
    }

    @Test
    public void supportsResultSetFeatures() throws Exception {
        assertFalse(md.supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE));
        assertFalse(md.supportsResultSetConcurrency(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE));
        assertFalse(md.supportsResultSetHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT));
    }

    @Test
    public void ownAndOthersVisible() throws Exception {
        assertFalse(md.ownUpdatesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
        assertFalse(md.ownInsertsAreVisible(ResultSet.TYPE_FORWARD_ONLY));
        assertFalse(md.ownDeletesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
        assertFalse(md.othersUpdatesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
        assertFalse(md.othersInsertsAreVisible(ResultSet.TYPE_FORWARD_ONLY));
        assertFalse(md.othersDeletesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
        assertFalse(md.updatesAreDetected(ResultSet.TYPE_FORWARD_ONLY));
        assertFalse(md.insertsAreDetected(ResultSet.TYPE_FORWARD_ONLY));
        assertFalse(md.deletesAreDetected(ResultSet.TYPE_FORWARD_ONLY));
    }

    @Test
    public void supportsOpenStatements() throws Exception {
        assertFalse(md.supportsOpenCursorsAcrossCommit());
        assertFalse(md.supportsOpenCursorsAcrossRollback());
        assertFalse(md.supportsOpenStatementsAcrossCommit());
        assertFalse(md.supportsOpenStatementsAcrossRollback());
    }

    @Test
    public void supportsDataDefinition() throws Exception {
        assertFalse(md.supportsDataDefinitionAndDataManipulationTransactions());
        assertFalse(md.supportsDataManipulationTransactionsOnly());
        assertFalse(md.dataDefinitionCausesTransactionCommit());
        assertFalse(md.dataDefinitionIgnoredInTransactions());
    }

    // ==================== int return 0 methods ====================
    @Test
    public void intZeroMethods() throws Exception {
        assertEquals(0, md.getMaxBinaryLiteralLength());
        assertEquals(0, md.getMaxCharLiteralLength());
        assertEquals(0, md.getMaxColumnNameLength());
        assertEquals(0, md.getMaxColumnsInGroupBy());
        assertEquals(0, md.getMaxColumnsInIndex());
        assertEquals(0, md.getMaxColumnsInOrderBy());
        assertEquals(0, md.getMaxColumnsInSelect());
        assertEquals(0, md.getMaxColumnsInTable());
        assertEquals(0, md.getMaxConnections());
        assertEquals(0, md.getMaxCursorNameLength());
        assertEquals(0, md.getMaxIndexLength());
        assertEquals(0, md.getMaxSchemaNameLength());
        assertEquals(0, md.getMaxProcedureNameLength());
        assertEquals(0, md.getMaxCatalogNameLength());
        assertEquals(0, md.getMaxRowSize());
        assertEquals(0, md.getMaxStatementLength());
        assertEquals(0, md.getMaxStatements());
        assertEquals(0, md.getMaxTableNameLength());
        assertEquals(0, md.getMaxTablesInSelect());
        assertEquals(0, md.getMaxUserNameLength());
        assertEquals(Connection.TRANSACTION_NONE, md.getDefaultTransactionIsolation());
        assertEquals(0, md.getResultSetHoldability());
        assertEquals(0, md.getSQLStateType());
    }

    // ==================== string return methods ====================
    @Test
    public void stringMethods() throws Exception {
        assertEquals(".", md.getCatalogSeparator());
        assertEquals("schema", md.getSchemaTerm());
        assertEquals("procedure", md.getProcedureTerm());
        assertEquals("database", md.getCatalogTerm());
        assertNotNull(md.getSQLKeywords());
        assertNotNull(md.getNumericFunctions());
        assertNotNull(md.getStringFunctions());
        assertNotNull(md.getSystemFunctions());
        assertNotNull(md.getTimeDateFunctions());
        assertNotNull(md.getIdentifierQuoteString());
        assertNotNull(md.getSearchStringEscape());
        assertNotNull(md.getExtraNameCharacters());
    }

    // ==================== boolean true methods ====================
    @Test
    public void usesLocalFiles_test() throws Exception {
        assertTrue(md.usesLocalFiles());
    }

    @Test
    public void usesLocalFilePerTable_test() throws Exception {
        assertTrue(md.usesLocalFilePerTable());
    }

    // ==================== empty ResultSet methods ====================
    @Test
    public void getCatalogs_test() throws Exception {
        ResultSet rs = md.getCatalogs();
        assertNotNull(rs);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void getSchemas_test() throws Exception {
        ResultSet rs = md.getSchemas();
        assertNotNull(rs);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void getSchemas_withArgs() throws Exception {
        ResultSet rs = md.getSchemas("cat", "schema%");
        assertNotNull(rs);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void getTables_test() throws Exception {
        ResultSet rs = md.getTables(null, null, null, null);
        assertNotNull(rs);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void getTableTypes_test() throws Exception {
        ResultSet rs = md.getTableTypes();
        assertNotNull(rs);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void getColumns_test() throws Exception {
        ResultSet rs = md.getColumns(null, null, null, null);
        assertNotNull(rs);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void getColumnPrivileges_test() throws Exception {
        ResultSet rs = md.getColumnPrivileges(null, null, null, null);
        assertNotNull(rs);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void getTablePrivileges_test() throws Exception {
        ResultSet rs = md.getTablePrivileges(null, null, null);
        assertNotNull(rs);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void getPrimaryKeys_test() throws Exception {
        ResultSet rs = md.getPrimaryKeys(null, null, null);
        assertNotNull(rs);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void getImportedKeys_test() throws Exception {
        ResultSet rs = md.getImportedKeys(null, null, null);
        assertNotNull(rs);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void getExportedKeys_test() throws Exception {
        ResultSet rs = md.getExportedKeys(null, null, null);
        assertNotNull(rs);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void getCrossReference_test() throws Exception {
        ResultSet rs = md.getCrossReference(null, null, null, null, null, null);
        assertNotNull(rs);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void getTypeInfo_test() throws Exception {
        ResultSet rs = md.getTypeInfo();
        assertNotNull(rs);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void getIndexInfo_test() throws Exception {
        ResultSet rs = md.getIndexInfo(null, null, null, false, false);
        assertNotNull(rs);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void getProcedures_test() throws Exception {
        ResultSet rs = md.getProcedures(null, null, null);
        assertNotNull(rs);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void getProcedureColumns_test() throws Exception {
        ResultSet rs = md.getProcedureColumns(null, null, null, null);
        assertNotNull(rs);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void getFunctions_test() throws Exception {
        ResultSet rs = md.getFunctions(null, null, null);
        assertNotNull(rs);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void getFunctionColumns_test() throws Exception {
        ResultSet rs = md.getFunctionColumns(null, null, null, null);
        assertNotNull(rs);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void getVersionColumns_test() throws Exception {
        ResultSet rs = md.getVersionColumns(null, null, null);
        assertNotNull(rs);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void getBestRowIdentifier_test() throws Exception {
        ResultSet rs = md.getBestRowIdentifier(null, null, null, 0, false);
        assertNotNull(rs);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void getUDTs_test() throws Exception {
        ResultSet rs = md.getUDTs(null, null, null, null);
        assertNotNull(rs);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void getSuperTypes_test() throws Exception {
        ResultSet rs = md.getSuperTypes(null, null, null);
        assertNotNull(rs);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void getSuperTables_test() throws Exception {
        ResultSet rs = md.getSuperTables(null, null, null);
        assertNotNull(rs);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void getAttributes_test() throws Exception {
        ResultSet rs = md.getAttributes(null, null, null, null);
        assertNotNull(rs);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void getClientInfoProperties_test() throws Exception {
        ResultSet rs = md.getClientInfoProperties();
        assertNotNull(rs);
        assertFalse(rs.next());
        rs.close();
    }

    @Test
    public void getPseudoColumns_test() throws Exception {
        ResultSet rs = md.getPseudoColumns(null, null, null, null);
        assertNotNull(rs);
        assertFalse(rs.next());
        rs.close();
    }

    // ==================== other ====================
    @Test
    public void getRowIdLifetime_test() throws Exception {
        assertEquals(RowIdLifetime.ROWID_UNSUPPORTED, md.getRowIdLifetime());
    }

    @Test
    public void unwrap_test() throws Exception {
        try {
            md.unwrap(DatabaseMetaData.class);
        } catch (Exception ignore) {
        }
        try {
            md.isWrapperFor(DatabaseMetaData.class);
        } catch (Exception ignore) {
        }
    }
}
