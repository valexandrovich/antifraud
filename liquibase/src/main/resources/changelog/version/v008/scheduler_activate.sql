CREATE OR REPLACE FUNCTION public.scheduler_activate(
    group_name character varying)
    RETURNS void
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
begin
    update scheduler s
    set enabled = false
    where s.group_name <> $1;

    update scheduler s
    set enabled = true
    where s.group_name = $1;
end;
$BODY$;