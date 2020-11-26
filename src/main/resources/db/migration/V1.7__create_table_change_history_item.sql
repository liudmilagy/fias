create table if not exists change_history_item
(
    changeid    bigint not null
        constraint change_history_item_pk
            primary key,
    objectid    bigint,
    adrobjectid varchar(36),
    opertypeid  bigint,
    ndocid      bigint,
    changedate  timestamp
);