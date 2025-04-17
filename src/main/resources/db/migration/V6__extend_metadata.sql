alter table movie
    add column metadata_update_time timestamptz not null default '1970-01-01',
    add column info_page_url text default null;
