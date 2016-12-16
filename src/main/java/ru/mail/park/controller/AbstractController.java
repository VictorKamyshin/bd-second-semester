package ru.mail.park.controller;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * Created by victor on 23.11.16.
 */
public abstract class AbstractController {
    protected final DataSource dataSource;

    @Autowired
    public AbstractController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    void init(){

    }
}
