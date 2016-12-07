package ru.mail.park.dao.implementation;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ru.mail.park.dao.PostDao;
import ru.mail.park.model.Post;
import ru.mail.park.response.Response;
import ru.mail.park.response.ResponseStatus;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Arrays;

/**
 * Created by victor on 23.11.16.
 */
public class PostDaoImpl extends BaseDaoImpl implements PostDao {
    public PostDaoImpl(DataSource dataSource) {
        this.tableName = Post.TABLE_NAME;
        this.ds = dataSource;
    }

    @Override
    public Response create(String postCreateJson) {
        final Post post;
        try(Connection connection = ds.getConnection()){
            post = new Post(new JsonParser().parse(postCreateJson).getAsJsonObject());
            String postPath = "";

            if (post.getParentId() != null) {
                final StringBuilder selectParendPath = new StringBuilder("SELECT material_path, count_of_childs FROM ");
                selectParendPath.append(tableName);
                selectParendPath.append(" WHERE id = ?;");
                try (PreparedStatement ps = connection.prepareStatement(selectParendPath.toString())) {
                    ps.setLong(1, post.getParentId());
                    try (ResultSet resultSet = ps.executeQuery()) {
                        resultSet.next();
                        String parentPath = resultSet.getString("path");
                        final StringBuilder pathBuilder = new StringBuilder(Integer.toString(resultSet.getInt("count_of_childs")));
                        while(pathBuilder.length()<4){
                            pathBuilder.append("0",0,1);
                        }
                        pathBuilder.append(parentPath,0,parentPath.length());
                        postPath = pathBuilder.toString();
                    }
                }
            } else {//если у нас есть родитель, то мы сгенерировали путь
                final String getCountOfRootPosts  = "SELECT count_of_root_posts FROM Threads WHERE id=?";
                try(PreparedStatement ps = connection.prepareStatement(getCountOfRootPosts)){
                    ps.setInt(1, Integer.parseInt(post.getThread().toString()));
                    try(ResultSet resultSet = ps.executeQuery()){
                        resultSet.next();
                        final StringBuilder pathBuilder = new StringBuilder(Integer.toString(resultSet.getInt("count_of_root_posts")));
                        while(pathBuilder.length()<4){
                            pathBuilder.insert(0, "0");
                        }
                        postPath = pathBuilder.toString();
                    }
                }
            }
            //план - каждый пост хранит количество своих детей
            //количество корневых постов хранит тред
            //по этому количеству и пути родителя мы можем сразу генерировать путь для нового поста
            //теперь у нас есть материальный путь для создаваемого поста - и это ценой всего двух точечных запросов
            final StringBuilder createPost = new StringBuilder("INSERT INTO ");
            createPost.append(tableName);
            createPost.append("(date, forum, isApproved, isDeleted, isEdited, isHighlighted, isSpam," +
                    " message, parent, thread, user, material_path)");
            createPost.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            try (PreparedStatement ps = connection.prepareStatement(createPost.toString(), Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, post.getDate());
                ps.setString(2, post.getForum().toString());
                ps.setBoolean(3, post.getIsApproved());
                ps.setBoolean(4, post.getIsDeleted());
                ps.setBoolean(5, post.getIsEdited());
                ps.setBoolean(6, post.getIsHighlighted());
                ps.setBoolean(7, post.getIsSpam());
                ps.setString(8, post.getMessage());
                ps.setObject(9, post.getParentId());
                ps.setLong(10, Long.parseLong(post.getThread().toString()));
                ps.setString(11, post.getUser().toString());
                ps.setString(12, postPath);
                ps.executeUpdate();
                try (ResultSet resultSet = ps.getGeneratedKeys()) {
                    resultSet.next();
                    post.setId(resultSet.getLong(1));
                    post.setPath(postPath);
                }
            } catch (SQLException e) {
                return handeSQLException(e);
            }
            final StringBuilder updateThreads = new StringBuilder("UPDATE Threads SET posts = posts + 1"); //"WHERE id = ?";
            if(post.getParentId()!=null){
                final StringBuilder updatePostChilds = new StringBuilder("UPDATE ");
                updatePostChilds.append(tableName);
                updatePostChilds.append(" SET count_of_childs = count_of_childs + 1 WHERE id=?");
                try(PreparedStatement ps = connection.prepareStatement(updatePostChilds.toString())){
                    ps.setLong(1,post.getParentId());
                    ps.executeUpdate();
                } catch(Exception e) { //у поста появится еще один дочерний пост, хотя по логике это должно быть после создания самого поста
                    e.printStackTrace();
                }
            } else {
                updateThreads.append(", count_of_root_posts = count_of_root_posts + 1 ");
            }
            updateThreads.append(" WHERE id=?");
            try (PreparedStatement ps = connection.prepareStatement(updateThreads.toString())) {
                ps.setLong(1, Long.parseLong(post.getThread().toString()));
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                return new Response(ResponseStatus.NOT_FOUND);
            }

        } catch(SQLException e){
            e.printStackTrace();
            return new Response(ResponseStatus.INVALID_REQUEST); //если произошла ошибка при парсе джсона
        }
        return new Response(ResponseStatus.OK, post);
    }

