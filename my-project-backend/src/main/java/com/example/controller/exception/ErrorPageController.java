package com.example.controller.exception;

import com.example.entity.RestBean;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

/**
 * Controller dedicated to handling error pages
 */

@RestController
@RequestMapping({"${server.error.path:${error.path:/error}}"})
public class ErrorPageController extends AbstractErrorController {

    public ErrorPageController(ErrorAttributes errorAttributes) {
        super(errorAttributes);
    }

    @RequestMapping
    public RestBean<Void> error(HttpServletRequest request) {
        HttpStatus status = this.getStatus(request);
        Map<String, Object> errorAttributes = this.getErrorAttributes(request, this.getAttributeOptions());
        String message = this.convertErrorMessage(status)
                .orElse(errorAttributes.get("message").toString());
        return RestBean.failure(status.value(), message);
    }

    /**
     * Converts error messages for certain special status codes
     * @param status the status code
     * @return the error message
     */

    private Optional<String> convertErrorMessage(HttpStatus status) {
        String value = switch (status.value()) {
            case 400 -> "Invalid request parameters";
            case 404 -> "Requested endpoint does not exist";
            case 405 -> "Incorrect request method";
            case 500 -> "Internal error, please contact the administrator";
            default -> null;
        };
        return Optional.ofNullable(value);
    }

    /**
     * Error attribute options, here we additionally include error message and exception type
     * @return options
     */

    private ErrorAttributeOptions getAttributeOptions(){
        return ErrorAttributeOptions
                .defaults()
                .including(ErrorAttributeOptions.Include.MESSAGE,
                        ErrorAttributeOptions.Include.EXCEPTION);
    }
}
