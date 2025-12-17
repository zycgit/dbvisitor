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
    | ARG2
    ;

esCmd
    : index
    | select
    | insert
    | update
    | delete
    | generic
    ;

index
    : (PUT | GET | HEAD) indexMgmtPath (json)? SEM?
    | POST indexMgmtPath (json)? SEM?
    | POST aliasesPath (json)? SEM?
    ;

indexMgmtPath
    : pathPart* SLASH (MAPPING_KW | SETTINGS_KW | OPEN_KW | CLOSE_KW) (ARG1 queryParams)?
    ;

aliasesPath
    : SLASH ALIASES_KW (ARG1 queryParams)?
    ;

select
    : (GET | HEAD) path (json)? SEM?
    | POST selectPath (json)? SEM?
    ;

selectPath
    : pathPart* SLASH (SEARCH_KW | COUNT_KW | MSEARCH_KW) (ARG1 queryParams)?
    ;

delete
    : DELETE path (json)? SEM?
    | POST deletePath (json)? SEM?
    ;

deletePath
    : pathPart* SLASH DELETE_BY_QUERY_KW (ARG1 queryParams)?
    ;

update
    : POST updatePath (json)? SEM?
    ;

updatePath
    : pathPart* SLASH (UPDATE_KW | UPDATE_BY_QUERY_KW) pathPart* (ARG1 queryParams)?
    ;

insert
    : PUT path (json)? SEM?
    | POST insertPath (json)? SEM?
    ;

insertPath
    : pathPart* SLASH (DOC_KW | CREATE_KW) pathPart* (ARG1 queryParams)?
    ;

generic
    : (OPTIONS | PATCH | POST) path (json)? SEM?
    ;

method
    : GET
    | POST
    | PUT
    | DELETE
    | HEAD
    | OPTIONS
    | PATCH
    ;

path
    : pathPart+ (ARG1 queryParams)?
    ;

pathPart
    : SLASH (ID | STAR | NUMBER | ARG2 | SEARCH_KW | COUNT_KW | MSEARCH_KW | DOC_KW | CREATE_KW | UPDATE_KW | UPDATE_BY_QUERY_KW | DELETE_BY_QUERY_KW | MAPPING_KW | SETTINGS_KW | ALIASES_KW | OPEN_KW | CLOSE_KW)?
    ;

queryParams
    : queryParam (AMPERSAND queryParam)*
    ;

queryParam
    : ID EQUALS (ID | NUMBER | STRING | TRUE | FALSE | ARG2)
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
