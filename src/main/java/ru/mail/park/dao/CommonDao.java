package ru.mail.park.dao;

import java.util.Map;

/**
 * Created by victor on 23.11.16.
 */
public interface CommonDao {
    void truncateAllTables();

    Map<String, Long> getCounts();
}
