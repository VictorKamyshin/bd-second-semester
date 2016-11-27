package ru.mail.park.dao.implementation;

import com.google.gson.JsonParser;
import ru.mail.park.dao.PostDao;
import ru.mail.park.model.Post;
import ru.mail.park.response.Response;
import ru.mail.park.response.ResponseStatus;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

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
            String parentPath = "";


            final StringBuilder builder = new StringBuilder("INSERT INTO ");
            builder.append(tableName);
            builder.append("");
        } catch(SQLException e){
            e.printStackTrace();
            return new Response(ResponseStatus.INVALID_REQUEST); //если произошла ошибка при парсе джсона
        }
        return new Response(ResponseStatus.OK,"asdsaddsa");
    }
}