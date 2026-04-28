alter table public.products
    add constraint fk_products_user
        foreign key (user_id) references public.users(id) on delete cascade;
