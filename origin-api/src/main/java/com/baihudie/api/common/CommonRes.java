package com.baihudie.api.common;

import lombok.Data;

@Data
public class CommonRes<T> {

    public static final String SUCCESS = "0";

    public static final String SYS_ERROR = "9";

    private String code;

    private String message;

    private T data;

    public static CommonRes success(Object data) {
        CommonRes res = new CommonRes();
        res.setCode(SUCCESS);
        res.setData(data);
        return res;
    }

}
