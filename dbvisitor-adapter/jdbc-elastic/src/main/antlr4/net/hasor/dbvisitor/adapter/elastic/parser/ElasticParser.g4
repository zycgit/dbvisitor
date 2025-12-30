parser grammar ElasticParser;

options { tokenVocab=ElasticLexer; }

esCommands  : hintCommand* EOF;
hintCommand : hint* esCmd;

hint        : HintCommentStart hints? HintCommentEnd;
hints       : hintIt ((SEM | COMMA) hintIt)*;
hintIt      : hintName=ID (EQUALS hintVal=hintValue)?;
hintValue   : ID | NUMBER | STRING | TRUE | FALSE | ARG1 | STAR | DOC_KW | CREATE_KW
            | UPDATE_KW | UPDATE_BY_QUERY_KW | DELETE_BY_QUERY_KW | SEARCH_KW | COUNT_KW
            | MSEARCH_KW | MAPPING_KW | SETTINGS_KW | ALIASES_KW | OPEN_KW | CLOSE_KW
            | CAT_KW | MGET_KW | EXPLAIN_KW | SOURCE_KW | REFRESH_KW | REINDEX_KW
            ;

esCmd       : header | mapping | settings | open | close | aliases | cat
            | update | updateQuery | delete | deleteQuery | query
            | refresh | reindex | insert | generic
            ;

// Specific Commands
settings    : (GET | PUT) settingsPath (json)? SEM?;
settingsPath: pathPart? SLASH SETTINGS_KW (ARG1 queryParams)?;

open        : POST openPath (json)? SEM?;
openPath    : pathPart SLASH OPEN_KW (ARG1 queryParams)?;

close       : POST closePath (json)? SEM?;
closePath   : pathPart SLASH CLOSE_KW (ARG1 queryParams)?;

aliases     : (GET | POST) aliasesPath (json)? SEM?;
aliasesPath : pathPart? SLASH ALIASES_KW pathPart* (ARG1 queryParams)?;

cat         : GET catPath SEM?;
catPath     : SLASH CAT_KW pathPart* (ARG1 queryParams)?;

refresh     : (POST | GET) refreshPath (json)? SEM?;
refreshPath : pathPart? SLASH REFRESH_KW (ARG1 queryParams)?;

reindex     : POST reindexPath (json)? SEM?;
reindexPath : SLASH REINDEX_KW (ARG1 queryParams)?;

mapping     : (GET | PUT | POST) mappingPath (json)? SEM?;
mappingPath : pathPart? SLASH MAPPING_KW pathPart* (ARG1 queryParams)?;

// Insert Commands
insert      : (POST | PUT) insertPath (json)? SEM?;
insertPath  : pathPart (pathPart)? SLASH (DOC_KW | CREATE_KW) (pathPart)? (ARG1 queryParams)?
            | pathPart (ARG1 queryParams)?
            ;

// Update Commands
update      : POST updatePath1 (json)? SEM?;
updatePath1 : pathPart (pathPart)? SLASH UPDATE_KW pathPart (ARG1 queryParams)?;

updateQuery : POST updatePath2 (json)? SEM?;
updatePath2 : pathPart? SLASH UPDATE_BY_QUERY_KW (ARG1 queryParams)?;

// Delete Commands
delete      : DELETE deletePath1 (json)? SEM?;
deletePath1 : pathPart (pathPart)? SLASH DOC_KW pathPart (ARG1 queryParams)?
            | pathPart (ARG1 queryParams)?
            ;
deleteQuery : POST deletePath2 (json)? SEM?;
deletePath2 : pathPart? SLASH DELETE_BY_QUERY_KW (ARG1 queryParams)?;

// query Commands
query       : (GET | POST) queryPath (json)? SEM?;
queryPath   : (pathPart (pathPart)?)? SLASH (SEARCH_KW | COUNT_KW | MSEARCH_KW | MGET_KW) (ARG1 queryParams)?
            | pathPart (pathPart)? SLASH (EXPLAIN_KW | SOURCE_KW) pathPart (ARG1 queryParams)?
            ;

// Generic Command (Fallback)
generic     : (GET | POST | PUT | DELETE) path (json)? SEM?;

// other Commands
header      : HEAD path (json)? SEM?;

// basic Commands
path        : pathPart+ (ARG1 queryParams)?;
pathPart    : SLASH (pathValue (COMMA pathValue)*)?;
pathValue   : (ID | STAR | NUMBER | ARG2 | SEARCH_KW | COUNT_KW | MSEARCH_KW | DOC_KW | CREATE_KW | UPDATE_KW | UPDATE_BY_QUERY_KW | DELETE_BY_QUERY_KW | MAPPING_KW | SETTINGS_KW | ALIASES_KW | OPEN_KW | CLOSE_KW | CAT_KW | MGET_KW | EXPLAIN_KW | SOURCE_KW | REFRESH_KW | REINDEX_KW)+;

queryParams: queryParam (AMPERSAND queryParam)*;
queryParam : ID (EQUALS (ID | NUMBER | STRING | TRUE | FALSE | ARG2))?;

json    : object | array | ARG1;
object  : LBRACE (pair (COMMA pair)*)? RBRACE;
pair    : (STRING | ARG1) COLON value;
array   : LBRACK (value (COMMA value)*)? RBRACK;
value   : STRING | NUMBER | object | array | TRUE | FALSE | NULL | ARG1;
