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

        ResultSet resultSet = statement
                .executeQuery("SELECT sql_redo FROM v$logmnr_contents WHERE table_name in ( " +
                        properties.getProperty("tables") +
                        " ) AND seg_owner = " +
                        properties.getProperty("schema") +
                        " AND operation IN (" +
                        properties.getProperty("operations") +
                        ")"
                );
        try {
            while (resultSet.next()) {
                iWritable.write(resultSet.getString("sql_redo"));
            }
        } catch (SQLTimeoutException ex) {
            logger.error(ex.getMessage());
        }
    }
}
