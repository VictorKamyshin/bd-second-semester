package ru.mail.park.response;

/**
 * Created by victor on 23.11.16.
 */
public class Response {
    private Integer code;
    private Object response;

    public Integer getCode() {
        return code;
    }

    public Object getObject() {
        return response;
    }

    public Response(Integer code, Object object) {
        this.code = code;
        this.response = object;
    }

    public Response(Integer code) {
        this.code = code;
        this.response = "";
    }
}
