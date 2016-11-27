package ru.mail.park.dao.implementation;

import ru.mail.park.dao.BaseDao;
import ru.mail.park.response.Response;
import ru.mail.park.response.ResponseStatus;

import javax.sql.DataSource;
import java.sql.Connection;
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
            new Response(ResponseStatus.UNKNOWN_ERROR);
        }
    }

    @Override
    public long getCount(){
        long count = 0;

        return count;
    }

    protected Response handeSQLException(SQLException e) {
        if (e.getErrorCode() == ALREADY_EXIST) {
            return new Response(ResponseStatus.ALREADY_EXIST); //не совсем так, по идее, надо отвечать тем же объектом
        } else {
            e.printStackTrace();
            return new Response(ResponseStatus.UNKNOWN_ERROR);
        }
    }
}
