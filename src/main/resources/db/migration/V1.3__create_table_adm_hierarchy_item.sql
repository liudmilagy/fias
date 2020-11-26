create table if not exists adm_hierarchy_item
(
    id          bigint not null
        constraint adm_hierarchy_item_pk
            primary key,
    objectid    bigint,
    parentobjid bigint,
    changeid    bigint,
    regioncode  varchar(4),
    areacode    varchar(4),
    citycode    varchar(4),
    placecode   varchar(4),
    plancode    varchar(4),
    streetcode  varchar(4),
    previd      bigint,
    nextid      bigint,
    updatedate  timestamp,
    startdate   timestamp,
    enddate     timestamp,
    isactive    smallint
);