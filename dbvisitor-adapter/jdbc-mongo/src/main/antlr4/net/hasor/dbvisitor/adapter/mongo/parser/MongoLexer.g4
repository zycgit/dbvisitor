lexer grammar MongoLexer;

// Comments
SingleLineComment : '//' ~[\r\n\u2028\u2029]* -> skip;
MultiLineComment  : '/*' .*? '*/'             -> skip;

// Whitespace
WHITESPACE     : [ \t] -> skip;
LineTerminator : [\r\n\u2028\u2029] -> skip;

// Keywords
DB      : 'db';
USE     : 'use';
SHOW    : 'show';
FIND    : 'find';
INSERT  : 'insert';
UPDATE  : 'update';
REMOVE  : 'remove';
COUNT   : 'count';
LIMIT   : 'limit';
K_SKIP  : 'skip';
SORT    : 'sort';
CREATE_COLLECTION : 'createCollection';
DROP_DATABASE : 'dropDatabase';
GET_COLLECTION_NAMES : 'getCollectionNames';
GET_COLLECTION_INFOS : 'getCollectionInfos';
RUN_COMMAND : 'runCommand';
AGGREGATE : 'aggregate';
DISTINCT : 'distinct';
FIND_ONE : 'findOne';
REPLACE_ONE : 'replaceOne';
DELETE_ONE : 'deleteOne';
DELETE_MANY : 'deleteMany';
INSERT_ONE : 'insertOne';
INSERT_MANY : 'insertMany';
UPDATE_ONE : 'updateOne';
UPDATE_MANY : 'updateMany';
BULK_WRITE : 'bulkWrite';
CREATE_INDEX : 'createIndex';
DROP_INDEX : 'dropIndex';
CREATE_USER : 'createUser';
DROP_USER : 'dropUser';
UPDATE_USER : 'updateUser';
GRANT_ROLES_TO_USER : 'grantRolesToUser';
REVOKE_ROLES_FROM_USER : 'revokeRolesFromUser';
CHANGE_USER_PASSWORD : 'changeUserPassword';
SERVER_STATUS : 'serverStatus';
VERSION : 'version';
GET_INDEXES : 'getIndexes';
STATS : 'stats';
CREATE_VIEW : 'createView';
RENAME_COLLECTION : 'renameCollection';
EXPLAIN : 'explain';
HINT : 'hint';
DROP    : 'drop';
NULL    : 'null';
TRUE    : 'true';
FALSE   : 'false';

// Comparison
EQ : '$eq';
GT : '$gt';
GTE : '$gte';
IN : '$in';
LT : '$lt';
LTE : '$lte';
NE : '$ne';
NIN : '$nin';

// Logical
AND : '$and';
NOT : '$not';
NOR : '$nor';
OR : '$or';

// Element
EXISTS : '$exists';
TYPE : '$type';

// Evaluation
EXPR : '$expr';
JSON_SCHEMA : '$jsonSchema';
MOD : '$mod';
REGEX : '$regex';
TEXT : '$text';
WHERE : '$where';

// Array
ALL : '$all';
ELEM_MATCH : '$elemMatch';
SIZE : '$size';

// Geospatial
GEO_INTERSECTS : '$geoIntersects';
GEO_WITHIN : '$geoWithin';
NEAR : '$near';
NEAR_SPHERE : '$nearSphere';

// Update
CURRENT_DATE : '$currentDate';
INC : '$inc';
MIN : '$min';
MAX : '$max';
MUL : '$mul';
RENAME : '$rename';
SET : '$set';
SET_ON_INSERT : '$setOnInsert';
UNSET : '$unset';

// Aggregation Stages
MATCH : '$match';
GROUP : '$group';
PROJECT : '$project';
DOLLAR_SORT : '$sort';
DOLLAR_LIMIT : '$limit';
DOLLAR_SKIP : '$skip';
DOLLAR_COUNT : '$count';
UNWIND : '$unwind';
LOOKUP : '$lookup';
OUT : '$out';
ADD_FIELDS : '$addFields';
REPLACE_ROOT : '$replaceRoot';
SAMPLE : '$sample';
SUM : '$sum';

// BSON Types
OBJECT_ID : 'ObjectId';
ISO_DATE : 'ISODate';
NUMBER_INT : 'NumberInt';
NUMBER_LONG : 'NumberLong';
NUMBER_DECIMAL : 'NumberDecimal';
TIMESTAMP : 'Timestamp';
BIN_DATA : 'BinData';
UUID : 'UUID';
MIN_KEY : 'MinKey';
MAX_KEY : 'MaxKey';
DB_REF : 'DBRef';
CODE : 'Code';
NEW : 'new';
DATE : 'Date';
REG_EXP : 'RegExp';

// Symbols
OPEN_PARENTHESIS    : '(';
CLOSED_PARENTHESIS  : ')';
OPEN_BRACE          : '{';
CLOSED_BRACE        : '}';
OPEN_BRACKET        : '[';
CLOSED_BRACKET      : ']';
COMMA               : ',';
COLON               : ':';
SEMICOLON           : ';';
DOT                 : '.';
ARG                 : '?';

// Literals
DOUBLE_QUOTED_STRING_LITERAL: '"' ((~["\\]) | STRING_ESCAPE)* '"';
SINGLE_QUOTED_STRING_LITERAL: '\'' ((~['\\]) | STRING_ESCAPE)* '\'';

// Numeric
NUMERIC_LITERAL : '-'? DecimalLiteral;

fragment DecimalLiteral
	: DecimalIntegerLiteral '.' DecimalDigit* ExponentPart?
	| '.' DecimalDigit+ ExponentPart?
	| DecimalIntegerLiteral ExponentPart?
	;

fragment DecimalIntegerLiteral
	: '0'
	| [1-9] DecimalDigit*
	;

fragment ExponentPart
	: [eE] [+-]? DecimalDigit+
	;

fragment DecimalDigit
	: [0-9]
	;

// Generic String (Identifier-like)
// Must be after keywords and specific literals
STRING_LITERAL: ((~[",\\ \t\n\r:.;(){}[\]\-?]) | STRING_ESCAPE )+;

fragment STRING_ESCAPE: '\\' [\\"'];

