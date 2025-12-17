lexer grammar ElasticLexer;

// Whitespace
WHITESPACE : [ \t\r\n]+ -> skip;

// Hint comments (kept for parser)
HintCommentStart  : '/*+';
HintCommentEnd    : '*/';
// Comments
BLOCK_COMMENT : '/*' ~'+' .*? '*/' -> skip;
LINE_COMMENT : '//' ~[\r\n]* -> skip;

// HTTP Methods
GET     : 'GET';
POST    : 'POST';
PUT     : 'PUT';
DELETE  : 'DELETE';
HEAD    : 'HEAD';
OPTIONS : 'OPTIONS';
PATCH   : 'PATCH';

// JSON Literals
TRUE    : 'true';
FALSE   : 'false';
NULL    : 'null';

// Separators
LBRACE  : '{';
RBRACE  : '}';
LBRACK  : '[';
RBRACK  : ']';
COLON   : ':';
COMMA   : ',';
SEM     : ';';

// Path specific chars
SLASH     : '/';
ARG1      : '?';
AMPERSAND : '&';
EQUALS    : '=';
STAR      : '*';
ARG2      : '{?}';

// Keywords
SEARCH_KW : '_search';
COUNT_KW  : '_count';
MSEARCH_KW: '_msearch';
DOC_KW    : '_doc';
CREATE_KW : '_create';
UPDATE_KW : '_update';
UPDATE_BY_QUERY_KW : '_update_by_query';
DELETE_BY_QUERY_KW : '_delete_by_query';

MAPPING_KW : '_mapping';
SETTINGS_KW: '_settings';
ALIASES_KW : '_aliases';
OPEN_KW    : '_open';
CLOSE_KW   : '_close';

// String
STRING    : '"' ( ~["\\] | '\\' . )* '"';

// Number
NUMBER    : '-'? ('0' | [1-9] [0-9]*) ('.' [0-9]+)? ([Ee] [+\-]? [0-9]+)?;

// Identifier
ID        : [a-zA-Z0-9_.\-%]+;

// Catch-all for safety (optional, but good for debugging)
// UNKNOWN : . ;

