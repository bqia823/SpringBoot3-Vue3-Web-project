package com.example.filter;

import com.alibaba.fastjson2.JSONObject;
import com.example.utils.Const;
import com.example.utils.SnowflakeIdGenerator;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Set;

/**
 * Request logging filter for recording all user request information
 */
@Slf4j
@Component
public class RequestLogFilter extends OncePerRequestFilter {

    @Resource
    SnowflakeIdGenerator generator;

    private final Set<String> ignores = Set.of("/swagger-ui", "/v3/api-docs");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(this.isIgnoreUrl(request.getServletPath())) {
            filterChain.doFilter(request, response);
        } else {
            long startTime = System.currentTimeMillis();
            this.logRequestStart(request);
            ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
            filterChain.doFilter(request, wrapper);
            this.logRequestEnd(wrapper, startTime);
            wrapper.copyBodyToResponse();
        }
    }

    /**
     * Determines whether the current request URL should be ignored for logging
     * @param url the URL path
     * @return whether to ignore
     */
    private boolean isIgnoreUrl(String url){
        for (String ignore : ignores) {
            if(url.startsWith(ignore)) return true;
        }
        return false;
    }

    /**
     * Logs request information at the end, including processing
     * time and response result
     * @param wrapper wrapper for reading the response content
     * @param startTime start time
     */
    public void logRequestEnd(ContentCachingResponseWrapper wrapper, long startTime) {
        long time = System.currentTimeMillis() - startTime;
        int status = wrapper.getStatus();
        String content = status != 200 ?
                status + " Error" : new String(wrapper.getContentAsByteArray());
        log.info("Request processing time: {}ms | Response result: {}", time, content);
    }

    /**
     * Logs request information at the start, including all request
     * details and corresponding user roles
     * @param request the request
     */
    public void logRequestStart(HttpServletRequest request) {
        long reqId = generator.nextId();
        MDC.put("reqId", String.valueOf(reqId));
        JSONObject object = new JSONObject();
        request.getParameterMap().forEach((k, v) -> object.put(k, v.length > 0 ? v[0] : null));
        Object id = request.getAttribute(Const.ATTR_USER_ID);
        if (id != null) {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            log.info("Request URL: \"{}\" ({}) | Remote IP address: {} │ Identity: {} (UID: {}) | Roles: {} | Request parameters: {}",
                    request.getServletPath(), request.getMethod(), request.getRemoteAddr(),
                    user.getUsername(), id, user.getAuthorities(), object);
        } else {
            log.info("Request URL: \"{}\" ({}) | Remote IP address: {} │ Identity: Unauthenticated | Request parameters: {}",
                    request.getServletPath(), request.getMethod(), request.getRemoteAddr(), object);
        }
    }
}
