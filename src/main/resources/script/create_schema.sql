-- DROP SCHEMA anime;

CREATE SCHEMA anime AUTHORIZATION root;

create table anime.anime
(
    id int not null,
    name varchar not null
);

create unique index anime_id_uindex
	on anime.anime (id);

alter table anime.anime
    add constraint anime_pk
        primary key (id);

create sequence anime.anime_id_seq;

alter table anime.anime alter column id set default nextval('anime.anime_id_seq');

alter sequence anime.anime_id_seq owned by anime.anime.id;

