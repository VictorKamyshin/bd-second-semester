package ru.mail.park.model;

import com.google.gson.JsonObject;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by victor on 23.11.16.
 */
public class Thread {
    public static final String TABLE_NAME = "Threads";
    public static final String SUBSCRIPTION_TABLE_NAME = "Subscribe";

    private static final String ID_COLUMN = "id";
    private static final String USER_EMAIL_COLUMN = "user";
    private static final String FORUM_COLUMN = "forum";
    private static final String TITLE_COLUMN = "title";
    private static final String ISCLOSED_COLUMN = "isClosed";
    private static final String DATE_COLUMN = "date";
    private static final String MESSAGE_COLUMN = "message";
    private static final String SLUG_COLUMN = "slug";
    private static final String ISDELETED_COLUMN = "isDeleted";
    public static final String LIKES_COLUMN = "likes";
    public static final String DISLIKES_COLUMN = "dislikes";
    public static final String POINTS_COLUMN = "points";
    public static final String POSTS_COLUMN = "posts";

    private long id;
    private Object user;
    private Object forum;
    private String title;
    private Boolean isClosed;
    private String date;
    private String message;
    private String slug;
    private Boolean isDeleted;
    private Integer likes;
    private Integer dislikes;
    private Integer points;
    private Long posts;

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
        date = resultSet.getString(DATE_COLUMN).substring(0, resultSet.getString(DATE_COLUMN).length()-2);
        message = resultSet.getString(MESSAGE_COLUMN);
        slug = resultSet.getString(SLUG_COLUMN);
        isDeleted = resultSet.getBoolean(ISDELETED_COLUMN);
        likes = resultSet.getInt(LIKES_COLUMN);
        dislikes = resultSet.getInt(DISLIKES_COLUMN);
        points = resultSet.getInt(POINTS_COLUMN);
        posts = resultSet.getLong(POSTS_COLUMN);
    }

    public Long getPosts() {
        return posts;
    }

    public void setPosts(Long posts) {
        this.posts = posts;
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

    public Integer getLikes() {
        return likes;
    }

    public Integer getDislikes() {
        return dislikes;
    }

    public Integer getPoints() {
        return points;
    }

    public long getId() {
        return id;
    }

    public void setUser(Object user) {
        this.user = user;
    }

    public void setForum(Object forum) {
        this.forum = forum;
    }
}
