create table if not exists param
(
    id         bigint not null
        constraint param_pk
            primary key,
    objectid   bigint,
    changeid   bigint,
    typeid     integer,
    value      varchar(8000),
    updatedate timestamp
);