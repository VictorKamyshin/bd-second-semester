package ru.mail.park.dao;

public interface BaseDao { //базовый дао
    //что должен уметь любой дао?
    //допустим, считать свои сущности, хотя хрен его знает
    Long getCount();

    void truncateTable();
}
