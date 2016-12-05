package ru.mail.park.dao;

import ru.mail.park.response.Response;

/**
 * Created by victor on 23.11.16.
 */
public interface ForumDao extends BaseDao {
    public Response create(String forumCreateJson);

    public Response details(String forumShortName, String[] related);
}
