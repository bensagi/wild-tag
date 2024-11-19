create table images (
    id varchar(255) not null,
    gcs_full_path text,
    status text,
    tagger_user_id text default 'unassigned',
    validator_user_id text default 'unassigned',
    coordinates text default '[]',
    gcs_tagged_path text default null,
    primary key (id)
);
alter table images add constraint fk_tagger_user foreign key (tagger_user_id) references users;
alter table images add constraint fk_validator_user foreign key (validator_user_id) references users;