package com.example.filter;

import com.example.entity.RestBean;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Rate limiting filter to prevent users from making high-frequency
 * requests. Utilizes Redis for rate limiting.
 */
@Slf4j
@Component
@Order(Const.ORDER_FLOW_LIMIT)
public class FlowLimitingFilter extends HttpFilter {

    @Resource
    StringRedisTemplate template;
    // Maximum request limit within the specified time period
    @Value("${spring.web.flow.limit}")
    int limit;
    // Time period for counting requests
    @Value("${spring.web.flow.period}")
    int period;
    // Block duration after exceeding the request limit
    @Value("${spring.web.flow.block}")
    int block;

    @Resource
    FlowUtils utils;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String address = request.getRemoteAddr();
        if (!tryCount(address))
            this.writeBlockMessage(response);
        else
            chain.doFilter(request, response);
    }


    /**
     * Attempts to count requests from a specific IP address.
     * If the limit is exceeded, further access is denied.
     * @param address the request IP address
     * @return whether the operation was successful
     */
    private boolean tryCount(String address) {
        synchronized (address.intern()) {
            if (Boolean.TRUE.equals(template.hasKey(Const.FLOW_LIMIT_BLOCK + address)))
                return false;
            String counterKey = Const.FLOW_LIMIT_COUNTER + address;
            String blockKey = Const.FLOW_LIMIT_BLOCK + address;
            return utils.limitPeriodCheck(counterKey, blockKey, block, limit, period);
        }
    }

    /**
     * Writes a block message to the response, indicating that the
     * user is making requests too frequently.
     * @param response the response
     * @throws IOException possible exception
     */
    private void writeBlockMessage(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.write(RestBean.forbidden("Too many requests, please try again later").asJsonString());
    }
}
