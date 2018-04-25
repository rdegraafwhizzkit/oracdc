package nl.whizzkit.oracdc.writer;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ConsoleWriter implements IWritable {
    @Override
    public void write(ResultSet resultSet) throws SQLException {
        System.out.println(resultSet.getString("sql_redo"));
    }
}
