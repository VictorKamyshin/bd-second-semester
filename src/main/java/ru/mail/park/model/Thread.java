package ru.mail.park.model;

import com.google.gson.JsonObject;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by victor on 23.11.16.
 */
public class Thread {
    public static final String TABLE_NAME = "Threads";
    public static final String SUBSCRIPTION_TABLE_NAME = "Subscriptions";

    private static final String ID_COLUMN = "id";
    private static final String USER_EMAIL_COLUMN = "user";
    private static final String FORUM_COLUMN = "forum";
    private static final String TITLE_COLUMN = "title";
    private static final String ISCLOSED_COLUMN = "isClosed";
    private static final String DATE_COLUMN = "date";
    private static final String MESSAGE_COLUMN = "message";
    private static final String SLUG_COLUMN = "slug";
    private static final String ISDELETED_COLUMN = "isDeleted";

    private long id;
    private Object user;
    private Object forum;
    private String title;
    private Boolean isClosed;
    private String date;
    private String message;
    private String slug;
    private Boolean isDeleted;

    public Thread(JsonObject object) {
        id = object.has(ID_COLUMN) ? object.get(ID_COLUMN).getAsInt() : 0;
        user = object.get(USER_EMAIL_COLUMN).getAsString();
        forum = object.get(FORUM_COLUMN).getAsString();
        title = object.get(TITLE_COLUMN).getAsString();
        isClosed = object.get(ISCLOSED_COLUMN).getAsBoolean();
        date = object.get(DATE_COLUMN).getAsString();
        message = object.get(MESSAGE_COLUMN).getAsString();
        slug = object.get(SLUG_COLUMN).getAsString();
        isDeleted = object.has(ISDELETED_COLUMN) ?object.get(ISDELETED_COLUMN).getAsBoolean() : false;
    }

    public Thread(ResultSet resultSet) throws SQLException {
        id = resultSet.getLong(ID_COLUMN);
        user = resultSet.getString(USER_EMAIL_COLUMN);
        forum = resultSet.getString(FORUM_COLUMN);
        title = resultSet.getString(TITLE_COLUMN);
        isClosed = resultSet.getBoolean(ISCLOSED_COLUMN);
        date = resultSet.getString(DATE_COLUMN);
        message = resultSet.getString(MESSAGE_COLUMN);
        slug = resultSet.getString(SLUG_COLUMN);
        isDeleted = resultSet.getBoolean(ISDELETED_COLUMN);
    }

    public void setId(long id) {
        this.id = id;
    }

    public Object getUser() {
        return user;
    }

    public Object getForum() {
        return forum;
    }

    public String getTitle() {
        return title;
    }

    public Boolean getIsClosed() {
        return isClosed;
    }

    public String getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    public String getSlug() {
        return slug;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setUser(Object user) {
        this.user = user;
    }

    public void setForum(Object forum) {
        this.forum = forum;
    }
}
