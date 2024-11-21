create table categories
(
    id         UUID DEFAULT gen_random_uuid() not null,
    categories text,
    primary key (id)
);