create table movie
(
    id      uuid    not null primary key,
    imdb_id varchar(255) unique,
    rating  varchar(255),
    title   varchar(255),
    year    integer not null,
    status  integer
);

create table queue_item
(
    movie_id uuid    not null references movie (id),
    index    integer not null
);
