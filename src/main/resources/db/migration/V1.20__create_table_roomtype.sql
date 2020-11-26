create table if not exists roomtype
(
    id         bigint not null
        constraint roomtype_pk
            primary key,
    name       varchar(100),
    shortname  varchar(50),
    "desc"     varchar(250),
    updatedate timestamp,
    startdate  timestamp,
    enddate    timestamp,
    isactive   boolean
);