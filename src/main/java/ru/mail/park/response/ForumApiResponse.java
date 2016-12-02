package ru.mail.park.response;

import java.util.Map;

/**
 * Created by victor on 02.12.16.
 */
public class ForumApiResponse {
    private Integer code = ResponseStatus.OK;
    private Object response;

    public Integer getCode() {
        return code;
    }

    public Object getResponse() {
        return response;
    }

    public ForumApiResponse(Response response) {
        code = response.getCode();
        this.response = response.getObject();
    }

    public ForumApiResponse(Map response) {
        this.response = response;
    }

    public ForumApiResponse(String response) {
        this.response = response;
    }
}
