package ru.mail.park.model;

import com.google.gson.JsonObject;

/**
 * Created by victor on 23.11.16.
 */
public class Thread {
    private static final String ID_COLUMN = "id";
    private static final String USER_EMAIL_COLUMN = "user";
    private static final String FORUM_COLUMN = "forum";
    private static final String TITLE_COLUMN = "title";
    private static final String ISCLOSED_COLUMN = "isClosed";
    private static final String DATE_COLUMN = "date";
    private static final String MESSAGE_COLUMN = "message";
    private static final String SLUG_COLUMN = "slug";
    public static final String ISDELETED_COLUMN = "isDeleted";
    public static final String TABLE_NAME = "threads";
    public static final String SUBSCRIPTION_TABLE_NAME = "Subscriptions";

    private long id;
    private String userEmail;
    private String forumShortName;
    private String title;
    private Boolean isClosed;
    private String date;
    private String message;
    private String slug;
    private Boolean isDeleted;

    public Thread(JsonObject object) {
        id = object.has(ID_COLUMN) ? object.get(ID_COLUMN).getAsInt() : 0;
        userEmail = object.get(USER_EMAIL_COLUMN).getAsString();
        forumShortName = object.get(FORUM_COLUMN).getAsString();
        title = object.get(TITLE_COLUMN).getAsString();
        isClosed = object.get(ISCLOSED_COLUMN).getAsBoolean();
        date = object.get(DATE_COLUMN).getAsString();
        message = object.get(MESSAGE_COLUMN).getAsString();
        slug = object.get(SLUG_COLUMN).getAsString();
        isDeleted = object.get(ISDELETED_COLUMN).getAsBoolean();
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getForumShortName() {
        return forumShortName;
    }

    public String getTitle() {
        return title;
    }

    public Boolean getClosed() {
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

    public Boolean getDeleted() {
        return isDeleted;
    }
}
