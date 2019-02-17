package com.longlong.base;

public class DataTaskException extends RuntimeException {

    private static final long serialVersionUID = 4615422570580250697L;

    public DataTaskException(String message) {
        super(message);
    }

    public DataTaskException(String message, Throwable cause) {
        super(message, cause);
    }
}
