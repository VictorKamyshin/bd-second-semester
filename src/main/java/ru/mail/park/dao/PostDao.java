package ru.mail.park.dao;

import ru.mail.park.response.Response;

/**
 * Created by victor on 23.11.16.
 */
public interface PostDao extends BaseDao {
    public Response create(String postCreateJson);

    public Response details(long postId, String[] related);

    public Response remove(String postRemoveJson);

    public Response restore(String postRestoreJson);

    public Response update(String postUpdateJson);

    public Response vote(String postVoteJson);

    public Response list(String forum, Long thread, String userEmail, String since, Integer limit, String order, String sort, String[] related);
}
