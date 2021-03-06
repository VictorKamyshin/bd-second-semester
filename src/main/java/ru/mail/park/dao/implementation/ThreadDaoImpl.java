package ru.mail.park.dao.implementation;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ru.mail.park.dao.ThreadDao;
import ru.mail.park.model.Post;
import ru.mail.park.model.Thread;
import ru.mail.park.response.Response;
import ru.mail.park.response.ResponseStatus;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by victor on 23.11.16.
 */
public class ThreadDaoImpl extends  BaseDaoImpl implements ThreadDao {

    private String subscriptionsName;

    private String postTableName;

    public ThreadDaoImpl(DataSource dataSource) {
        this.tableName = Thread.TABLE_NAME;
        this.subscriptionsName = Thread.SUBSCRIPTION_TABLE_NAME;
        this.ds = dataSource;
        this.postTableName = Post.TABLE_NAME;
    }

    @Override
    public void truncateTable() {
        try (Connection connection = ds.getConnection()) {
            Truncator.truncByQuery(connection, "SET FOREIGN_KEY_CHECKS = 0;");
            Truncator.truncByQuery(connection, "TRUNCATE TABLE " + tableName);
            Truncator.truncByQuery(connection, "TRUNCATE TABLE " + subscriptionsName);
            Truncator.truncByQuery(connection, "SET FOREIGN_KEY_CHECKS = 1;");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Response create(String threadCreateJson){
        final Thread thread;
        try (Connection connection = ds.getConnection()) {
            thread = new Thread(new JsonParser().parse(threadCreateJson).getAsJsonObject());
            final StringBuilder threadCreate = new StringBuilder("INSERT INTO ");
            threadCreate.append(tableName);
            threadCreate.append("(user, forum, title, isClosed," +
                    " date, message, slug, isDeleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            try (PreparedStatement ps = connection.prepareStatement(threadCreate.toString(), Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, thread.getUser().toString());
                ps.setString(2, thread.getForum().toString());
                ps.setString(3, thread.getTitle());
                ps.setBoolean(4, thread.getIsClosed());
                ps.setString(5,thread.getDate());
                ps.setString(6, thread.getMessage());
                ps.setString(7, thread.getSlug());
                ps.setBoolean(8, thread.getIsDeleted());
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
    public Response details(long threadId, String[] related) {
        final Thread thread;
        if (related != null) {
            if (Arrays.asList(related).contains("thread")) {
                return new Response(ResponseStatus.INCORRECT_REQUEST);
            }
        }
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
            final Integer threadId = new JsonParser().parse(threadCloseJson).getAsJsonObject().get("thread").getAsInt();
            final StringBuilder threadCloseQuery = new StringBuilder("UPDATE ");
            threadCloseQuery.append(tableName);
            threadCloseQuery.append(" SET isClosed = 1 WHERE id = ?");
            try (PreparedStatement ps = connection.prepareStatement(threadCloseQuery.toString())) {
                ps.setLong(1, threadId);
                ps.execute();
            } catch (SQLException e) {
                return handeSQLException(e);
            }
        } catch(SQLException e){
            e.printStackTrace();
            return new Response(ResponseStatus.INVALID_REQUEST);
        }
        //может, отдавать прямо то, что нам пришло?
        return new Response(ResponseStatus.OK, new Gson().fromJson(threadCloseJson, Object.class));
    }

    @Override
    public Response open(String threadCloseJson){
        try(Connection connection = ds.getConnection()){
            final Long threadId = new JsonParser().parse(threadCloseJson).getAsJsonObject().get("thread").getAsLong();
            final StringBuilder threadCloseQuery = new StringBuilder("UPDATE ");
            threadCloseQuery.append(tableName);
            threadCloseQuery.append(" SET isClosed = 0 WHERE id = ?");
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

    @Override
    public Response remove(String threadRemoveJson){
        //надо отметить тред как удаленный, а так же - все посты в нем
        try(Connection connection = ds.getConnection()){
            final Long threadId = new JsonParser().parse(threadRemoveJson).getAsJsonObject().get("thread").getAsLong();
            final StringBuilder threadRemoveQuery = new StringBuilder("UPDATE ");
            threadRemoveQuery.append(tableName);
            threadRemoveQuery.append(" SET isDeleted = 1, posts = 0 WHERE id = ?");
            try (PreparedStatement ps = connection.prepareStatement(threadRemoveQuery.toString())) {
                ps.setLong(1, threadId);
                ps.execute();
            } catch (SQLException e) {
                return handeSQLException(e);
            } //отметили тред как удаленный
            //теперь идем отмечать как удаленные все посты из этого треда
            final StringBuilder postsRemoveQuery = new StringBuilder("UPDATE ");
            postsRemoveQuery.append(postTableName);
            postsRemoveQuery.append(" SET isDeleted = 1 WHERE thread = ?");
            try (PreparedStatement ps = connection.prepareStatement(postsRemoveQuery.toString())) {
                ps.setLong(1, threadId);
                ps.execute();
            } catch (SQLException e) {
                return handeSQLException(e);
            } //удолил
        } catch(SQLException e){
            e.printStackTrace();
            return new Response(ResponseStatus.INVALID_REQUEST);
        }
        return new Response(ResponseStatus.OK, new Gson().fromJson(threadRemoveJson, Object.class));
    }

    @Override
    public Response restore(String threadRestoreJson){
        try(Connection connection = ds.getConnection()){
            final Long threadId = new JsonParser().parse(threadRestoreJson).getAsJsonObject().get("thread").getAsLong();

            //теперь идем восстанавливать все посты из этого треда
            final StringBuilder postsRestoreQuery = new StringBuilder("UPDATE ");
            postsRestoreQuery.append(postTableName);
            postsRestoreQuery.append(" SET isDeleted = 0 WHERE thread = ?");
            try (PreparedStatement ps = connection.prepareStatement(postsRestoreQuery.toString())) {
                ps.setLong(1, threadId);
                ps.execute();
            }

            Long countOfPosts = null;
            try(PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) AS countPosts FROM Posts WHERE thread = ?")) {
                ps.setLong(1, threadId);
                try (ResultSet resultSet = ps.executeQuery()) {
                    resultSet.next();
                    countOfPosts = resultSet.getLong("countPosts");
                }
            }

            final StringBuilder threadRestoreQuery = new StringBuilder("UPDATE ");
            threadRestoreQuery.append(tableName);
            threadRestoreQuery.append(" SET isDeleted = 0, posts = ? WHERE id = ?");
            try (PreparedStatement ps = connection.prepareStatement(threadRestoreQuery.toString())) {
                ps.setLong(1,countOfPosts);
                ps.setLong(2, threadId);
                ps.execute();
            }

        } catch(SQLException e){
            e.printStackTrace();
            return new Response(ResponseStatus.INVALID_REQUEST);
        }
        return new Response(ResponseStatus.OK, new Gson().fromJson(threadRestoreJson, Object.class));
    }

    @Override
    public Response update(String threadUpdateJson){
        final Long threadId;
        try (Connection connection = ds.getConnection()) {
            final JsonObject jsonObject = new JsonParser().parse(threadUpdateJson).getAsJsonObject();
            threadId = jsonObject.get("thread").getAsLong();
            final String newMessage = jsonObject.get("message").getAsString();
            final String newSlug = jsonObject.get("slug").getAsString();
            final StringBuilder updateThreadQuery = new StringBuilder("UPDATE ");
            updateThreadQuery.append(tableName);
            updateThreadQuery.append(" SET message = ?, slug = ? WHERE id = ?");
            try (PreparedStatement ps = connection.prepareStatement(updateThreadQuery.toString())) {
                ps.setString(1, newMessage);
                ps.setString(2,newSlug);
                ps.setLong(3,threadId);
                ps.executeUpdate();
            } catch (SQLException e) {
                return handeSQLException(e);
            }
        } catch (SQLException e){
            e.printStackTrace();
            return new Response(ResponseStatus.INVALID_REQUEST);
        }
        return details(threadId, null);
    }

    public Response vote(String threadVoteJson) {
        final Long threadId;
        try (Connection connection = ds.getConnection()) {
            final JsonObject jsonObject = new JsonParser().parse(threadVoteJson).getAsJsonObject();
            threadId = jsonObject.get("thread").getAsLong();
            final Integer threadVote = jsonObject.get("vote").getAsInt();
            final StringBuilder threadVoteQuery = new StringBuilder("UPDATE ");
            threadVoteQuery.append(tableName);
            threadVoteQuery.append(" SET");
            if(threadVote>0){
                threadVoteQuery.append(" likes = likes + 1, points = points + 1");
            } else {
                threadVoteQuery.append(" dislikes = dislikes + 1, points = points - 1");
            }
            threadVoteQuery.append(" WHERE id = ?");
            try (PreparedStatement ps = connection.prepareStatement(threadVoteQuery.toString())) {
                ps.setLong(1,threadId);
                ps.executeUpdate();
            } catch (SQLException e) {
                return handeSQLException(e);
            }
        } catch (SQLException e){
            e.printStackTrace();
            return new Response(ResponseStatus.INVALID_REQUEST);
        }
        return details(threadId, null);
    }

    @Override
    public Response subscribe(String subscribeJson){
        try (Connection connection = ds.getConnection()) {
            final JsonObject jsonObject = new JsonParser().parse(subscribeJson).getAsJsonObject();
            final String subscriberEmail = jsonObject.get("user").getAsString();
            final Long threadId = jsonObject.get("thread").getAsLong();
            final StringBuilder subscribeQuery = new StringBuilder("INSERT INTO ");
            subscribeQuery.append(subscriptionsName);
            subscribeQuery.append("(user, thread) VALUES (?,?)");
            try (PreparedStatement ps = connection.prepareStatement(subscribeQuery.toString())) {
                ps.setString(1,subscriberEmail);
                ps.setLong(2,threadId);
                ps.executeUpdate();
            } catch (SQLException e) {
                return handeSQLException(e);
            }
            } catch (SQLException e){
            e.printStackTrace();
            return new Response(ResponseStatus.INVALID_REQUEST);
        }
        return new Response(ResponseStatus.OK, new Gson().fromJson(subscribeJson, Object.class));
    }

    @Override
    public Response unsubscribe(String unsubscribeJson){
        try (Connection connection = ds.getConnection()) {
            final JsonObject jsonObject = new JsonParser().parse(unsubscribeJson).getAsJsonObject();
            final String subscriberEmail = jsonObject.get("user").getAsString();
            final Long threadId = jsonObject.get("thread").getAsLong();
            final StringBuilder unsubscribeQuery = new StringBuilder("DELETE FROM ");
            unsubscribeQuery.append(subscriptionsName);
            unsubscribeQuery.append(" WHERE user = ? AND thread = ?");
            try (PreparedStatement ps = connection.prepareStatement(unsubscribeQuery.toString())) {
                ps.setString(1,subscriberEmail);
                ps.setLong(2,threadId);
                ps.execute();
            } catch (SQLException e) {
                return handeSQLException(e);
            }
        } catch (SQLException e){
            e.printStackTrace();
            return new Response(ResponseStatus.INVALID_REQUEST);
        }
        return new Response(ResponseStatus.OK, new Gson().fromJson(unsubscribeJson, Object.class));
    }

    public Response list(String forum, String userEmail, String since, Integer limit, String order, String[] related){
        ArrayList<Thread> threads = new ArrayList<>(); //крч, объявили список постов
        if((forum==null&&userEmail==null)||(forum!=null&&userEmail!=null)){
            return new Response(ResponseStatus.INVALID_REQUEST);
        } //такого запроса нам приходить не должно
        try (Connection connection = ds.getConnection()) {
            final StringBuilder threadListQuery = new StringBuilder("SELECT * FROM ");
            threadListQuery.append(tableName);

            threadListQuery.append(" WHERE ");
            if(forum!=null){
                threadListQuery.append("forum = ? ");
            } else {
                threadListQuery.append(" user = ? ");
            }

            if(since!=null) {
                threadListQuery.append("AND date > ? ");
            }

            threadListQuery.append("ORDER BY date ");
            if(order==null||"desc".equals(order)){
                threadListQuery.append("DESC ");
            } else {
                threadListQuery.append("ASC ");
            }
            if(limit!=null){
                threadListQuery.append("LIMIT ?");
            } //собрали наш чудо-запрос
            //System.out.println(threadListQuery.toString());
            try (PreparedStatement ps = connection.prepareStatement(threadListQuery.toString())) {
                Integer fieldCounter = 1;
                if(forum!=null){
                    ps.setString(fieldCounter,forum);
                    fieldCounter++;
                } else {
                    ps.setString(fieldCounter,userEmail);
                    fieldCounter++;
                }

                if(since!=null) {
                    ps.setString(fieldCounter, since);
                    fieldCounter++;
                }

                if(limit!=null){
                    ps.setInt(fieldCounter, limit);
                    fieldCounter++;
                }

                try (ResultSet resultSet = ps.executeQuery()) {
                    while(resultSet.next()) {
                        threads.add(new Thread(resultSet));
                    }
                } catch (SQLException e) {
                    return handeSQLException(e);
                }
                //осталось обыграть релейтед
                for(Thread thread:threads){
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
            }
        } catch (SQLException e){
            e.printStackTrace();
            return new Response(ResponseStatus.INVALID_REQUEST);
        }
        return new Response(ResponseStatus.OK, threads);
    }

}
