package me.iclasen.supersearcher.Database;

import java.sql.*;

// Connection handler singleton, allows all the objects to share a single connection
public class ConnectionHandler {
    private static ConnectionHandler instance = null;
    private static Connection connection;

    public Connection getConnection() {
        return connection;
    }

    private final static String CONNECTION_URL = "jdbc:h2:mem:;INIT=RUNSCRIPT FROM 'classpath:setup.sql'\\;";

    private ConnectionHandler() {
        try {
            connection = DriverManager.getConnection(CONNECTION_URL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static ConnectionHandler getInstance() {
        if(instance == null) {
            instance = new ConnectionHandler();
        }
        return instance;
    }

    public static void destroy() throws SQLException {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
