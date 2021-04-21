-- DROP SCHEMA anime;

CREATE SCHEMA anime AUTHORIZATION root;

create table anime.tb_anime
(
    id int not null,
    name varchar not null
);

create unique index tb_anime_id_uindex
	on anime.tb_anime (id);

alter table anime.tb_anime
    add constraint anime_pk
        primary key (id);

create sequence anime.tb_anime_id_seq;

alter table anime.tb_anime alter column id set default nextval('anime.tb_anime_id_seq');

alter sequence anime.tb_anime_id_seq owned by anime.tb_anime.id;

