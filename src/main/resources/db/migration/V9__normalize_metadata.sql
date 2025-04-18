create table cover
(
    id    bigserial primary key,
    ratio double precision not null,
    url   text             not null
);

create table metadata
(
    source_id     text        not null,
    source_type   text        not null,
    info_page_url text        not null,
    movie_id      uuid        not null references movie (id) on delete cascade,
    update_time   timestamptz not null,
    title         text        not null,
    year          integer,
    rating        text,
    cover_id      bigint      references cover (id) on delete set null,

    unique (source_id, source_type),
    unique (movie_id, source_type)
);

create index metadata_update_time on metadata (update_time asc);

with inserted_covers
         as (insert into cover (ratio, url) select cover_ratio as ration, cover_url as url from movie returning id, url )

insert
into metadata (source_id,
               source_type,
               movie_id,
               cover_id,
               info_page_url,
               update_time,
               title,
               year,
               rating)
select imdb_id,
       'IMDB',
       movie.id,
       inserted_covers.id,
       info_page_url,
       metadata_update_time,
       title,
       year,
       rating
from movie
         left join inserted_covers on inserted_covers.url = movie.cover_url;

drop index movie_metadata_update_time_idx;
alter table movie
    drop column cover_url,
    drop column cover_ratio,
    drop column metadata_update_time,
    drop column imdb_id,
    drop column rating,
    drop column title,
    drop column year,
    drop column info_page_url;
