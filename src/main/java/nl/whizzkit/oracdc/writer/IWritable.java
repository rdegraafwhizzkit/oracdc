package nl.whizzkit.oracdc.writer;

import java.sql.ResultSet;

public interface IWritable {
    void write(ResultSet resultSet) throws Exception;
}
