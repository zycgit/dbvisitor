parser grammar RedisParser;

options {
    superClass = JedisBaseParser;
    tokenVocab = RedisLexer;
}

root
    : commands? EOF
    ;

// We can omit newline only if it's the last statement
commands
    : command NEWLINE*
    | command NEWLINE+ commands
    ;

command
    : serverCommands
    | keysCommands
    | stringCommands
    | listCommands
    | setCommands
    | sortedSetCommands
    | hashCommands
    ;

serverCommands
    : moveCommand
    | waitCommand
    | waitaofCommand
    | pingCommand
    | echoCommand
    | selectCommand
    ;

keysCommands
    : copyCommand
    | deleteCommand
    | unlinkCommand
    | dumpCommand
    | existsCommand
    | expireCommand
    | expireAtCommand
    | expireTimeCommand
    | pExpireCommand
    | pExpireAtCommand
    | pExpireTimeCommand
    | keysCommand
    | objectCommand
    | persistCommand
    | ttlCommand
    | pTtlCommand
    | randomKeyCommand
    | renameCommand
    | renameNxCommand
    | scanCommand
    | touchCommand
    | typeCommand
    ;

stringCommands
    : strSetCommand
    | getCommand
    | incrementCommand
    | incrementByCommand
    | decrementCommand
    | decrementByCommand
    | appendCommand
    | getDeleteCommand
    | getExCommand
    | getRangeCommand
    | getSetCommand
    | mGetCommand
    | mSetCommand
    | mSetNxCommand
    | pSetExCommand
    | setExCommand
    | setNxCommand
    | setRangeCommand
    | stringLengthCommand
    | substringCommand
    ;

listCommands
    : lmoveCommand
    | blmoveCommand
    | lmpopCommand
    | blmpopCommand
    | lpopCommand
    | rpopCommand
    | blpopCommand
    | brpopCommand
    | rpopLpushCommand
    | brpopLpushCommand
    | lindexCommand
    | linsertCommand
    | llenCommand
    | lposCommand
    | lpushCommand
    | lpushxCommand
    | rpushCommand
    | rpushxCommand
    | lrangeCommand
    | lremCommand
    | lsetCommand
    | ltrimCommand
    ;

setCommands
    : saddCommand
    | scardCommand
    | sdiffCommand
    | sdiffstoreCommand
    | sinterCommand
    | sintercardCommand
    | sinterstoreCommand
    | sismemberCommand
    | smismemberCommand
    | smembersCommand
    | smoveCommand
    | spopCommand
    | srandmemberCommand
    | sremCommand
    | sscanComman
    | sunionCommand
    | sunionstoreCommand
    ;

sortedSetCommands
    : zmpopCommand
    | bzmpopCommand
    | zpopmaxCommand
    | bzpopmaxCommand
    | zpopminCommand
    | bzpopminCommand
    | zaddCommand
    | zcardCommand
    | zcountCommand
    | zdiffCommand
    | zdiffstoreCommand
    | zincrbyCommand
    | zinterCommand
    | zintercardCommand
    | zinterstoreCommand
    | zlexcountCommand
    | zscoreCommand
    | zmscoreCommand
    | zrandmemberCommand
    | zrangeCommand
    | zrangebylexCommand
    | zrangebyscoreCommand
    | zrangestoreCommand
    | zrankCommand
    | zrevrankCommand
    | zremCommand
    | zremrangebylexCommand
    | zremrangebyrankCommand
    | zremrangebyscoreCommand
    | zrevrangeCommand
    | zrevrangebylexCommand
    | zrevrangebyscoreCommand
    | zscanCommand
    | zunionCommand
    | zunionstoreCommand
    ;

