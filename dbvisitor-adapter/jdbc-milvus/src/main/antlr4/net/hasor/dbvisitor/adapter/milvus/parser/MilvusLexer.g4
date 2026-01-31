lexer grammar MilvusLexer;

// Hints & Comments
HintCommentStart  : '/*+';
HintCommentEnd    : '*/';
SingleLineComment : '--' ~[\r\n]* -> skip;
MultiLineComment  : '/*' -> pushMode(COMMENT_EXIT);

// Keywords
CLEAR: C L E A R;
CONNECT: C O N N E C T;
COUNT: C O U N T;
CREATE: C R E A T E;
DATABASE: D A T A B A S E;
USER: U S E R;
ROLE: R O L E;
ALIAS: A L I A S;
PARTITION: P A R T I T I O N;
INDEX: I N D E X;
ON: O N;
USING: U S I N G;
WITH: W I T H;
LIST: L I S T;
DATABASES: D A T A B A S E S;
USERS: U S E R S;
ROLES: R O L E S;
GRANTS: G R A N T S;
INDEXES: I N D E X E S;
PARTITIONS: P A R T I T I O N S;
DELETE: D E L E T E;
ENTITIES: E N T I T I E S;
GRANT: G R A N T;
PRIVILEGE: P R I V I L E G E;
REVOKE: R E V O K E;
SHOW: S H O W;
PROGRESS: P R O G R E S S;
LOADING: L O A D I N G;
OF: O F;
EXIT: E X I T;
HELP: H E L P;
INSERT: I N S E R T;
IMPORT: I M P O R T;
FILE: F I L E;
ROW: R O W;
LOAD: L O A D;
QUERY: Q U E R Y;
SELECT: S E L E C T;
ORDER: O R D E R;
BY: B Y;
ASC: A S C;
DESC: D E S C;
RELEASE: R E L E A S E;
SEARCH: S E A R C H;
RENAME: R E N A M E;
VERSION: V E R S I O N;
SET: S E T;
PROPERTIES: P R O P E R T I E S;
GLOBAL: G L O B A L;

PASSWORD: P A S S W O R D;
FOR: F O R;
ALTER: A L T E R;

TABLE: T A B L E;
TABLES: T A B L E S;
IF: I F;
EXISTS: E X I S T S;
NOT: N O T;
NULL: N U L L;
PRIMARY: P R I M A R Y;
KEY: K E Y;
DEFAULT: D E F A U L T;
COMMENT: C O M M E N T;
AUTO_ID: A U T O '_' I D;

// Types
BOOL: B O O L;
INT8: I N T '8';
INT16: I N T '16';
INT32: I N T '32';
INT64: I N T '64';
FLOAT: F L O A T;
DOUBLE: D O U B L E;
VARCHAR: V A R C H A R;
JSON: J S O N;
FLOAT_VECTOR: F L O A T '_' V E C T O R;
BINARY_VECTOR: B I N A R Y '_' V E C T O R;
FLOAT16_VECTOR: F L O A T '16' '_' V E C T O R;
BFLOAT16_VECTOR: B F L O A T '16' '_' V E C T O R;
SPARSE_FLOAT_VECTOR: S P A R S E '_' F L O A T '_' V E C T O R;
ARRAY: A R R A Y;

// Options
OPT_URI: '-uri' | '--uri';
OPT_T: '-t';
OPT_TOKEN_LONG: '--token';
OPT_TLS: '-tls' | '--tlsmode';
OPT_CERT: '-cert' | '--certificate';
OPT_DB: '-db' | '--db_name';
OPT_USER: '-u' | '--username';
OPT_PASS: '--password';
OPT_ROLE: '-r' | '--role_name';
OPT_COLLECTION: '-c' | '--collection_name';
OPT_ALIAS: '-a' | '--alias';
OPT_ALTER: '-A' | '--alter';
OPT_PARTITION: '-p' | '--partition';
OPT_DESC: '-d' | '--partition_description';
OPT_OBJ: '-o' | '--object_name';
OPT_OBJ_TYPE_LONG: '--object_type'; 
OPT_INDEX_NAME: '-in' | '--index_name';
OPT_INDEX: '-i' | '--index';
OPT_TIMEOUT_LONG: '--timeout';
OPT_NEW_NAME: '-n' | '--new_name';

OPEN_PAREN: '(';
CLOSE_PAREN: ')';
COMMA: ',';
EQUALS: '=';
ARG: '?';

SEMI: ';';
STRING_LITERAL
    : '\'' ( ~['\\\r\n] | '\\' . | '\'\'' )* '\''
    | '"'  ( ~["\\\r\n] | '\\' . | '""'   )* '"'
    ;

FLOAT_LITERAL: [0-9]+ '.' [0-9]* ([eE] [+-]? [0-9]+)?
             | '.' [0-9]+ ([eE] [+-]? [0-9]+)?
             | [0-9]+ [eE] [+-]? [0-9]+
             ;
INTEGER: [0-9]+;

OPEN_BRACKET: '[';
CLOSE_BRACKET: ']';



WHERE: W H E R E;
FROM: F R O M;

INTO: I N T O;
VALUES: V A L U E S;
TO: T O;
DROP: D R O P;

AND: A N D | '&&';
OR: O R | '||';
IN: I N;
LIKE: L I K E;
IS: I S;
TRUE: T R U E;
FALSE: F A L S E;
LIMIT: L I M I T;
OFFSET: O F F S E T;
TOPK: T O P K;
METRIC: M E T R I C '_' T Y P E;
PARAMS: P A R A M S;
OUT_FIELDS: O U T '_' F I E L D S;
ROUND_DECIMAL: R O U N D '_' D E C I M A L;
ANNS_FIELD: A N N S '_' F I E L D;
CONSISTENCY_LEVEL: C O N S I S T E N C Y '_' L E V E L;
VECTOR: V E C T O R;

// Operators
GT: '>';
LT: '<';
GTE: '>=';
LTE: '<=';
EQ: '==';
NE: '!=' | '<>';
LT_MINUS_GT: '<->';
PLUS: '+';
MINUS: '-';
STAR: '*';
SLASH: '/';
MOD: '%';
NOT_OP: '!';

IDENTIFIER: [a-zA-Z_] [a-zA-Z0-9_.-]*;

WS: [ \t\r\n]+ -> skip;

fragment A: [aA];
fragment B: [bB];
fragment C: [cC];
fragment D: [dD];
fragment E: [eE];
fragment F: [fF];
fragment G: [gG];
fragment H: [hH];
fragment I: [iI];
fragment J: [jJ];
fragment K: [kK];
fragment L: [lL];
fragment M: [mM];
fragment N: [nN];
fragment O: [oO];
fragment P: [pP];
fragment Q: [qQ];
fragment R: [rR];
fragment S: [sS];
fragment T: [tT];
fragment U: [uU];
fragment V: [vV];
fragment W: [wW];
fragment X: [xX];
fragment Y: [yY];
fragment Z: [zZ];

mode COMMENT_EXIT;
CLOS_TAG : '*/' -> popMode;
ANY      : . -> skip;
