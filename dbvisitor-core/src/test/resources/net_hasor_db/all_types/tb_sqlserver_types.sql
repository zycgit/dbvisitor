create table tb_sqlserver_types
(
    f_sys_guid         varchar(36) default newid(),
    c_bit              bit,
    c_decimal          decimal,
    c_decimal_n        decimal(10),
    c_decimal_n_n      decimal(10, 3),
    c_numeric          numeric,
    c_numeric_n        numeric(10),
    c_numeric_n_n      numeric(10, 3),
    c_smallint         smallint,
    c_tinyint          tinyint,
    c_int              int,
    c_bigint           bigint,
    c_smallmoney       smallmoney,
    c_money            money,
    c_float            float,
    c_float_n          float(10),
    c_real             real,

    c_date             date,
    c_datetimeoffset   datetimeoffset,
    c_datetimeoffset_0 datetimeoffset(0),
    c_datetimeoffset_1 datetimeoffset(1),
    c_datetimeoffset_2 datetimeoffset(2),
    c_datetimeoffset_3 datetimeoffset(3),
    c_datetimeoffset_4 datetimeoffset(4),
    c_datetimeoffset_5 datetimeoffset(5),
    c_datetimeoffset_6 datetimeoffset(6),
    c_datetimeoffset_7 datetimeoffset(7),
    c_datetime         datetime,
    c_datetime2_0      datetime2(0),
    c_datetime2_1      datetime2(1),
    c_datetime2_2      datetime2(2),
    c_datetime2_3      datetime2(3),
    c_datetime2_4      datetime2(4),
    c_datetime2_5      datetime2(5),
    c_datetime2_6      datetime2(6),
    c_datetime2_7      datetime2(7),
    c_smalldatetime    smalldatetime,
    c_time             time,
    c_time_0           time(0),
    c_time_1           time(1),
    c_time_2           time(2),
    c_time_3           time(3),
    c_time_4           time(4),
    c_time_5           time(5),
    c_time_6           time(6),
    c_time_7           time(7),

    c_char             char,
    c_char_n           char(12),
    c_varchar          varchar,
    c_varchar_n        varchar(12),
    c_varchar_max      varchar(max),
    c_text             text,
    c_nchar            nchar,
    c_nchar_n          nchar(12),
    c_nvarchar         nvarchar,
    c_nvarchar_n       nvarchar(12),
    c_nvarchar_max     nvarchar(max),
    c_ntext            ntext,

    c_binary           binary,
    c_binary_n         binary(12),
    c_varbinary        varbinary,
    c_varbinary_n      varbinary(12),
    c_varbinary_max    varbinary(max),
    c_image            image,
    c_rowversion       rowversion,
    c_hierarchyid      hierarchyid,
    c_sql_variant      sql_variant,
    c_xml              xml,
    c_geometry         geometry,
    c_geography        geography,
    c_sysname          sysname
);

create table tb_sqlserver_types_ext1
(
    f_sys_guid  varchar(36) default newid(),
    c_timestamp timestamp,
);

create table tb_sqlserver_types_ext2
(
    f_sys_guid         varchar(36) default newid(),
    c_uniqueidentifier uniqueidentifier
);

create table tb_sqlserver_types_default
(
    f_sys_guid         varchar(36)       default newid(),
    c_bit              bit               default 1,
    c_decimal          decimal           default 12.34,
    c_decimal_n        decimal(10)       default 12.34,
    c_decimal_n_n      decimal(10, 3)    default 12.34,
    c_numeric          numeric           default 12.34,
    c_numeric_n        numeric(10)       default 12.34,
    c_numeric_n_n      numeric(10, 3)    default 12.34,
    c_smallint         smallint          default 12.34,
    c_tinyint          tinyint           default 12.34,
    c_int              int               default 12.34,
    c_bigint           bigint            default 12.34,
    c_smallmoney       smallmoney        default 12.34,
    c_money            money             default 12.34,
    c_float            float             default 12.34,
    c_float_n          float(10)         default 12.34,
    c_real             real              default 12.34,

    c_date             date              default '2001-02-03',
    c_datetimeoffset   datetimeoffset    default '2001-02-03 04:05:06.1234567 -00:01',
    c_datetimeoffset_0 datetimeoffset(0) default '2001-02-03 04:05:06 -00:01',
    c_datetimeoffset_1 datetimeoffset(1) default '2001-02-03 04:05:06.1 -00:01',
    c_datetimeoffset_2 datetimeoffset(2) default '2001-02-03 04:05:06.12 -00:01',
    c_datetimeoffset_3 datetimeoffset(3) default '2001-02-03 04:05:06.123 -00:01',
    c_datetimeoffset_4 datetimeoffset(4) default '2001-02-03 04:05:06.1234 -00:01',
    c_datetimeoffset_5 datetimeoffset(5) default '2001-02-03 04:05:06.12345 -00:01',
    c_datetimeoffset_6 datetimeoffset(6) default '2001-02-03 04:05:06.123456 -00:01',
    c_datetimeoffset_7 datetimeoffset(7) default '2001-02-03 04:05:06.1234567 -00:01',
    c_datetime         datetime          default '2001-02-03 04:05:06.123',
    c_datetime2        datetime2         default '2001-02-03 04:05:06.1234567',
    c_datetime2_0      datetime2(0)      default '2001-02-03 04:05:06',
    c_datetime2_1      datetime2(1)      default '2001-02-03 04:05:06.1',
    c_datetime2_2      datetime2(2)      default '2001-02-03 04:05:06.12',
    c_datetime2_3      datetime2(3)      default '2001-02-03 04:05:06.123',
    c_datetime2_4      datetime2(4)      default '2001-02-03 04:05:06.1234',
    c_datetime2_5      datetime2(5)      default '2001-02-03 04:05:06.12345',
    c_datetime2_6      datetime2(6)      default '2001-02-03 04:05:06.123456',
    c_datetime2_7      datetime2(7)      default '2001-02-03 04:05:06.1234567',
    c_smalldatetime    smalldatetime     default '2001-02-03 04:05:06',
    c_time             time              default '04:05:06',
    c_time_0           time(0)           default '04:05:06',
    c_time_1           time(1)           default '04:05:06',
    c_time_2           time(2)           default '04:05:06',
    c_time_3           time(3)           default '04:05:06',
    c_time_4           time(4)           default '04:05:06',
    c_time_5           time(5)           default '04:05:06',
    c_time_6           time(6)           default '04:05:06',
    c_time_7           time(7)           default '04:05:06',

    c_char             char              default 'a',
    c_char_n           char(12)          default 'abcde',
    c_varchar          varchar           default 'a',
    c_varchar_n        varchar(12)       default 'abcde',
    c_varchar_max      varchar(max)      default 'abcde',
    c_text             text              default 'abcde',
    c_nchar            nchar             default 'a',
    c_nchar_n          nchar(12)         default 'abcde',
    c_nvarchar         nvarchar          default 'a',
    c_nvarchar_n       nvarchar(12)      default 'abcde',
    c_nvarchar_max     nvarchar(max)     default 'abcde',
    c_ntext            ntext             default 'abcde',

    c_binary           binary            default 0x03,
    c_binary_n         binary(12)        default 0x4F4F4F4F,
    c_varbinary        varbinary         default 0x03,
    c_varbinary_n      varbinary(12)     default 0x4F4F4F4F,
    c_varbinary_max    varbinary(max)    default 0x4F4F4F4F,
    c_image            image             default 0x4F4F4F4F,
    c_xml              xml               default '<root>abcde</root>'
);

INSERT INTO tb_sqlserver_types_default (f_sys_guid) VALUES ('a');