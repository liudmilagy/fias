create table if not exists reestr_object
(
    objectid         bigint not null
        constraint reestr_object_pk
            primary key,
    createdate timestamp,
    changeid   bigint,
    levelid   bigint,
    updatedate timestamp,
    objectguid varchar(36),
    isactive   smallint
);