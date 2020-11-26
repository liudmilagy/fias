create table if not exists housetype
(
    id         bigint not null
        constraint housetype_pk
            primary key,
    name       varchar(50),
    shortname  varchar(50),
    "desc"     varchar(250),
    updatedate timestamp,
    startdate  timestamp,
    enddate    timestamp,
    isactive   boolean
);