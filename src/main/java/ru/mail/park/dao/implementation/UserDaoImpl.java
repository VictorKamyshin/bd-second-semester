package ru.mail.park.dao.implementation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ru.mail.park.dao.UserDao;
import ru.mail.park.model.User;
import ru.mail.park.response.Response;
import ru.mail.park.response.ResponseStatus;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
    public void truncateTable() {
        try (Connection connection = ds.getConnection()) {
            Truncator.truncByQuery(connection, "SET FOREIGN_KEY_CHECKS = 0;");
            Truncator.truncByQuery(connection, "TRUNCATE TABLE " + tableName);
            Truncator.truncByQuery(connection, "TRUNCATE TABLE " + followerTableName);
            Truncator.truncByQuery(connection, "SET FOREIGN_KEY_CHECKS = 1;");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    ps.setString(1, follower);
                    ps.setString(2, followee);
                    ps.execute();
                }
            } catch (SQLException e) {
                return handeSQLException(e);
            }
        } catch (SQLException e){
            return new Response(ResponseStatus.INCORRECT_REQUEST);
        }
        return details(follower);
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
                    ps.setString(1, follower);
                    ps.setString(2, followee);
                    ps.execute();
                }
            } catch (SQLException e) {
                return handeSQLException(e);
            }
        } catch (SQLException e){
            return new Response(ResponseStatus.INCORRECT_REQUEST);
        }
        return details(follower);
    }

    public Response list(String userEmail, String forum, Boolean isFollowing,Integer limit, String order, Long sinceId){
        ArrayList<User> users = new ArrayList<>();
        //return new Response(ResponseStatus.OK,"asdasda");
        if((forum==null&&userEmail==null)||(forum!=null&&userEmail!=null)){
            return new Response(ResponseStatus.INVALID_REQUEST);
        } //такого запроса нам приходить не должно
        try (Connection connection = ds.getConnection()) {
            StringBuilder userListQuery = new StringBuilder("SELECT Users.* ");
            /*userListQuery.append("group_concat(distinct f1.follower_email) as followers, ");
            userListQuery.append("group_concat(distinct f2.following_email) as following, ");
            userListQuery.append("group_concat(distinct s.thread) as subscriptions "); */
            //вот тут начинается логика, потому что база джоина у разных запросов разная
            if(forum!=null){ //значит, мы получаем список юзеров на форуме
                userListQuery.append("FROM Posts P ");
                userListQuery.append("JOIN Users ON Users.email = P.user ");
            } else { //значит мы получаем фоловеров или фоловимых
                userListQuery.append("FROM Followers UF ");
                if(isFollowing) {
                    userListQuery.append("JOIN Users ON Users.email = UF.follower_email ");
                } else {
                    userListQuery.append("JOIN Users ON Users.email = UF.following_email ");
                }
            }
            //вот эта штука вызывает большие вопрсоы
            /*
            userListQuery.append("LEFT JOIN Followers f1 on f1.following_email = Users.email ");
            userListQuery.append("LEFT JOIN Followers f2 on f2.follower_email = Users.email ");
            userListQuery.append("LEFT JOIN Subscribe s on s.user = Users.email "); //эти три строки нужны всегда - они формируют правильные данные о юзере
            */
            if(userEmail!=null) {
                if(isFollowing) {
                    userListQuery.append(" WHERE UF.following_email = ? ");
                } else {
                    userListQuery.append(" WHERE UF.follower_email = ? ");
                }
            } else {
                userListQuery.append(" WHERE P.forum = ? ");
            }

            if (sinceId != null) {
                userListQuery.append("AND Users.id >= ? ");
            }

            //userListQuery.append("GROUP BY Users.email, Users.id ");
            userListQuery.append("ORDER BY Users.name ");

            if(order==null||"desc".equals(order)){
                userListQuery.append("DESC ");
            } else {
                userListQuery.append("ASC ");
            }
            if(limit!=null){
                userListQuery.append("LIMIT ?");
            } //собрали наш чудо-запрос

            //System.out.println(userListQuery.toString());
            try (PreparedStatement ps = connection.prepareStatement(userListQuery.toString())) {
                Integer fieldCounter = 1;
                if(forum!=null){
                    ps.setString(fieldCounter,forum);
                    fieldCounter++;
                } else {
                    ps.setString(fieldCounter,userEmail);
                    fieldCounter++;
                }

                if(sinceId!=null) {
                    ps.setLong(fieldCounter, sinceId);
                    fieldCounter++;
                }

                if(limit!=null){
                    ps.setInt(fieldCounter, limit);
                    fieldCounter++;
                }
                try (ResultSet resultSet = ps.executeQuery()) {
                    while(resultSet.next()) {
                        User tmpUser = new User(resultSet);
                        //System.out.println(tmpUser.getEmail());
                        tmpUser.setListOfFollowers(getFollowers(connection,tmpUser.getEmail()));
                        tmpUser.setListOfFollowing(getFollowing(connection,tmpUser.getEmail()));
                        tmpUser.setListOfSubscriptions(getSubscription(connection,tmpUser.getEmail()));
                        users.add(tmpUser);
                    }
                } catch (SQLException e) {
                    return handeSQLException(e);
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
            return new Response(ResponseStatus.INCORRECT_REQUEST);
        }
        return new Response(ResponseStatus.OK, users);
    }

    @Override
    public Response details(String userEmail){
        final User user;
        try(Connection connection = ds.getConnection()){
            final StringBuilder getUserDetails = new StringBuilder("SELECT Users.* ");
            getUserDetails.append("FROM Users ");
            getUserDetails.append("WHERE Users.email = ? ");
            try (PreparedStatement ps = connection.prepareStatement(getUserDetails.toString())) {
                ps.setString(1,userEmail);
                try (ResultSet resultSet = ps.executeQuery()) {
                    resultSet.next();
                    user = new User(resultSet);
                    user.setListOfFollowers(getFollowers(connection,userEmail));
                    user.setListOfFollowing(getFollowing(connection,userEmail));
                    user.setListOfSubscriptions(getSubscription(connection,userEmail));
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

    private List<String> getFollowers(Connection connection, String userEmail) throws SQLException{
        final List<String> followers = new ArrayList<>();
        try(PreparedStatement ps = connection.prepareStatement("Select follower_email FROM  Followers WHERE following_email = ?;")){
            ps.setString(1,userEmail);
            try (ResultSet resultSet = ps.executeQuery()) {
                while(resultSet.next()){
                    followers.add(resultSet.getString(1));
                }
            }
        }
        return  followers;
    }

    private List<String> getFollowing(Connection connection, String userEmail) throws SQLException{
        final List<String> following = new ArrayList<>();
        try(PreparedStatement ps = connection.prepareStatement("Select following_email FROM  Followers WHERE follower_email = ?;")){
            ps.setString(1,userEmail);
            try (ResultSet resultSet = ps.executeQuery()) {
                while(resultSet.next()){
                    following.add(resultSet.getString(1));
                }
            }
        }
        return  following;
    }

    private List<Integer> getSubscription(Connection connection, String userEmail) throws SQLException{
        final List<Integer> subscriptions = new ArrayList<>();
        try(PreparedStatement ps = connection.prepareStatement("Select thread FROM Subscribe WHERE user = ?;")){
            ps.setString(1,userEmail);
            try (ResultSet resultSet = ps.executeQuery()) {
                while(resultSet.next()){
                    subscriptions.add(resultSet.getInt(1));
                }
            }
        }
        return  subscriptions;
    }

    public Response details1(String userEmail){
        final User user;
        //return new Response(ResponseStatus.OK,"asdasda");/*
        try (Connection connection = ds.getConnection()) {
            final StringBuilder getUserDetails = new StringBuilder("SELECT Users.*, ");
            getUserDetails.append("group_concat(distinct f1.follower_email) as followers, ");
            getUserDetails.append("group_concat(distinct f2.following_email) as following, ");
            getUserDetails.append("group_concat(distinct s.thread) as subscriptions");
            getUserDetails.append(" FROM Users");
            getUserDetails.append(" LEFT JOIN Followers f1 on f1.following_email = Users.email");
            getUserDetails.append(" LEFT JOIN Followers f2 on f2.follower_email = Users.email");
            getUserDetails.append(" LEFT JOIN Subscribe s on s.user = Users.email");
            getUserDetails.append(" WHERE Users.email = ? ");
            getUserDetails.append("GROUP BY Users.id");
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
                ps.setBoolean(3, user.getIsAnonymous());
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
