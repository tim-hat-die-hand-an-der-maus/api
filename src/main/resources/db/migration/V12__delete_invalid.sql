alter table queue_item
  drop constraint queue_item_movie_id_fkey,
  add constraint queue_item_movie_id_fkey foreign key (movie_id) references movie (id) on delete cascade;

delete
from movie
where id in ('ca1f139b-2b1b-4bb5-9904-680cabe1331a',
             'b68f7532-5022-4e83-893d-e8a0cf08d92d',
             'fb0d6a33-3891-4c48-90b5-562afcebe7a8'
  );
