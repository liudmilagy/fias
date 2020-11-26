create table if not exists paramtype
(
    id         bigint not null
        constraint paramtype_pk
            primary key,
    name       varchar(50),
    code       varchar(50),
    "desc"     varchar(120),
    updatedate timestamp,
    startdate  timestamp,
    enddate    timestamp,
    isactive   boolean
);