package ru.feeland.modulesystem.responce;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;
import java.util.StringJoiner;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiExceptionResponse {

    private int code;
    private String message;
    private Long timestamp;
    private Map<String, String> validationErrors;

    public ApiExceptionResponse() {
    }

    public ApiExceptionResponse(int code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public ApiExceptionResponse(int code, Map<String, String> validationErrors) {
        this.code = code;
        this.validationErrors = validationErrors;
        this.timestamp = System.currentTimeMillis();
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(Map<String, String> validationErrors) {
        this.validationErrors = validationErrors;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ApiExceptionResponse.class.getSimpleName() + "[", "]")
                .add("code=" + code)
                .add("message='" + message + "'")
                .add("timestamp=" + timestamp)
                .add("validationErrors=" + validationErrors)
                .toString();
    }
}
