create table anime.tb_user
(
    id serial not null,
    name varchar(100) not null,
    add username varchar(100) not null,
    password varchar(150) not null,
    authoriries varchar(150) not null
);

create unique index tb_user_id_uindex
	on anime.tb_user (id);

alter table anime.tb_user
    add constraint user_pk
        primary key (id);

