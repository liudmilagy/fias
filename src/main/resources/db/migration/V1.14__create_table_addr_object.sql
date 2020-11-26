create table if not exists addr_object
(
    id         bigint not null
        constraint addr_object_pk
            primary key,
    objectid   bigint,
    objectguid varchar(36),
    changeid   bigint,
    name       varchar(250),
    typename   varchar(50),
    level      varchar(10),
    opertypeid bigint,
    previd     bigint,
    nextid     bigint,
    updatedate timestamp,
    startdate  timestamp,
    enddate    timestamp,
    isactual   smallint,
    isactive   smallint,
    createdate timestamp,
    levelid    bigint
);