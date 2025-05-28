alter table queue_item
  add column user_id uuid references canonical_user (id) default null;
