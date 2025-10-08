package com.reliaquest.api.controller;

import com.reliaquest.api.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class MockEmployeeControllerAdvice {

    @ExceptionHandler
    protected ResponseEntity<?> handleException(Throwable ex) {
        log.error("Error handling web request.", ex);
        return ResponseEntity.internalServerError().body(Response.error(ex.getMessage()));
    }
}
