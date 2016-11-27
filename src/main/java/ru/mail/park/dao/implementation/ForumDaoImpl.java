package ru.mail.park.dao.implementation;

import com.google.gson.JsonParser;
import ru.mail.park.dao.ForumDao;
import ru.mail.park.model.Forum;
import ru.mail.park.model.User;
import ru.mail.park.response.Response;
import ru.mail.park.response.ResponseStatus;

import javax.sql.DataSource;
import java.sql.*;

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
            final StringBuilder builderForUserEmail = new StringBuilder("SELECT id from ").append(usertableName)
                    .append("where email = \'").append(forum.getUserEmail()).append('\'');
            final String queryForUserId = builderForUserEmail.toString();
            //тут крч, два стула, на одном нормализация и размеры базы,
            //на втором - скорость обработки запросов
            //есть мысль о том, что можно вытащить в вспомогательные таблицы вообще все
            //нормализация при этом идет по пизде, но и хер с ней, можно даже на внешние ключи забить
            //мы их и не используем, надо только в error_test заглянуть
            //заглянули, каких-то засад с внешними ключами там нет

            final StringBuilder builder = new StringBuilder("INSERT INTO ");
            builder.append(tableName);
            builder.append("(name, short_name, user_email) VALUES (?, ?, ?)");
            final String query = builder.toString();
            try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, forum.getName());
                ps.setString(2, forum.getShortName());
                ps.setString(3, forum.getUserEmail());
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
}
