package nl.whizzkit.oracdc.writer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ConsoleWriter implements IWritable {
    @Override
    public void write(ResultSet resultSet) throws SQLException {
        System.out.println(new JsonResultSet(resultSet).toString());
    }
}

class JsonResultSet {

    private static Gson gson = new GsonBuilder().create();

    @SuppressWarnings({"unused"})
    private String scn, timestamp, operation, seg_owner, table_name, row_id, sql_redo;

    JsonResultSet(ResultSet resultSet) throws SQLException {
        this.scn = resultSet.getString("scn");
        this.timestamp = resultSet.getString("timestamp");
        this.operation = resultSet.getString("operation");
        this.seg_owner = resultSet.getString("seg_owner");
        this.table_name = resultSet.getString("table_name");
        this.row_id = resultSet.getString("row_id");
        this.sql_redo = resultSet.getString("sql_redo");
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}