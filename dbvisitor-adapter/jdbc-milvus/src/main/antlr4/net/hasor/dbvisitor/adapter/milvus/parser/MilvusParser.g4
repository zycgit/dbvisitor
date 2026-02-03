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
    | grantCmd
    | revokeCmd
    | importCmd
    | loadCmd
    | selectCmd
    | countCmd
    | releaseCmd
    | renameCmd
    ;

createCmd
    : CREATE DATABASE (IF NOT EXISTS)? dbName=identifier
    | CREATE TABLE (IF NOT EXISTS)? collectionName=identifier OPEN_PAREN fieldDefinition (COMMA fieldDefinition)* CLOSE_PAREN (WITH withOptionList)?
    | CREATE PARTITION (IF NOT EXISTS)? partitionName=identifier ON (TABLE)? collectionName=identifier
    | CREATE INDEX (indexName=identifier)? ON (TABLE)? collectionName=identifier OPEN_PAREN fieldName=identifier CLOSE_PAREN (USING algo=indexAlgo)? (WITH withOptionList)?
    | CREATE ALIAS aliasName=identifier FOR (TABLE)? collectionName=identifier
    | CREATE USER (IF NOT EXISTS)? userName=identifier PASSWORD password=STRING_LITERAL
    | CREATE ROLE (IF NOT EXISTS)? roleName=identifier
    ;

alterCmd
    : ALTER DATABASE dbName=identifier SET PROPERTIES propertiesList
    | ALTER ALIAS aliasName=identifier FOR (TABLE)? collectionName=identifier
    ;

propertiesList: OPEN_PAREN property (COMMA property)* CLOSE_PAREN;

property: (identifier | STRING_LITERAL) EQUALS (STRING_LITERAL | identifier | INTEGER | FLOAT_LITERAL | TRUE | FALSE);

dropCmd
    : DROP DATABASE (IF EXISTS)? dbName=identifier
    | DROP TABLE (IF EXISTS)? collectionName=identifier
    | DROP PARTITION (IF EXISTS)? partitionName=identifier ON (TABLE)? collectionName=identifier
    | DROP INDEX indexName=identifier ON (TABLE)? collectionName=identifier
    | DROP ALIAS (IF EXISTS)? aliasName=identifier
    | DROP USER (IF EXISTS)? userName=identifier
    | DROP ROLE (IF EXISTS)? roleName=identifier
    ;

showCmd
    : SHOW DATABASES
    | SHOW TABLES
    | SHOW TABLE collectionName=identifier
    | SHOW CREATE TABLE collectionName=identifier
    | SHOW PARTITION partitionName=identifier ON (TABLE)? collectionName=identifier
    | SHOW USERS
    | SHOW ROLES
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
    ;

insertCmd
    : INSERT INTO collectionName=identifier (PARTITION partitionName=identifier)? (OPEN_PAREN columnList=identifiers CLOSE_PAREN)? VALUES OPEN_PAREN valueList=terms CLOSE_PAREN
    ;

upsertCmd
    : UPSERT INTO collectionName=identifier (PARTITION partitionName=identifier)? (OPEN_PAREN columnList=identifiers CLOSE_PAREN)? VALUES OPEN_PAREN valueList=terms CLOSE_PAREN
    ;

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
    : SELECT selectElements FROM collectionName=identifier (PARTITION partitionName=identifier)? (WHERE expression)? (ORDER BY sortClause)? (LIMIT (limit=INTEGER | limit=ARG))? (OFFSET (offset=INTEGER | offset=ARG))? (WITH propertiesList)?
    ;

countCmd
    : COUNT FROM collectionName=identifier (PARTITION partitionName=identifier)? (WHERE expression)?
    ;

grantCmd
    : GRANT ROLE roleName=identifier TO userName=identifier                                                             # GrantRoleToUser
    | GRANT privilege=identifier ON objectType=identifier (objectName=identifier | star=STAR) TO ROLE roleName=identifier    # GrantPrivilegeToRole
    ;

