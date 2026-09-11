parser grammar MilvusParser;

options { tokenVocab=MilvusLexer; }

root
    : (hintCommand SEMI?)* EOF
    ;

hintCommand
    : hint* command
    ;

hint
    : HintCommentStart hints? HintCommentEnd
    ;

hints
    : hintItem ((COMMA | SEMI) hintItem)*
    ;

hintItem
    : name=identifier (EQUALS value=hintValue)?
    ;

hintValue
    : identifier
    | literal
    ;

command
    : createCmd
    | alterCmd
    | dropCmd
    | showCmd
    | insertCmd
    | upsertCmd
    | updateCmd
    | deleteCmd
    | truncateCmd
    | grantCmd
    | revokeCmd
    | importCmd
    | loadCmd
    | selectCmd
    | countCmd
    | analyzeCmd
    | releaseCmd
    | renameCmd
    | flushCmd
    | compactCmd
    | transferCmd
    ;

createCmd
    : CREATE DATABASE (IF NOT EXISTS)? dbName=identifier (WITH propertiesList)?
    | CREATE RESOURCE GROUP groupName=identifier (CONFIG config=literal)?
    | CREATE PRIVILEGE GROUP groupName=identifier
    | CREATE TABLE (IF NOT EXISTS)? collectionName=identifier OPEN_PAREN fieldDefinition (COMMA fieldDefinition)* (COMMA functionDefinition)* CLOSE_PAREN (WITH propertiesList)?
    | CREATE PARTITION (IF NOT EXISTS)? partitionName=identifier ON (TABLE)? collectionName=identifier
    | CREATE INDEX (indexName=identifier)? ON (TABLE)? collectionName=identifier OPEN_PAREN fieldName=identifier CLOSE_PAREN (USING algo=indexAlgo)? (WITH withOptionList)?
    | CREATE ALIAS aliasName=identifier FOR (TABLE)? collectionName=identifier
    | CREATE USER (IF NOT EXISTS)? userName=identifier PASSWORD password=(STRING_LITERAL | ARG) (WITH propertiesList)?
    | CREATE ROLE (IF NOT EXISTS)? roleName=identifier (WITH propertiesList)?
    ;

alterCmd
    : ALTER DATABASE dbName=identifier SET PROPERTIES propertiesList
    | ALTER RESOURCE GROUP groupName=identifier CONFIG config=literal
    | ALTER RESOURCE GROUPS CONFIG config=literal
    | ALTER PRIVILEGE GROUP groupName=identifier (ADD | DROP) privilegeNames
    | ALTER USER userName=identifier PASSWORD password=(STRING_LITERAL | ARG) REPLACE oldPassword=(STRING_LITERAL | ARG) (WITH propertiesList)?
    | ALTER USER userName=identifier WITH propertiesList
    | ALTER ROLE roleName=identifier WITH propertiesList
    | ALTER DATABASE dbName=identifier DROP PROPERTIES propertyNames
    | ALTER ALIAS aliasName=identifier FOR (TABLE)? collectionName=identifier
    | ALTER TABLE collectionName=identifier ADD COLUMN? fieldDefinition
    | ALTER TABLE collectionName=identifier (ADD | ALTER) functionDefinition
    | ALTER TABLE collectionName=identifier DROP FUNCTION functionName=identifier
    | ALTER TABLE collectionName=identifier (ALTER COLUMN fieldName=identifier)? SET PROPERTIES propertiesList
    | ALTER TABLE collectionName=identifier (ALTER COLUMN fieldName=identifier)? DROP PROPERTIES propertyNames
    | ALTER INDEX indexName=identifier ON TABLE? collectionName=identifier SET PROPERTIES propertiesList
    | ALTER INDEX indexName=identifier ON TABLE? collectionName=identifier DROP PROPERTIES propertyNames
    ;

truncateCmd: TRUNCATE TABLE collectionName=identifier (IN DATABASE dbName=identifier)?;

propertiesList: OPEN_PAREN property (COMMA property)* CLOSE_PAREN;
propertyNames: OPEN_PAREN propertyName (COMMA propertyName)* CLOSE_PAREN;
privilegeNames: OPEN_PAREN identifier (COMMA identifier)* CLOSE_PAREN;
propertyName: identifier | STRING_LITERAL;

