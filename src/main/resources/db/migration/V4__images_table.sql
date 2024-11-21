create table images
(
    id                UUID DEFAULT gen_random_uuid() not null,
    gcs_full_path     text,
    status            text,
    tagger_user_id    uuid default null,
    validator_user_id uuid default null,
    coordinates       text default '[]',
    gcs_tagged_path   text default null,
    primary key (id)
);
alter table images
    add constraint fk_tagger_user foreign key (tagger_user_id) references users;
alter table images
    add constraint fk_validator_user foreign key (validator_user_id) references users;