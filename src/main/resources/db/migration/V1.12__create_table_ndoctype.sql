create table if not exists ndoctype
(
    id        bigint not null
        constraint ndoctype_pk
            primary key,
    name      varchar(500),
    startdate timestamp,
    enddate   timestamp
);