property: (identifier | STRING_LITERAL) EQUALS (STRING_LITERAL | identifier | INTEGER | FLOAT_LITERAL | TRUE | FALSE);

dropCmd
    : DROP DATABASE (IF EXISTS)? dbName=identifier
    | DROP RESOURCE GROUP groupName=identifier
    | DROP TABLE (IF EXISTS)? collectionName=identifier
    | DROP PARTITION (IF EXISTS)? partitionName=identifier ON (TABLE)? collectionName=identifier
    | DROP INDEX indexName=identifier ON (TABLE)? collectionName=identifier
    | DROP ALIAS (IF EXISTS)? aliasName=identifier
    | DROP USER (IF EXISTS)? userName=identifier
    | DROP ROLE (IF EXISTS)? roleName=identifier (WITH propertiesList)?
    | DROP PRIVILEGE GROUP groupName=identifier
    ;

showCmd
    : SHOW DATABASES
    | SHOW VERSION
    | SHOW HEALTH
    | SHOW REPLICAS FROM TABLE? collectionName=identifier
    | SHOW (PERSISTENT | QUERY) SEGMENTS FROM TABLE? collectionName=identifier
    | SHOW RESOURCE GROUPS
    | SHOW RESOURCE GROUP groupName=identifier
    | SHOW COMPACTION PLANS? compactionId=(INTEGER | ARG)
    | SHOW FLUSH ALL flushTimestamp=(INTEGER | ARG) (IN DATABASE database=privilegeScope)?
    | SHOW DATABASE dbName=identifier
    | SHOW STATS FROM TABLE? collectionName=identifier (PARTITION partitionName=identifier)?
    | SHOW ALIASES FROM TABLE? collectionName=identifier
    | SHOW ALIAS aliasName=identifier
    | SHOW TABLES
    | SHOW TABLE collectionName=identifier
    | SHOW CREATE TABLE collectionName=identifier
    | SHOW PARTITION partitionName=identifier ON (TABLE)? collectionName=identifier
    | SHOW USERS
    | SHOW USER userName=identifier
    | SHOW ROLE roleName=identifier (ON DATABASE database=privilegeScope)?
    | SHOW ROLES
    | SHOW PRIVILEGE GROUPS
    | SHOW GRANTS FOR ROLE roleName=identifier
    | SHOW PARTITIONS FROM (TABLE)? collectionName=identifier
    | SHOW INDEX indexName=identifier ON (TABLE)? collectionName=identifier
    | SHOW INDEXES FROM (TABLE)? collectionName=identifier
    | SHOW USERS
    | SHOW ROLES
    | SHOW GRANTS FOR ROLE roleName=identifier ON (TABLE | USER) objectName=identifier
    | SHOW GRANTS FOR ROLE roleName=identifier ON GLOBAL
    | SHOW PROGRESS OF INDEX (indexName=identifier)? ON (TABLE)? collectionName=identifier
    | SHOW PROGRESS OF LOADING ON (TABLE)? collectionName=identifier (PARTITION partitionName=identifier)?
    | SHOW (PROGRESS OF)? IMPORT jobId=literal
    | SHOW IMPORTS FROM (TABLE)? collectionName=identifier (WITH propertiesList)?
    ;

insertCmd
    : INSERT INTO collectionName=identifier (PARTITION partitionName=identifier)? (OPEN_PAREN columnList=identifiers CLOSE_PAREN)? VALUES valuesClause
    ;

upsertCmd
    : UPSERT INTO collectionName=identifier (PARTITION partitionName=identifier)? (OPEN_PAREN columnList=identifiers CLOSE_PAREN)? VALUES valuesClause
    ;

valuesClause: valueRow (COMMA valueRow)* | ARG;
valueRow: OPEN_PAREN terms CLOSE_PAREN;

updateCmd
    : UPDATE collectionName=identifier (PARTITION partitionName=identifier)? SET setClauseList (WHERE expression)? (ORDER BY sortClause)? (LIMIT (limit=INTEGER | limit=ARG))?
    ;

