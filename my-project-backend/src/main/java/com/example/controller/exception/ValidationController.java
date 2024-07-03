package com.example.controller.exception;

import com.example.entity.RestBean;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Controller for handling interface parameter validation
 */

@Slf4j
@RestControllerAdvice
public class ValidationController {

    /**
     * Consistent with Spring Boot, logs a warning message instead of throwing an exception if validation fails
     * @param exception validation exception
     * @return validation result
     */

    @ExceptionHandler(ValidationException.class)
    public RestBean<Void> validateError(ValidationException exception) {
        log.warn("Resolved [{}: {}]", exception.getClass().getName(), exception.getMessage());
        return RestBean.failure(400, "Invalid request parameters");
    }
}
