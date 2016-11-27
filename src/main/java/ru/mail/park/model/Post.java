package ru.mail.park.model;

import com.google.gson.JsonObject;

/**
 * Created by victor on 23.11.16.
 */
public class Post {
    public static final String TABLE_NAME = "posts";
    public static final String DATE_COLUMN = "date"; //так-то это нифига не столбцы, а поля джсона
    public static final String DISLIKES_COLUMN = "dislikes";
    public static final String FORUM_COLUMN = "forum";
    public static final String ID_COLUMN = "id";
    public static final String ISAPPROVED_COLUMN = "isApproved";
    public static final String ISDELETED_COLUMN = "isDeleted";
    public static final String ISEDITED_COLUMN = "isEdited";
    public static final String ISHIGHLIGHTED_COLUMN = "isHighlighted";
    public static final String ISSPAM_COLUMN = "isSpam";
    public static final String LIKES_COLUMN = "likes";
    public static final String MESSAGE_COLUMN = "message";
    public static final String PARENT_COLUMN = "parent";
    public static final String POINTS_COLUMN = "points";
    public static final String THREAD_COLUMN = "thread";
    public static final String USER_COLUMN = "user";
    public static final String PATCH_COLUMN = "path";

    private String date;
    private long dislikes;
    private Object forum; //объект, не обязательно строка
    private long id;
    private boolean isApproved;
    private boolean isDeleted;
    private boolean isEdited;
    private boolean isHighlighted;
    private boolean isSpam;
    private long likes;
    private String message;
    private Long parentId;
    private long points;
    private Object thread; //тоже объект
    private Object userEmail;// и это тоже объект
    private String path;

    public Post(JsonObject object) {
        id = object.has(ID_COLUMN) ? object.get(ID_COLUMN).getAsInt() : 0;

        try{ //опциональные параметры, т.е. их может не быть в пришедшем джсоне
            parentId = object.get(PARENT_COLUMN).getAsLong();
        } catch(Exception e){
            parentId = null;
        }
        isApproved = object.has(ISAPPROVED_COLUMN) && object.get(ISAPPROVED_COLUMN).getAsBoolean();
        isDeleted = object.has(ISDELETED_COLUMN) && object.get(ISDELETED_COLUMN).getAsBoolean();
        isEdited = object.has(ISEDITED_COLUMN) && object.get(ISEDITED_COLUMN).getAsBoolean();
        isHighlighted = object.has(ISHIGHLIGHTED_COLUMN) && object.get(ISHIGHLIGHTED_COLUMN).getAsBoolean();
        isSpam = object.has(ISSPAM_COLUMN) && object.get(ISSPAM_COLUMN).getAsBoolean();

        date = object.get(DATE_COLUMN).getAsString();
        thread = object.get(THREAD_COLUMN).getAsLong();
        message = object.get(MESSAGE_COLUMN).getAsString();
        userEmail = object.get(USER_COLUMN).getAsShort();
        forum = object.get(FORUM_COLUMN).getAsString();
    }


}
