package ru.mail.park.dao.implementation;

import com.google.gson.JsonObject;
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
    public Response follow(String userFollowJson){
        final String follower;
        try (Connection connection = ds.getConnection()) {
            final JsonObject userFollowObject = new JsonParser().parse(userFollowJson).getAsJsonObject();
            follower = userFollowObject.get("follower").getAsString();
            final String followee = userFollowObject.get("followee").getAsString();
            try {
                final String createFollow = "INSERT INTO Followers (follower_email, following_email) VALUES (?,?)";
                try (PreparedStatement ps = connection.prepareStatement(createFollow)) {
                    ps.setString(1, followee);
                    ps.setString(2, follower);
                    ps.execute();
                }
            } catch (SQLException e) {
                return handeSQLException(e);
            }
        } catch (SQLException e){
            return new Response(ResponseStatus.INCORRECT_REQUEST);
        }
        return new Response(ResponseStatus.OK,follower);
    }

    @Override
    public Response unfollow(String userUnfollowJson){
        final String follower;
        try (Connection connection = ds.getConnection()) {
            final JsonObject userFollowObject = new JsonParser().parse(userUnfollowJson).getAsJsonObject();
            follower = userFollowObject.get("follower").getAsString();
            final String followee = userFollowObject.get("followee").getAsString();
            try {
                final String deleteFollow = "DELETE FROM Followers WHERE follower_email=? AND following_email=?";
                try (PreparedStatement ps = connection.prepareStatement(deleteFollow)) {
                    ps.setString(1, followee);
                    ps.setString(2, follower);
                    ps.execute();
                }
            } catch (SQLException e) {
                return handeSQLException(e);
            }
        } catch (SQLException e){
            return new Response(ResponseStatus.INCORRECT_REQUEST);
        }
        return new Response(ResponseStatus.OK,follower);
    }

    @Override
    public Response details(String userEmail){
        final User user;
        try (Connection connection = ds.getConnection()) {
            StringBuilder getUserDetails = new StringBuilder("SELECT Users.*, ");
            getUserDetails.append("group_concat(distinct f1.follower_email) as followers, ");
            getUserDetails.append("group_concat(distinct f2.following_email) as following, ");
            getUserDetails.append("group_concat(distinct s.thread) as subscriptions");
            getUserDetails.append(" FROM Users");
            getUserDetails.append(" LEFT JOIN Followers f1 on f1.follower_email = Users.email");
            getUserDetails.append(" LEFT JOIN Followers f2 on f2.following_email = Users.email");
            getUserDetails.append(" LEFT JOIN Subscribe s on s.user = Users.email");
            getUserDetails.append(" WHERE Users.email = ?");
            try (PreparedStatement ps = connection.prepareStatement(getUserDetails.toString())) {
                ps.setString(1,userEmail);
                try (ResultSet resultSet = ps.executeQuery()) {
                    resultSet.next();
                    user = new User(resultSet);
                } catch (Exception e) {
                    e.printStackTrace();
                    return new Response(ResponseStatus.NOT_FOUND);
                }
            } catch ( Exception e) {
                e.printStackTrace();
                return new Response(ResponseStatus.INVALID_REQUEST);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(ResponseStatus.INCORRECT_REQUEST);
        }
        return new Response(ResponseStatus.OK,user);
    }

    @Override
    public Response create(String userCreateJson){
        final User user;
        try (Connection connection = ds.getConnection()) {
            user = new User(new JsonParser().parse(userCreateJson).getAsJsonObject());
            final StringBuilder createUserQuery = new StringBuilder("INSERT INTO ");
            createUserQuery.append(tableName);
            createUserQuery.append("(about, email, isAnonymous, name, username) VALUES (?, ?, ?, ?, ?)");
            try (PreparedStatement ps = connection.prepareStatement(createUserQuery.toString(),
                    Statement.RETURN_GENERATED_KEYS)) {
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

    @Override
    public Response updateProfile(String profileUpdateJson ) {
        final String email;
        try (Connection connection = ds.getConnection()) {
            JsonObject jsonObject = new JsonParser().parse(profileUpdateJson).getAsJsonObject();
            email = jsonObject.get("user").getAsString();
            final String name = jsonObject.get("name").getAsString();
            final String about = jsonObject.get("about").getAsString();
            final StringBuilder updateProfileQuery = new StringBuilder("UPDATE ");
            updateProfileQuery.append(tableName);
            updateProfileQuery.append(" SET about = ?, name = ? WHERE email = ?");
            try (PreparedStatement ps = connection.prepareStatement(updateProfileQuery.toString())) {
                ps.setString(1, about);
                ps.setString(2, name);
                ps.setString(3, email);
                ps.executeUpdate();
            } catch (SQLException e) {
                return handeSQLException(e);
            }
        } catch (SQLException e){
            e.printStackTrace();
            return new Response(ResponseStatus.INVALID_REQUEST);
        }
        return details(email);
    }
}