setClauseList
    : setClause (COMMA setClause)*
    ;

setClause
    : columnName=identifier EQUALS value=term
    ;

deleteCmd
    : DELETE FROM (TABLE)? collectionName=identifier (PARTITION partitionName=identifier)? (WHERE expression)? (ORDER BY sortClause)? (LIMIT (limit=INTEGER | limit=ARG))?
    ;

selectCmd
    : SELECT selectElements FROM collectionName=identifier (PARTITION partitionName=identifier)? (WHERE expression)? (ORDER BY (sortClause | hybridClause))? (LIMIT (limit=INTEGER | limit=ARG))? (OFFSET (offset=INTEGER | offset=ARG))? (WITH propertiesList)?
    ;

countCmd
    : (COUNT | SELECT COUNT OPEN_PAREN STAR CLOSE_PAREN) FROM collectionName=identifier (PARTITION partitionName=identifier)? (WHERE expression)? (WITH propertiesList)?
    ;

analyzeCmd
    : ANALYZE texts=literal (ON TABLE collectionName=identifier OPEN_PAREN fieldName=identifier CLOSE_PAREN)? (WITH analyzerOptions)?
    ;

analyzerOptions: OPEN_PAREN analyzerOption (COMMA analyzerOption)* CLOSE_PAREN;
analyzerOption: identifier EQUALS literal;

grantCmd
    : GRANT ROLE roleName=identifier TO userName=identifier                                                             # GrantRoleToUser
    | GRANT PRIVILEGE privilege=identifier ON DATABASE database=privilegeScope TABLE collection=privilegeScope TO ROLE roleName=identifier # GrantScopedPrivilege
    | GRANT privilege=identifier ON objectType=identifier (objectName=identifier | star=STAR) TO ROLE roleName=identifier    # GrantPrivilegeToRole
    ;

revokeCmd
    : REVOKE ROLE roleName=identifier FROM userName=identifier                                                              # RevokeRoleFromUser
    | REVOKE PRIVILEGE privilege=identifier ON DATABASE database=privilegeScope TABLE collection=privilegeScope FROM ROLE roleName=identifier # RevokeScopedPrivilege
    | REVOKE privilege=identifier ON objectType=identifier (objectName=identifier | star=STAR) FROM ROLE roleName=identifier     # RevokePrivilegeFromRole
    ;

privilegeScope: identifier | STAR;

importCmd
    : IMPORT FROM (FILE)? files=literal INTO (TABLE)? collectionName=identifier (PARTITION partitionName=identifier)? (WITH propertiesList)? (RETURNING resultName=identifier)?
    ;

renameCmd
    : ALTER TABLE collectionName=identifier RENAME TO newName=identifier (IN DATABASE targetDatabase=identifier)?
    ;

loadCmd
    : LOAD TABLE collectionName=identifier (PARTITION partitionName=identifier)? (WITH loadOptions)?
    ;

loadOptions: OPEN_PAREN loadOption (COMMA loadOption)* CLOSE_PAREN;
loadOption: identifier EQUALS literal;

releaseCmd
    : RELEASE TABLE collectionName=identifier (PARTITION partitionName=identifier)?
    ;

flushCmd
    : FLUSH collections=identifiers (IN DATABASE dbName=identifier)? (WITH propertiesList)?
    | FLUSH ALL TABLES (IN DATABASE database=privilegeScope)? (WITH propertiesList)?
    ;

compactCmd
    : COMPACT TABLE? collectionName=identifier (WITH propertiesList)?
    ;

transferCmd
    : TRANSFER NODES count=(INTEGER | ARG) FROM RESOURCE GROUP sourceGroup=identifier TO RESOURCE GROUP targetGroup=identifier
    | TRANSFER REPLICAS count=(INTEGER | ARG) OF TABLE? collectionName=identifier FROM RESOURCE GROUP sourceGroup=identifier TO RESOURCE GROUP targetGroup=identifier
    ;

selectElements
    : STAR
    | selectElement (COMMA selectElement)*
    ;

selectElement
    : fieldName=identifier
    ;

