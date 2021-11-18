CREATE TABLE movie
(
    id      uuid               not null primary key,
    imdb_id varchar(64) unique not null,
    rating  varchar(8)         not null,
    title   text               not null,
    year    integer            not null,
    status  varchar(64)        not null
);

CREATE TABLE queue_item
(
    movie_id uuid    not null references movie (id),
    index    integer not null
);
