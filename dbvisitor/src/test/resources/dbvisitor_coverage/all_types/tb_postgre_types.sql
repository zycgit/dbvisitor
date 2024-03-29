/* https://www.postgresql.org/docs/13/index.html */
create table tb_postgre_types
(
    c_bigserial                 bigserial primary key,
    c_smallserial               smallserial,
    c_serial                    serial,
    c_serial2                   serial2,
    c_serial4                   serial4,
    c_serial8                   serial8,
    --
    c_smallint                  smallint,
    c_integer                   integer,
    c_int                       int,
    c_bigint                    bigint,
    c_int2                      int2,
    c_int4                      int4,
    c_int8                      int8,
    c_decimal                   decimal,
    c_decimal_p                 decimal(6),
    c_decimal_p_s               decimal(6, 3),
    c_numeric                   numeric,
    c_numeric_p                 numeric(6),
    c_numeric_p_s               numeric(6, 3),
    c_real                      real,
    c_double_precision          double precision,
    c_float                     float,
    c_float4                    float4,
    c_float8                    float8,
    c_float_n                   float(16),
    --
    c_money                     money,
    --
    c_character                 character,
    c_character_n               character(123),
    c_character_varying         character varying,
    c_character_varying_n       character varying(123),
    c_char                      char,
    c_char_n                    char(123),
    c_varchar                   varchar,
    c_varchar_n                 varchar(123),
    c_text                      text,
    --
    c_timestamp                 timestamp,
    c_timestamp_n               timestamp(6),
    c_timestamp_without_tz      timestamp without time zone,
    c_timestamp_n_without_tz    timestamp(6) without time zone,
    c_timestamp_with_tz         timestamp with time zone,
    c_timestamp_n_with_tz       timestamp(6) with time zone,
    c_date                      date,
    c_time                      time,
    c_time_n                    time(6),
    c_time_without_tz           time without time zone,
    c_time_n_without_tz         time(6) without time zone,
    c_time_with_tz              time with time zone,
    c_time_n_with_tz            time(6) with time zone,
    c_interval                  interval,
    c_interval_year             interval year,
    c_interval_month            interval month,
    c_interval_day              interval day,
    c_interval_hour             interval hour,
    c_interval_minute           interval minute,
    c_interval_second           interval second,
    c_interval_year_to_month    interval year to month,
    c_interval_day_to_hour      interval day to hour,
    c_interval_day_to_minute    interval day to minute,
    c_interval_day_to_second    interval day to second,
    c_interval_hour_to_minute   interval hour to minute,
    c_interval_hour_to_second   interval hour to second,
    c_interval_minute_to_second interval minute to second,
    --
    c_boolean                   boolean,
    --
    c_point                     point,
    c_line                      line,
    c_lseg                      lseg,
    c_box                       box,
    c_path                      path,
    c_polygon                   polygon,
    c_circle                    circle,
    --
    c_cidr                      cidr,
    c_inet                      inet,
    c_macaddr                   macaddr,
    c_macaddr8                  macaddr8,
    --
    c_bit                       bit,
    c_bit_12                    bit(12),
    c_bit_1024                  bit(1024),
    c_bit_varying               bit varying,
    c_bit_varying_12            bit varying(12),
    c_bit_varying_1024          bit varying(1024),
    c_bytea                     bytea,
    --
    c_tsvector                  tsvector,
    c_tsquery                   tsquery,
    --
    c_uuid                      uuid,
    --
    c_xml                       xml,
    --
    c_json                      json,
    c_jsonb                     jsonb,
    --
    c_int4range                 int4range,
    c_int8range                 int8range,
    c_numrange                  numrange,
    c_tsrange                   tsrange,
    c_tstzrange                 tstzrange,
    c_daterange                 daterange,
    --
    c_pg_lsn                    pg_lsn,
    c_txid_snapshot             txid_snapshot,
    --
    c_oid                       oid,
    c_name                      name,
    --
    a_smallint                  smallint[],
    a_integer                   integer[],
    a_int                       int[],
    a_bigint                    bigint[],
    a_int2                      int2 [],
    a_int4                      int4 [],
    a_int8                      int8 [],
    a_decimal                   decimal[],
    a_decimal_p                 decimal(6)[],
    a_decimal_p_s               decimal(6, 3)[],
    a_numeric                   numeric[],
    a_numeric_p                 numeric(6)[],
    a_numeric_p_s               numeric(6, 3)[],
    a_real                      real[],
    a_double_precision          double precision[],
    a_float                     float[],
    a_float4                    float4 [],
    a_float8                    float8 [],
    a_float_n                   float(16)[],
    --
    a_money                     money[],
    --
    a_character                 character[],
    a_character_n               character(123)[],
    a_character_varying         character varying[],
    a_character_varying_n       character varying(123)[],
    a_char                      char[],
    a_char_n                    char(123)[],
    a_varchar                   varchar[],
    a_varchar_n                 varchar(123)[],
    a_text                      text[],
    --
    a_timestamp                 timestamp[],
    a_timestamp_n               timestamp(6)[],
    a_timestamp_without_tz      timestamp without time zone [],
    a_timestamp_n_without_tz    timestamp(6) without time zone [],
    a_timestamp_with_tz         timestamp with time zone[],
    a_timestamp_n_with_tz       timestamp(6) with time zone[],
    a_date                      date[],
    a_time                      time[],
    a_time_n                    time(6)[],
    a_time_without_tz           time without time zone [],
    a_time_n_without_tz         time(6) without time zone [],
    a_time_with_tz              time with time zone[],
    a_time_n_with_tz            time(6) with time zone[],
    a_interval                  interval [],
    a_interval_year             interval year [],
    a_interval_month            interval month [],
    a_interval_day              interval day [],
    a_interval_hour             interval hour [],
    a_interval_minute           interval minute [],
    a_interval_second           interval second [],
    a_interval_year_to_month    interval year to month [],
    a_interval_day_to_hour      interval day to hour [],
    a_interval_day_to_minute    interval day to minute [],
    a_interval_day_to_second    interval day to second [],
    a_interval_hour_to_minute   interval hour to minute [],
    a_interval_hour_to_second   interval hour to second [],
    a_interval_minute_to_second interval minute to second [],
    --
    a_boolean                   boolean[],
    --
    a_point                     point[],
    a_line                      line[],
    a_lseg                      lseg[],
    a_box                       box[],
    a_path                      path [],
    a_polygon                   polygon[],
    a_circle                    circle[],
    --
    a_cidr                      cidr[],
    a_inet                      inet[],
    a_macaddr                   macaddr[],
    a_macaddr8                  macaddr8[],
    --
    a_bit                       bit[],
    a_bit_12                    bit(12)[],
    a_bit_1024                  bit(1024)[],
    a_bit_varying               bit varying[],
    a_bit_varying_12            bit varying(12)[],
    a_bit_varying_1024          bit varying(1024)[],
    a_bytea                     bytea[],
    --
    a_tsvector                  tsvector[],
    a_tsquery                   tsquery[],
    --
    a_uuid                      uuid[],
    --
    a_xml                       xml [],
    --
    a_json                      json[],
    a_jsonb                     jsonb[],
    --
    a_int4range                 int4range[],
    a_int8range                 int8range[],
    a_numrange                  numrange[],
    a_tsrange                   tsrange[],
    a_tstzrange                 tstzrange[],
    a_daterange                 daterange[],
    --
    a_pg_lsn                    pg_lsn[],
    a_txid_snapshot             txid_snapshot[],
    --
    a_oid                       oid[],
    a_name                      name []
    -- c_enum example_enum
);

