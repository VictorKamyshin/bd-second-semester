package ru.mail.park.dao.implementation;

import com.google.gson.JsonParser;
import ru.mail.park.dao.UserDao;
import ru.mail.park.model.User;
import ru.mail.park.response.Response;
import ru.mail.park.response.ResponseStatus;

import javax.sql.DataSource;
import java.sql.*;

/**
 * Created by victor on 23.11.16.
 */
public class UserDaoImpl extends BaseDaoImpl implements UserDao {
    private String followerTableName;

    public UserDaoImpl(DataSource dataSource) {
        this.tableName = User.TABLE_NAME;
        this.followerTableName = User.FOLLOWER_TABLE_NAME;
        this.ds = dataSource;
    }

    @Override
    public Response create(String userCreateJson){
        final User user;
        try (Connection connection = ds.getConnection()) {
            user = new User(new JsonParser().parse(userCreateJson).getAsJsonObject());
            final StringBuilder builder = new StringBuilder("INSERT INTO ");
            builder.append(tableName);
            builder.append("(about, email, isAnonymous, name, username) VALUES (?, ?, ?, ?, ?)");
            final String query = builder.toString();
            try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, user.getAbout()); //второй параметр говорит базе, чтобы она вернула
                ps.setString(2, user.getEmail()); //айдишник сгенерированного юзера
                ps.setBoolean(3, user.getAnonymous());
                ps.setString(4, user.getName());
                ps.setString(5, user.getUsername());
                ps.executeUpdate();
                try (ResultSet resultSet = ps.getGeneratedKeys()) {
                    resultSet.next();
                    user.setId(resultSet.getLong(1));
                }
            } catch (SQLException e) {
                return handeSQLException(e);
            }

        } catch (SQLException e){
            e.printStackTrace();
            return new Response(ResponseStatus.INVALID_REQUEST); //если произошла ошибка при парсе джсона
        }
        return new Response(ResponseStatus.OK, user); //в ответе будут лишние поля, но тесты от этого не сломаются
    }
}
