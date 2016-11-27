package ru.mail.park.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.mail.park.dao.CommonDao;
import ru.mail.park.dao.implementation.CommonDaoImpl;
import ru.mail.park.response.Response;
import ru.mail.park.response.ResponseStatus;

import javax.sql.DataSource;

/**
 * Created by victor on 23.11.16.
 */
@RestController
@RequestMapping(value = "/db/api")
public class CommonController extends AbstractController {
    private CommonDao commonDao;

    public CommonController(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    void init() {
        super.init();
        commonDao = new CommonDaoImpl(dataSource);
    }

    @RequestMapping(value = "/clear", method = RequestMethod.POST)
    public Response clear() {
        commonDao.truncateAllTables();
        return new Response(ResponseStatus.OK);
    }
}
