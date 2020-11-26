create table if not exists operationtype
(
    id         bigint not null
        constraint operationtype_pk
            primary key,
    name       varchar(100),
    shortname  varchar(100),
    "desc"     varchar(250),
    updatedate timestamp,
    startdate  timestamp,
    enddate    timestamp,
    isactive   boolean
);