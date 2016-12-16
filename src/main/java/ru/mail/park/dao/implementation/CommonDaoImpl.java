package ru.mail.park.dao.implementation;

import ru.mail.park.dao.*;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by victor on 23.11.16.
 */
public class CommonDaoImpl extends BaseDaoImpl implements CommonDao {
    private UserDao userDao;
    private ForumDao forumDAO;
    private ThreadDao threadDAO;
    private PostDao postDAO;

    public CommonDaoImpl(DataSource dataSource) {
        userDao = new UserDaoImpl(dataSource);
        forumDAO = new ForumDaoImpl(dataSource);
        threadDAO = new ThreadDaoImpl(dataSource);
        postDAO = new PostDaoImpl(dataSource);
    }

    @Override
    public void truncateAllTables() {
        postDAO.truncateTable();
        threadDAO.truncateTable();
        forumDAO.truncateTable();
        userDao.truncateTable();
    }

    @Override
    public Map<String, Long> getCounts() {
        Map<String, Long> response = new HashMap<>();
        response.put("user", userDao.getCount());
        response.put("forum", forumDAO.getCount());
        response.put("thread", threadDAO.getCount());
        response.put("post", postDAO.getCount());

        return response;
    }
}
