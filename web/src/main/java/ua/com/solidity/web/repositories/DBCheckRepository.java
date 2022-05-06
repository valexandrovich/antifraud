package ua.com.solidity.web.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ua.com.solidity.web.dto.TableCheck;
import ua.com.solidity.web.dto.TableCheckMapper;

import java.util.List;

@Repository
public class DBCheckRepository{

    @Autowired
    JdbcTemplate jdbcTemplate;

    public List<TableCheck> getDbCheck() {
        String sql = "select table_name, (xpath('/row/cnt/text()', xml_count))[1]::text::int as row_count from (select table_name, table_schema,          query_to_xml(format('select count(*) as cnt from %I.%I', table_schema, table_name), false, true, '') as xml_count  from information_schema.tables where table_schema = 'public') t";
        return jdbcTemplate.query(sql, new TableCheckMapper());
    }
}
