package com.labdigitiser.network.model;

public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String message;
    private String error;
    private int code;

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public String getError() {
        return error;
    }

    public int getCode() {
        return code;
    }

    public String getReadableMessage() {
        if (message != null && !message.trim().isEmpty()) {
            return message;
        }
        if (error != null && !error.trim().isEmpty()) {
            return error;
        }
        return success ? "Success" : "Request failed";
    }
}
