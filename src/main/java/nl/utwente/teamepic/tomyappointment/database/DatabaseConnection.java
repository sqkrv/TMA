package nl.utwente.teamepic.tomyappointment.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static Connection connection;

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        if (connection == null || connection.isClosed()) {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://";

            Properties properties = new Properties();
            properties.setProperty("user", "");
            properties.setProperty("password", "");

            connection = DriverManager.getConnection(url, properties);
        }

        return connection;
    }
}
