create table if not exists objectlevel
(
    level      integer not null
        constraint objectlevel_pk
            primary key,
    name       varchar(250),
    shortname  varchar(50),
    updatedate timestamp,
    startdate  timestamp,
    enddate    timestamp,
    isactive   boolean
);