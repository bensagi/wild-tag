create table users
(
    id       UUID DEFAULT gen_random_uuid() not null,
    name     text,
    email    text,
    password text,
    primary key (id)
);
create unique index users_email_uindex on users (email);