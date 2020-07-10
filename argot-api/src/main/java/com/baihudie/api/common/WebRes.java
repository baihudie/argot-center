package com.baihudie.api.common;

import lombok.Data;

@Data
public class WebRes<T> {

    public static final String SUCCESS = "0";

    public static final String SYS_ERROR = "9";

    private String code;

    private String message;

    private T data;

    public static WebRes success(Object data) {
        WebRes res = new WebRes();
        res.setCode(SUCCESS);
        res.setData(data);
        return res;
    }

}
