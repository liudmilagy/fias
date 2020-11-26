create table if not exists ndockind
(
    id   bigint not null
        constraint ndockind_pk
            primary key,
    name varchar(500)
);