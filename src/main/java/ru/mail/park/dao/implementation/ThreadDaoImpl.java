package ru.mail.park.dao.implementation;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import ru.mail.park.dao.ThreadDao;
import ru.mail.park.model.Thread;
import ru.mail.park.response.Response;
import ru.mail.park.response.ResponseStatus;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Arrays;

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
            final StringBuilder threadCreate = new StringBuilder("INSERT INTO ");
            threadCreate.append(tableName);
            threadCreate.append("(user_email, forum_short_name, title, isClosed," +
                    " date, message, slug, getIsDeleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            try (PreparedStatement ps = connection.prepareStatement(threadCreate.toString(), Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, thread.getUser().toString());
                ps.setString(2, thread.getForum().toString());
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

    @Override
    public Response details(long threadId, String[] related){
        final Thread thread;
        try(Connection connection = ds.getConnection()){
            final StringBuilder threadDetails = new StringBuilder("SELECT * FROM ");
            threadDetails.append(tableName);
            threadDetails.append(" WHERE id  = ? ");
            try (PreparedStatement ps = connection.prepareStatement(threadDetails.toString())) {
                ps.setLong(1, threadId);
                try (ResultSet resultSet = ps.executeQuery()) {
                    resultSet.next();
                    thread = new Thread(resultSet);
                } catch (SQLException e) {
                    return handeSQLException(e);
                }

                if (related != null) {
                    if (Arrays.asList(related).contains("user")) {
                        final String email = thread.getUser().toString();
                        thread.setUser(new UserDaoImpl(ds).details(email).getObject());
                    }
                    if (Arrays.asList(related).contains("forum")) {
                        final String forumShortName = thread.getForum().toString();
                        thread.setForum(new ForumDaoImpl(ds).details(forumShortName,null).getObject());
                    }
                }
            }
        } catch(SQLException e){
            e.printStackTrace();
            return new Response(ResponseStatus.INVALID_REQUEST);
        }
        return new Response(ResponseStatus.OK, thread);
    }

    @Override
    public Response close(String threadCloseJson){
        try(Connection connection = ds.getConnection()){
            final Integer threadId = new JsonParser().parse(threadCloseJson).getAsJsonObject().get("thread").getAsInt();;
            final StringBuilder threadCloseQuery = new StringBuilder("UPDATE ");
            threadCloseQuery.append(tableName);
            threadCloseQuery.append(" SET isClosed = 1 WHERE id = ?");
            try (PreparedStatement ps = connection.prepareStatement(threadCloseQuery.toString())) {
                ps.setLong(1, threadId);
                ps.execute();
            } catch (SQLException e) {
                return handeSQLException(e);
            }
        }  catch(SQLException e){
            e.printStackTrace();
            return new Response(ResponseStatus.INVALID_REQUEST);
        }
        //может, отдавать прямо то, что нам пришло?
        return new Response(ResponseStatus.OK, new Gson().fromJson(threadCloseJson, Object.class));
    }

}
