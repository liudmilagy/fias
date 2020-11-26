create table if not exists mun_hierarchy_item
(
    id          bigint not null
        constraint mun_hierarchy_item_pk
            primary key,
    objectid    bigint,
    parentobjid bigint,
    changeid    bigint,
    oktmo       varchar(11),
    previd      bigint,
    nextid      bigint,
    updatedate  timestamp,
    startdate   timestamp,
    enddate     timestamp,
    isactive    smallint
);