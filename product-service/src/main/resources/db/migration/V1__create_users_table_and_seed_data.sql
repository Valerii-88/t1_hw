create table if not exists public.users (
    id bigserial primary key,
    username varchar(255) unique
);

insert into public.users (username)
values ('test_user_1'),
       ('test_user_2'),
       ('test_user_3')
on conflict (username) do nothing;
