create table if not exists apartmenttype
(
    id         bigint not null
        constraint apartmenttype_pk
            primary key,
    name       varchar(50),
    shortname  varchar(50),
    "desc"     varchar(250),
    updatedate timestamp,
    startdate  timestamp,
    enddate    timestamp,
    isactive   boolean
);