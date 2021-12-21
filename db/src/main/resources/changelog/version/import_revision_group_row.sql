CREATE OR REPLACE FUNCTION public.insert_import_revision_group_row(
    id uuid,
    revision_group uuid,
    source_group bigint,
    data jsonb,
    mask bigint)
    RETURNS bigint
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
begin
    if (not exists(select * from import_revision_group_rows x where x.source_group = $3 and x.data = $4)) then
        insert into import_revision_group_rows(id, revision_group, source_group, data, handled)
        values($1, $2, $3, $4, false);
        return ($5 << 1) + 1;
    end if;
    return $5 << 1;
end;
$BODY$;

ALTER FUNCTION public.insert_import_revision_group_row(uuid, uuid, bigint, jsonb, bigint)
    OWNER TO postgres;
