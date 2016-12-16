package ru.mail.park.dao;

import ru.mail.park.response.Response;

/**
 * Created by victor on 23.11.16.
 */
public interface UserDao extends BaseDao {
    public Response create(String userCreateJson);

    public Response follow(String userFollowJson);

    public Response unfollow(String userUnfollowJson);

    public Response details(String userEmail);

    public Response updateProfile(String updateProfileJson);

    public Response list(String userEmail, String forum, Boolean isFollowing,Integer limit, String order, Long sinceId);
}
