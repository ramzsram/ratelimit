package com.microservice.ratelimit.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.Date;
@Data
public class CustomException extends Exception{
    private HttpStatus code;
    private String message;
    private Date date;
    public CustomException(HttpStatus code, String message, Date date)
    {
        super(message);
        this.code = code;
        this.message = message;
        this.date = date;
    }
}