INSERT INTO tb_postgre_types ( c_smallserial
                             , c_serial
                             , c_bigserial
                             , c_serial2
                             , c_serial4
                             , c_serial8
                             , c_smallint
                             , c_integer
                             , c_int
                             , c_bigint
                             , c_int2
                             , c_int4
                             , c_int8
                             , c_decimal
                             , c_decimal_p
                             , c_decimal_p_s
                             , c_numeric
                             , c_numeric_p
                             , c_numeric_p_s
                             , c_real
                             , c_double_precision
                             , c_float
                             , c_float4
                             , c_float8
                             , c_float_n
                             , c_money
                             , c_character
                             , c_character_n
                             , c_character_varying
                             , c_character_varying_n
                             , c_char
                             , c_char_n
                             , c_varchar
                             , c_varchar_n
                             , c_text
                             , c_timestamp
                             , c_timestamp_n
                             , c_timestamp_without_tz
                             , c_timestamp_n_without_tz
                             , c_timestamp_with_tz
                             , c_timestamp_n_with_tz
                             , c_date
                             , c_time
                             , c_time_n
                             , c_time_without_tz
                             , c_time_n_without_tz
                             , c_time_with_tz
                             , c_time_n_with_tz
                             , c_interval
                             , c_interval_year
                             , c_interval_month
                             , c_interval_day
                             , c_interval_hour
                             , c_interval_minute
                             , c_interval_second
                             , c_interval_year_to_month
                             , c_interval_day_to_hour
                             , c_interval_day_to_minute
                             , c_interval_day_to_second
                             , c_interval_hour_to_minute
                             , c_interval_hour_to_second
                             , c_interval_minute_to_second
                             , c_boolean
                             , c_point
                             , c_line
                             , c_lseg
                             , c_box
                             , c_path
                             , c_polygon
                             , c_circle
                             , c_cidr
                             , c_inet
                             , c_macaddr
                             , c_macaddr8
                             , c_bit
                             , c_bit_12
                             , c_bit_1024
                             , c_bit_varying
                             , c_bit_varying_12
                             , c_bit_varying_1024
                             , c_bytea
--                                     , c_tsvector
--                                     , c_tsquery
--                                     , c_uuid
--                                     , c_xml
                             , c_json
                             , c_jsonb
                             , c_int4range
                             , c_int8range
                             , c_numrange
                             , c_tsrange
                             , c_tstzrange
                             , c_daterange
                             , c_pg_lsn
                             , c_txid_snapshot
                             , c_oid
                             , c_name)
