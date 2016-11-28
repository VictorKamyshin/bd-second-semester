package ru.mail.park.dao.implementation;

import com.google.gson.JsonParser;
import ru.mail.park.dao.PostDao;
import ru.mail.park.model.Post;
import ru.mail.park.response.Response;
import ru.mail.park.response.ResponseStatus;

import javax.sql.DataSource;
import java.sql.*;

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
                final String getCountOfRootPosts  = "SELECT count_of_root_posts FROM threads WHERE id=?";
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
            createPost.append("(date, forum_short_name, isApproved, isDeleted, isEdited, isHighlighted, isSpam," +
                    " message, parent_id, thread_id, user_email, material_path)");
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
                ps.setString(11, post.getUserEmail().toString());
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
            final StringBuilder updateThreads = new StringBuilder("UPDATE threads SET posts = posts + 1"); //"WHERE id = ?";
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

}