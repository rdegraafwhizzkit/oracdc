package nl.whizzkit.oracdc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

class CDCUtils {

    static void prepareNLS(Connection connection) throws SQLException {
        if (connection != null)
            connection
                    .createStatement()
                    .execute("ALTER SESSION SET NLS_DATE_FORMAT = 'DD-MON-YYYY HH24:MI:SS'");
    }

    static void startLogMnr(Connection connection, String timestamp) throws SQLException {
        if (connection != null)
            connection
                    .prepareCall("begin DBMS_LOGMNR.START_LOGMNR( STARTTIME => '" + timestamp + "', OPTIONS => DBMS_LOGMNR.DICT_FROM_ONLINE_CATALOG + DBMS_LOGMNR.CONTINUOUS_MINE); end;")
                    .execute();
    }

    static void endLogMnr(Connection connection) throws SQLException {
        if (connection != null)
            connection
                    .prepareCall("begin DBMS_LOGMNR.END_LOGMNR; end;")
                    .execute();
    }

    static Properties readProperties(String name) throws IOException {
        Properties properties = new Properties();
        properties.load(Class.class.getResourceAsStream("/" + name + ".properties"));
        return properties;
    }

}
