package ru.mail.park.controller;

import org.springframework.web.bind.annotation.*;
import ru.mail.park.dao.PostDao;
import ru.mail.park.dao.implementation.PostDaoImpl;
import ru.mail.park.response.ForumApiResponse;

import javax.sql.DataSource;

/**
 * Created by victor on 23.11.16.
 */
@RestController
@RequestMapping(value = "/db/api/post")
public class PostController extends  AbstractController{
    private PostDao postDao;

    public PostController(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    void init() {
        super.init();
        postDao = new PostDaoImpl(dataSource);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ForumApiResponse create(@RequestBody String body) {
        return new ForumApiResponse(postDao.create(body));
    }

    @RequestMapping(value = "/details", method = RequestMethod.GET)
    public ForumApiResponse details(@RequestParam(value = "post") long postId,
                                    String[] related) {
        return new ForumApiResponse(postDao.details(postId,related));
    }

    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    public ForumApiResponse remove(@RequestBody String body) { return new ForumApiResponse(postDao.remove(body)); }

    @RequestMapping(value = "/restore", method = RequestMethod.POST)
    public ForumApiResponse restore(@RequestBody String body) { return new ForumApiResponse(postDao.restore(body)); }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ForumApiResponse update(@RequestBody String body) { return new ForumApiResponse(postDao.update(body)); }

    @RequestMapping(value = "/vote", method = RequestMethod.POST)
    public ForumApiResponse vote(@RequestBody String body) { return new ForumApiResponse(postDao.vote(body)); }

}
