create table if not exists stead
(
    id         bigint not null
        constraint stead_pk
            primary key,
    objectid   bigint,
    objectguid varchar(36),
    changeid   bigint,
    number     varchar(250),
    opertypeid varchar(2),
    previd     bigint,
    nextid     bigint,
    updatedate timestamp,
    startdate  timestamp,
    enddate    timestamp,
    isactual   smallint,
    isactive   smallint
);