alter table movie
  alter column metadata_update_time drop default,
  alter column info_page_url set not null;
