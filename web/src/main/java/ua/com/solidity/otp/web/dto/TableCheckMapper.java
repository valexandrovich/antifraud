package ua.com.solidity.otp.web.dto;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TableCheckMapper implements RowMapper<TableCheck> {
    @Override
    public TableCheck mapRow(ResultSet resultSet, int i) throws SQLException {
        TableCheck tc = new TableCheck();
        tc.setTableName(resultSet.getString(1));
        tc.setRowsCount(resultSet.getInt(2));
        return tc;
    }
}
