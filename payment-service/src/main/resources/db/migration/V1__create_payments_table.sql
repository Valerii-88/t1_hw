create table if not exists public.payments (
    id bigserial primary key,
    user_id bigint not null,
    product_id bigint not null,
    amount numeric(19, 2) not null check (amount > 0),
    description varchar(255) not null,
    created_at timestamp not null
);
