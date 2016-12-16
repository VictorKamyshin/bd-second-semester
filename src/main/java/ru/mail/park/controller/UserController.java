package ru.mail.park.controller;

import org.springframework.web.bind.annotation.*;
import ru.mail.park.dao.PostDao;
import ru.mail.park.dao.UserDao;
import ru.mail.park.dao.implementation.PostDaoImpl;
import ru.mail.park.dao.implementation.UserDaoImpl;
import ru.mail.park.response.ForumApiResponse;

import javax.sql.DataSource;

/**
 * Created by victor on 23.11.16.
 */
@RestController
@RequestMapping(value = "/db/api/user")
public class UserController extends AbstractController {
    private UserDao userDao;
    private PostDao postDao;

    public UserController(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    void init() {
        super.init();
        userDao = new UserDaoImpl(dataSource);
        postDao = new PostDaoImpl(dataSource);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ForumApiResponse create(@RequestBody String body){
        return new ForumApiResponse(userDao.create(body));
    }

    @RequestMapping(value = "/details", method = RequestMethod.GET)
    public ForumApiResponse details(@RequestParam(value = "user") String email) {
        return new ForumApiResponse(userDao.details(email));
    }

    @RequestMapping(value = "/follow", method = RequestMethod.POST)
    public ForumApiResponse follow(@RequestBody String body) {
        return new ForumApiResponse(userDao.follow(body));
    }

    @RequestMapping(value = "/unfollow", method = RequestMethod.POST)
    public ForumApiResponse unfollow(@RequestBody String body) {
        return new ForumApiResponse(userDao.unfollow(body));
    }

    @RequestMapping(value = "/updateProfile", method = RequestMethod.POST)
    public ForumApiResponse updateProfile(@RequestBody String body) {
        return new ForumApiResponse(userDao.updateProfile(body));
    }

    @RequestMapping(value = "/listPosts", method = RequestMethod.GET)
    public ForumApiResponse listPosts(@RequestParam(value = "user") String userEmail,
                                      @RequestParam(value = "since", required = false) String since,
                                      @RequestParam(value = "limit", required = false) Integer limit,
                                      @RequestParam(value = "order", required = false) String order) {
        return new ForumApiResponse(postDao.list(null, null, userEmail, since, limit, order, null, null));
    }

    @RequestMapping(value = "/listFollowers", method = RequestMethod.GET)
    public ForumApiResponse listFollowers(@RequestParam(value = "user") String userEmail,
                                      @RequestParam(value = "since_id", required = false) Long since,
                                      @RequestParam(value = "limit", required = false) Integer limit,
                                      @RequestParam(value = "order", required = false) String order) {
        return new ForumApiResponse(userDao.list(userEmail,null, true, limit, order, since));
    }

    @RequestMapping(value = "/listFollowing", method = RequestMethod.GET)
    public ForumApiResponse listFollowing(@RequestParam(value = "user") String userEmail,
                                          @RequestParam(value = "since_id", required = false) Long since,
                                          @RequestParam(value = "limit", required = false) Integer limit,
                                          @RequestParam(value = "order", required = false) String order) {
        return new ForumApiResponse(userDao.list(userEmail,null, false, limit, order, since));
    }
}
