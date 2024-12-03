delete
from images;
delete
from users;
insert into images (coordinates, gcs_full_path, gcs_tagged_path, status, tagger_user_id, validator_user_id)
values ('[]', 'path1', 'tagged_path1', 'PENDING', null, null);
insert into images (coordinates, gcs_full_path, gcs_tagged_path, status, tagger_user_id, validator_user_id)
values ('[]', 'path2', 'tagged_path2', 'PENDING', null, null);
insert into images (coordinates, gcs_full_path, gcs_tagged_path, status, tagger_user_id, validator_user_id)
values ('[]', 'path3', 'tagged_path3', 'PENDING', null, null);
insert into images (coordinates, gcs_full_path, gcs_tagged_path, status, tagger_user_id, validator_user_id)
values ('[]', 'path4', 'tagged_path4', 'PENDING', null, null);
insert into images (coordinates, gcs_full_path, gcs_tagged_path, status, tagger_user_id, validator_user_id)
values ('[]', 'path5', 'tagged_path5', 'PENDING', null, null);

insert into users (name, email, role)
values ('test user', 'test@email.com', 'ADMIN');
insert into users (name, email, role)
values ('test user2', 'test2@email.com', 'USER');
