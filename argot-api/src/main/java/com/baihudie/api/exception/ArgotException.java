package com.baihudie.api.exception;

import lombok.Data;

@Data
public class ArgotException extends RuntimeException {

    private int errorCode;

    private String message;

    public ArgotException(int errorCode, String message, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
        this.message = message;
    }

    public ArgotException(int errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

}
