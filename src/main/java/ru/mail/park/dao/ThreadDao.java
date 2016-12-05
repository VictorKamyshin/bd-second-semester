package ru.mail.park.dao;

import ru.mail.park.response.Response;

/**
 * Created by victor on 23.11.16.
 */
public interface ThreadDao extends BaseDao {
    public Response create(String threadCreateJson);

    public Response details(long threadId, String[] related);
}
