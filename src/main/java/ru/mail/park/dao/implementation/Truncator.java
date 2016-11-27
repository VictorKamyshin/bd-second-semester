package ru.mail.park.dao.implementation;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by victor on 23.11.16.
 */
public class Truncator {
    public static void truncByQuery(Connection connection, String query) throws SQLException {
        try (Statement st = connection.createStatement()) { //дропалка и все, что с ней связано
            st.execute(query);
        }
    }
}