sortClause
    : fieldName=identifier (ASC | DESC)?
    | fieldName=identifier distanceOperator vectorValue
    ;

vectorValue
    : listLiteral
    | STRING_LITERAL
    | ARG
    ;

hybridClause: HYBRID OPEN_PAREN annClause (COMMA annClause)+ CLOSE_PAREN;
annClause: fieldName=identifier distanceOperator vectorValue (WHERE expression)? LIMIT (limit=INTEGER | limit=ARG) (WITH propertiesList)?;
functionDefinition: FUNCTION name=identifier USING type=identifier OPEN_PAREN inputs=identifiers CLOSE_PAREN
                    INTO OPEN_PAREN outputs=identifiers CLOSE_PAREN (DESCRIPTION description=(STRING_LITERAL | ARG))? (WITH propertiesList)?;

distanceOperator
    : LT_MINUS_GT
    | LT_EQ_GT
    | LT_HASH_GT
    | TILDE_EQ
    | LT_PCT_GT
    | LT_Q_GT
    ;

// Expressions

expression
    : OPEN_PAREN expression CLOSE_PAREN                          # parenExpression
    | expression (STAR | SLASH | MOD) expression                 # binaryExpression
    | expression (PLUS | MINUS) expression                       # binaryExpression
    | expression (GT | LT | GTE | LTE) expression                # comparatorExpression
    | expression (EQ | EQUALS | NE) expression                   # comparatorExpression
    | NOT expression                                             # notExpression
    | expression AND expression                                 # logicalExpression
    | expression OR expression                                  # logicalExpression
    | fieldName=identifier NOT? IN (listLiteral | parenListLiteral | ARG)           # inExpression
    | fieldName=identifier LIKE (pattern=STRING_LITERAL | ARG)   # likeExpression
    | fieldName=identifier NOT? BETWEEN lower=term AND upper=term # betweenExpression
    | fieldName=identifier IS NOT? NULL                         # nullExpression
    | funcName=identifier OPEN_PAREN funcArgs? CLOSE_PAREN       # funcExpression
    | term                                                       # termExpression
    ;

funcArgs
    : expression (COMMA expression)*
    ;

parenListLiteral: OPEN_PAREN literal (COMMA literal)* CLOSE_PAREN;

term
    : ARG
    | identifier
    | literal
    | fieldName=identifier distanceOperator vectorValue  // vectorDistance
    ;

// Options
uriOption: OPT_URI (STRING_LITERAL | IDENTIFIER);
tokenOption: (OPT_T | OPT_TOKEN_LONG) (STRING_LITERAL | IDENTIFIER);
tlsOption: OPT_TLS INTEGER;
certOption: OPT_CERT STRING_LITERAL;
dbOption: OPT_DB (STRING_LITERAL | IDENTIFIER);
userOption: OPT_USER (STRING_LITERAL | IDENTIFIER);
passwordOption: OPT_PASS (STRING_LITERAL | IDENTIFIER);
roleOption: OPT_ROLE (STRING_LITERAL | IDENTIFIER);
collectionOption: OPT_COLLECTION (STRING_LITERAL | IDENTIFIER);
aliasOption: OPT_ALIAS (STRING_LITERAL | IDENTIFIER);
alterOption: OPT_ALTER;
partitionOption: OPT_PARTITION (STRING_LITERAL | IDENTIFIER);
descOption: OPT_DESC (STRING_LITERAL | IDENTIFIER);
objectOption: OPT_OBJ (STRING_LITERAL | IDENTIFIER);
typeOption: (OPT_T | OPT_OBJ_TYPE_LONG) (STRING_LITERAL | IDENTIFIER);
indexNameOption: OPT_INDEX_NAME (STRING_LITERAL | IDENTIFIER);
indexOption: OPT_INDEX (STRING_LITERAL | IDENTIFIER);
timeoutOption: (OPT_T | OPT_TIMEOUT_LONG) INTEGER;
newNameOption: OPT_NEW_NAME (STRING_LITERAL | identifier);
withOptionList: OPEN_PAREN withOption (COMMA withOption)* CLOSE_PAREN;
withOption: identifier EQUALS (STRING_LITERAL | identifier | INTEGER | FLOAT_LITERAL | TRUE | FALSE);

