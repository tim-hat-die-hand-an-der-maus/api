create table movie
(
    id      uuid    not null primary key,
    imdb_id varchar(255) unique not null,
    rating  varchar(255) not null,
    title   varchar(255) not null,
    year    integer not null,
    status  integer not null
);

create table queue_item
(
    movie_id uuid    not null references movie (id),
    index    integer not null
);
