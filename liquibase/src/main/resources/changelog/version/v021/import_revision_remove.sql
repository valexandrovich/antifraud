DROP FUNCTION IF EXISTS public.import_revision_remove(uuid);
CREATE OR REPLACE FUNCTION public.import_revision_remove(
    revision uuid)
    RETURNS void
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL SAFE
AS $BODY$
BEGIN
    delete from import_revision_group g where g.revision = $1;
    delete from import_revision r where r.id = $1;
END;
$BODY$;
