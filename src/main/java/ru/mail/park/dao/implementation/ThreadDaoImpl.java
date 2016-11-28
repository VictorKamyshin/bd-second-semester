package ru.mail.park.dao.implementation;

import com.google.gson.JsonParser;
import ru.mail.park.dao.ThreadDao;
import ru.mail.park.model.Thread;
import ru.mail.park.response.Response;
import ru.mail.park.response.ResponseStatus;

import javax.sql.DataSource;
import java.sql.*;

/**
 * Created by victor on 23.11.16.
 */
public class ThreadDaoImpl extends  BaseDaoImpl implements ThreadDao {
    private String subscriptionsName;
    public ThreadDaoImpl(DataSource dataSource) {
        this.tableName = Thread.TABLE_NAME;
        this.subscriptionsName = Thread.SUBSCRIPTION_TABLE_NAME;
        this.ds = dataSource;
    }

    @Override
    public Response create(String threadCreateJson){
        final Thread thread;
        try (Connection connection = ds.getConnection()) {
            thread = new Thread(new JsonParser().parse(threadCreateJson).getAsJsonObject());
            final StringBuilder builder = new StringBuilder("INSERT INTO ");
            builder.append(tableName);
            builder.append("(user_email, forum_short_name, title, isClosed," +
                    " date, message, slug, getIsDeleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            final String query = builder.toString();
            try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, thread.getUserEmail());
                ps.setString(2, thread.getForumShortName());
                ps.setString(3, thread.getTitle());
                ps.setBoolean(4, thread.getClosed());
                ps.setString(5,thread.getDate());
                ps.setString(6, thread.getMessage());
                ps.setString(7, thread.getSlug());
                ps.setBoolean(8, thread.getDeleted());
                ps.executeUpdate();
                try (ResultSet resultSet = ps.getGeneratedKeys()) {
                    resultSet.next();
                    thread.setId(resultSet.getLong(1));
                }
            } catch (SQLException e) {
                return handeSQLException(e);
            }
        } catch(SQLException e){
            e.printStackTrace();
            return new Response(ResponseStatus.INVALID_REQUEST); //если произошла ошибка при парсе джсона
        }
        return new Response(ResponseStatus.OK, thread); //в ответе будут лишние поля, но тесты от этого не сломаются
    }
}
