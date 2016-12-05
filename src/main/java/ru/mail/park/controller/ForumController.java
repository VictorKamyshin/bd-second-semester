package ru.mail.park.controller;

import org.springframework.web.bind.annotation.*;
import ru.mail.park.dao.ForumDao;
import ru.mail.park.dao.implementation.ForumDaoImpl;
import ru.mail.park.response.ForumApiResponse;

import javax.sql.DataSource;

/**
 * Created by victor on 23.11.16.
 */
@RestController
@RequestMapping(value = "/db/api/forum")
public class ForumController extends AbstractController{
    private ForumDao forumDao;

    public ForumController(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    void init() {
        super.init();
        forumDao = new ForumDaoImpl(dataSource);
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

}
