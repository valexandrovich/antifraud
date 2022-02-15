CREATE OR REPLACE FUNCTION public.get_table_info(
    catalog character varying,
    schema character varying,
    "table" character varying)
    RETURNS jsonb
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
declare
    res jsonb;
begin
    res := array_to_json(array_agg(row_to_json(data)))
           from
               (select column_name as "name", data_type as "type", udt_name as "type2", CASE WHEN is_nullable = 'YES' THEN true WHEN is_nullable = 'NO' THEN false ELSE null END as "nullable", character_maximum_length as "length"
                from
                    information_schema.columns
                where table_catalog = $1 and table_schema = $2 and table_name = $3
                order by ordinal_position) data;
    return res;
end
$BODY$;