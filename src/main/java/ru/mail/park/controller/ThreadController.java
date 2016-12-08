package ru.mail.park.controller;

import org.springframework.web.bind.annotation.*;
import ru.mail.park.dao.PostDao;
import ru.mail.park.dao.ThreadDao;
import ru.mail.park.dao.implementation.PostDaoImpl;
import ru.mail.park.dao.implementation.ThreadDaoImpl;
import ru.mail.park.response.ForumApiResponse;

import javax.sql.DataSource;

/**
 * Created by victor on 23.11.16.
 */
@RestController
@RequestMapping(value = "/db/api/thread")
public class ThreadController extends AbstractController{
    private ThreadDao threadDao;

    private PostDao postDao;

    public ThreadController(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    void init() {
        super.init();
        threadDao = new ThreadDaoImpl(dataSource);
        postDao = new PostDaoImpl(dataSource);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ForumApiResponse create(@RequestBody String body){
        return new ForumApiResponse(threadDao.create(body));
    }

    @RequestMapping(value = "/details", method = RequestMethod.GET)
    public ForumApiResponse details(@RequestParam(value = "thread") long threadId,
                                    @RequestParam(value = "related", required = false) String[] related){
        return new ForumApiResponse(threadDao.details(threadId,related));
    }

    @RequestMapping(value = "/close", method = RequestMethod.POST)
    public ForumApiResponse close(@RequestBody String body) { return new ForumApiResponse(threadDao.close(body)); }

    @RequestMapping(value = "/open", method = RequestMethod.POST)
    public ForumApiResponse open(@RequestBody String body) { return new ForumApiResponse(threadDao.open(body)); }

    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    public ForumApiResponse remove(@RequestBody String body) { return new ForumApiResponse(threadDao.remove(body)); }

    @RequestMapping(value = "/restore", method = RequestMethod.POST)
    public ForumApiResponse retsore(@RequestBody String body) { return new ForumApiResponse(threadDao.restore(body)); }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ForumApiResponse update(@RequestBody String body) { return new ForumApiResponse(threadDao.update(body)); }

    @RequestMapping(value = "/vote", method = RequestMethod.POST)
    public ForumApiResponse vote(@RequestBody String body) { return new ForumApiResponse(threadDao.vote(body)); }

    @RequestMapping(value = "/subscribe", method = RequestMethod.POST)
    public ForumApiResponse subscribe(@RequestBody String body) { return new ForumApiResponse(threadDao.subscribe(body)); }

    @RequestMapping(value = "/unsubscribe", method = RequestMethod.POST)
    public ForumApiResponse unsubscribe(@RequestBody String body) { return new ForumApiResponse(threadDao.unsubscribe(body)); }

    @RequestMapping(value = "/listPosts", method = RequestMethod.GET, params = {"thread"})
    public ForumApiResponse listPosts(@RequestParam(value = "thread") Long threadId,
                                         @RequestParam(value = "since", required = false) String since,
                                         @RequestParam(value = "limit", required = false) Integer limit,
                                         @RequestParam(value = "order", required = false) String order,
                                         @RequestParam(value = "sort", required = false) String sort) {
        return new ForumApiResponse(postDao.list(null, threadId, since, limit, order, sort, null));
    }

}
