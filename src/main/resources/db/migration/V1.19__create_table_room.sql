create table if not exists room
(
    id         bigint not null
        constraint room_pk
            primary key,
    objectid   bigint,
    objectguid varchar(36),
    changeid   bigint,
    number     varchar(50),
    roomtype   varchar(1),
    opertypeid varchar(2),
    previd     bigint,
    nextid     bigint,
    updatedate timestamp,
    startdate  timestamp,
    enddate    timestamp,
    isactual   smallint,
    isactive   smallint
);