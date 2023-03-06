create table tb_sql92_types
(
    c0  smallint,
    c1  integer,
    c2  float,
    c3  float(7),
    c4  double precision,
    c5  integer,
    c6  decimal,
    c7  decimal(8),
    c8  decimal(8, 3),

    c9  char,
    c10 char(8),
    c11 varchar,
    c12 varchar(8),
    c13 nchar,
    c14 nchar(8),
    c15 NATIONAL CHAR VARYING,
    c16 NATIONAL CHAR VARYING(8),

    c17 date,
    c18 time,
    c19 time(4),
    c20 time with time zone,
    c21 time(4) with time zone,
    c22 TIMESTAMP,
    c23 TIMESTAMP with time zone,
    c24 TIMESTAMP(4) with time zone,

    c25 BIT,
    c26 BIT(5),
    c27 BIT VARYING,
    c28 BIT VARYING(5),
    unique (c1),
    constraint "dddd" unique (c1, c2)
)