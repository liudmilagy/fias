create table if not exists addr_obj_division_item
(
    id       bigint not null
        constraint addr_obj_division_item_pk
            primary key,
    parentid bigint,
    childid  bigint,
    changeid bigint
);
