package com.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for handling JWT tokens
 */
@Component
public class JwtUtils {


    // Secret key for signing JWT tokens
    @Value("${spring.security.jwt.key}")
    private String key;
    // Token expiration time in hours
    @Value("${spring.security.jwt.expire}")
    private int expire;
    // Cooldown time for generating JWT tokens to prevent frequent login attempts, in seconds
    @Value("${spring.security.jwt.limit.base}")
    private int limit_base;
    // Extended block time for users who continue to abuse token requests
    @Value("${spring.security.jwt.limit.upgrade}")
    private int limit_upgrade;
    // Number of attempts allowed before triggering extended block time
    @Value("${spring.security.jwt.limit.frequency}")
    private int limit_frequency;

    @Resource
    StringRedisTemplate template;

    @Resource
    FlowUtils utils;

    /**
     * Invalidates the specified JWT token
     * @param headerToken the token from the request header
     * @return whether the operation was successful
     */
    public boolean invalidateJwt(String headerToken) {
        String token = this.convertToken(headerToken);
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            DecodedJWT verify = jwtVerifier.verify(token);
            return deleteToken(verify.getId(), verify.getExpiresAt());
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * Quickly calculates the expiration time based on configuration
     * @return the expiration time
     */
    public Date expireTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, expire);
        return calendar.getTime();
    }

    /**
     * Generates a JWT token based on UserDetails
     * @param user the user details
     * @param username the username
     * @param userId the user ID
     * @return the token
     */
    public String createJwt(UserDetails user, String username, int userId) {
        if (this.frequencyCheck(userId)) {
            Algorithm algorithm = Algorithm.HMAC256(key);
            Date expire = this.expireTime();
            return JWT.create()
                    .withJWTId(UUID.randomUUID().toString())
                    .withClaim("id", userId)
                    .withClaim("name", username)
                    .withClaim("authorities", user.getAuthorities()
                            .stream()
                            .map(GrantedAuthority::getAuthority).toList())
                    .withExpiresAt(expire)
                    .withIssuedAt(new Date())
                    .sign(algorithm);
        } else {
            return null;
        }
    }

    /**
     * Parses the JWT token
     * @param headerToken the token from the request header
     * @return DecodedJWT
     */
    public DecodedJWT resolveJwt(String headerToken) {
        String token = this.convertToken(headerToken);
        if (token == null) return null;
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            DecodedJWT verify = jwtVerifier.verify(token);
            if (this.isInvalidToken(verify.getId())) return null;
            Map<String, Claim> claims = verify.getClaims();
            return new Date().after(claims.get("exp").asDate()) ? null : verify;
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    /**
     * Converts the contents of the JWT object to UserDetails
     * @param jwt the decoded JWT object
     * @return UserDetails
     */
    public UserDetails toUser(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims();
        return User
                .withUsername(claims.get("name").asString())
                .password("******")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();
    }

    /**
     * Extracts the user ID from the JWT object
     * @param jwt the decoded JWT object
     * @return the user ID
     */
    public Integer toId(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims();
        return claims.get("id").asInt();
    }

    /**
     * Frequency check to prevent users from frequently requesting JWT
     * tokens, employs a staged block mechanism
     * If the user continues to abuse token requests, the block time is extended
     * @param userId the user ID
     * @return whether the frequency check passed
     */
    private boolean frequencyCheck(int userId) {
        String key = Const.JWT_FREQUENCY + userId;
        return utils.limitOnceUpgradeCheck(key, limit_frequency, limit_base, limit_upgrade);
    }

    /**
     * Validates and converts the token from the request header
     * @param headerToken the token from the request header
     * @return the converted token
     */
    private String convertToken(String headerToken) {
        if (headerToken == null || !headerToken.startsWith("Bearer "))
            return null;
        return headerToken.substring(7);
    }

    /**
     * Adds the token to the Redis blacklist
     * @param uuid the token ID
     * @param time the expiration time
     * @return whether the operation was successful
     */
    private boolean deleteToken(String uuid, Date time) {
        if (this.isInvalidToken(uuid))
            return false;
        Date now = new Date();
        long expire = Math.max(time.getTime() - now.getTime(), 0);
        template.opsForValue().set(Const.JWT_BLACK_LIST + uuid, "", expire, TimeUnit.MILLISECONDS);
        return true;
    }

    /**
     * Checks if the token is in the Redis blacklist
     * @param uuid the token ID
     * @return whether the token is invalid
     */
    private boolean isInvalidToken(String uuid) {
        return Boolean.TRUE.equals(template.hasKey(Const.JWT_BLACK_LIST + uuid));
    }
}
