create table cover_tmp
(
    metadata_source_id   text             not null,
    metadata_source_type text             not null,
    url                  text             not null,
    ratio                double precision not null,

    unique (metadata_source_id, metadata_source_type),
    foreign key (metadata_source_id, metadata_source_type) references metadata (source_id, source_type) on delete cascade
);


insert into cover_tmp (metadata_source_id, metadata_source_type, url, ratio)
select m.source_id, m.source_type, c.url, c.ratio
from cover c
         join metadata m on m.cover_id = c.id;

alter table metadata
    drop column cover_id;
drop table cover;
alter table cover_tmp
    rename to cover;
