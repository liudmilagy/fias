create table if not exists house
(
    id         bigint not null
        constraint house_pk
            primary key,
    objectid   bigint,
    objectguid varchar(36),
    changeid   bigint,
    housenum   varchar(50),
    addnum1    varchar(50),
    addnum2    varchar(50),
    housetype  bigint,
    addtype1   bigint,
    addtype2   bigint,
    opertypeid bigint,
    previd     bigint,
    nextid     bigint,
    updatedate timestamp,
    startdate  timestamp,
    enddate    timestamp,
    isactual   smallint,
    isactive   smallint
);