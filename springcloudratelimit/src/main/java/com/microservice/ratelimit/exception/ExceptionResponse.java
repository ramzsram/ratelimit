package com.microservice.ratelimit.exception;

import feign.FeignException;
import lombok.Data;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
public class ExceptionResponse {

    public static final Map<Integer, String> STATUS_CODE_TO_EXCEPTION_MESSAGE_MAP = new HashMap();
    static
    {
        STATUS_CODE_TO_EXCEPTION_MESSAGE_MAP.put(429, "Too Many requests from client received. Please refer response header 'X-RateLimit-TimeToWait' for waiting time");
        STATUS_CODE_TO_EXCEPTION_MESSAGE_MAP.put(500, "Internal Server Error occured while accessing the specified path");
        STATUS_CODE_TO_EXCEPTION_MESSAGE_MAP.put(403, "Request Forbidden. Please check auth tokens");
        STATUS_CODE_TO_EXCEPTION_MESSAGE_MAP.put(404, "Specified path not found");
        STATUS_CODE_TO_EXCEPTION_MESSAGE_MAP.put(401, "UnAuthorized access. Please check the token");
    }

    private int code;
    private CustomException exception;
    public ExceptionResponse(Throwable exception)
    {
        if(exception instanceof CustomException)
        {
            code = ((CustomException) exception).getCode().value();
        }
        else if(exception instanceof ResponseStatusException)
        {
            code = ((ResponseStatusException) exception).getRawStatusCode();
        }
        else if(exception instanceof FeignException)
        {
            code = ((FeignException) exception).status();
        }
        else
        {
            code = 500;
        }
        this.exception = new CustomException(HttpStatus.valueOf(code), exception.getLocalizedMessage(), new Date());
    }

    public String getExceptionResponse()
    {
        JSONObject errorResponse = new JSONObject();
        errorResponse.put("code", this.code);
        JSONObject exceptionResponse = new JSONObject();
        exceptionResponse.put("statusCode", this.exception.getCode());
        exceptionResponse.put("message", this.exception.getMessage());
        exceptionResponse.put("timeStamp", this.exception.getDate());
        errorResponse.put("exception", exceptionResponse);
        JSONObject response = new JSONObject();
        response.put("error", errorResponse);
        return response.toString();
    }
}