outFieldsOption: OUT_FIELDS (identifiers | STAR);
limitOption: LIMIT INTEGER;
offsetOption: OFFSET INTEGER;
topkOption: TOPK INTEGER;
metricOption: METRIC (STRING_LITERAL | identifier);
paramsOption: PARAMS STRING_LITERAL;
annsFieldOption: ANNS_FIELD identifier;
roundDecimalOption: ROUND_DECIMAL INTEGER;
consistencyLevelOption: CONSISTENCY_LEVEL (STRING_LITERAL | identifier);

fieldDefinition: fieldName=identifier fieldType fieldConstraint* (WITH propertiesList)?;

fieldType
    : BOOL | INT8 | INT16 | INT32 | INT64 | FLOAT | DOUBLE | JSON
    | VARCHAR OPEN_PAREN INTEGER CLOSE_PAREN
    | FLOAT_VECTOR OPEN_PAREN INTEGER CLOSE_PAREN
    | BINARY_VECTOR OPEN_PAREN INTEGER CLOSE_PAREN
    | FLOAT16_VECTOR OPEN_PAREN INTEGER CLOSE_PAREN
    | BFLOAT16_VECTOR OPEN_PAREN INTEGER CLOSE_PAREN
    | INT8_VECTOR OPEN_PAREN INTEGER CLOSE_PAREN
    | SPARSE_FLOAT_VECTOR (OPEN_PAREN INTEGER CLOSE_PAREN)?
    | ARRAY (LT arrayElementType GT OPEN_PAREN capacity=INTEGER CLOSE_PAREN)?
    ;

arrayElementType
    : BOOL | INT8 | INT16 | INT32 | INT64 | FLOAT | DOUBLE
    | VARCHAR OPEN_PAREN INTEGER CLOSE_PAREN
    ;

fieldConstraint
    : PRIMARY KEY
    | PARTITION KEY
    | CLUSTERING KEY
    | NOT NULL
    | NULL
    | DEFAULT ((PLUS | MINUS)? (INTEGER | FLOAT_LITERAL) | STRING_LITERAL | IDENTIFIER | TRUE | FALSE)
    | COMMENT STRING_LITERAL
    | AUTO_ID
    ;

identifiers: identifier (COMMA identifier)*;

identifier
    : IDENTIFIER
    | INT8_VECTOR
    | ALL
    | VECTOR
    | METRIC
    | TOPK
    | PARAMS
    | INDEX
    | PARTITION
    | CLUSTERING
    | HEALTH
    | PERSISTENT
    | SEGMENTS
    | USER
    | ROLE
    | DATABASE
    | DEFAULT
    | ARG
    | ALIAS
    | CONSISTENCY_LEVEL
    | GLOBAL
    | PROGRESS
    | LOADING
    | OF
    | ROUND_DECIMAL
    | ANNS_FIELD
    | OUT_FIELDS
    | SELECT
    | INSERT
    | DELETE
    | TRUNCATE
    | UPDATE
    | CREATE
    | DROP
    | GRANT
    | REVOKE
    | PRIVILEGE
    | ANALYZE
    | LOAD
    | IMPORT
    | RELEASE
    | SEARCH
    | HYBRID
    | FUNCTION
    | DESCRIPTION
    | QUERY
    | FLUSH
    | COMPACT
    | RESOURCE
    | TRANSFER
    | NODES
    | REPLICAS
    | CONFIG
    | GROUP
    | GROUPS
    | BETWEEN
    | REPLACE
    | COMPACTION
    | PLANS
    | ADD
    | COLUMN
    | STATS
    | ALIASES
    ;

literals: literal (COMMA literal)*;
terms: term (COMMA term)*;

literal
    : STRING_LITERAL
    | INTEGER
    | FLOAT_LITERAL
    | listLiteral
    | ARG
    | TRUE
    | FALSE
    | NULL
    | identifier
    ;

listLiteral: OPEN_BRACKET (literal (COMMA literal)*)? CLOSE_BRACKET;

indexAlgo
    : STRING_LITERAL
    | identifier
    ;
