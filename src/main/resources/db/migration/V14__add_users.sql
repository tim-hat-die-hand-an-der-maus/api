create table canonical_user
(
  id           uuid primary key not null,
  display_name text             not null
);

create table telegram_user
(
  id           bigint primary key not null,
  canonical_id uuid unique        not null references canonical_user (id),
  first_name   text               not null,
  last_name    text
);
