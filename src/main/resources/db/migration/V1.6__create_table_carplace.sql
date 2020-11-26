create table if not exists carplace
(
    id         bigint not null
        constraint carplace_pk
            primary key,
    objectid   bigint,
    objectguid varchar(36),
    changeid   bigint,
    number     varchar(50),
    opertypeid varchar(2),
    previd     bigint,
    nextid     bigint,
    updatedate timestamp,
    startdate  timestamp,
    enddate    timestamp,
    isactual   smallint,
    isactive   smallint
);