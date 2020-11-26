create table if not exists addressobjecttype
(
    id         bigint not null
        constraint addressobjecttype_pk
            primary key,
    level      bigint,
    shortname  varchar(50),
    name       varchar(250),
    "desc"     varchar(250),
    updatedate timestamp,
    startdate  timestamp,
    enddate    timestamp,
    isactive   boolean
);