hashCommands
    : hdelCommand
    | hexistsCommand
    | hexpireCommand
    | hexpireAtCommand
    | hexpireTimeCommand
    | hpexpireCommand
    | hpexpireAtCommand
    | hpexpireTimeCommand
    | hgetCommand
    | hgetAllCommand
    | hincrByCommand
    | hkeysCommand
    | hlenCommand
    | hmgetCommand
    | hsetCommand
    | hsetnxCommand
    | hmsetCommand
    | hpersistCommand
    | httlCommand
    | hpttlCommand
    | hrandfieldCommand
    | hscanCommand
    | hstrlenCommand
    | hvalsCommand
    ;

hdelCommand
    : HDEL hashKeyName identifier+
    ;

hexistsCommand
    : HEXISTS hashKeyName identifier
    ;

hexpireCommand
    : HEXPIRE hashKeyName decimal expireOptions? fieldsClause
    ;

hpexpireCommand
    : HPEXPIRE hashKeyName decimal expireOptions? fieldsClause
    ;

fieldsClause
    : FIELDS integer identifier+
    ;

hexpireAtCommand
    : HEXPIREAT hashKeyName decimal expireOptions? fieldsClause
    ;

hpexpireAtCommand
    : HPEXPIREAT hashKeyName decimal expireOptions? fieldsClause
    ;

hexpireTimeCommand
    : HEXPIRETIME hashKeyName fieldsClause
    ;

hpexpireTimeCommand
    : HPEXPIRETIME hashKeyName fieldsClause
    ;

hgetCommand
    : HGET hashKeyName identifier
    ;

hmgetCommand
    : HMGET hashKeyName identifier+
    ;

hgetAllCommand
    : HGETALL hashKeyName
    ;

hincrByCommand
    : HINCRBY hashKeyName identifier decimal
    ;

hkeysCommand
    : HKEYS hashKeyName
    ;

hlenCommand
    : HLEN hashKeyName
    ;

hsetCommand
    : HSET hashKeyName filedValueClause+
    ;

hmsetCommand
    : HMSET hashKeyName filedValueClause+
    ;

hsetnxCommand
    : HSETNX hashKeyName filedValueClause
    ;

filedValueClause
    : field=identifier value=identifier
    ;

hpersistCommand
    : HPERSIST hashKeyName fieldsClause
    ;

httlCommand
    : HTTL hashKeyName fieldsClause
    ;

hpttlCommand
    : HPTTL hashKeyName fieldsClause
    ;

hrandfieldCommand
    : HRANDFIELD hashKeyName (decimal WITHVALUES?)?
    ;

hscanCommand
    : HSCAN hashKeyName decimal matchClause? countClause? NOVALUES?
    ;

hstrlenCommand
    : HSTRLEN hashKeyName identifier
    ;

hvalsCommand
    : HVALS hashKeyName
    ;

zmpopCommand
    : ZMPOP integer sortedSetKeyName+ minMaxClause countClause?
    ;

bzmpopCommand
    : BZMPOP timeout=integer number=integer sortedSetKeyName+ minMaxClause countClause?
    ;

zpopmaxCommand
    : ZPOPMAX sortedSetKeyName integer?
    ;

bzpopmaxCommand
    : BZPOPMAX sortedSetKeyName+ integer
    ;

zpopminCommand
    : ZPOPMIN sortedSetKeyName integer?
    ;

bzpopminCommand
    : BZPOPMIN sortedSetKeyName+ integer
    ;

minMaxClause
    : MIN
    | MAX
    ;

zaddCommand
    : ZADD sortedSetKeyName keyExistenceClause? keyUpdateClause? CH? INCR? scoreMemberClause+
    ;

keyUpdateClause
    : GT
    | LT
    ;

scoreMemberClause
    : decimal identifier
    ;

zcardCommand
    : ZCARD sortedSetKeyName
    ;

zcountCommand
    : ZCOUNT sortedSetKeyName min=decimalScore max=decimalScore
    ;

zdiffCommand
    : ZDIFF integer sortedSetKeyName+ WITHSCORES?
    ;

zdiffstoreCommand
    : ZDIFFSTORE identifier integer sortedSetKeyName+
    ;

