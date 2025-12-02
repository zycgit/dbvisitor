parser grammar MongoParser;

options { tokenVocab=MongoLexer; }

mongoCommands
	: commands EOF
	;

commands: (command | SEMICOLON)+;

command
	: DB DOT collection DOT mongoOp SEMICOLON?
    | DB DOT dbOp SEMICOLON?
	| USE databaseName SEMICOLON?
	| SHOW showTarget SEMICOLON?
	;

dbOp
    : createCollectionOp
    | dropDatabaseOp
    | getCollectionNamesOp
    | getCollectionInfosOp
    | runCommandOp
    | createUserOp
    | dropUserOp
    | updateUserOp
    | grantRolesToUserOp
    | revokeRolesFromUserOp
    | changeUserPasswordOp
    | serverStatusOp
    | versionOp
    | statsOp
    | createViewOp
    ;

createCollectionOp
    : CREATE_COLLECTION arguments
    ;

createViewOp
    : CREATE_VIEW arguments
    ;

dropDatabaseOp
    : DROP_DATABASE arguments
    ;

getCollectionNamesOp
    : GET_COLLECTION_NAMES arguments
    ;

getCollectionInfosOp
    : GET_COLLECTION_INFOS arguments
    ;

runCommandOp
    : RUN_COMMAND arguments
    ;

createUserOp
    : CREATE_USER arguments
    ;

dropUserOp
    : DROP_USER arguments
    ;

updateUserOp
    : UPDATE_USER arguments
    ;

grantRolesToUserOp
    : GRANT_ROLES_TO_USER arguments
    ;

revokeRolesFromUserOp
    : REVOKE_ROLES_FROM_USER arguments
    ;

changeUserPasswordOp
    : CHANGE_USER_PASSWORD arguments
    ;

serverStatusOp
    : SERVER_STATUS arguments
    ;

versionOp
    : VERSION arguments
    ;

statsOp
    : STATS arguments
    ;

mongoOp
	: findOp
	| insertOp
	| updateOp
	| removeOp
	| countOp
    | dropOp
    | aggregateOp
    | distinctOp
    | findOneOp
    | replaceOneOp
    | deleteOneOp
    | deleteManyOp
    | insertOneOp
    | insertManyOp
    | updateOneOp
    | updateManyOp
    | bulkWriteOp
    | createIndexOp
    | dropIndexOp
    | getIndexesOp
    | renameCollectionOp
    | statsOp
	;

dropOp
    : DROP arguments
    ;

aggregateOp : AGGREGATE arguments ;
distinctOp : DISTINCT arguments ;
findOneOp : FIND_ONE arguments ;
replaceOneOp : REPLACE_ONE arguments ;
deleteOneOp : DELETE_ONE arguments ;
deleteManyOp : DELETE_MANY arguments ;
insertOneOp : INSERT_ONE arguments ;
insertManyOp : INSERT_MANY arguments ;
updateOneOp : UPDATE_ONE arguments ;
updateManyOp : UPDATE_MANY arguments ;
bulkWriteOp : BULK_WRITE arguments ;
createIndexOp : CREATE_INDEX arguments ;
dropIndexOp : DROP_INDEX arguments ;
getIndexesOp : GET_INDEXES arguments ;
renameCollectionOp : RENAME_COLLECTION arguments ;

findOp
	: FIND arguments (DOT methodCall)*
	;

insertOp
	: INSERT arguments
	;

updateOp
	: UPDATE arguments
	;

removeOp
	: REMOVE arguments
	;

countOp
	: COUNT arguments
	;

methodCall
	: (LIMIT arguments)     #limitOp
	| (K_SKIP arguments)    #skipOp
	| (SORT arguments)      #sortOp
    | (EXPLAIN arguments)   #explainOp
    | (HINT arguments)      #hintOp
	;

databaseName
	: identifier | DOUBLE_QUOTED_STRING_LITERAL | SINGLE_QUOTED_STRING_LITERAL
	;