    @Override
    public Response details(long postId, String[] related){
        final Post post;
        try(Connection connection = ds.getConnection()){
            final StringBuilder postDetails = new StringBuilder("SELECT * FROM ");
            postDetails.append(tableName);
            postDetails.append(" WHERE id  = ? ");
            try (PreparedStatement ps = connection.prepareStatement(postDetails.toString())) {
                ps.setLong(1, postId);
                try (ResultSet resultSet = ps.executeQuery()) {
                    resultSet.next();
                    post = new Post(resultSet);
                } catch (SQLException e) {
                    return handeSQLException(e);
                }

                if (related != null) {
                    if (Arrays.asList(related).contains("user")) {
                        final String email = post.getUser().toString();
                        post.setUser(new UserDaoImpl(ds).details(email).getObject());
                    }
                    if (Arrays.asList(related).contains("forum")) {
                        final String forumShortName = post.getForum().toString();
                        post.setForum(new ForumDaoImpl(ds).details(forumShortName,null).getObject());
                    }
                    if (Arrays.asList(related).contains("thread")) {
                        final Long threadId = Long.parseLong(post.getThread().toString());
                        post.setForum(new ThreadDaoImpl(ds).details(threadId, null).getObject());
                    }
                }
            }
        } catch(SQLException e){
            e.printStackTrace();
            return new Response(ResponseStatus.INVALID_REQUEST);
        }
        return new Response(ResponseStatus.OK, post);
    }

    @Override
    public Response remove(String postRemoveJson){
        try(Connection connection = ds.getConnection()){
            final Long postId = new JsonParser().parse(postRemoveJson).getAsJsonObject().get("post").getAsLong();
            final StringBuilder postRemoveQuery = new StringBuilder("UPDATE ");
            postRemoveQuery.append(tableName);
            postRemoveQuery.append(" SET isDeleted = 1 WHERE id = ?");
            try (PreparedStatement ps = connection.prepareStatement(postRemoveQuery.toString())) {
                ps.setLong(1, postId);
                ps.execute(); //окей, пост пометили как удаленный - но еще надо сделать поправку данные, которые храняться у треда
            } catch (SQLException e) {
                return handeSQLException(e);
            }

            final String updateThreadsQuery = "UPDATE Threads SET posts = posts + 1 WHERE id=?";
            try (PreparedStatement ps = connection.prepareStatement(updateThreadsQuery)) {
                //нужно откуда-то добыть адйи поста
                final Long threadId = getThreadIdByPostId(postId); //добыл - приверяй.
                if(threadId==null){
                    return new Response(ResponseStatus.NOT_FOUND);
                }
                ps.setLong(1, threadId);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                return new Response(ResponseStatus.NOT_FOUND);
            }
        } catch(SQLException e){
            e.printStackTrace();
            return new Response(ResponseStatus.INVALID_REQUEST);
        }
        return new Response(ResponseStatus.OK, new Gson().fromJson(postRemoveJson, Object.class));
    }

