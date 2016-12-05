package ru.mail.park.dao.implementation;

import com.google.gson.JsonParser;
import ru.mail.park.dao.ForumDao;
import ru.mail.park.model.Forum;
import ru.mail.park.model.User;
import ru.mail.park.response.Response;
import ru.mail.park.response.ResponseStatus;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Arrays;

/**
 * Created by victor on 23.11.16.
 */
public class ForumDaoImpl extends BaseDaoImpl implements ForumDao {
    String usertableName;
    public ForumDaoImpl(DataSource dataSource) {
        this.tableName = Forum.TABLE_NAME;
        this.ds = dataSource;
        this.usertableName  = User.TABLE_NAME;
    }

    @Override
    public Response create(String forumCreateJson) {
        final Forum forum;
        try (Connection connection = ds.getConnection()) {
            forum = new Forum(new JsonParser().parse(forumCreateJson).getAsJsonObject());
            final StringBuilder builder = new StringBuilder("INSERT INTO ");
            builder.append(tableName);
            builder.append("(name, short_name, user_email) VALUES (?, ?, ?)");
            final String query = builder.toString();
            try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, forum.getName());
                ps.setString(2, forum.getShortName());
                ps.setString(3, forum.getUser().toString());
                ps.executeUpdate();
                try (ResultSet resultSet = ps.getGeneratedKeys()) {
                    resultSet.next();
                    forum.setId(resultSet.getLong(1));
                }
            } catch(SQLException e){
                return handeSQLException(e);
            }
        } catch(SQLException e){
            e.printStackTrace();
            return new Response(ResponseStatus.INVALID_REQUEST); //если произошла ошибка при парсе джсона
        }
        return new  Response(ResponseStatus.OK, forum);
    }

    @Override
    public Response details(String forumShortName, String[] related){
        final Forum forum;
        try (Connection connection = ds.getConnection()) {
            final StringBuilder forumDetails = new StringBuilder("SELECT * FROM ");
            forumDetails.append(tableName);
            forumDetails.append(" WHERE short_name  = ? ");
            try (PreparedStatement ps = connection.prepareStatement(forumDetails.toString())) {
                ps.setString(1, forumShortName);
                try (ResultSet resultSet = ps.executeQuery()) {
                    resultSet.next();
                    forum = new Forum(resultSet);
                } catch (SQLException e) {
                    return handeSQLException(e);
                }
                if (related != null) {
                    if (Arrays.asList(related).contains("user")) {
                        String email = forum.getUser().toString();
                        //тут можно либо денормализовать базу и хранить у форума все данные юзера
                        //скорее всего нет, т.к. у юзера еще есть список фоловеров, который так просто не денормализуешь
                        forum.setUser(new UserDaoImpl(ds).details(email).getObject());
                    }
                }
            }
        } catch (Exception e) {
            return new Response(ResponseStatus.INVALID_REQUEST);
        }
        return new Response(ResponseStatus.OK, forum);
    }


}
