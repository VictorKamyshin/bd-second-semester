package ru.mail.park.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.mail.park.dao.ThreadDao;
import ru.mail.park.dao.implementation.ThreadDaoImpl;
import ru.mail.park.response.Response;

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
    public Response create(@RequestBody String body){
        return threadDao.create(body);
    }
}
