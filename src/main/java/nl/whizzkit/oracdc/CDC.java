package nl.whizzkit.oracdc;

import nl.whizzkit.oracdc.writer.IWritable;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.Properties;

public class CDC implements IShutdownThread {

    private Connection connection;
    private Logger logger = Logger.getLogger(CDC.class);
    private Statement statement;

    public static void main(String[] args) throws Exception {
        new CDC();
    }

    @Override
    public void shutdown() {
        try {
            statement.cancel();
            CDCUtils.endLogMnr(connection);
            if (connection != null)
                connection.close();
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    private CDC() throws Exception {

        ShutdownThread shutdownThread = new ShutdownThread(this);
        Runtime.getRuntime().addShutdownHook(shutdownThread);

        Properties properties = CDCUtils.readProperties("cdc");

        IWritable iWritable = (IWritable) (Class.forName(properties.getProperty("writer")).getConstructor().newInstance());

        Class.forName(properties.getProperty("driver"));
        connection = DriverManager.getConnection(properties.getProperty("url"), CDCUtils.readProperties("db"));

        CDCUtils.prepareNLS(connection);
        CDCUtils.startLogMnr(connection, properties.getProperty("start"));

        statement = connection.createStatement();
        statement.setFetchSize(1);
        statement.setQueryTimeout(0);

        ResultSet resultSet = statement
                .executeQuery(String.format(
                        "SELECT scn,timestamp,operation,seg_owner,table_name,row_id,sql_redo FROM v$logmnr_contents WHERE table_name in (%s) AND seg_owner = %s AND operation IN (%s)",
                        properties.getProperty("tables"),
                        properties.getProperty("schema"),
                        properties.getProperty("operations")
                        )
                );
        try {
            while (resultSet.next())
                iWritable.write(resultSet);
        } catch (SQLTimeoutException ex) {
            logger.error(ex.getMessage());
        }
    }
}
