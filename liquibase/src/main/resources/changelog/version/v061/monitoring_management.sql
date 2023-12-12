-- TAG MANAGEMENT
-- 1 - ADD TAG
-- 2 - REMOVE TAG
-- 3 - UPDATE TAG
create or replace function tag_manage(optionParam integer default 1, codeParam varchar default '',
                                      descriptionParam varchar default '')
    returns bigint
    security definer
    language plpgsql as
$$
declare
    tag_id bigint;

begin

    if optionParam = 1 then
        insert into tag_type (code, description, physical, juridical)
        values (codeParam, descriptionParam, true, true)
        returning id into tag_id;

        return tag_id;
    end if;

    if optionParam = 2 then
        delete from tag_type where code = codeParam;
        return 2;
    end if;

    if optionParam = 3 then
        update tag_type
        set description = descriptionParam,
            physical    = true,
            juridical   = true
        where code = codeParam;
        return 3;
    end if;

end;
$$;


-- ADD MONITORING RULE
create or replace function add_rule(emailParam varchar, relationTagCode varchar,
                                    isJuridicalParam boolean default true)
    returns bigint
    security definer
    language plpgsql as
$$
declare
    matching_id            bigint;
    tag_row                record;
    rule_id                bigint;
    relation_tag_id        bigint;
    relationTagDescription varchar;
begin

    if isJuridicalParam = true then
        select id into matching_id from notification_juridical_tag_matching where email = emailParam;
        if not found then
            insert into notification_juridical_tag_matching(email)
            values (emailParam)
            returning id into matching_id;
        end if;


        select into relationTagDescription description from tag_type where code = relationTagCode;
        select into relation_tag_id id from tag_type where code = relationTagCode;
        if not found then
            raise notice 'Relation tag  %  not found!', relationTagCode;
            return -1;
        end if;

        for tag_row in select * from tag_type where code not like 'R%'
            loop
                insert into notification_juridical_tag_condition(matching_id, description)
                values (matching_id, relationTagDescription || ' - ' || tag_row.description)
                returning id into rule_id;

                insert into notification_juridical_condition_tag_type (condition_id, tag_type_id)
                values (rule_id, tag_row.id),
                       (rule_id, relation_tag_id);
            end loop;
        return 1;
    else


        select id into matching_id from notification_physical_tag_matching where email = emailParam;
        if not found then
            insert into notification_physical_tag_matching(email)
            values (emailParam)
            returning id into matching_id;
        end if;


        select into relationTagDescription description from tag_type where code = relationTagCode;
        select into relation_tag_id id from tag_type where code = relationTagCode;
        if not found then
            raise notice 'Relation tag  %  not found!', relationTagCode;
            return -1;
        end if;

        for tag_row in select * from tag_type where code not like 'R%'
            loop
                insert into notification_physical_tag_condition(matching_id, description)
                values (matching_id, relationTagDescription || ' - ' || tag_row.description)
                returning id into rule_id;

                insert into notification_physical_condition_tag_type (condition_id, tag_type_id)
                values (rule_id, tag_row.id),
                       (rule_id, relation_tag_id);
            end loop;
        return 1;
    end if;
end;
$$;


-- MAKE MONITORING


create or replace function juridical_monitoring(option integer default 1, eventId bigint default 0) returns varchar
    language plpgsql
    security definer as
$$
declare
    condition_record record;
    result           varchar := 'done';
begin

    if option = 1 then
        for condition_record in select distinct npctt.condition_id from notification_juridical_condition_tag_type npctt
            loop

                insert into ycompany_package_monitoring_notification (ycompany_id, sent, email, condition_id)
                select p.company_id,
                       false                         as sent,
                       nptm.email,
                       condition_record.condition_id as condition_id
                from (SELECT company_id
                      FROM public.yctag
                      WHERE tag_type_id IN (select tag_type_id
                                            from notification_juridical_condition_tag_type npctt
                                            where npctt.condition_id = condition_record.condition_id)
                      GROUP BY company_id
                      HAVING COUNT(DISTINCT tag_type_id) = (select count(*)
                                                            from notification_juridical_condition_tag_type npctt
                                                            where npctt.condition_id = condition_record.condition_id)) p
                         left join notification_juridical_tag_condition nptc on nptc.id = condition_record.condition_id
                         left join notification_juridical_tag_matching nptm on nptc.matching_id = nptm.id;
            end loop;
    end if;

    if option = 2 then

        if eventId > 0 then
            update ycompany_package_monitoring_notification set sent = false where id = eventId;
        else
            update ycompany_package_monitoring_notification set sent = false where 1 = 1;
        end if;

    end if;

    if option = 3 then
        if eventId > 0 then
            update ycompany_package_monitoring_notification set sent = true where id = eventId;
        else
            update ycompany_package_monitoring_notification set sent = true where 1 = 1;
        end if;
    end if;
    if option = 4 then
        if eventId > 0 then
            delete from ycompany_package_monitoring_notification where id = eventId;
        else
            truncate ycompany_package_monitoring_notification restart identity cascade;
        end if;

    end if;


    return result;
end;
$$;





create or replace function physical_monitoring(option integer default 1, eventId bigint default 0) returns varchar
    language plpgsql
    security definer as
$$
declare
    condition_record record;
    result           varchar := 'done';
begin

    if option = 1 then
        for condition_record in select distinct npctt.condition_id from notification_physical_condition_tag_type npctt
            loop

                insert into yperson_package_monitoring_notification (yperson_id, sent, email, condition_id)
                select p.person_id,
                       false                         as sent,
                       nptm.email,
                       condition_record.condition_id as condition_id
                from (SELECT person_id
                      FROM public.ytag
                      WHERE tag_type_id IN (select tag_type_id
                                            from notification_physical_condition_tag_type npctt
                                            where npctt.condition_id = condition_record.condition_id)
                      GROUP BY person_id
                      HAVING COUNT(DISTINCT tag_type_id) = (select count(*)
                                                            from notification_physical_condition_tag_type npctt
                                                            where npctt.condition_id = condition_record.condition_id)) p
                         left join notification_physical_tag_condition nptc on nptc.id = condition_record.condition_id
                         left join notification_physical_tag_matching nptm on nptc.matching_id = nptm.id;
            end loop;
    end if;

    if option = 2 then

        if eventId > 0 then
            update yperson_package_monitoring_notification set sent = false where id = eventId;
        else
            update yperson_package_monitoring_notification set sent = false where 1 = 1;
        end if;

    end if;

    if option = 3 then
        if eventId > 0 then
            update yperson_package_monitoring_notification set sent = true where id = eventId;
        else
            update yperson_package_monitoring_notification set sent = true where 1 = 1;
        end if;
    end if;
    if option = 4 then
        if eventId > 0 then
            delete from yperson_package_monitoring_notification where id = eventId;
        else
            truncate yperson_package_monitoring_notification restart identity cascade;
        end if;

    end if;


    return result;
end;
$$;