    @Override
    public Response restore(String postRestoreJson){
        try(Connection connection = ds.getConnection()) {
            final Long postId = new JsonParser().parse(postRestoreJson).getAsJsonObject().get("post").getAsLong();
            final StringBuilder postRestoreQuery = new StringBuilder("UPDATE ");
            postRestoreQuery.append(tableName);
            postRestoreQuery.append(" SET isDeleted = 0 WHERE id = ?");
            try (PreparedStatement ps = connection.prepareStatement(postRestoreQuery.toString())) {
                ps.setLong(1, postId);
                ps.execute(); //окей, пост пометили как удаленный - но еще надо сделать поправку данные, которые храняться у треда
            } catch (SQLException e) {
                e.printStackTrace();
                return new Response(ResponseStatus.NOT_FOUND);
            }

            final String updateThreadsQuery = "UPDATE Threads SET posts = posts + 1 WHERE id=?";
            try (PreparedStatement ps = connection.prepareStatement(updateThreadsQuery)) {
                //нужно откуда-то добыть адйи поста
                final Long threadId = getThreadIdByPostId(postId); //добыл - приверяй.
                if(threadId==null){
                    return new Response(ResponseStatus.NOT_FOUND);
                }
                ps.setLong(1, threadId);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                return new Response(ResponseStatus.NOT_FOUND);
            }
        } catch(SQLException e){
            e.printStackTrace();
            return new Response(ResponseStatus.INVALID_REQUEST);
        }
        return new Response(ResponseStatus.OK, new Gson().fromJson(postRestoreJson, Object.class));
    }

    @Override
    public Response update(String postUpdateJson){
        final Long postId;
        try (Connection connection = ds.getConnection()) {
            JsonObject jsonObject = new JsonParser().parse(postUpdateJson).getAsJsonObject();
            postId = jsonObject.get("post").getAsLong();
            final String newMessage = jsonObject.get("message").getAsString();
            final StringBuilder updatePostQuery = new StringBuilder("UPDATE ");
            updatePostQuery.append(tableName);
            updatePostQuery.append(" SET message = ? WHERE id = ?");
            try (PreparedStatement ps = connection.prepareStatement(updatePostQuery.toString())) {
                ps.setString(1, newMessage);
                ps.setLong(2,postId);
                ps.executeUpdate();
            } catch (SQLException e) {
                return handeSQLException(e);
            }

        } catch (SQLException e){
            e.printStackTrace();
            return new Response(ResponseStatus.INVALID_REQUEST);
        }
        return details(postId, null);
    }

    @Override
    public Response vote(String postVoteJson){
        final Long postId;
        try (Connection connection = ds.getConnection()) {
            JsonObject jsonObject = new JsonParser().parse(postVoteJson).getAsJsonObject();
            postId = jsonObject.get("post").getAsLong();
            final Integer postVote = jsonObject.get("vote").getAsInt();
            final StringBuilder postVoteQuery = new StringBuilder("UPDATE ");
            postVoteQuery.append(tableName);
            postVoteQuery.append(" SET");
            if(postVote>0){
                postVoteQuery.append(" likes = likes + 1, points = points + 1");
            } else {
                postVoteQuery.append(" dislikes = dislikes + 1, points = points - 1");
            }
            postVoteQuery.append(" WHERE id = ?");
            try (PreparedStatement ps = connection.prepareStatement(postVoteQuery.toString())) {
                ps.setLong(1,postId);
                ps.executeUpdate();
            } catch (SQLException e) {
                return handeSQLException(e);
            }
        } catch (SQLException e){
            e.printStackTrace();
            return new Response(ResponseStatus.INVALID_REQUEST);
        }
        return details(postId, null);

    }

    private Long getThreadIdByPostId(Long postId){
        final Long threadId;
        try(Connection connection = ds.getConnection()){
            final StringBuilder getThreadIdByPost = new StringBuilder("SELECT thread FROM ");
            getThreadIdByPost.append(tableName);
            getThreadIdByPost.append(" WHERE id = ?");
            try (PreparedStatement ps = connection.prepareStatement(getThreadIdByPost.toString())) {
                ps.setLong(1,postId);
                try (ResultSet resultSet = ps.executeQuery()) {
                    resultSet.next();
                    threadId = resultSet.getLong(1);
                }
            } catch(SQLException e) {
                e.printStackTrace();
                return null;
            }
        } catch(SQLException e){
            e.printStackTrace();
            return null;
        }
        return threadId;
    }

}