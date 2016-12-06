package ru.mail.park.controller;

import org.springframework.web.bind.annotation.*;
import ru.mail.park.dao.ThreadDao;
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

    public ThreadController(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    void init() {
        super.init();
        threadDao = new ThreadDaoImpl(dataSource);
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

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ForumApiResponse close(@RequestBody String body) { return new ForumApiResponse(threadDao.close(body)); }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ForumApiResponse open(@RequestBody String body) { return new ForumApiResponse(threadDao.open(body)); }


}
