package ru.mail.park.model;

import com.google.gson.JsonObject;

/**
 * Created by victor on 23.11.16.
 */
public class Forum {
    public static final String TABLE_NAME = "forums";
    private static final String ID_COLUMN = "id";
    private static final String NAME_COLUMN = "name";
    private static final String SHORTNAME_COLUMN = "short_name";
    private static final String USER_EMAIL_COLUMN = "user";

    private long id;
    private String name;
    private String shortName;
    private String userEmail;

    public Forum(JsonObject object) {
        id = object.has(ID_COLUMN) ? object.get(ID_COLUMN).getAsInt() : 0;
        name = object.get(NAME_COLUMN).getAsString();
        shortName = object.get(SHORTNAME_COLUMN).getAsString();
        userEmail = object.get(USER_EMAIL_COLUMN).getAsString();
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

    public String getUserEmail() {
        return userEmail;
    }

    public void setId(long id) {
        this.id = id;
    }


}