zincrbyCommand
    : ZINCRBY sortedSetKeyName decimal identifier
    ;

zinterCommand
    : ZINTER integer sortedSetKeyName+ weightsClause? aggregateClause? WITHSCORES?
    ;

zintercardCommand
    : ZINTERCARD integer sortedSetKeyName+ limitClause?
    ;

zinterstoreCommand
    : ZINTERSTORE identifier integer sortedSetKeyName+ weightsClause? aggregateClause?
    ;

weightsClause
    : WEIGHTS decimal+
    ;

aggregateClause
    : AGGREGATE (MIN | MAX | SUM)
    ;

zlexcountCommand
    : ZLEXCOUNT sortedSetKeyName min=lexicalScore max=lexicalScore
    ;

zscoreCommand
    : ZSCORE sortedSetKeyName identifier
    ;

zmscoreCommand
    : ZMSCORE sortedSetKeyName identifier+
    ;

zrandmemberCommand
    : ZRANDMEMBER sortedSetKeyName (decimal WITHSCORES?)?
    ;

zrangeCommand
    : ZRANGE sortedSetKeyName begin=lexicalScore end=lexicalScore rangeTypeClause? REV? limitOffsetClause? WITHSCORES?
    ;

zrangebylexCommand
    : ZRANGEBYLEX sortedSetKeyName min=lexicalScore max=lexicalScore limitOffsetClause?
    ;

zrangebyscoreCommand
    : ZRANGEBYSCORE sortedSetKeyName min=decimalScore max=decimalScore WITHSCORES? limitOffsetClause?
    ;

zrangestoreCommand
    : ZRANGESTORE identifier sortedSetKeyName min=lexicalScore max=lexicalScore rangeTypeClause? REV? limitOffsetClause?
    ;

rangeTypeClause
    : BYSCORE
    | BYLEX
    ;

limitOffsetClause
    : LIMIT offset=decimal count=decimal
    ;

zrankCommand
    : ZRANK sortedSetKeyName identifier WITHSCORE?
    ;

zrevrankCommand
    : ZREVRANK sortedSetKeyName identifier WITHSCORE?
    ;

zremCommand
    : ZREM sortedSetKeyName identifier+
    ;

zremrangebylexCommand
    : ZREMRANGEBYLEX sortedSetKeyName min=lexicalScore max=lexicalScore
    ;

zremrangebyrankCommand
    : ZREMRANGEBYRANK sortedSetKeyName begin=decimal end=decimal
    ;

zremrangebyscoreCommand
    : ZREMRANGEBYSCORE sortedSetKeyName min=decimalScore max=decimalScore
    ;

zrevrangeCommand
    : ZREVRANGE sortedSetKeyName begin=decimal end=decimal WITHSCORES?
    ;

zrevrangebylexCommand
    : ZREVRANGEBYLEX sortedSetKeyName max=lexicalScore min=lexicalScore limitOffsetClause?
    ;

zrevrangebyscoreCommand
    : ZREVRANGEBYSCORE sortedSetKeyName max=decimalScore min=decimalScore WITHSCORES? limitOffsetClause?
    ;

zscanCommand
    : ZSCAN sortedSetKeyName decimal matchClause? countClause?
    ;

zunionCommand
    : ZUNION integer sortedSetKeyName+ weightsClause? aggregateClause? WITHSCORES?
    ;

zunionstoreCommand
    : ZUNIONSTORE identifier integer sortedSetKeyName+ weightsClause? aggregateClause?
    ;

saddCommand
    : SADD setKeyName identifier+
    ;

scardCommand
    : SCARD setKeyName
    ;

sdiffCommand
    : SDIFF setKeyName+
    ;

sdiffstoreCommand
    : SDIFFSTORE identifier setKeyName+
    ;

sinterCommand
    : SINTER setKeyName+
    ;

sintercardCommand
    : SINTERCARD integer setKeyName+ limitClause?
    ;

