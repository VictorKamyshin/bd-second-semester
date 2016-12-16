package ru.mail.park.controller;

import org.springframework.web.bind.annotation.*;
import ru.mail.park.dao.ForumDao;
import ru.mail.park.dao.PostDao;
import ru.mail.park.dao.ThreadDao;
import ru.mail.park.dao.UserDao;
import ru.mail.park.dao.implementation.ForumDaoImpl;
import ru.mail.park.dao.implementation.PostDaoImpl;
import ru.mail.park.dao.implementation.ThreadDaoImpl;
import ru.mail.park.dao.implementation.UserDaoImpl;
import ru.mail.park.response.ForumApiResponse;

import javax.sql.DataSource;

/**
 * Created by victor on 23.11.16.
 */
@RestController
@RequestMapping(value = "/db/api/forum")
public class ForumController extends AbstractController{
    private ForumDao forumDao;

    private PostDao postDao;

    private ThreadDao threadDao;

    private UserDao userDao;

    public ForumController(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    void init() {
        super.init();
        forumDao = new ForumDaoImpl(dataSource);
        postDao = new PostDaoImpl(dataSource);
        threadDao = new ThreadDaoImpl(dataSource);
        userDao = new UserDaoImpl(dataSource);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ForumApiResponse create(@RequestBody String body){
        return new ForumApiResponse(forumDao.create(body));
    }

    @RequestMapping(value = "/details", method = RequestMethod.GET)
    public ForumApiResponse details(@RequestParam(value = "forum") String shortName,
                                    @RequestParam(value = "related", required = false) String[] related ){
        return new ForumApiResponse(forumDao.details(shortName, related));
    }

    @RequestMapping(value = "/listPosts", method = RequestMethod.GET)
    public ForumApiResponse listPosts(@RequestParam(value = "forum") String forum,
                                      @RequestParam(value = "since", required = false) String since,
                                      @RequestParam(value = "limit", required = false) Integer limit,
                                      @RequestParam(value = "order", required = false) String order,
                                      @RequestParam(value = "related", required = false) String[] related) {
        return new ForumApiResponse(postDao.list(forum, null, null, since, limit, order, null, related));
    }

    @RequestMapping(value = "/listThreads", method = RequestMethod.GET)
    public ForumApiResponse listThreads(@RequestParam(value = "forum") String forum,
                                      @RequestParam(value = "since", required = false) String since,
                                      @RequestParam(value = "limit", required = false) Integer limit,
                                      @RequestParam(value = "order", required = false) String order,
                                      @RequestParam(value = "related", required = false) String[] related) {
        return new ForumApiResponse(threadDao.list(forum, null, since, limit, order, related));
    }

    @RequestMapping(value = "/listUsers", method = RequestMethod.GET)
    public ForumApiResponse listUsers(@RequestParam(value = "forum") String forum,
                                          @RequestParam(value = "since_id", required = false) Long since,
                                          @RequestParam(value = "limit", required = false) Integer limit,
                                          @RequestParam(value = "order", required = false) String order) {
        return new ForumApiResponse(userDao.list(null, forum, false, limit, order, since));
    }

}
