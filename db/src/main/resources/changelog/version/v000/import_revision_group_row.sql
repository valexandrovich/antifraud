CREATE OR REPLACE FUNCTION public.insert_import_revision_group_row(
    id uuid,
    revision_group uuid,
    source_group bigint,
    digest character varying,
    data jsonb,
    mask bigint)
    RETURNS bigint
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
DECLARE handled_count boolean;
begin
    handled_count := test_import_revision_group_row($3, $4, $2);
    if (not handled_count) then
        insert into import_revision_group_rows(id, revision_group, last_revision_group, source_group, digest, data, handled)
        values($1, $2, $2, $3, $4, $5, false);
        return ($6 << 1) | 1;
    end if;
    return $6 << 1;
end;
$BODY$;
