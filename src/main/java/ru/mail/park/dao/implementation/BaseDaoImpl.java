package ru.mail.park.dao.implementation;

import ru.mail.park.dao.BaseDao;
import ru.mail.park.response.Response;
import ru.mail.park.response.ResponseStatus;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by victor on 22.11.16.
 */
public abstract class BaseDaoImpl implements BaseDao {
    public static final int ALREADY_EXIST = 1062;
    protected String tableName = "";
    protected DataSource ds;

    @Override
    public void truncateTable() {
        try (Connection connection = ds.getConnection()) { //дропалку для юзеров и тредов напишем отдельно
            Truncator.truncByQuery(connection, "SET FOREIGN_KEY_CHECKS = 0;");
            Truncator.truncByQuery(connection, "TRUNCATE TABLE " + tableName);
            Truncator.truncByQuery(connection, "SET FOREIGN_KEY_CHECKS = 1;");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Long getCount(){
        Long count;
        try (Connection connection = ds.getConnection()) {
            final StringBuilder usersCount = new StringBuilder("SELECT COUNT(*) FROM ");
            usersCount.append(tableName);
            try (PreparedStatement ps = connection.prepareStatement(usersCount.toString())) {
                try (ResultSet resultSet = ps.executeQuery()) {
                    resultSet.next();
                    count  = resultSet.getLong(1);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

        } catch (SQLException e){
            return null;
        }
        return count;
    }

    protected Response handeSQLException(SQLException e) {
        if (e.getErrorCode() == ALREADY_EXIST) {
            return new Response(ResponseStatus.ALREADY_EXIST); //не совсем так, по идее, надо отвечать тем же объектом
        } else {
            //e.printStackTrace();
            return new Response(ResponseStatus.NOT_FOUND);
        }
    }
}