limitClause
    : LIMIT integer
    ;

sinterstoreCommand
    : SINTERSTORE identifier setKeyName+
    ;

sismemberCommand
    : SISMEMBER setKeyName identifier
    ;

smismemberCommand
    : SMISMEMBER setKeyName identifier+
    ;

smembersCommand
    : SMEMBERS setKeyName
    ;

smoveCommand
    : SMOVE src=setKeyName dst=setKeyName member=setKeyName
    ;

spopCommand
    : SPOP setKeyName integer?
    ;

srandmemberCommand
    : SRANDMEMBER setKeyName decimal?
    ;

sremCommand
    : SREM setKeyName identifier+
    ;

sscanComman
    : SSCAN setKeyName decimal matchClause? countClause?
    ;

sunionCommand
    : SUNION setKeyName+
    ;

sunionstoreCommand
    : SUNIONSTORE identifier setKeyName+
    ;

lmoveCommand
    : LMOVE src=listKeyName dst=listKeyName from=leftOrRightClause to=leftOrRightClause
    ;

leftOrRightClause
    : LEFT
    | RIGHT
    ;

blmoveCommand
    : BLMOVE src=listKeyName dst=listKeyName from=leftOrRightClause to=leftOrRightClause integer
    ;

lmpopCommand
    : LMPOP integer listKeyName+ leftOrRightClause countClause?
    ;

blmpopCommand
    : BLMPOP timeout=integer numkeys=integer listKeyName+ leftOrRightClause countClause?
    ;

lpopCommand
    : LPOP listKeyName integer?
    ;

rpopCommand
    : RPOP listKeyName integer?
    ;

blpopCommand
    : BLPOP listKeyName+ integer
    ;

brpopCommand
    : BRPOP listKeyName+ integer
    ;

rpopLpushCommand
    : RPOPLPUSH src=listKeyName dst=listKeyName
    ;

brpopLpushCommand
    : BRPOPLPUSH src=listKeyName dst=listKeyName integer
    ;

lindexCommand
    : LINDEX listKeyName decimal
    ;

linsertCommand
    : LINSERT listKeyName beforeOrAfterClause pivot=identifier ele=identifier
    ;

beforeOrAfterClause
    : BEFORE
    | AFTER
    ;

llenCommand
    : LLEN listKeyName
    ;

lposCommand
    : LPOS listKeyName identifier rankClause? countClause? maxLenClause?
    ;

rankClause
    : RANK decimal
    ;

maxLenClause
    : MAXLEN integer
    ;

lpushCommand
    : LPUSH listKeyName identifier+
    ;

lpushxCommand
    : LPUSHX listKeyName identifier+
    ;

rpushCommand
    : RPUSH listKeyName identifier+
    ;

rpushxCommand
    : RPUSHX listKeyName identifier+
    ;

lrangeCommand
    : LRANGE listKeyName begin=decimal end=decimal
    ;

lremCommand
    : LREM listKeyName decimal identifier
    ;

lsetCommand
    : LSET listKeyName decimal identifier
    ;

ltrimCommand
    : LTRIM listKeyName begin=decimal end=decimal
    ;

copyCommand
    : COPY keyName identifier dbClause? REPLACE?
    ;

dbClause
    : DB databaseName
    ;

databaseName
    : integer
    ;

deleteCommand
    : DEL keyName+
    ;

unlinkCommand
    : UNLINK keyName+
    ;

dumpCommand
    : DUMP keyName
    ;

existsCommand
    : EXISTS keyName+
    ;

expireCommand
    : EXPIRE keyName decimal expireOptions?
    ;

expireAtCommand
    : EXPIREAT keyName decimal expireOptions?
    ;

pExpireCommand
    : PEXPIRE keyName decimal expireOptions?
    ;

pExpireAtCommand
    : PEXPIREAT keyName decimal expireOptions?
    ;