showTarget
	: identifier // dbs, collections, tables, etc.
	;

functionCall
	: identifier arguments
	;

arguments
	: OPEN_PARENTHESIS argumentList? CLOSED_PARENTHESIS
	;

argumentList
	: propertyValue ( COMMA propertyValue )*
	;

objectLiteral
	: OPEN_BRACE propertyNameAndValueList? COMMA? CLOSED_BRACE
	;

arrayLiteral
	: OPEN_BRACKET elementList? CLOSED_BRACKET
	;

elementList
	: propertyValue ( COMMA propertyValue )*
	;

propertyNameAndValueList
	: propertyAssignment ( COMMA propertyAssignment )*
	;

propertyAssignment
	: propertyName COLON propertyValue
	;

propertyValue
	: literal
	| objectLiteral
	| arrayLiteral
	| functionCall
    | bsonLiteral
	;

bsonLiteral
    : OBJECT_ID arguments
    | ISO_DATE arguments
    | NUMBER_INT arguments
    | NUMBER_LONG arguments
    | NUMBER_DECIMAL arguments
    | TIMESTAMP arguments
    | BIN_DATA arguments
    | UUID arguments
    | MIN_KEY arguments
    | MAX_KEY arguments
    | DB_REF arguments
    | CODE arguments
    | NEW DATE arguments
    | NEW REG_EXP arguments
    ;

literal
	: ARG | NULL | TRUE | FALSE | DOUBLE_QUOTED_STRING_LITERAL | SINGLE_QUOTED_STRING_LITERAL | NUMERIC_LITERAL
	;

collection
	: ARG | identifier;

propertyName
	: identifier | DOUBLE_QUOTED_STRING_LITERAL | SINGLE_QUOTED_STRING_LITERAL
	;

identifier
	: STRING_LITERAL
	| FIND | INSERT | UPDATE | REMOVE | COUNT | LIMIT | K_SKIP | SORT
	| USE | SHOW | DB
    | CREATE_COLLECTION | DROP_DATABASE | DROP
    | GET_COLLECTION_NAMES | GET_COLLECTION_INFOS | RUN_COMMAND
    | AGGREGATE | DISTINCT | FIND_ONE | REPLACE_ONE
    | DELETE_ONE | DELETE_MANY | INSERT_ONE | INSERT_MANY
    | UPDATE_ONE | UPDATE_MANY | BULK_WRITE
    | CREATE_INDEX | DROP_INDEX
    | EQ | GT | GTE | IN | LT | LTE | NE | NIN
    | AND | NOT | NOR | OR
    | EXISTS | TYPE
    | EXPR | JSON_SCHEMA | MOD | REGEX | TEXT | WHERE
    | ALL | ELEM_MATCH | SIZE
    | GEO_INTERSECTS | GEO_WITHIN | NEAR | NEAR_SPHERE
    | CURRENT_DATE | INC | MIN | MAX | MUL | RENAME | SET | SET_ON_INSERT | UNSET
    | MATCH | GROUP | PROJECT | DOLLAR_SORT | DOLLAR_LIMIT | DOLLAR_SKIP | DOLLAR_COUNT
    | UNWIND | LOOKUP | OUT | ADD_FIELDS | REPLACE_ROOT | SAMPLE | SUM
    | OBJECT_ID | ISO_DATE | NUMBER_INT | NUMBER_LONG | NUMBER_DECIMAL
    | TIMESTAMP | BIN_DATA | UUID | MIN_KEY | MAX_KEY | DB_REF | CODE
    | NEW | DATE | REG_EXP
    | CREATE_VIEW | RENAME_COLLECTION | EXPLAIN | HINT
    | CREATE_USER | DROP_USER | UPDATE_USER | GRANT_ROLES_TO_USER | REVOKE_ROLES_FROM_USER | CHANGE_USER_PASSWORD
    | SERVER_STATUS | VERSION | GET_INDEXES | STATS
	;