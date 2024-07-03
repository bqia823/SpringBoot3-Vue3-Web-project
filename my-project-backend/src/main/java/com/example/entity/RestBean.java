package com.example.entity;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.slf4j.MDC;

import java.util.Optional;

/**
 * Response entity class encapsulation in RESTful style
 *
 * @param code    the status code
 * @param data    the response data
 * @param message additional message
 * @param <T>     the type of the response data
 */
public record RestBean<T>(long id, int code, T data, String message) {
    public static <T> RestBean<T> success(T data) {
        return new RestBean<>(requestId(), 200, data, "Request successful");
    }

    public static <T> RestBean<T> success() {
        return success(null);
    }

    public static <T> RestBean<T> forbidden(String message) {
        return failure(403, message);
    }

    public static <T> RestBean<T> unauthorized(String message) {
        return failure(401, message);
    }

    public static <T> RestBean<T> failure(int code, String message) {
        return new RestBean<>(requestId(), code, null, message);
    }

    /**
     * Quickly converts the current entity to a JSON string format
     *
     * @return the JSON string
     */
    public String asJsonString() {
        return JSONObject.toJSONString(this, JSONWriter.Feature.WriteNulls);
    }

    /**
     * Retrieves the current request ID for quick error localization
     *
     * @return the request ID
     */
    private static long requestId() {
        String requestId = Optional.ofNullable(MDC.get("reqId")).orElse("0");
        return Long.parseLong(requestId);
    }
}
