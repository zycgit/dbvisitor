parser grammar ElasticParser;

options { tokenVocab=ElasticLexer; }

esCommands
    : hintCommand* EOF
    ;

hintCommand
    : hint* esCmd
    ;

hint
    : HintCommentStart hints? HintCommentEnd
    ;

hints
    : hintIt ((SEM | COMMA) hintIt)*
    ;

hintIt
    : hintName=ID (EQUALS hintVal=hintValue)?
    ;

hintValue
    : ID
    | NUMBER
    | STRING
    | TRUE
    | FALSE
    | ARG1
    | STAR
    | DOC_KW
    | CREATE_KW
    | UPDATE_KW
    | UPDATE_BY_QUERY_KW
    | DELETE_BY_QUERY_KW
    | SEARCH_KW
    | COUNT_KW
    | MSEARCH_KW
    | MAPPING_KW
    | SETTINGS_KW
    | ALIASES_KW
    | OPEN_KW
    | CLOSE_KW
    | CAT_KW
    | MGET_KW
    | EXPLAIN_KW
    | SOURCE_KW
    ;

esCmd
    : header
    | search
    | count
    | msearch
    | mapping
    | settings
    | open
    | close
    | aliases
    | doc
    | create
    | queryOne
    | mget
    | explain
    | insert
    | update
    | updateByQuery
    | deleteByQuery
    | delete
    | cat
    | createIndex
    | addDoc
    ;

header
    : HEAD path (json)? SEM?
    ;

delete
    : DELETE deletePath SEM?
    ;

deletePath
    : pathPart+ (ARG1 queryParams)?
    ;

search
    : (GET | POST) searchPath (json)? SEM?
    ;

searchPath
    : pathPart* SLASH SEARCH_KW (ARG1 queryParams)?
    ;

count
    : (GET | POST) countPath (json)? SEM?
    ;

countPath
    : pathPart* SLASH COUNT_KW (ARG1 queryParams)?
    ;

msearch
    : (GET | POST) msearchPath (json)? SEM?
    ;

msearchPath
    : pathPart* SLASH MSEARCH_KW (ARG1 queryParams)?
    ;

mapping
    : (GET | PUT | POST) mappingPath (json)? SEM?
    ;

mappingPath
    : pathPart* SLASH MAPPING_KW (ARG1 queryParams)?
    ;

settings
    : (GET | PUT) settingsPath (json)? SEM?
    ;

settingsPath
    : pathPart* SLASH SETTINGS_KW (ARG1 queryParams)?
    ;

open
    : POST openPath (json)? SEM?
    ;

openPath
    : pathPart* SLASH OPEN_KW (ARG1 queryParams)?
    ;

close
    : POST closePath (json)? SEM?
    ;

closePath
    : pathPart* SLASH CLOSE_KW (ARG1 queryParams)?
    ;

aliases
    : (GET | POST) aliasesPath (json)? SEM?
    ;

aliasesPath
    : SLASH ALIASES_KW (ARG1 queryParams)?
    ;

cat
    : GET catPath SEM?
    ;

catPath
    : SLASH CAT_KW pathPart* (ARG1 queryParams)?
    ;

doc
    : (POST | PUT) docPath (json)? SEM?
    ;

docPath
    : pathPart* SLASH DOC_KW pathPart* (ARG1 queryParams)?
    ;

create
    : (POST | PUT) createPath (json)? SEM?
    ;

createPath
    : pathPart* SLASH CREATE_KW pathPart* (ARG1 queryParams)?
    ;

insert
    : GET path (json)? SEM?
    ;

mget
    : (GET | POST) mgetPath (json)? SEM?
    ;

mgetPath
    : pathPart* SLASH MGET_KW (ARG1 queryParams)?
    ;

explain
    : (GET | POST) explainPath (json)? SEM?
    ;

explainPath
    : pathPart* SLASH EXPLAIN_KW pathPart* (ARG1 queryParams)?
    ;

queryOne
    : GET queryOnePath (json)? SEM?
    ;

queryOnePath
    : pathPart* SLASH SOURCE_KW pathPart* (ARG1 queryParams)?
    ;

update
    : POST updateDocPath (json)? SEM?
    ;

updateDocPath
    : pathPart* SLASH UPDATE_KW pathPart* (ARG1 queryParams)?
    ;

updateByQuery
    : POST updateByQueryPath (json)? SEM?
    ;

updateByQueryPath
    : pathPart* SLASH UPDATE_BY_QUERY_KW (ARG1 queryParams)?
    ;

deleteByQuery
    : POST deleteByQueryPath (json)? SEM?
    ;

deleteByQueryPath
    : pathPart* SLASH DELETE_BY_QUERY_KW (ARG1 queryParams)?
    ;

createIndex
    : PUT path (json)? SEM?
    ;

addDoc
    : POST path (json)? SEM?
    ;

method
    : HEAD
    | OPTIONS
    | PATCH
    ;

path
    : pathPart+ (ARG1 queryParams)?
    ;

pathPart
    : SLASH (pathValue (COMMA pathValue)*)?
    ;

pathValue
    : (ID | STAR | NUMBER | ARG2 | SEARCH_KW | COUNT_KW | MSEARCH_KW | DOC_KW | CREATE_KW | UPDATE_KW | UPDATE_BY_QUERY_KW | DELETE_BY_QUERY_KW | MAPPING_KW | SETTINGS_KW | ALIASES_KW | OPEN_KW | CLOSE_KW | CAT_KW | MGET_KW | EXPLAIN_KW | SOURCE_KW)+
    ;

queryParams
    : queryParam (AMPERSAND queryParam)*
    ;

queryParam
    : ID (EQUALS (ID | NUMBER | STRING | TRUE | FALSE | ARG2))?
    ;

json
    : object
    | array
    | ARG1
    ;

object
    : LBRACE (pair (COMMA pair)*)? RBRACE
    ;

pair
    : (STRING | ARG1) COLON value
    ;

array
    : LBRACK (value (COMMA value)*)? RBRACK
    ;

value
    : STRING
    | NUMBER
    | object
    | array
    | TRUE
    | FALSE
    | NULL
    | ARG1
    ;
