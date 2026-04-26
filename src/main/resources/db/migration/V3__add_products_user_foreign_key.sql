do
$$
begin
    if not exists (
        select 1
        from pg_constraint
        where conname = 'fk_products_user'
          and conrelid = 'public.products'::regclass
    ) then
        alter table public.products
            add constraint fk_products_user
                foreign key (user_id) references public.users(id) on delete cascade;
    end if;
end
$$;