revokeCmd
    : REVOKE ROLE roleName=identifier FROM userName=identifier                                                              # RevokeRoleFromUser
    | REVOKE privilege=identifier ON objectType=identifier (objectName=identifier | star=STAR) FROM ROLE roleName=identifier     # RevokePrivilegeFromRole
    ;

importCmd
    : IMPORT FROM (FILE)? fileName=STRING_LITERAL INTO (TABLE)? collectionName=IDENTIFIER (PARTITION partitionName=IDENTIFIER)?
    ;

renameCmd
    : ALTER TABLE collectionName=IDENTIFIER RENAME TO newName=IDENTIFIER
    ;

loadCmd
    : LOAD TABLE collectionName=IDENTIFIER (PARTITION partitionName=IDENTIFIER)?
    ;

releaseCmd
    : RELEASE TABLE collectionName=IDENTIFIER (PARTITION partitionName=IDENTIFIER)?
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
    | ARG
    ;

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
    | NOT expression                                             # notExpression
    | expression (STAR | SLASH | MOD) expression                 # binaryExpression
    | expression (PLUS | MINUS) expression                       # binaryExpression
    | expression (GT | LT | GTE | LTE) expression                # comparatorExpression
    | expression (EQ | EQUALS | NE) expression                   # comparatorExpression
    | expression (AND | OR) expression                           # logicalExpression
    | fieldName=identifier IN (listLiteral | parenListLiteral | ARG)                # inExpression
    | fieldName=identifier LIKE (pattern=STRING_LITERAL | ARG)   # likeExpression
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
withOption: identifier EQUALS (STRING_LITERAL | identifier | INTEGER);

outFieldsOption: OUT_FIELDS (identifiers | STAR);
limitOption: LIMIT INTEGER;
offsetOption: OFFSET INTEGER;
topkOption: TOPK INTEGER;
metricOption: METRIC (STRING_LITERAL | identifier);
paramsOption: PARAMS STRING_LITERAL;
annsFieldOption: ANNS_FIELD identifier;
roundDecimalOption: ROUND_DECIMAL INTEGER;
consistencyLevelOption: CONSISTENCY_LEVEL (STRING_LITERAL | identifier);

fieldDefinition: fieldName=identifier fieldType fieldConstraint*;

fieldType
    : BOOL | INT8 | INT16 | INT32 | INT64 | FLOAT | DOUBLE | JSON
    | VARCHAR OPEN_PAREN INTEGER CLOSE_PAREN
    | FLOAT_VECTOR OPEN_PAREN INTEGER CLOSE_PAREN
    | BINARY_VECTOR OPEN_PAREN INTEGER CLOSE_PAREN
    | FLOAT16_VECTOR OPEN_PAREN INTEGER CLOSE_PAREN
    | BFLOAT16_VECTOR OPEN_PAREN INTEGER CLOSE_PAREN
    | SPARSE_FLOAT_VECTOR OPEN_PAREN INTEGER CLOSE_PAREN
    | ARRAY
    ;

fieldConstraint
    : PRIMARY KEY
    | NOT NULL
    | DEFAULT (STRING_LITERAL | IDENTIFIER | INTEGER)
    | COMMENT STRING_LITERAL
    | AUTO_ID
    ;

identifiers: identifier (COMMA identifier)*;

identifier
    : IDENTIFIER
    | VECTOR
    | METRIC
    | TOPK
    | PARAMS
    | INDEX
    | PARTITION
    | USER
    | ROLE
    | DATABASE
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
    | UPDATE
    | CREATE
    | DROP
    | GRANT
    | REVOKE
    | PRIVILEGE
    | LOAD
    | IMPORT
    | RELEASE
    | SEARCH
    | QUERY
    | FLUSH
    | COMPACT
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
    | identifier
    ;

listLiteral: OPEN_BRACKET literal (COMMA literal)* CLOSE_BRACKET;

indexAlgo
    : STRING_LITERAL
    | identifier
    ;