expireOptions
    : NX
    | XX
    | GT
    | LT
    ;

expireTimeCommand
    : EXPIRETIME keyName
    ;

pExpireTimeCommand
    : PEXPIRETIME keyName
    ;

keysCommand
    : KEYS keyPattern
    ;

moveCommand
    : MOVE keyName databaseName
    ;

objectCommand
    : OBJECT objectOptions keyName
    ;

objectOptions
    : ENCODING
    | FREQ
    | IDLETIME
    | REFCOUNT
    ;

persistCommand
    : PERSIST keyName
    ;

ttlCommand
    : TTL keyName
    ;

pTtlCommand
    : PTTL keyName
    ;

randomKeyCommand
    : RANDOMKEY
    ;

renameCommand
    : RENAME keyName identifier
    ;

renameNxCommand
    : RENAMENX keyName identifier
    ;

scanCommand
    : SCAN decimal matchClause? countClause? typeClause?
    ;

matchClause
    : MATCH keyPattern
    ;

countClause
    : COUNT integer
    ;

typeClause
    : TYPE identifier
    ;

touchCommand
    : TOUCH keyName+
    ;

typeCommand
    : TYPE keyName
    ;

waitCommand
    : WAIT replicas=integer timeout=integer
    ;

waitaofCommand
    : WAITAOF numlocal=integer replicas=integer timeout=integer
    ;

pingCommand
    : PING stringKeyName?
    ;

echoCommand
    : ECHO stringKeyName?
    ;

selectCommand
    : SELECT integer
    ;

strSetCommand
    : SET stringKeyName identifier keyExistenceClause? GET? (expirationClause | KEEPTTL)?
    ;

keyExistenceClause
    : NX
    | XX
    ;

expirationClause
    : EX integer
    | PX integer
    | EXAT integer
    | PXAT integer
    ;

getCommand
    : GET stringKeyName
    ;

incrementCommand
    : INCR stringKeyName
    ;

incrementByCommand
    : INCRBY stringKeyName decimal
    ;

decrementCommand
    : DECR stringKeyName
    ;

decrementByCommand
    : DECRBY stringKeyName decimal
    ;

appendCommand
    : APPEND stringKeyName identifier
    ;

getDeleteCommand
    : GETDEL stringKeyName
    ;

getExCommand
    : GETEX stringKeyName (expirationClause | PERSIST)?
    ;

getRangeCommand
    : GETRANGE stringKeyName start=decimal end=decimal
    ;

getSetCommand
    : GETSET stringKeyName identifier
    ;

mGetCommand
    : MGET stringKeyName+
    ;

mSetCommand
    : MSET keyValueClause+
    ;

keyValueClause
    : stringKeyName identifier
    ;

mSetNxCommand
    : MSETNX keyValueClause+
    ;

pSetExCommand
    : PSETEX stringKeyName integer identifier
    ;

setExCommand
    : SETEX stringKeyName integer identifier
    ;

setNxCommand
    : SETNX stringKeyName identifier
    ;

setRangeCommand
    : SETRANGE stringKeyName integer identifier
    ;

stringLengthCommand
    : STRLEN stringKeyName
    ;

substringCommand
    : SUBSTR stringKeyName start=decimal end=decimal
    ;

decimal
    : ARG | INTEGER_NUM | FLOAT_NUM;

integer
    : ARG | INTEGER_NUM;

decimalScore
    : ARG | INTEGER_NUM | FLOAT_NUM | DECIMAL_SCORE;

identifier
    : ARG | INTEGER_NUM | FLOAT_NUM | DECIMAL_SCORE | IDENTIFIER;

lexicalScore
    : identifier
    ;

stringKeyName
    : identifier
    ;

listKeyName
    : identifier
    ;

setKeyName
    : identifier
    ;

sortedSetKeyName
    : identifier
    ;

hashKeyName
    : identifier
    ;

keyName
    : identifier
    ;

keyPattern
    : identifier
    ;