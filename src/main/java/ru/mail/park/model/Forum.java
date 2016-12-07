package ru.mail.park.model;

import com.google.gson.JsonObject;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by victor on 23.11.16.
 */
public class Forum {
    public static final String TABLE_NAME = "Forums";
    private static final String ID_COLUMN = "id";
    private static final String NAME_COLUMN = "name";
    private static final String SHORTNAME_COLUMN = "short_name";
    private static final String USER_EMAIL_COLUMN = "user";
    //важный момент - мы должны называть поля в таблицах так же, как они называются в приходящих к нам джсонах
    private long id;
    private String name;
    private String shortName;
    private Object user;

    public Forum(JsonObject object) {
        id = object.has(ID_COLUMN) ? object.get(ID_COLUMN).getAsInt() : 0;
        name = object.get(NAME_COLUMN).getAsString();
        shortName = object.get(SHORTNAME_COLUMN).getAsString();
        user = object.get(USER_EMAIL_COLUMN).getAsString();
    }

    public Forum(ResultSet resultSet) throws SQLException{
        id = resultSet.getLong(ID_COLUMN);
        name = resultSet.getString(NAME_COLUMN);
        shortName = resultSet.getString(SHORTNAME_COLUMN);
        user = resultSet.getString(USER_EMAIL_COLUMN);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public Object getUser() {
        return user;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUser(Object userEmail) {
        this.user = userEmail;
    }
}
