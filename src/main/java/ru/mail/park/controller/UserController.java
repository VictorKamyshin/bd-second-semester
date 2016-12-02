package ru.mail.park.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.mail.park.dao.UserDao;
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

    public UserController(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    void init() {
        super.init();
        userDao = new UserDaoImpl(dataSource);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ForumApiResponse create(@RequestBody String body){
        return new ForumApiResponse(userDao.create(body));
    }
}
