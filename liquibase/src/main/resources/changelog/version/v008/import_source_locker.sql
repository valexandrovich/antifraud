CREATE OR REPLACE FUNCTION public.import_source_locker(
    source_name character varying,
    lock_state boolean)
    RETURNS boolean
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
declare
    import_source_id bigint;
Begin
    select id into import_source_id from import_source where name = $1;
    if (import_source_id is null) then
        return false;
    end if;
    return import_source_locker_by_id(import_source_id, $2);
End;
$BODY$;
