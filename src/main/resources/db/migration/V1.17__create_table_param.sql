create table if not exists addr_obj_param
(
    id         bigint not null
        constraint addr_obj_param_pk
            primary key,
    objectid   bigint,
    changeid   bigint,
    typeid     integer,
    value      varchar(8000),
    updatedate timestamp,

    startdate timestamp,
    enddate timestamp,
    changeidend text
);

create table if not exists houses_param
(
    id         bigint not null
        constraint houses_param_pk
            primary key,
    objectid   bigint,
    changeid   bigint,
    typeid     integer,
    value      varchar(8000),
    updatedate timestamp,

    startdate timestamp,
    enddate timestamp,
    changeidend text
);

create table if not exists apartments_param
(
    id         bigint not null
        constraint apartments_param_pk
            primary key,
    objectid   bigint,
    changeid   bigint,
    typeid     integer,
    value      varchar(8000),
    updatedate timestamp,

    startdate timestamp,
    enddate timestamp,
    changeidend text
);

create table if not exists rooms_param
(
    id         bigint not null
        constraint rooms_param_pk
            primary key,
    objectid   bigint,
    changeid   bigint,
    typeid     integer,
    value      varchar(8000),
    updatedate timestamp,

    startdate timestamp,
    enddate timestamp,
    changeidend text
);

create table if not exists steads_param
(
    id         bigint not null
        constraint steads_param_pk
            primary key,
    objectid   bigint,
    changeid   bigint,
    typeid     integer,
    value      varchar(8000),
    updatedate timestamp,

    startdate timestamp,
    enddate timestamp,
    changeidend text
);

create table if not exists carplaces_param
(
    id         bigint not null
        constraint carplaces_param_pk
            primary key,
    objectid   bigint,
    changeid   bigint,
    typeid     integer,
    value      varchar(8000),
    updatedate timestamp,

    startdate timestamp,
    enddate timestamp,
    changeidend text
);