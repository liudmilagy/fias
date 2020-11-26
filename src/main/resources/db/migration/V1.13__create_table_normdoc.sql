create table if not exists normdoc
(
    id         bigint not null
        constraint normdoc_pk
            primary key,
    name       varchar(8000),
    date       timestamp,
    number     varchar(150),
    type       bigint,
    kind       bigint,
    updatedate timestamp,
    orgname    varchar(255),
    regnum     varchar(100),
    regdate    timestamp,
    accdate    timestamp,
    comment    varchar(8000)
);