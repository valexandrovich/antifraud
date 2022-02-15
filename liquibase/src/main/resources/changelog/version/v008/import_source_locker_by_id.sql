CREATE OR REPLACE FUNCTION public.import_source_locker_by_id(
    import_source_id bigint,
    lock_state boolean)
    RETURNS boolean
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
declare
    session_start timestamp with time zone;
    session_pid integer;
    updated_count integer;
Begin
    session_pid := pg_backend_pid();
    select backend_start into session_start from pg_stat_activity where pid = session_pid;

    if ($2) then
        if not exists(
                select *
                from
                    import_source_session s
                        inner join
                    pg_stat_activity a
                    on a.backend_start = s.backend_start and a.pid = s.pid
                where s.source = $1
            ) then
            if exists(select * from import_source_session s where s.source = import_source_id) then
                update import_source_session s
                set
                    backend_start = session_start,
                    pid = session_pid
                where s.source = $1;
            else
                insert into import_source_session(source, pid, backend_start)
                values($1, session_pid, session_start);
            end if;
        else
            return false;
        end if;
    else
        update import_source_session s
        set
            pid = null,
            backend_start = null
        where
                s.source = $1 and s.pid = session_pid and
                s.backend_start = session_start;
        GET DIAGNOSTICS updated_count = ROW_COUNT;
        return updated_count > 0;
    end if;
    return true;
End;
$BODY$;