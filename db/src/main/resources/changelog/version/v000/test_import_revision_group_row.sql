CREATE OR REPLACE FUNCTION public.test_import_revision_group_row(
    source_group bigint,
    digest character varying,
    revision_group uuid)
    RETURNS boolean
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
Declare
    updated_count integer;
BEGIN
    update import_revision_group_rows as r
    set last_revision_group = $3
    where r.source_group = $1 and r.digest = $2;
    GET DIAGNOSTICS updated_count = ROW_COUNT;
    return updated_count > 0;
END;
$BODY$;