VALUES ( 1
       , 1
       , 1
       , 1
       , 1
       , 1
       , 1
       , 1
       , 1
       , 1
       , 1
       , 1
       , 1
       , 1
       , 1
       , 1.000
       , 1
       , 1
       , 1.000
       , 1
       , 1
       , 1
       , 1
       , 1
       , 1
       , '$1.00'
       , '1'
       , '1'
       , '1'
       , '1'
       , '1'
       , '1'
       , '1'
       , '1'
       , '1'
       , '2021-06-02 09:11:34.000000'
       , '2021-06-02 09:11:32.000000'
       , '2021-06-02 09:11:46.000000'
       , '2021-06-02 09:11:48.000000'
       , '2021-06-02 09:11:49.547000'
       , '2021-06-02 09:11:50.731000'
       , '2021-06-02'
       , '09:11:50'
       , '09:11:50'
       , '09:11:50'
       , '09:11:50'
       , '09:11:50.000000'
       , '09:11:50.000000'
       , '0 years 0 mons 0 days 0 hours 0 mins 1.00 secs'
       , '0 years 0 mons 0 days 0 hours 0 mins 0.00 secs'
       , '0 years 0 mons 0 days 0 hours 0 mins 0.00 secs'
       , '0 years 0 mons 0 days 0 hours 0 mins 0.00 secs'
       , '0 years 0 mons 0 days 0 hours 0 mins 0.00 secs'
       , '0 years 0 mons 0 days 0 hours 0 mins 0.00 secs'
       , '0 years 0 mons 0 days 0 hours 0 mins 1.00 secs'
       , '0 years 0 mons 0 days 0 hours 0 mins 0.00 secs'
       , '0 years 0 mons 0 days 0 hours 0 mins 0.00 secs'
       , '0 years 0 mons 0 days 0 hours 0 mins 0.00 secs'
       , '0 years 0 mons 0 days 0 hours 0 mins 1.00 secs'
       , '0 years 0 mons 0 days 0 hours 0 mins 0.00 secs'
       , '0 years 0 mons 0 days 0 hours 0 mins 1.00 secs'
       , '0 years 0 mons 0 days 0 hours 0 mins 1.00 secs'
       , true
       , '(1.0,2.0)'
       , '{1.0,-1.0,1.0}'
       , '[(1.0,2.0),(1.0 ,2.0)]'
       , '(1.1,2.1),(1.0,2.0)'
       , '((1.0,2.0),(1.1,2.1),(1.2,2.2))'
       , '((1.0,2.0),(1.1,2.1),(1.2,2.2))'
       , '<(1.0,2.0),2.0>'
       , '111.0.0.0/8'
       , '192.168.1.1'
       , '42:c1:41:67:fe:23'
       , null
       , '1'
       , '101010101010'
       , '1010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010'
       , '1'
       , '1'
       , '1'
       , '1'
--        , '''1'''
--        , '''1'''
--        , '1b1f430c-1f72-46a3-9539-34b8c5230e60'
--        , '<a></a>'
       , '1'
       , '1'
       , '[4 ,5)'
       , '[4 ,5)'
       , '(3 ,5)'
       , null
       , null
       , null
       , null
       , null
       , null
       , null);