create table users (
    id varchar(255) not null,
    name text,
    email text,
    password text,
    primary key (id)
);
create unique index users_email_uindex on users (email);