package ru.mail.park.dao;

import ru.mail.park.response.Response;

/**
 * Created by victor on 23.11.16.
 */
public interface ThreadDao extends BaseDao {
    public Response create(String threadCreateJson);

    public Response details(long threadId, String[] related);

    public Response close(String threadCloseJson);

    public Response open(String threadOpenJson);

    public Response remove(String threadRemoveJson);

    public Response restore(String threadRestoreJson);

    public Response update(String threadUpdateJson);

    public Response vote(String threadVoteJson);

    public Response subscribe(String subscribeJson);

    public Response unsubscribe(String unsubscribeJson);

    public Response list(String forum, String userEmail, String since, Integer limit, String order, String[] related);
}
