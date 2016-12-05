package ru.mail.park.model;

import com.google.gson.JsonObject;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by victor on 23.11.16.
 */
public class User {
    public static final String TABLE_NAME = "Users";
    public static final String FOLLOWER_TABLE_NAME = "Followers";//плохо - пересекается с названием поля
    //на случай,если данные нам начнут приходить данные в другом виде не хардкодим названия полей джсона
    private static final String ABOUT_COLUMN = "about";
    private static final String EMAIL_COLUMN = "email";
    private static final String ID_COLUMN = "id";
    private static final String ISANONYMOUS_COLUMN = "isAnonymous";
    private static final String NAME_COLUMN = "name";
    private static final String USERNAME_COLUMN = "username";
    private static final String FOLLOWERS_COLUMN = "followers";
    private static final String FOLLOWING_COLUMN = "following";
    private static final String SUBSCRIPTIONS_COLUMN = "subscriptions";


    private String about;
    private String email;
    private long id;
    private Boolean isAnonymous;
    private String[] listOfFollowers;
    private String[] listOfFollowing;
    private Integer[] listOfSubscriptions;
    private String name;
    private String username;

    public User(String about, String email, long id, boolean isAnonymous, String name, String username) {
        this.about = about;
        this.email = email;
        this.id = id;
        this.isAnonymous = isAnonymous;
        this.name = name;
        this.username = username;
        this.listOfFollowers = new String[]{};
        this.listOfFollowing = new String[]{};
        this.listOfSubscriptions = new Integer[]{};
    }

    public User(ResultSet resultSet) throws SQLException{
        about = resultSet.getString(ABOUT_COLUMN);
        email = resultSet.getString(EMAIL_COLUMN);
        id = resultSet.getLong(ID_COLUMN);
        isAnonymous = resultSet.getBoolean(ISANONYMOUS_COLUMN);
        name = resultSet.getString(NAME_COLUMN);
        username = resultSet.getString(USERNAME_COLUMN);

        try{
            listOfFollowers = resultSet.getString(FOLLOWERS_COLUMN).split(",");
        } catch(NullPointerException e){
            listOfFollowers = new String[]{};
        }

        try{
            listOfFollowing = resultSet.getString(FOLLOWING_COLUMN).split(",");
        } catch (NullPointerException e){
            listOfFollowing = new String[]{};
        }

        try{
            final String[] threadsIds = resultSet.getString(SUBSCRIPTIONS_COLUMN).split(",");
            listOfSubscriptions = new Integer[threadsIds.length];
            for( Integer i = 0; i < threadsIds.length; i++) {
                listOfSubscriptions[i] = Integer.parseInt(threadsIds[i]);
            }
        } catch (NullPointerException e){
            listOfSubscriptions = new Integer[]{};
        }

    }

    public User(JsonObject object){
        try {
            about = object.get(ABOUT_COLUMN).getAsString();
        } catch (RuntimeException e) {
            about = null; //потому что поле about не обязательно и может быть пустым
        }
        email = object.get(EMAIL_COLUMN).getAsString();
        id = object.has(ID_COLUMN) ? object.get(ID_COLUMN).getAsInt() : 0; //вообще говоря, пользователь к нам приходит без айдишника
        isAnonymous = object.has(ISANONYMOUS_COLUMN) && object.get(ISANONYMOUS_COLUMN).getAsBoolean();
        //логическое выражение, если у пользователя существует нужный параметр и он истинный
        try {
            name = object.get(NAME_COLUMN).getAsString();
        } catch (RuntimeException e) {
            name = null;
        }
        try {
            username = object.get(USERNAME_COLUMN).getAsString();
        } catch (RuntimeException e) {
            username = null;
        } //аналогично, эти поля могут быть пустыми, хотя и декларируются как обязательные
    }

    public String getAbout() {
        return about;
    }

    public String getEmail() {
        return email;
    }

    public long getId() {
        return id;
    }

    public Boolean getAnonymous() {
        return isAnonymous;
    }

    public String[] getListOfFollowers() {
        return listOfFollowers;
    }

    public String[] getListOfFollowing() {
        return listOfFollowing;
    }

    public Integer[] getListOfSubscriptions() {
        return listOfSubscriptions;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public void setId(long id) {
        this.id = id;
    }
}
